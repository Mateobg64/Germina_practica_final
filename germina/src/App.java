import controlador.Controller;
import modelo.BinaryRepository;
import modelo.IBackupIO;
import modelo.IRepository;
import modelo.JSONBackupIO;
import modelo.Model;
import vista.BaseView;
import vista.InteractiveView;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        boolean guardarCadaOperacion = false;
        boolean ascii = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--save":
                    i++;
                    if (i == args.length || (!args[i].equals("each") && !args[i].equals("exit"))) {
                        mostrarUso();
                        return;
                    }
                    guardarCadaOperacion = args[i].equals("each");
                    break;
                case "--ascii":
                    ascii = true;
                    break;
                default:
                    mostrarUso();
                    return;
            }
        }

        Path home = Path.of(System.getProperty("user.home"));
        Path fichero = home.resolve("germina.bin");
        IRepository repositorio = new BinaryRepository(fichero);
        IBackupIO exportador = new JSONBackupIO(home);
        Model modelo = new Model(repositorio, exportador, guardarCadaOperacion);
        BaseView vista = new InteractiveView(ascii);
        Controller controlador = new Controller(modelo, vista);
        vista.setControlador(controlador);
        controlador.iniciar();
    }

    private static void mostrarUso() {
        System.err.println("Uso: java -jar germina.jar [--save each|exit] [--ascii]");
    }
}
