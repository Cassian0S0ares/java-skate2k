package com.skate2k.logica;

/** Uma manobra executada: tipo, pontos já com o potencializador aplicado e dificuldade efetiva. */
public final class Manobra {
    public final TipoManobra tipo;
    public final int pontos;
    public final int dificuldade;

    private Manobra(TipoManobra tipo, int pontos, int dificuldade) {
        this.tipo = tipo;
        this.pontos = pontos;
        this.dificuldade = dificuldade;
    }

    public static Manobra de(TipoManobra tipo, float potencial) {
        return new Manobra(tipo, Math.round(tipo.pontosBase * potencial), tipo.dificuldade);
    }

    /** Grab rende mais quanto mais tempo o jogador segura o skate. */
    public static Manobra grab(float segundos, float potencial) {
        int base = TipoManobra.GRAB.pontosBase + Math.round(segundos * 170f);
        int dificuldade = TipoManobra.GRAB.dificuldade + (segundos > 0.7f ? 1 : 0);
        return new Manobra(TipoManobra.GRAB, Math.round(base * potencial), dificuldade);
    }

    /** Grinds rendem por tempo em cima do obstáculo. */
    public static Manobra grind(boolean boardslide, float segundos, float potencial) {
        TipoManobra tipo = boardslide ? TipoManobra.BOARDSLIDE : TipoManobra.GRIND_5050;
        int base = tipo.pontosBase + Math.round(segundos * (boardslide ? 300f : 220f));
        int dificuldade = tipo.dificuldade + (segundos > 1.2f ? 1 : 0) + (segundos > 2.4f ? 1 : 0);
        return new Manobra(tipo, Math.round(base * potencial), dificuldade);
    }

    public String nome() {
        return tipo.nome;
    }

    @Override
    public String toString() {
        return tipo.nome + " +" + pontos;
    }
}
