
import java.io.*;
import java.net.*;

public class TCPServidorMultiple {

    static GenerarClave keyObj;

    public static void main(String args[]) throws IOException {
        ServerSocket servidor;
        servidor = new ServerSocket(6000);
        System.out.println("Servidor iniciado...");
        try {
            keyObj = recogerClave();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        while (true) {
            Socket cliente = new Socket();
            cliente = servidor.accept();//esperando cliente	
            TCPHiloServidor hilo = new TCPHiloServidor(cliente, "./src", keyObj);
            hilo.start();
        }
    }

    static GenerarClave recogerClave() throws IOException, ClassNotFoundException {
        File keyFichero = new File("miClave.key");
        GenerarClave keyObj;
        ObjectInputStream clave;
        clave = new ObjectInputStream(new FileInputStream(keyFichero));
        keyObj = (GenerarClave) clave.readObject();
        return keyObj;
    }
}
