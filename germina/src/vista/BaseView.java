package vista;

import controlador.Controller;

public abstract class BaseView {
    protected Controller controlador;

    public void setControlador(Controller controlador) {
        this.controlador = controlador;
    }

    public abstract void iniciar();
    public abstract void mostrarMensaje(String mensaje);
    public abstract void mostrarError(String mensaje);
    public abstract void finalizar();
}