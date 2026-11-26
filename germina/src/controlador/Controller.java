package controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import modelo.BackupIOException;
import modelo.Cultivo;
import modelo.HuertoException;
import modelo.Model;
import modelo.RepositoryException;
import modelo.Tarea;
import modelo.TareaCultivo;
import modelo.TipoTarea;
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
            int total = modelo.cargarDatos();
            if (total < 0) {
                vista.mostrarMensaje("No se encontraron datos previos. Parece la primera ejecución.");
            } else {
                vista.mostrarMensaje("Cultivos cargados: " + total);
            }
        } catch (RepositoryException e) {
            vista.mostrarError(e.getMessage());
            vista.finalizar();
            return;
        }
        try {
            vista.iniciar();
        } finally {
            finalizar();
        }
    }

    public void finalizar() {
        try {
            modelo.guardarDatos();
            vista.mostrarMensaje("Estado guardado en germina.bin.");
        } catch (RepositoryException e) {
            vista.mostrarError(e.getMessage());
        } finally {
            vista.finalizar();
        }
    }

    public LocalDate hoy() {
        return modelo.hoy();
    }

    public List<Cultivo> listarCultivos(String bancal)
            throws RepositoryException {
        return modelo.listarCultivos(bancal);
    }

    public List<String> getBancales()
            throws RepositoryException {
        return modelo.getBancales();
    }

    public List<Cultivo> getConflictosRotacion(Cultivo propuesta, UUID excluir)
            throws RepositoryException, HuertoException {
        return modelo.getConflictosRotacion(propuesta, excluir);
    }

    public Cultivo agregarCultivo(Cultivo propuesta)
            throws RepositoryException, HuertoException {
        return modelo.agregarCultivo(propuesta);
    }

    public void modificarCultivo(UUID id, Cultivo propuesta)
            throws RepositoryException, HuertoException {
        modelo.modificarCultivo(id, propuesta);
    }

    public void eliminarCultivo(UUID id)
            throws RepositoryException, HuertoException {
        modelo.eliminarCultivo(id);
    }

    public List<Tarea> listarTareas(UUID id)
            throws RepositoryException, HuertoException {
        return modelo.listarTareas(id);
    }

    public void agregarTarea(UUID cultivo, TipoTarea tipo, LocalDate fecha, String descripcion)
            throws RepositoryException, HuertoException {
        modelo.agregarTarea(cultivo, tipo, fecha, descripcion);
    }

    public void modificarTarea(UUID cultivo, UUID tarea, TipoTarea tipo, LocalDate fecha, String descripcion)
            throws RepositoryException, HuertoException {
        modelo.modificarTarea(cultivo, tarea, tipo, fecha, descripcion);
    }

    public void marcarTarea(UUID cultivo, UUID tarea, boolean completada)
            throws RepositoryException, HuertoException {
        modelo.marcarTarea(cultivo, tarea, completada);
    }

    public void eliminarTarea(UUID cultivo, UUID tarea)
            throws RepositoryException, HuertoException {
        modelo.eliminarTarea(cultivo, tarea);
    }

    public List<TareaCultivo> getTareas(LocalDate desde, LocalDate hasta, String bancal, TipoTarea tipo)
            throws RepositoryException {
        return modelo.getTareas(desde, hasta, bancal, tipo);
    }

    public List<TareaCultivo> getAgenda()
            throws RepositoryException {
        return modelo.getAgenda();
    }

    public void exportarCultivos(String nombre)
            throws RepositoryException, BackupIOException {
        modelo.exportarCultivos(nombre);
    }

    public int importarCultivos(String nombre)
            throws RepositoryException, BackupIOException, HuertoException {
        return modelo.importarCultivos(nombre);
    }
}
