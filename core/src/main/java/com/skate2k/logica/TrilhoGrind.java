package com.skate2k.logica;

/** Linha 3D onde o skate pode encaixar um grind (corrimão, borda de banco, topo de mureta). */
public final class TrilhoGrind {
    public final float ax, ay, az;
    public final float bx, by, bz;
    public final TipoElemento tipo;
    public final float comprimento;

    TrilhoGrind(float ax, float ay, float az, float bx, float by, float bz, TipoElemento tipo) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.bx = bx;
        this.by = by;
        this.bz = bz;
        this.tipo = tipo;
        this.comprimento = (float) Math.hypot(bx - ax, by - ay);
    }

    /** Posição de 0 a 1 do ponto mais próximo do trilho, no plano do chão. */
    public float projetar(float px, float py) {
        float dx = bx - ax;
        float dy = by - ay;
        float t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        return Math.max(0f, Math.min(1f, t));
    }

    public float distancia(float px, float py) {
        float t = projetar(px, py);
        return (float) Math.hypot(px - (ax + (bx - ax) * t), py - (ay + (by - ay) * t));
    }

    public float xEm(float t) { return ax + (bx - ax) * t; }
    public float yEm(float t) { return ay + (by - ay) * t; }
    public float zEm(float t) { return az + (bz - az) * t; }
    public float anguloRad() { return (float) Math.atan2(by - ay, bx - ax); }
}
