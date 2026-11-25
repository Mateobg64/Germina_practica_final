package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Cultivo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private String especie;
    private Familia familia;
    private String bancal;
    private LocalDate fechaSiembra;
    private int diasHastaCosecha;
    private int riegoCadaDias;
    private boolean cosechado;
    private final List<Tarea> tareas;

    public Cultivo(String especie, Familia familia, String bancal, LocalDate fechaSiembra, int diasHastaCosecha, int riegoCadaDias) {
        this.id = UUID.randomUUID();
        this.especie = especie;
        this.familia = familia;
        this.bancal = bancal;
        this.fechaSiembra = fechaSiembra;
        this.diasHastaCosecha = diasHastaCosecha;
        this.riegoCadaDias = riegoCadaDias;
        this.cosechado = false;
        this.tareas = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public Familia getFamilia() {
        return familia;
    }

    public void setFamilia(Familia familia) {
        this.familia = familia;
    }

    public String getBancal() {
        return bancal;
    }

    public void setBancal(String bancal) {
        this.bancal = bancal;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public int getDiasHastaCosecha() {
        return diasHastaCosecha;
    }

    public void setDiasHastaCosecha(int diasHastaCosecha) {
        this.diasHastaCosecha = diasHastaCosecha;
    }

    public int getRiegoCadaDias() {
        return riegoCadaDias;
    }

    public void setRiegoCadaDias(int riegoCadaDias) {
        this.riegoCadaDias = riegoCadaDias;
    }

    public boolean isCosechado() {
        return cosechado;
    }

    public void setCosechado(boolean cosechado) {
        this.cosechado = cosechado;
    }

    public LocalDate getFechaCosecha() {
        return fechaSiembra.plusDays(diasHastaCosecha);
    }

    public List<Tarea> getTareas() {
        return new ArrayList<>(tareas);
    }

    public void agregarTarea(Tarea tarea) {
        tareas.add(tarea);
    }

    public void eliminarTarea(Tarea tarea) {
        tareas.remove(tarea);
    }
}