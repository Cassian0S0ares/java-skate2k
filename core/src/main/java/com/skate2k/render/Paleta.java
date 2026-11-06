package com.skate2k.render;

import com.badlogic.gdx.graphics.Color;
import com.skate2k.logica.Fase;
import com.skate2k.logica.TipoPiso;

/** Cores do jogo: uma base "adesivo" vibrante e uma paleta própria para cada cenário. */
public final class Paleta {
    private Paleta() {}

    public static final Color TINTA = hex("1b1030");
    public static final Color TINTA_SOMBRA = new Color(0.07f, 0.03f, 0.13f, 0.65f);
    public static final Color SOMBRA_CHAO = new Color(0.06f, 0.02f, 0.14f, 0.32f);
    public static final Color BRANCO = hex("fff8ec");
    public static final Color ROSA = hex("ff2e88");
    public static final Color CIANO = hex("19e3ff");
    public static final Color AMARELO = hex("ffd92e");
    public static final Color LIMA = hex("a6ff2e");
    public static final Color LARANJA = hex("ff8a1f");
    public static final Color ROXO = hex("7a3cff");
    public static final Color VERMELHO = hex("ff3b3b");
    public static final Color CINZA = hex("8d88a8");
    public static final Color PAINEL = new Color(0.11f, 0.06f, 0.2f, 0.88f);

    public static Color hex(String h) {
        return Color.valueOf(h);
    }

    public static Color copia(Color c, float alfa) {
        return new Color(c.r, c.g, c.b, alfa);
    }

    /** Conjunto de cores de um cenário. */
    public static final class Tema {
        public final Color fundo;
        public final Color asfalto;
        public final Color faixa;
        public final Color calcada;
        public final Color concreto;
        public final Color grama;
        public final Color terra;
        public final Color pisoParque;
        public final Color pintado;
        public final Color rampa;
        public final Color listraRampa;
        public final Color mureta;
        public final Color banco;
        public final Color escada;
        public final Color plataforma;

        Tema(String fundo, String asfalto, String faixa, String calcada, String concreto, String grama, String terra,
             String pisoParque, String pintado, String rampa, String listraRampa, String mureta, String banco,
             String escada, String plataforma) {
            this.fundo = hex(fundo);
            this.asfalto = hex(asfalto);
            this.faixa = hex(faixa);
            this.calcada = hex(calcada);
            this.concreto = hex(concreto);
            this.grama = hex(grama);
            this.terra = hex(terra);
            this.pisoParque = hex(pisoParque);
            this.pintado = hex(pintado);
            this.rampa = hex(rampa);
            this.listraRampa = hex(listraRampa);
            this.mureta = hex(mureta);
            this.banco = hex(banco);
            this.escada = hex(escada);
            this.plataforma = hex(plataforma);
        }

        public Color piso(TipoPiso tipo) {
            switch (tipo) {
                case ASFALTO: return asfalto;
                case FAIXA: return asfalto;
                case FAIXA_PEDESTRE: return asfalto;
                case CALCADA: return calcada;
                case CONCRETO: return concreto;
                case GRAMA: return grama;
                case TERRA: return terra;
                case PISO_PARQUE: return pisoParque;
                default: return pintado;
            }
        }
    }

    private static final Tema RUA = new Tema("2a1f47", "3f3860", "ffd92e", "c9b8dc", "a79dbf", "57c96f", "b98a55",
            "e3c9a0", "6e5cd6", "ff5fa8", "ffd92e", "f5a35c", "c7753d", "e2d6f0", "b39ddb");
    private static final Tema PARQUE = new Tema("1d6b4f", "4a4a5e", "fff8ec", "d9cfa8", "c5c1a4", "56d66b", "c99b5c",
            "f2d79c", "e87f5a", "ff9b3d", "fff8ec", "e8e2c0", "b5602e", "efe7c9", "cfc6a0");
    private static final Tema SKATEPARK = new Tema("20234f", "3b3f73", "ffd92e", "9fb3e0", "a9bdf0", "57c96f", "b98a55",
            "e3c9a0", "8d97f5", "19e3ff", "ff2e88", "ff5fa8", "ffd92e", "dfe6ff", "6f7ae6");
    private static final Tema SUBURBIO = new Tema("3f7a3a", "4f4c68", "fff8ec", "efe3c8", "d5ccb4", "8fe05c", "c99b5c",
            "f2d79c", "ec8fb0", "ff7b5a", "fff8ec", "f7efe0", "a8623a", "f7efe0", "e8b48a");

    public static Tema tema(Fase.Tema tema) {
        switch (tema) {
            case RUA: return RUA;
            case PARQUE: return PARQUE;
            case SKATEPARK: return SKATEPARK;
            default: return SUBURBIO;
        }
    }

    public static Color claro(Color c, float f) {
        return new Color(Math.min(1, c.r + (1 - c.r) * f), Math.min(1, c.g + (1 - c.g) * f),
                Math.min(1, c.b + (1 - c.b) * f), c.a);
    }

    public static Color escuro(Color c, float f) {
        return new Color(c.r * (1 - f), c.g * (1 - f), c.b * (1 - f), c.a);
    }
}
