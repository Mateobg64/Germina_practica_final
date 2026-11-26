package vista;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import modelo.BackupIOException;
import modelo.Cultivo;
import modelo.Familia;
import modelo.HuertoException;
import modelo.RepositoryException;
import modelo.Tarea;
import modelo.TareaCultivo;
import modelo.TipoTarea;

public class InteractiveView extends BaseView {
    private final EntradaConsola entrada = new EntradaConsola();
    private final boolean ascii;

    public InteractiveView(boolean ascii) {
        this.ascii = ascii;
    }

    @Override
    public void iniciar() {
        mostrarMensaje("Bienvenido a Germina");
        try {
            menuPrincipal();
        } catch (NoSuchElementException e) {
            mostrarMensaje("\nFin de la entrada.");
        }
    }

    private void menuPrincipal() {
        while (true) {
            mostrarMensaje("""

                    --- GERMINA ---
                    1. Nuevo cultivo
                    2. Listar todos los cultivos
                    3. Listar cultivos por bancal
                    4. Agenda
                    5. Calendario
                    6. Exportar JSON
                    7. Importar JSON
                    0. Salir""");
            int opcion = entrada.numero("Opción: ", 0, 7);
            try {
                switch (opcion) {
                    case 0 -> { return; }
                    case 1 -> nuevoCultivo();
                    case 2 -> listarCultivos(null);
                    case 3 -> listarPorBancal();
                    case 4 -> agenda();
                    case 5 -> new CalendarMenu(controlador, entrada, ascii).ejecutar();
                    case 6 -> {
                        controlador.exportarCultivos(entrada.texto("Nombre del fichero JSON: "));
                        mostrarMensaje("Cultivos exportados al directorio personal.");
                    }
                    case 7 -> {
                        int total = controlador.importarCultivos(entrada.texto("Nombre del fichero JSON: "));
                        mostrarMensaje("Cultivos importados: " + total);
                    }
                    default -> throw new IllegalStateException("Opción fuera del menú.");
                }
            } catch (RepositoryException | HuertoException | BackupIOException e) {
                mostrarError(e.getMessage());
            }
        }
    }

    private void nuevoCultivo() throws RepositoryException, HuertoException {
        Cultivo propuesta = leerCultivo();
        if (confirmarRotacion(propuesta, null)) {
            controlador.agregarCultivo(propuesta);
            mostrarMensaje("Cultivo creado y tareas generadas.");
        }
    }

    private Cultivo leerCultivo() throws RepositoryException {
        String especie = entrada.texto("Especie: ");
        Familia[] familias = Familia.values();
        for (int i = 0; i < familias.length; i++) {
            System.out.printf("%d. %s (%s)%n", i + 1, familias[i], familias[i].getEjemplos());
        }
        Familia familia = familias[entrada.numero("Familia: ", 1, familias.length) - 1];
        List<String> bancales = controlador.getBancales();
        mostrarMensaje(bancales.isEmpty() ? "Todavía no hay bancales." : "Bancales: " + String.join(", ", bancales));
        String bancal = entrada.texto("Bancal (existente o nuevo): ");
        LocalDate siembra = entrada.fecha("Fecha de siembra (AAAA-MM-DD): ");
        int dias = entrada.numero("Días hasta la cosecha: ", 1, 730);
        int riego = entrada.numero("Regar cada cuántos días: ", 1, dias);
        return new Cultivo(especie, familia, bancal, siembra, dias, riego);
    }

    private boolean confirmarRotacion(Cultivo propuesta, UUID excluir) throws RepositoryException, HuertoException {
        List<Cultivo> conflictos = controlador.getConflictosRotacion(propuesta, excluir);
        if (conflictos.isEmpty()) {
            return true;
        }
        mostrarMensaje("Aviso de rotación. Cultivos de la misma familia en ese bancal:");
        for (Cultivo cultivo : conflictos) {
            mostrarMensaje(cultivo.getEspecie() + " · siembra " + cultivo.getFechaSiembra());
        }
        return entrada.confirmar("¿Quieres continuar?");
    }

    private void listarPorBancal() throws RepositoryException, HuertoException {
        List<String> bancales = controlador.getBancales();
        if (bancales.isEmpty()) {
            mostrarMensaje("Todavía no hay bancales.");
            return;
        }
        String bancal = entrada.elegir(bancales, "Bancal: ");
        if (bancal != null) {
            listarCultivos(bancal);
        }
    }

    private void listarCultivos(String bancal) throws RepositoryException, HuertoException {
        while (true) {
            List<Cultivo> cultivos = controlador.listarCultivos(bancal);
            if (cultivos.isEmpty()) {
                mostrarMensaje("No hay cultivos en este listado.");
                return;
            }
            for (int i = 0; i < cultivos.size(); i++) {
                Cultivo cultivo = cultivos.get(i);
                System.out.printf("%d. %s · %s · %s%n", i + 1, cultivo.getEspecie(),
                        cultivo.getBancal(), cultivo.getFechaSiembra());
            }
            int indice = entrada.numero("Cultivo (0 para volver): ", 0, cultivos.size());
            if (indice == 0) {
                return;
            }
            detalle(cultivos.get(indice - 1));
        }
    }

