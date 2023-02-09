
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TCPCliente {

    public static void main(String[] args) {
        String Host = "localhost";
        int Puerto = 6000;// puerto remoto

        try {
            Socket cliente = new Socket(Host, Puerto);

            // CREO FLUJO DE SALIDA AL SERVIDOR	
            PrintWriter fsalida = new PrintWriter(cliente.getOutputStream(), true);
            // CREO FLUJO DE ENTRADA DESDE SERVIDOR	
            ObjectInputStream reciboDelServidor = new ObjectInputStream(cliente.getInputStream());

            // FLUJO PARA ENTRADA ESTANDAR
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String cadena;

            do {
                System.out.print("Introduce el nombre del fichero: ");
                cadena = in.readLine();
                fsalida.println(cadena);
                FicheroEnvio fe = (FicheroEnvio) reciboDelServidor.readObject();
                System.out.println(fe.getDirectorio() + "/" + fe.getNombre() + "\t(" + fe.getLongitudFichero() + ")");

                byte[] resumenFicheroRecibido = fe.getContenidoFichero();
                byte [] ficheroDescifrado = descifrarBites(resumenFicheroRecibido);
                
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

                //TODO - generar fichero  
            } while (!cadena.trim().equals("*"));

            fsalida.close();
            reciboDelServidor.close();
            System.out.println("Fin del envío... ");
            in.close();
            cliente.close();
        } catch (Exception e) {
            System.out.println("Se ha producido un error");
            System.out.println(e.getMessage());
        }
    }//

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
        File keyFichero = new File("miClave.key");
        GenerarClave keyObj;

        ObjectInputStream clave;
        byte[] fichBytesDescifrados = null;

        try {
            clave = new ObjectInputStream(new FileInputStream(keyFichero));
            keyObj = (GenerarClave) clave.readObject();

            // Cifrando byte[] con Cipher.
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, keyObj.getClave());
            fichBytesDescifrados = c.doFinal(bitesCifrados);

        } catch (IOException ex) {
            System.out.println("Error I/O");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
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
}//
