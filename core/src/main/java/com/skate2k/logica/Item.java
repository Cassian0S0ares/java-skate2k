package com.skate2k.logica;

/** Item no mapa. Depois de coletado, reaparece no mesmo lugar após um tempo. */
public final class Item {
    public static final float RAIO_COLETA = 1.05f;
    public static final float ALTURA_COLETA = 2.6f;
    private static final float TEMPO_REAPARECER = 18f;

    public final TipoItem tipo;
    public final float x;
    public final float y;
    public final float z;
    private float esperaReaparecer;

    public Item(TipoItem tipo, float x, float y, float z) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void atualizar(float dt) {
        if (esperaReaparecer > 0) esperaReaparecer -= dt;
    }

    public boolean disponivel() {
        return esperaReaparecer <= 0;
    }

    public boolean alcancado(float px, float py, float pz) {
        return disponivel()
                && Math.hypot(px - x, py - y) <= RAIO_COLETA
                && pz - z <= ALTURA_COLETA
                && pz - z >= -ALTURA_COLETA;
    }

    public void coletar() {
        esperaReaparecer = TEMPO_REAPARECER;
    }
}
