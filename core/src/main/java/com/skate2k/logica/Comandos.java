package com.skate2k.logica;

/**
 * Intenção do jogador em um quadro. Pode vir do teclado (TelaJogo) ou de um robô de testes
 * (BotDemo): a lógica não sabe a diferença e nunca lê dispositivos de entrada.
 */
public final class Comandos {
    /** Eixo horizontal da tela: -1 esquerda, +1 direita. */
    public float eixoX;
    /** Eixo vertical da tela: -1 baixo, +1 cima. */
    public float eixoY;
    public boolean pulo;
    /** Verdadeiro só no quadro em que a tecla foi pressionada. */
    public boolean kickflip;
    /** Verdadeiro só no quadro em que a tecla foi pressionada. */
    public boolean impossible;
    public boolean grab;
    /** -1 gira para um lado, +1 para o outro, 0 parado. */
    public int giro;

    public void limpar() {
        eixoX = 0;
        eixoY = 0;
        pulo = false;
        kickflip = false;
        impossible = false;
        grab = false;
        giro = 0;
    }
}
