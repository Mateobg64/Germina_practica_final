package vista;

import java.util.Scanner;

public class InteractiveView extends BaseView {
    private final Scanner teclado = new Scanner(System.in);

    @Override
    public void iniciar() {
        mostrarMensaje("Bienvenido a Germina");

        while (true) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Consultar número de cultivos");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            if (!teclado.hasNextLine()) {
                return;
            }

            String opcion = teclado.nextLine().trim();

            switch (opcion) {
                case "1":
                    int total = controlador.contarCultivos();
                    mostrarMensaje("Cultivos registrados: " + total);
                    break;
                case "0":
                    return;
                default:
                    mostrarError("Opción no válida. Escribe 1 o 0.");
            }
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        System.err.println(mensaje);
    }

    @Override
    public void finalizar() {
        mostrarMensaje("Hasta pronto.");
        teclado.close();
    }
}