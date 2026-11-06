package com.skate2k.logica;

/** Catálogo de manobras. A dificuldade define a experiência de estilo que cada uma rende. */
public enum TipoManobra {
    KICKFLIP("Kickflip", 100, 1),
    DOUBLE_KICKFLIP("Double Kickflip", 320, 3),
    TRIPLE_KICKFLIP("Triple Kickflip", 750, 5),
    IMPOSSIBLE("Impossible", 160, 2),
    DOUBLE_IMPOSSIBLE("Double Impossible", 420, 4),
    HARDFLIP("Hardflip", 150, 2),
    VARIAL("Varial", 100, 1),
    GIRO_180("Giro 180", 90, 1),
    GIRO_360("Giro 360", 240, 2),
    GIRO_540("Giro 540", 480, 3),
    GIRO_720("Giro 720", 850, 4),
    GRAB("Pega-Pão", 60, 1),
    GRIND_5050("50-50", 80, 1),
    BOARDSLIDE("Boardslide", 120, 2),
    GAP_ESCADA("Gap da Escada", 300, 3),
    VOO_LONGO("Voo da Galinha", 120, 1);

    public final String nome;
    public final int pontosBase;
    public final int dificuldade;

    TipoManobra(String nome, int pontosBase, int dificuldade) {
        this.nome = nome;
        this.pontosBase = pontosBase;
        this.dificuldade = dificuldade;
    }
}
