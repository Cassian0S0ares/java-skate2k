package com.skate2k.logica;

/** Direção de subida de rampas e escadas, nos eixos do mapa. */
public enum Direcao {
    MAIS_X(1, 0), MENOS_X(-1, 0), MAIS_Y(0, 1), MENOS_Y(0, -1);

    public final int dx;
    public final int dy;

    Direcao(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
