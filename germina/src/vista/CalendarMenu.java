package vista;

import controlador.Controller;
import es.usal.progiii.calendar.ConsoleCalendar;
import es.usal.progiii.calendar.EventStyle;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import modelo.RepositoryException;
import modelo.Tarea;
import modelo.TareaCultivo;
import modelo.TipoTarea;

class CalendarMenu {
    private final Controller controlador;
    private final EntradaConsola entrada;
    private final ConsoleCalendar calendario = new ConsoleCalendar();
    private boolean mensual = true;
    private LocalDate referencia;
    private String filtroBancal;
    private TipoTarea filtroTipo;

    CalendarMenu(Controller controlador, EntradaConsola entrada, boolean ascii) {
        this.controlador = controlador;
        this.entrada = entrada;
        this.referencia = controlador.hoy();
        calendario.setModoAscii(ascii);
    }

    void ejecutar() throws RepositoryException {
        while (true) {
            dibujar();
            System.out.println("""
                    --- CALENDARIO ---
                    1. Vista mensual
                    2. Vista semanal
                    3. Anterior
                    4. Siguiente
                    5. Ir a una fecha
                    6. Volver a hoy
                    7. Filtrar por bancal
                    8. Filtrar por tipo de tarea
                    9. Quitar filtros
                    0. Volver al menú principal""");
            switch (entrada.numero("Opción: ", 0, 9)) {
                case 0 -> { return; }
                case 1 -> mensual = true;
                case 2 -> mensual = false;
                case 3 -> mover(-1);
                case 4 -> mover(1);
                case 5 -> referencia = entrada.fecha("Fecha (AAAA-MM-DD): ");
                case 6 -> referencia = controlador.hoy();
                case 7 -> elegirBancal();
                case 8 -> {
                    TipoTarea tipo = entrada.elegir(Arrays.asList(TipoTarea.values()), "Tipo de tarea: ");
                    if (tipo != null) {
                        filtroTipo = tipo;
                    }
                }
                case 9 -> {
                    filtroBancal = null;
                    filtroTipo = null;
                }
                default -> throw new IllegalStateException("Opción fuera del menú.");
            }
        }
    }

    private void mover(int paso) {
        LocalDate nueva = mensual ? referencia.plusMonths(paso) : referencia.plusWeeks(paso);
        if (nueva.getYear() >= 1 && nueva.getYear() <= 9999) {
            referencia = nueva;
        }
    }

    private void elegirBancal() throws RepositoryException {
        List<String> bancales = controlador.getBancales();
        if (bancales.isEmpty()) {
            System.out.println("Todavía no hay bancales.");
            return;
        }
        String elegido = entrada.elegir(bancales, "Bancal: ");
        if (elegido != null) {
            filtroBancal = elegido;
        }
    }

    private void dibujar() throws RepositoryException {
        YearMonth mes = YearMonth.from(referencia);
        LocalDate desde = mensual ? mes.atDay(1)
                : referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate hasta = mensual ? mes.atEndOfMonth() : desde.plusDays(6);
        LocalDate hoy = controlador.hoy();
        calendario.setHoy(hoy);
        calendario.setSubtitulo("Bancal: " + (filtroBancal == null ? "todos" : filtroBancal)
                + " | Tipo: " + (filtroTipo == null ? "todos" : filtroTipo));
        calendario.limpiarEventos();

        for (TareaCultivo elemento : controlador.getTareas(desde, hasta, filtroBancal, filtroTipo)) {
            Tarea tarea = elemento.getTarea();
            EventStyle estilo = EventStyle.NORMAL;
            if (tarea.isCompletada()) {
                estilo = EventStyle.COMPLETADO;
            } else if (tarea.isAtrasada(hoy)) {
                estilo = EventStyle.ATRASADO;
            }
            String texto = tarea.getTipo() + " - " + elemento.getCultivo().getEspecie()
                    + " (" + elemento.getCultivo().getBancal() + ")";
            calendario.agregarEvento(tarea.getFecha(), texto, estilo);
        }
        if (mensual) {
            calendario.imprimirMes(mes);
        } else {
            calendario.imprimirSemana(referencia);
        }
    }
}
