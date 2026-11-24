import controlador.Controller;
import modelo.Model;
import vista.BaseView;
import vista.InteractiveView;

public class App {
    public static void main(String[] args) {
        Model modelo = new Model();
        BaseView vista = new InteractiveView();
        Controller controlador = new Controller(modelo, vista);

        vista.setControlador(controlador);
        controlador.iniciar();
    }
}