package modelo;

import java.util.List;

public interface IBackupIO {
    void exportarCultivos(List<Cultivo> cultivos, String nombreFichero) throws BackupIOException;
    List<Cultivo> importarCultivos(String nombreFichero) throws BackupIOException;
    String getDescripcion();
}
