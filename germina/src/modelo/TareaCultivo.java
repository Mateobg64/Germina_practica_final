package modelo;

public class TareaCultivo {
    private final Cultivo cultivo;
    private final Tarea tarea;

    public TareaCultivo(Cultivo cultivo, Tarea tarea) {
        this.cultivo = cultivo;
        this.tarea = tarea;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public Tarea getTarea() {
        return tarea;
    }
}
