package modelo;

public class HuertoException extends Exception {
    private static final long serialVersionUID = 1L;

    public HuertoException(String mensaje) {
        super(mensaje);
    }

    public HuertoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