    private void detalle(Cultivo cultivo) throws RepositoryException, HuertoException {
        UUID id = cultivo.getId();
        while (true) {
            System.out.printf("%n%s (%s) · Bancal %s · %s%n",
                    cultivo.getEspecie(), cultivo.getFamilia(), cultivo.getBancal(),
                    cultivo.isCosechado() ? "Cosechado" : "En cultivo");
            System.out.printf("Siembra %s · Cosecha prevista %s (%d días) · Riego cada %d días%n",
                    cultivo.getFechaSiembra(), cultivo.getFechaCosecha(),
                    cultivo.getDiasHastaCosecha(), cultivo.getRiegoCadaDias());
            List<Tarea> tareas = controlador.listarTareas(id);
            LocalDate hoy = controlador.hoy();
            for (int i = 0; i < tareas.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, describirTarea(tareas.get(i), hoy));
            }
            mostrarMensaje("""
                    1. Modificar cultivo
                    2. Añadir tarea manual
                    3. Gestionar una tarea
                    4. Eliminar cultivo
                    0. Volver""");
            switch (entrada.numero("Acción: ", 0, 4)) {
                case 0 -> { return; }
                case 1 -> {
                    mostrarMensaje("Introduce los datos actualizados del cultivo.");
                    Cultivo propuesta = leerCultivo();
                    if (confirmarRotacion(propuesta, id)) {
                        controlador.modificarCultivo(id, propuesta);
                        mostrarMensaje("Cultivo actualizado.");
                    }
                }
                case 2 -> agregarTarea(id);
                case 3 -> gestionarTarea(id, tareas);
                case 4 -> {
                    if (entrada.confirmar("¿Eliminar el cultivo y sus tareas?")) {
                        controlador.eliminarCultivo(id);
                        mostrarMensaje("Cultivo eliminado.");
                        return;
                    }
                }
                default -> throw new IllegalStateException("Opción fuera del menú.");
            }
        }
    }

    private String describirTarea(Tarea tarea, LocalDate hoy) {
        String estado = "Pendiente";
        if (tarea.isCompletada()) {
            estado = "Completada";
        } else if (tarea.isAtrasada(hoy)) {
            estado = "Atrasada";
        }
        String origen = tarea.isGenerada() ? "Automática" : "Manual";
        return tarea.getFecha() + " · " + tarea.getTipo() + " · " + tarea.getDescripcion()
                + " [" + estado + ", " + origen + "]";
    }

    private void agregarTarea(UUID cultivo) throws RepositoryException, HuertoException {
        TipoTarea tipo = entrada.elegir(Arrays.asList(TipoTarea.values()), "Tipo de tarea: ");
        if (tipo != null) {
            LocalDate fecha = entrada.fecha("Fecha (AAAA-MM-DD): ");
            String descripcion = entrada.texto("Descripción: ");
            controlador.agregarTarea(cultivo, tipo, fecha, descripcion);
            mostrarMensaje("Tarea manual añadida.");
        }
    }

    private void gestionarTarea(UUID cultivo, List<Tarea> tareas) throws RepositoryException, HuertoException {
        if (tareas.isEmpty()) {
            mostrarMensaje("El cultivo no tiene tareas.");
            return;
        }
        int indice = entrada.numero("Número de tarea (0 para volver): ", 0, tareas.size());
        if (indice == 0) {
            return;
        }
        Tarea tarea = tareas.get(indice - 1);
        mostrarMensaje("1. Marcar como " + (tarea.isCompletada() ? "pendiente" : "completada"));
        mostrarMensaje("2. Modificar tarea\n3. Eliminar tarea\n0. Volver");
        switch (entrada.numero("Acción: ", 0, 3)) {
            case 0 -> { return; }
            case 1 -> controlador.marcarTarea(cultivo, tarea.getId(), !tarea.isCompletada());
            case 2 -> {
                TipoTarea tipo = entrada.elegir(Arrays.asList(TipoTarea.values()), "Tipo de tarea: ");
                if (tipo != null) {
                    LocalDate fecha = entrada.fecha("Fecha (AAAA-MM-DD): ");
                    String descripcion = entrada.texto("Descripción: ");
                    controlador.modificarTarea(cultivo, tarea.getId(), tipo, fecha, descripcion);
                }
            }
            case 3 -> {
                if (entrada.confirmar("¿Eliminar esta tarea?")) {
                    controlador.eliminarTarea(cultivo, tarea.getId());
                }
            }
            default -> throw new IllegalStateException("Opción fuera del menú.");
        }
    }

    private void agenda() throws RepositoryException, HuertoException {
        while (true) {
            List<TareaCultivo> tareas = controlador.getAgenda();
            mostrarMensaje("\n--- AGENDA (incluye atrasadas) ---");
            if (tareas.isEmpty()) {
                mostrarMensaje("No hay tareas pendientes hasta dentro de siete días.");
                return;
            }
            LocalDate hoy = controlador.hoy();
            for (int i = 0; i < tareas.size(); i++) {
                TareaCultivo elemento = tareas.get(i);
                System.out.printf("%d. %s (%s) · %s%n", i + 1, elemento.getCultivo().getEspecie(),
                        elemento.getCultivo().getBancal(), describirTarea(elemento.getTarea(), hoy));
            }
            int indice = entrada.numero("Tarea para completar (0 para volver): ", 0, tareas.size());
            if (indice == 0) {
                return;
            }
            TareaCultivo elemento = tareas.get(indice - 1);
            controlador.marcarTarea(elemento.getCultivo().getId(), elemento.getTarea().getId(), true);
            mostrarMensaje("Tarea completada.");
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
        entrada.cerrar();
        mostrarMensaje("Hasta pronto.");
    }
}
