package modelo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class JSONBackupIO implements IBackupIO {
    private final Path home;
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe()).create();

    public JSONBackupIO(Path home) {
        this.home = home;
    }

    private Path ruta(String nombre) {
        if (nombre.isBlank() || nombre.equals(".") || nombre.equals("..")
                || nombre.contains("/") || nombre.contains("\\")) {
            throw new IllegalArgumentException("Escribe solo el nombre del fichero.");
        }
        if (!nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            nombre += ".json";
        }
        return home.resolve(nombre);
    }

    @Override
    public void exportarCultivos(List<Cultivo> cultivos, String nombreFichero) throws BackupIOException {
        try (Writer salida = Files.newBufferedWriter(ruta(nombreFichero))) {
            gson.toJson(cultivos, salida);
        } catch (IOException | RuntimeException e) {
            throw new BackupIOException("Error al exportar: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cultivo> importarCultivos(String nombreFichero) throws BackupIOException {
        try (Reader entrada = Files.newBufferedReader(ruta(nombreFichero))) {
            Cultivo[] cultivos = gson.fromJson(entrada, Cultivo[].class);
            if (cultivos == null) {
                throw new IOException("El fichero debe contener un listado JSON.");
            }
            return Arrays.asList(cultivos);
        } catch (IOException | RuntimeException e) {
            throw new BackupIOException("Error al importar: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDescripcion() {
        return "JSON";
    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter salida, LocalDate fecha) throws IOException {
            salida.value(fecha.toString());
        }

        @Override
        public LocalDate read(JsonReader entrada) throws IOException {
            return LocalDate.parse(entrada.nextString());
        }
    }
}
