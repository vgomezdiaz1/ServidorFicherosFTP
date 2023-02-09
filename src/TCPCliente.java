
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class TCPCliente {

    static GenerarClave keyObj;

    public static void main(String[] args) {
        String Host = "localhost";
        int Puerto = 6000;// puerto remoto
        try {
            keyObj = recogerClave();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        Socket cliente = null;
        PrintWriter fsalida = null;
        ObjectInputStream reciboDelServidor = null;
        BufferedReader in = null;
        try {
            cliente = new Socket(Host, Puerto);
            fsalida = new PrintWriter(cliente.getOutputStream(), true);
            reciboDelServidor = new ObjectInputStream(cliente.getInputStream());
            in = new BufferedReader(new InputStreamReader(System.in));
            String cadena;
            do {
                System.out.print("Introduce el nombre del fichero: ");
                cadena = in.readLine();
                fsalida.println(cadena);
                FicheroEnvio fe = (FicheroEnvio) reciboDelServidor.readObject();
                if (fe.getCodigo() == 200) {
                    System.out.println(fe.getDirectorio() + "/" + fe.getNombre() + "\t(" + fe.getLongitudFichero() + ")");
                    byte[] resumenFicheroRecibido = fe.getContenidoFichero();
                    byte[] ficheroDescifrado = descifrarBites(resumenFicheroRecibido);
                    String claveFinal = obtenerHash(ficheroDescifrado);
                    if (claveFinal.equals(fe.getFirmaFichero())) {
                        System.out.println("Fichero correctamente recibido");
                        System.out.println("Contenido: ");
                        for (int i = 0; i < fe.getLongitudFichero(); i++) {
                            System.out.print((char) ficheroDescifrado[i]);
                        }
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
            System.out.println("Se ha producido un error");
            System.out.println(e.getMessage());
        } finally {
            try {
                fsalida.close();
                reciboDelServidor.close();
                in.close();
                cliente.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Fin del envío... ");
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

    public static byte[] descifrarBites(byte[] bitesCifrados) {
        byte[] fichBytesDescifrados = null;
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, keyObj.getClave());
            fichBytesDescifrados = c.doFinal(bitesCifrados);
        } catch (InvalidKeyException ex) {
            System.out.println("Clave no valida");
        } catch (IllegalBlockSizeException ex) {
            System.out.println(ex.getMessage());
        } catch (BadPaddingException ex) {
            System.out.println(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
        } catch (NoSuchPaddingException ex) {
            System.out.println(ex.getMessage());
        } catch (java.lang.IllegalArgumentException ex) {

        }
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
