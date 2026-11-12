package com.skate2k.render;

/** Relógio contínuo: mudar a velocidade altera a cadência, sem trocar a fase do ciclo. */
final class AnimacaoMovimento {
    private float ultimoTempo = Float.NaN;
    private float fase;
    private boolean rapido;

    void atualizar(float tempo, float velocidadeRelativa, boolean movendo) {
        float dt = Float.isNaN(ultimoTempo) ? 0f : Math.max(0f, Math.min(0.05f, tempo - ultimoTempo));
        ultimoTempo = tempo;
        if (!movendo) {
            fase = 0f;
            rapido = false;
            return;
        }
        float velocidade = Math.max(0f, Math.min(1f, velocidadeRelativa));
        fase = (fase + dt * (6f + 8f * velocidade)) % 4f;
        // Evita alternar as duas folhas quando a velocidade oscila perto do limite.
        if (velocidade > 0.75f) rapido = true;
        else if (velocidade < 0.65f) rapido = false;
    }

    int quadro() { return (int) fase; }
    boolean rapido() { return rapido; }
}
