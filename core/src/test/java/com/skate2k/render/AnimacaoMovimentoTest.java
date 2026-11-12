package com.skate2k.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimacaoMovimentoTest {
    @Test
    void acelerarEFrearNaoDependeDoTempoTotalDeJogo() {
        AnimacaoMovimento inicio = new AnimacaoMovimento();
        AnimacaoMovimento depoisDeUmaHora = new AnimacaoMovimento();
        // Passo binário exato para evitar diferenças de precisão do float na comparação.
        for (int i = 0; i < 1024; i++) {
            float velocidade = i % 128 / 127f;
            inicio.atualizar(i / 64f, velocidade, true);
            depoisDeUmaHora.atualizar(3600f + i / 64f, velocidade, true);
            assertEquals(inicio.quadro(), depoisDeUmaHora.quadro());
        }
    }

    @Test
    void velocidadeNaoTrocaQuadroSemPassagemDeTempo() {
        AnimacaoMovimento animacao = new AnimacaoMovimento();
        animacao.atualizar(120f, 0.2f, true);
        animacao.atualizar(120.05f, 0.2f, true);
        int quadro = animacao.quadro();
        animacao.atualizar(120.05f, 1f, true);
        assertEquals(quadro, animacao.quadro());
    }

    @Test
    void oscilacaoPertoDoLimiteNaoAlternaGrupo() {
        AnimacaoMovimento animacao = new AnimacaoMovimento();
        animacao.atualizar(0f, 0.8f, true);
        for (int i = 1; i < 100; i++) {
            animacao.atualizar(i / 60f, i % 2 == 0 ? 0.69f : 0.71f, true);
            assertTrue(animacao.rapido());
        }
        animacao.atualizar(2f, 0.6f, true);
        assertFalse(animacao.rapido());
        animacao.atualizar(2.02f, 0f, false);
        assertEquals(0, animacao.quadro());
    }
}
