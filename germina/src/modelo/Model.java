package modelo;

import java.util.ArrayList;

public class Model {
    private final ArrayList<Cultivo> cultivos = new ArrayList<>();

    public int contarCultivos() {
        return cultivos.size();
    }
}