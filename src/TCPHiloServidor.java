
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class TCPHiloServidor extends Thread {

    BufferedReader reciboDesdeElCliente;
    ObjectOutputStream envioAlCliente;
    PrintWriter fsalida;
    Socket socket = null;
    String directorio = null;
    static GenerarClave keyObj;

    public TCPHiloServidor(Socket socket, String directorio, GenerarClave keyObj) throws IOException {// CONSTRUCTOR
        this.socket = socket;
        this.directorio = directorio;
        this.keyObj = keyObj;
        // se crean flujos de entrada y salida
        reciboDesdeElCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        envioAlCliente = new ObjectOutputStream(socket.getOutputStream());
    }

    public void run() {// tarea a realizar con el cliente
        String nombreFichero = "";

        System.out.println("COMUNICO CON: " + socket.toString());
        while (!nombreFichero.trim().equals("*")) {
            try {
                nombreFichero = reciboDesdeElCliente.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } // obtener cadena
            //System.out.println(cadena);
            if (!nombreFichero.trim().equals("*")) {
                File fichero = null;
                FileInputStream ficheroIn = null;
                try {
                    System.out.println("Seleccione el fichero: " + directorio + "/" + nombreFichero);
                    fichero = new File(directorio, nombreFichero);
                    ficheroIn = new FileInputStream(fichero);
                    long bytes = fichero.length();
                    byte[] buff = new byte[(int) bytes];
                    int i, j = 0;
                    System.out.println("Lo recorro y lo meto en un buffer");
                    while ((i = ficheroIn.read()) != -1) {
                        buff[j] = (byte) i;
                        j++;
                    }
                    System.out.println("Genero el objeto");
                    String clave = obtenerHash(buff);
                    byte[] bitesCifrados = cifrarFichero(buff);
                    FicheroEnvio fe = new FicheroEnvio(200, bitesCifrados, nombreFichero, directorio, bytes, clave);
                    envioAlCliente.writeObject(fe);
                } catch (Exception e) {
                    FicheroEnvio fe = new FicheroEnvio(404, null, nombreFichero, directorio, 0, null);
                    try {
                        envioAlCliente.writeObject(fe);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } finally {
                    try {
                        ficheroIn.close();
                    } catch (Exception e) {
                    }
                }
            }
        }

        System.out.println("FIN CON: " + socket.toString());
        try {
            reciboDesdeElCliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static byte[] cifrarFichero(byte[] arrayBites) {
        byte[] fichBytesCifrados = null;
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keyObj.getClave());
            fichBytesCifrados = c.doFinal(arrayBites);
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
        return fichBytesCifrados;

    }
}
