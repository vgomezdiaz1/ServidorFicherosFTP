
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

public class TCPHiloServidor extends Thread {

    BufferedReader reciboDesdeElCliente;
    ObjectOutputStream envioAlCliente;
    PrintWriter fsalida;
    Socket socket = null;
    static GenerarClave keyObj;

    public TCPHiloServidor(Socket socket, GenerarClave keyObj) throws IOException {
        this.socket = socket;
        this.keyObj = keyObj;
        reciboDesdeElCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        envioAlCliente = new ObjectOutputStream(socket.getOutputStream());
    }

    public void run() {
        String nombreFichero = "";
        System.out.println("COMUNICO CON: " + socket.toString());
        try {
            while (!nombreFichero.trim().equals("*")) {
                nombreFichero = reciboDesdeElCliente.readLine();
                if (!nombreFichero.trim().equals("*")) {
                    File fichero = null;
                    FileInputStream ficheroIn = null;
                    try {
                        System.out.println("Seleccione el fichero: " + nombreFichero);
                        fichero = new File(nombreFichero);
                        ficheroIn = new FileInputStream(fichero);
                        long bytes = fichero.length();
                        byte[] buff = new byte[(int) bytes];
                        int i, j = 0;
                        while ((i = ficheroIn.read()) != -1) {
                            buff[j] = (byte) i;
                            j++;
                        }
                        String clave = obtenerHash(buff);
                        //clave = clave + "1";
                        //buff[1] = 1;
                        byte[] bitesCifrados = null;
                        try {
                            bitesCifrados = cifrarFichero(buff);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FicheroEnvio fe = new FicheroEnvio(200, bitesCifrados, fichero.getName(), bytes, clave);
                        envioAlCliente.writeObject(fe);
                    } catch (Exception e) {
                        FicheroEnvio fe = new FicheroEnvio(404, null, nombreFichero, 0, null);
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
        } catch (IOException e) {
            System.out.println("Cliente cerrado abruptamente");
        } finally {
            try {
                reciboDesdeElCliente.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("FIN CON: " + socket.toString());
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

    public static byte[] cifrarFichero(byte[] arrayBites) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] fichBytesCifrados = null;
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keyObj.getClave());
        fichBytesCifrados = c.doFinal(arrayBites);
        return fichBytesCifrados;
    }
}
