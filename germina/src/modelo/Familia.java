package modelo;

public enum Familia {
    SOLANACEAS("Tomate, pimiento, berenjena, patata"),
    CUCURBITACEAS("Calabacín, pepino, melón, sandía"),
    LEGUMINOSAS("Judía, guisante, haba, garbanzo"),
    BRASICACEAS("Col, brócoli, coliflor, rábano"),
    ALIACEAS("Cebolla, ajo, puerro"),
    APIACEAS("Zanahoria, apio, perejil, chirivía"),
    ASTERACEAS("Lechuga, escarola, alcachofa"),
    AMARANTACEAS("Acelga, espinaca, remolacha"),
    OTRAS("Cualquier otra familia");

    private final String ejemplos;

    Familia(String ejemplos) {
        this.ejemplos = ejemplos;
    }

    public String getEjemplos() {
        return ejemplos;
    }
}