package com.skate2k.render;

/** Projeção isométrica: converte coordenadas do mapa (x, y, z) em coordenadas de tela. */
public final class Projecao {
    public static final float LARGURA_TILE = 80f;
    public static final float ALTURA_TILE = 40f;
    public static final float ESCALA_Z = 40f;

    private Projecao() {}

    public static float sx(float x, float y) {
        return (x - y) * LARGURA_TILE / 2f;
    }

    public static float sy(float x, float y, float z) {
        return (x + y) * ALTURA_TILE / 2f + z * ESCALA_Z;
    }
}
