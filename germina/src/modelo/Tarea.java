package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Tarea implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private TipoTarea tipo;
    private LocalDate fecha;
    private String descripcion;
    private boolean completada;
    private final boolean generada;

    public Tarea(TipoTarea tipo, LocalDate fecha, String descripcion,
                 boolean generada) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.completada = false;
        this.generada = generada;
    }

    public UUID getId() {
        return id;
    }

    public TipoTarea getTipo() {
        return tipo;
    }

    public void setTipo(TipoTarea tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public boolean isGenerada() {
        return generada;
    }

    public boolean isAtrasada(LocalDate hoy) {
        return !completada && fecha.isBefore(hoy);
    }
}