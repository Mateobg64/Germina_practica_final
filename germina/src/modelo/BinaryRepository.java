package modelo;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class BinaryRepository implements IRepository {
    private final Path fichero;
    private boolean habiaDatos;
    private ArrayList<Cultivo> cultivos = new ArrayList<>();

    public BinaryRepository(Path fichero) {
        this.fichero = fichero;
    }

    @Override
    public void cargar() throws RepositoryException {
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(fichero))) {
            entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "maxdepth=24;maxarray=1000000;java.base/*;modelo.Cultivo;modelo.Tarea;modelo.Familia;modelo.TipoTarea;!*"));
            Object datos = entrada.readObject();
            if (!(datos instanceof ArrayList<?> lista)) {
                throw new IOException("El fichero no contiene un listado de cultivos.");
            }
            ArrayList<Cultivo> recuperados = new ArrayList<>();
            for (Object elemento : lista) {
                if (!(elemento instanceof Cultivo cultivo)) {
                    throw new IOException("Se ha encontrado un cultivo no válido.");
                }
                recuperados.add(cultivo);
            }
            cultivos = recuperados;
            habiaDatos = true;
        } catch (NoSuchFileException e) {
            habiaDatos = false;
            cultivos = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RepositoryException("Error al cargar " + fichero + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean habiaDatos() {
        return habiaDatos;
    }

    @Override
    public void guardar() throws RepositoryException {
        Path temporal = null;
        try {
            temporal = Files.createTempFile(fichero.toAbsolutePath().getParent(), "germina-", ".tmp");
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                salida.writeObject(cultivos);
            }
            Files.move(temporal, fichero, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RepositoryException("Error al guardar " + fichero + ": " + e.getMessage(), e);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException e) {
                    temporal.toFile().deleteOnExit();
                }
            }
        }
    }

    @Override
    public Cultivo agregarCultivo(Cultivo cultivo) throws RepositoryException {
        for (Cultivo existente : cultivos) {
            if (existente.getId().equals(cultivo.getId())) {
                throw new RepositoryException("Ya existe un cultivo con ese identificador.");
            }
        }
        cultivos.add(cultivo);
        return cultivo;
    }

    @Override
    public void eliminarCultivo(Cultivo cultivo) throws RepositoryException {
        cultivos.remove(indiceDe(cultivo));
    }

    @Override
    public void modificarCultivo(Cultivo cultivo) throws RepositoryException {
        cultivos.set(indiceDe(cultivo), cultivo);
    }

    private int indiceDe(Cultivo cultivo) throws RepositoryException {
        for (int i = 0; i < cultivos.size(); i++) {
            if (cultivos.get(i).getId().equals(cultivo.getId())) {
                return i;
            }
        }
        throw new RepositoryException("No se ha encontrado el cultivo.");
    }

    @Override
    public ArrayList<Cultivo> getTodosLosCultivos() {
        return new ArrayList<>(cultivos);
    }
}
