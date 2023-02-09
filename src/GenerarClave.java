import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.*;
import javax.crypto.*;

public class GenerarClave implements Serializable {

    private SecretKey clave;

    public Key getClave() {
        return clave;
    }

    public void setClave(SecretKey clave) {
        this.clave = clave;
    }

    public GenerarClave() {
    }

    public static void main(String[] args) {
        ObjectOutputStream claveObj = null;
        File fichero = null;
        GenerarClave key = null;
        KeyGenerator keyGen;

        try {
            key = new GenerarClave();

            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            key.setClave(keyGen.generateKey());
            fichero = new File("miClave.key");
            claveObj = new ObjectOutputStream(new FileOutputStream(fichero));
            claveObj.writeObject(key);
            System.out.println("Clave generada de tipo:" + key.getClave().getAlgorithm());
            System.out.println("Clave format:" + key.getClave().getFormat());
            System.out.println("Clave Encoded:" + key.getClave().getEncoded());

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (claveObj != null) {
                    claveObj.close();
                }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
        }

    }

}
