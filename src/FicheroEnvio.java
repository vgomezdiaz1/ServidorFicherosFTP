import java.io.Serializable;

public class FicheroEnvio implements Serializable {
    private byte[] contenidoFichero;
    private String nombre;
    private String directorio;
    private long longitudFichero;
    private String firmaFichero; // añadido para seguridad

    public FicheroEnvio(byte[] contenidoFichero, String nombre, String directorio, long longitudFichero, String firmaFichero) {
        this.contenidoFichero = contenidoFichero;
        this.nombre = nombre;
        this.directorio = directorio;
        this.longitudFichero = longitudFichero;
        this.firmaFichero = firmaFichero;
    }
    
    public byte[] getContenidoFichero() {
        return contenidoFichero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDirectorio() {
        return directorio;
    }

    public long getLongitudFichero() {
        return longitudFichero;
    }

    public void setContenidoFichero(byte[] contenidoFichero) {
        this.contenidoFichero = contenidoFichero;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDirectorio(String directorio) {
        this.directorio = directorio;
    }

    public String getFirmaFichero() {
        return firmaFichero;
    }

    public void setFirmaFichero(String firmaFichero) {
        this.firmaFichero = firmaFichero;
    }

    
}