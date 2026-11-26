package modelo;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class Model {
    private final IRepository repositorio;
    private final IBackupIO exportador;
    private final boolean guardarCadaOperacion;

    public Model(IRepository repositorio, IBackupIO exportador, boolean guardarCadaOperacion) {
        this.repositorio = repositorio;
        this.exportador = exportador;
        this.guardarCadaOperacion = guardarCadaOperacion;
    }

    public int cargarDatos() throws RepositoryException {
        repositorio.cargar();
        return repositorio.habiaDatos() ? repositorio.getTodosLosCultivos().size() : -1;
    }

    public void guardarDatos() throws RepositoryException {
        repositorio.guardar();
    }

    private void guardarSiCorresponde() throws RepositoryException {
        if (guardarCadaOperacion) {
            try {
                guardarDatos();
            } catch (RepositoryException e) {
                throw new RepositoryException("Cambio aplicado en memoria. " + e.getMessage(), e);
            }
        }
    }

    public LocalDate hoy() {
        return LocalDate.now();
    }

    public List<Cultivo> listarCultivos(String bancal) throws RepositoryException {
        List<Cultivo> cultivos = repositorio.getTodosLosCultivos();
        if (bancal != null) {
            String normalizado = normalizarBancal(bancal);
            cultivos.removeIf(cultivo -> !cultivo.getBancal().equals(normalizado));
        }
        cultivos.sort(Comparator.comparing(Cultivo::getFechaSiembra).reversed());
        return cultivos;
    }

    public List<String> getBancales() throws RepositoryException {
        Set<String> bancales = new TreeSet<>();
        for (Cultivo cultivo : repositorio.getTodosLosCultivos()) {
            bancales.add(cultivo.getBancal());
        }
        return new ArrayList<>(bancales);
    }

    private String normalizarBancal(String bancal) {
        return bancal.strip().toUpperCase(Locale.ROOT);
    }

    public List<Cultivo> getConflictosRotacion(Cultivo propuesta, UUID excluir)
            throws RepositoryException, HuertoException {
        validarCultivo(propuesta);
        List<Cultivo> conflictos = new ArrayList<>();
        if (propuesta.getFamilia() == Familia.OTRAS) {
            return conflictos;
        }
        for (Cultivo existente : listarCultivos(propuesta.getBancal())) {
            if (existente.getId().equals(excluir) || existente.getFamilia() != propuesta.getFamilia()) {
                continue;
            }
            LocalDate primera = existente.getFechaSiembra();
            LocalDate segunda = propuesta.getFechaSiembra();
            if (primera.isAfter(segunda)) {
                primera = propuesta.getFechaSiembra();
                segunda = existente.getFechaSiembra();
            }
            if (primera.getYear() > LocalDate.MAX.getYear() - 3 || segunda.isBefore(primera.plusYears(3))) {
                conflictos.add(existente);
            }
        }
        return conflictos;
    }

    public Cultivo agregarCultivo(Cultivo propuesta) throws RepositoryException, HuertoException {
        validarCultivo(propuesta);
        Cultivo cultivo = new Cultivo(propuesta.getEspecie().strip(), propuesta.getFamilia(),
                normalizarBancal(propuesta.getBancal()), propuesta.getFechaSiembra(),
                propuesta.getDiasHastaCosecha(), propuesta.getRiegoCadaDias());
        generarTareas(cultivo);
        repositorio.agregarCultivo(cultivo);
        guardarSiCorresponde();
        return cultivo;
    }

    public void modificarCultivo(UUID id, Cultivo propuesta) throws RepositoryException, HuertoException {
        Cultivo cultivo = buscarCultivo(id);
        validarCultivo(propuesta);
        String especie = propuesta.getEspecie().strip();
        boolean regenerar = !cultivo.getEspecie().equals(especie)
                || !cultivo.getFechaSiembra().equals(propuesta.getFechaSiembra())
                || cultivo.getDiasHastaCosecha() != propuesta.getDiasHastaCosecha()
                || cultivo.getRiegoCadaDias() != propuesta.getRiegoCadaDias();
        cultivo.setEspecie(especie);
        cultivo.setFamilia(propuesta.getFamilia());
        cultivo.setBancal(normalizarBancal(propuesta.getBancal()));
        cultivo.setFechaSiembra(propuesta.getFechaSiembra());
        cultivo.setDiasHastaCosecha(propuesta.getDiasHastaCosecha());
        cultivo.setRiegoCadaDias(propuesta.getRiegoCadaDias());
        if (regenerar) {
            for (Tarea tarea : cultivo.getTareas()) {
                if (tarea.isGenerada() && !tarea.isCompletada()) {
                    cultivo.eliminarTarea(tarea);
                }
            }
            generarTareas(cultivo);
        }
        actualizarCultivo(cultivo);
    }

    public void eliminarCultivo(UUID id) throws RepositoryException, HuertoException {
        repositorio.eliminarCultivo(buscarCultivo(id));
        guardarSiCorresponde();
    }

    private void validarCultivo(Cultivo cultivo) throws HuertoException {
        if (cultivo == null || cultivo.getEspecie() == null || cultivo.getEspecie().isBlank()
                || cultivo.getBancal() == null || cultivo.getBancal().isBlank()
                || cultivo.getFamilia() == null || cultivo.getFechaSiembra() == null) {
            throw new HuertoException("La especie, la familia, el bancal y la fecha son obligatorios.");
        }
        if (cultivo.getDiasHastaCosecha() < 1 || cultivo.getDiasHastaCosecha() > 730) {
            throw new HuertoException("Los días hasta cosecha deben estar entre 1 y 730.");
        }
        if (cultivo.getRiegoCadaDias() < 1 || cultivo.getRiegoCadaDias() > cultivo.getDiasHastaCosecha()) {
            throw new HuertoException("El intervalo de riego debe estar entre 1 y los días hasta cosecha.");
        }
        try {
            cultivo.getFechaCosecha();
        } catch (DateTimeException e) {
            throw new HuertoException("La fecha prevista de cosecha está fuera de rango.", e);
        }
    }

    private Cultivo buscarCultivo(UUID id) throws RepositoryException, HuertoException {
        for (Cultivo cultivo : repositorio.getTodosLosCultivos()) {
            if (cultivo.getId().equals(id)) {
                return cultivo;
            }
        }
        throw new HuertoException("El cultivo ya no existe.");
    }

    private void generarTareas(Cultivo cultivo) {
        LocalDate siembra = cultivo.getFechaSiembra();
        List<Tarea> conservadas = cultivo.getTareas();
        generar(cultivo, conservadas, TipoTarea.SIEMBRA, siembra, "Siembra");
        int dias = cultivo.getDiasHastaCosecha();
        int intervalo = cultivo.getRiegoCadaDias();
        for (int dia = intervalo; dia < dias; dia += intervalo) {
            generar(cultivo, conservadas, TipoTarea.RIEGO, siembra.plusDays(dia), "Riego");
        }
        generar(cultivo, conservadas, TipoTarea.ABONADO, siembra.plusDays(dias / 2), "Abonado");
        generar(cultivo, conservadas, TipoTarea.COSECHA, cultivo.getFechaCosecha(), "Cosecha");
    }

    private void generar(Cultivo cultivo, List<Tarea> conservadas, TipoTarea tipo, LocalDate fecha, String descripcion) {
        for (Tarea tarea : conservadas) {
            if (tarea.isCompletada() && tarea.getTipo() == tipo
                    && (tipo != TipoTarea.RIEGO || tarea.getFecha().equals(fecha))) {
                return;
            }
        }
        cultivo.agregarTarea(new Tarea(tipo, fecha, descripcion + " de " + cultivo.getEspecie(), true));
    }

    public List<Tarea> listarTareas(UUID id) throws RepositoryException, HuertoException {
        List<Tarea> tareas = buscarCultivo(id).getTareas();
        tareas.sort(Comparator.comparing(Tarea::getFecha));
        return tareas;
    }

    private Tarea buscarTarea(Cultivo cultivo, UUID id) throws HuertoException {
        for (Tarea tarea : cultivo.getTareas()) {
            if (tarea.getId().equals(id)) {
                return tarea;
            }
        }
        throw new HuertoException("La tarea no pertenece a este cultivo.");
    }

    public void agregarTarea(UUID idCultivo, TipoTarea tipo, LocalDate fecha, String descripcion)
            throws RepositoryException, HuertoException {
        validarTarea(tipo, fecha, descripcion);
        Cultivo cultivo = buscarCultivo(idCultivo);
        cultivo.agregarTarea(new Tarea(tipo, fecha, descripcion.strip(), false));
        actualizarCultivo(cultivo);
    }

    public void modificarTarea(UUID idCultivo, UUID idTarea, TipoTarea tipo, LocalDate fecha, String descripcion)
            throws RepositoryException, HuertoException {
        validarTarea(tipo, fecha, descripcion);
        Cultivo cultivo = buscarCultivo(idCultivo);
        Tarea tarea = buscarTarea(cultivo, idTarea);
        tarea.setTipo(tipo);
        tarea.setFecha(fecha);
        tarea.setDescripcion(descripcion.strip());
        actualizarCultivo(cultivo);
    }

    public void marcarTarea(UUID idCultivo, UUID idTarea, boolean completada)
            throws RepositoryException, HuertoException {
        Cultivo cultivo = buscarCultivo(idCultivo);
        buscarTarea(cultivo, idTarea).setCompletada(completada);
        actualizarCultivo(cultivo);
    }

    public void eliminarTarea(UUID idCultivo, UUID idTarea) throws RepositoryException, HuertoException {
        Cultivo cultivo = buscarCultivo(idCultivo);
        cultivo.eliminarTarea(buscarTarea(cultivo, idTarea));
        actualizarCultivo(cultivo);
    }

    private void validarTarea(TipoTarea tipo, LocalDate fecha, String descripcion) throws HuertoException {
        if (tipo == null || fecha == null || descripcion == null || descripcion.isBlank()) {
            throw new HuertoException("El tipo, la fecha y la descripción de la tarea son obligatorios.");
        }
    }

    private void actualizarCultivo(Cultivo cultivo) throws RepositoryException {
        actualizarCosecha(cultivo);
        repositorio.modificarCultivo(cultivo);
        guardarSiCorresponde();
    }

    private void actualizarCosecha(Cultivo cultivo) {
        boolean cosechado = false;
        for (Tarea tarea : cultivo.getTareas()) {
            if (tarea.getTipo() == TipoTarea.COSECHA && tarea.isCompletada()) {
                cosechado = true;
                break;
            }
        }
        cultivo.setCosechado(cosechado);
    }

    public List<TareaCultivo> getTareas(LocalDate desde, LocalDate hasta, String bancal, TipoTarea tipo)
            throws RepositoryException {
        List<TareaCultivo> resultado = new ArrayList<>();
        for (Cultivo cultivo : listarCultivos(bancal)) {
            for (Tarea tarea : cultivo.getTareas()) {
                LocalDate fecha = tarea.getFecha();
                if ((desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta))
                        && (tipo == null || tarea.getTipo() == tipo)) {
                    resultado.add(new TareaCultivo(cultivo, tarea));
                }
            }
        }
        resultado.sort(Comparator.comparing(elemento -> elemento.getTarea().getFecha()));
        return resultado;
    }

    public List<TareaCultivo> getAgenda() throws RepositoryException {
        List<TareaCultivo> agenda = getTareas(null, hoy().plusDays(7), null, null);
        agenda.removeIf(elemento -> elemento.getTarea().isCompletada());
        return agenda;
    }

    public void exportarCultivos(String nombre) throws RepositoryException, BackupIOException {
        exportador.exportarCultivos(repositorio.getTodosLosCultivos(), nombre);
    }

    public int importarCultivos(String nombre) throws RepositoryException, BackupIOException, HuertoException {
        List<Cultivo> importados = exportador.importarCultivos(nombre);
        Set<UUID> ids = new HashSet<>();
        for (Cultivo existente : repositorio.getTodosLosCultivos()) {
            ids.add(existente.getId());
        }
        List<Cultivo> nuevos = new ArrayList<>();
        for (Cultivo cultivo : importados) {
            validarCultivo(cultivo);
            if (cultivo.getId() == null) {
                throw new HuertoException("Hay un cultivo sin identificador en el JSON.");
            }
            if (ids.add(cultivo.getId())) {
                validarTareasImportadas(cultivo);
                cultivo.setEspecie(cultivo.getEspecie().strip());
                cultivo.setBancal(normalizarBancal(cultivo.getBancal()));
                actualizarCosecha(cultivo);
                nuevos.add(cultivo);
            }
        }
        for (Cultivo cultivo : nuevos) {
            repositorio.agregarCultivo(cultivo);
        }
        if (!nuevos.isEmpty()) {
            guardarSiCorresponde();
        }
        return nuevos.size();
    }

    private void validarTareasImportadas(Cultivo cultivo) throws HuertoException {
        List<Tarea> tareas;
        try {
            tareas = cultivo.getTareas();
        } catch (NullPointerException e) {
            throw new HuertoException("Hay un cultivo sin lista de tareas en el JSON.", e);
        }
        Set<UUID> ids = new HashSet<>();
        for (Tarea tarea : tareas) {
            if (tarea == null || tarea.getId() == null || !ids.add(tarea.getId())) {
                throw new HuertoException("Hay una tarea con identificador ausente o repetido.");
            }
            validarTarea(tarea.getTipo(), tarea.getFecha(), tarea.getDescripcion());
        }
    }
}
