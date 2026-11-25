package modelo;

public class BackupIOException extends Exception {
    private static final long serialVersionUID = 1L;

    public BackupIOException(String mensaje) {
        super(mensaje);
    }

    public BackupIOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
