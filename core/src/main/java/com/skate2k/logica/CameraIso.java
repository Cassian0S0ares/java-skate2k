package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

/**
 * Posição da câmera no mapa. Normalmente persegue o jogador de forma suave. Na restrição "só vale
 * se filmar", age como cinegrafista: fica parada até o jogador sair de quadro e só então corre
 * atrás dele, e é esse movimento que libera as manobras.
 */
public final class CameraIso {
    private float x;
    private float y;
    private float z;
    private float velocidade;
    private boolean perseguindo;

    public CameraIso(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void atualizar(float dt, Jogador j, Restricao restricao) {
        float antesX = x;
        float antesY = y;
        if (restricao.exigeCameraEmMovimento()) {
            atualizarCinegrafista(dt, j);
        } else {
            float k = 1f - (float) Math.exp(-CAMERA_SEGUIR * dt);
            x += (j.x + j.vx * 0.18f - x) * k;
            y += (j.y + j.vy * 0.18f - y) * k;
        }
        float kz = 1f - (float) Math.exp(-3f * dt);
        z += (j.z - z) * kz;
        velocidade = dt > 0 ? (float) Math.hypot(x - antesX, y - antesY) / dt : 0f;
    }

    private void atualizarCinegrafista(float dt, Jogador j) {
        float dx = j.x - x;
        float dy = j.y - y;
        float dist = (float) Math.hypot(dx, dy);
        if (!perseguindo && dist > CAMERA_ZONA_MORTA) perseguindo = true;
        if (perseguindo && dist < CAMERA_REENQUADRA) perseguindo = false;
        if (perseguindo && dist > 0.001f) {
            float passo = Math.min(dist, CAMERA_VELOCIDADE_DOLLY * dt);
            x += dx / dist * passo;
            y += dy / dist * passo;
        }
    }

    public boolean emMovimento() {
        return velocidade > CAMERA_LIMIAR_MOVIMENTO;
    }

    public float x() { return x; }
    public float y() { return y; }
    public float z() { return z; }
}
