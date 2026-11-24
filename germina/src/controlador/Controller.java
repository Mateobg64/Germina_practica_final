package controlador;

import modelo.Model;
import vista.BaseView;

public class Controller {
    private final Model modelo;
    private final BaseView vista;

    public Controller(Model modelo, BaseView vista) {
        this.modelo = modelo;
        this.vista = vista;
    }

    public void iniciar() {
        try {
            vista.iniciar();
        } finally {
            finalizar();
        }
    }

    public int contarCultivos() {
        return modelo.contarCultivos();
    }

    public void finalizar() {
        vista.finalizar();
    }
}