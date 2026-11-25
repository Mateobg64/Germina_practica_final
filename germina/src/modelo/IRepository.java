package modelo;

import java.util.ArrayList;

public interface IRepository {
    default boolean habiaDatos() {
        return true;
    }

    void cargar() throws RepositoryException;
    void guardar() throws RepositoryException;
    Cultivo agregarCultivo(Cultivo cultivo) throws RepositoryException;
    void eliminarCultivo(Cultivo cultivo) throws RepositoryException;
    void modificarCultivo(Cultivo cultivo) throws RepositoryException;
    ArrayList<Cultivo> getTodosLosCultivos() throws RepositoryException;
}
