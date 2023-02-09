
import java.io.*;
import java.net.*;

public class TCPServidorMultiple {

    public static void main(String args[]) throws IOException {
        ServerSocket servidor;
        servidor = new ServerSocket(6000);
        System.out.println("Servidor iniciado...");
        
        while (true) {
            Socket cliente = new Socket();
            cliente = servidor.accept();//esperando cliente	
            TCPHiloServidor hilo = new TCPHiloServidor(cliente, "./src");
            hilo.start();
        }
    }
}
