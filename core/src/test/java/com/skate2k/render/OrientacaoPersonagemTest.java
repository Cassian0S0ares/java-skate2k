package com.skate2k.render;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrientacaoPersonagemTest {
    @Test
    void acompanhaAsQuatroDirecoesProjetadasNaTela() {
        // Projecao: +X sobe à direita; +Y sobe à esquerda; -X desce à esquerda; -Y desce à direita.
        float[][] velocidades = {{2, 0}, {0, 2}, {-2, 0}, {0, -2}};
        int[] linhas = {3, 2, 1, 0}; // NE, NW, SW, SE
        for (int i = 0; i < velocidades.length; i++) {
            float yaw = FolhaPersonagem.rumoDoSprite(0f, velocidades[i][0], velocidades[i][1], false, 180f);
            assertEquals(linhas[i], FolhaPersonagem.linhaDoRumo(yaw));
        }
    }

    @Test
    void giroSoAlteraPoseDuranteOVoo() {
        float noChao = FolhaPersonagem.rumoDoSprite(0f, 2f, 0f, false, 180f);
        float noAr = FolhaPersonagem.rumoDoSprite(0f, 2f, 0f, true, 180f);
        assertEquals(3, FolhaPersonagem.linhaDoRumo(noChao));
        assertEquals(1, FolhaPersonagem.linhaDoRumo(noAr));
    }

    @Test
    void paradoPreservaRumoSemDependerDeVelocidadeResidual() {
        float rumo = -(float) Math.PI / 2f;
        assertEquals(rumo, FolhaPersonagem.rumoDoSprite(rumo, 0.001f, 0.001f, false, 0f));
    }
}
