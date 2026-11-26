package vista;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

class EntradaConsola {
    private final Scanner teclado = new Scanner(System.in);

    String leer(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().strip();
    }

    String texto(String mensaje) {
        while (true) {
            String valor = leer(mensaje);
            if (!valor.isEmpty()) {
                return valor;
            }
            System.err.println("Introduce un texto.");
        }
    }

    int numero(String mensaje, int minimo, int maximo) {
        while (true) {
            String entrada = leer(mensaje);
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // Se muestra el mismo mensaje para entradas no numéricas y valores fuera de rango.
            }
            System.err.println("Introduce un número entre " + minimo + " y " + maximo + ".");
        }
    }

    LocalDate fecha(String mensaje) {
        while (true) {
            try {
                LocalDate fecha = LocalDate.parse(leer(mensaje));
                if (fecha.getYear() >= 1 && fecha.getYear() <= 9999) {
                    return fecha;
                }
            } catch (DateTimeParseException e) {
                // Se vuelve a pedir el dato.
            }
            System.err.println("Introduce una fecha válida con formato AAAA-MM-DD.");
        }
    }

    boolean confirmar(String pregunta) {
        while (true) {
            String respuesta = leer(pregunta + " (s/n): ");
            if (respuesta.equalsIgnoreCase("s")) {
                return true;
            }
            if (respuesta.equalsIgnoreCase("n")) {
                return false;
            }
            System.err.println("Escribe s o n.");
        }
    }

    <T> T elegir(List<T> opciones, String mensaje) {
        for (int i = 0; i < opciones.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, opciones.get(i));
        }
        System.out.println("0. Volver");
        int indice = numero(mensaje, 0, opciones.size());
        return indice == 0 ? null : opciones.get(indice - 1);
    }

    void cerrar() {
        teclado.close();
    }
}
