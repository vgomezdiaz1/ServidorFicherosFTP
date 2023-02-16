
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class TCPCliente {

    static GenerarClave keyObj;
    static boolean claveLeida = false;

    public static void main(String[] args) {
        String Host = "localhost";
        int Puerto = 6000;// puerto remoto
        try {
            keyObj = recogerClave();
            claveLeida = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        if (claveLeida) {
            Socket cliente = null;
            PrintWriter fsalida = null;
            ObjectInputStream reciboDelServidor = null;
            BufferedReader in = null;
            String cadena = "";
            try {
                cliente = new Socket(Host, Puerto);
                fsalida = new PrintWriter(cliente.getOutputStream(), true);
                reciboDelServidor = new ObjectInputStream(cliente.getInputStream());
                in = new BufferedReader(new InputStreamReader(System.in));
                do {
                    System.out.print("Introduce el nombre del fichero: ");
                    cadena = in.readLine();
                    fsalida.println(cadena);
                    FicheroEnvio fe = (FicheroEnvio) reciboDelServidor.readObject();
                    if (fe.getCodigo() == 200) {
                        System.out.println(fe.getNombre() + "\t(" + fe.getLongitudFichero() + ")");
                        byte[] resumenFicheroRecibido = fe.getContenidoFichero();
                        byte[] ficheroDescifrado = null;
                        try {
                            ficheroDescifrado = descifrarBites(resumenFicheroRecibido);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String claveFinal = obtenerHash(ficheroDescifrado);
                        if (claveFinal.equals(fe.getFirmaFichero())) {
                            System.out.println("Fichero correctamente recibido");
                            System.out.println(fe.getNombre());
                            FileOutputStream fileOuputStream = new FileOutputStream(fe.getNombre());
                            fileOuputStream.write(ficheroDescifrado);
                            fileOuputStream.close();
                        } else {
                            System.out.println("Fichero corrupto: firma distinta a la esperada");
                            System.out.println("Contacte con el administrador");
                        }
                    } else if (fe.getCodigo() == 404) {
                        System.out.println("El fichero " + fe.getNombre() + " no ha sido encontrado");
                    } else {
                        System.out.println("Se ha producido un error");
                    }
                } while (!cadena.trim().equals("*"));
            } catch (Exception e) {
                if (!cadena.equals("*")) {
                    System.out.println("Se ha producido un error");
                    System.out.println(e.getMessage());
                }
            } finally {
                try {
                    fsalida.close();
                    reciboDelServidor.close();
                    in.close();
                    cliente.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Fin de la conexión... ");
            }
        } else {
            System.out.println("No se ha podido acceder a la clave");
        }
    }

    public static String Hexadecimal(byte[] resumen) {
        String hex = "";
        for (int i = 0; i < resumen.length; i++) {
            String h = Integer.toHexString(resumen[i] & 0xFF) + ":";
            if (h.length() == 1) {
                hex += "0";
            }
            hex += h;
        }
        return hex.toUpperCase();
    }

    public static String obtenerHash(byte[] bitesFichero) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(bitesFichero);
            byte[] resumen = md.digest();
            return Hexadecimal(resumen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] descifrarBites(byte[] bitesCifrados) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] fichBytesDescifrados = null;
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keyObj.getClave());
        fichBytesDescifrados = c.doFinal(bitesCifrados);
        return fichBytesDescifrados;
    }

    static GenerarClave recogerClave() throws IOException, ClassNotFoundException {
        File keyFichero = new File("miClave.key");
        GenerarClave keyObj;
        ObjectInputStream clave;
        clave = new ObjectInputStream(new FileInputStream(keyFichero));
        keyObj = (GenerarClave) clave.readObject();
        return keyObj;
    }
}//
