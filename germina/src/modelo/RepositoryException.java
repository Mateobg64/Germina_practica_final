package modelo;

public class RepositoryException extends Exception {
    private static final long serialVersionUID = 1L;

    public RepositoryException(String mensaje) {
        super(mensaje);
    }

    public RepositoryException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
