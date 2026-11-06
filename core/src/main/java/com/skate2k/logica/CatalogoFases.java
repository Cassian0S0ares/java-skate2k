package com.skate2k.logica;

import static com.skate2k.logica.Direcao.*;
import static com.skate2k.logica.TipoPiso.*;

import com.skate2k.logica.Cenario.TipoEnfeite;

/** As quatro fases do jogo, montadas à mão: rua, parque, skatepark e subúrbio. */
public final class CatalogoFases {
    public static final int TOTAL = 4;

    private CatalogoFases() {}

    public static Fase criar(int indice) {
        switch (indice) {
            case 0: return rua();
            case 1: return parque();
            case 2: return skatepark();
            case 3: return suburbio();
            default: throw new IllegalArgumentException("Fase inexistente: " + indice);
        }
    }

    // ------------------------------------------------------------------------------- 1: rua

    private static Fase rua() {
        Cenario c = new Cenario(30, 24, CALCADA, ASFALTO);
        c.preencher(0, 9, 30, 7, ASFALTO);
        for (int x = 0; x < 30; x += 3) c.pintar(x, 12, FAIXA);
        c.preencher(14, 9, 3, 7, FAIXA_PEDESTRE);

        // Prédios atrás do mapa
        c.fundo(VisualBloco.PREDIO, 0, 24, 8, 3, 5.5f, "PADARIA", 0);
        c.fundo(VisualBloco.PREDIO, 8, 24, 7, 3, 7f, "XEROX 24H", 1);
        c.fundo(VisualBloco.PREDIO, 15, 24, 8, 3, 5f, "PASTEL", 2);
        c.fundo(VisualBloco.PREDIO, 23, 24, 7, 3, 6.5f, "BAR DO ZÉ", 3);
        c.fundo(VisualBloco.PREDIO, 30, 16, 3, 11, 6f, "LOJA", 1);
        c.fundo(VisualBloco.PREDIO, 30, 5, 3, 11, 7.5f, "ROLÊ", 0);
        c.fundo(VisualBloco.PREDIO, 30, -4, 3, 9, 5f, "BANCA", 2);

        // Conjunto de escada com corrimão, plataforma e acesso por rampa
        c.escada(20, 16.5f, 4, 2.5f, MAIS_Y, 1.25f, true);
        c.plataforma(20, 19, 4, 3, 1.25f);
        c.rampa(24, 19, 3.5f, 3, MENOS_X, 1.25f);

        // Rampas de chute
        c.rampa(10, 4.5f, 2.6f, 2.5f, MAIS_X, 0.85f);
        c.rampa(25.5f, 10, 2.6f, 2.5f, MENOS_X, 0.95f);
        c.rampa(3, 13, 2.6f, 2.2f, MAIS_X, 0.9f);

        // Bancos, muretas e corrimão plano
        c.banco(5.5f, 18, 3.2f, 0.7f, 0.55f);
        c.banco(22, 6, 3.2f, 0.7f, 0.55f);
        c.mureta(9.5f, 20.5f, 8, 0.6f, 0.7f);
        c.mureta(1.5f, 2.5f, 6, 0.6f, 0.6f);
        c.corrimao(15, 7.5f, 0.85f, 21, 7.5f, 0.85f);

        // Obstáculos da rua
        c.bloco(VisualBloco.CARRO, 6, 10, 2.6f, 1.2f, 0.9f, 0);
        c.bloco(VisualBloco.CARRO, 20.5f, 13.5f, 2.6f, 1.2f, 0.9f, 1);
        c.bloco(VisualBloco.LIXEIRA, 13, 17, 0.5f, 0.5f, 0.8f, 0);
        c.bloco(VisualBloco.LIXEIRA, 2, 8, 0.5f, 0.5f, 0.8f, 0);
        c.bloco(VisualBloco.HIDRANTE, 18, 8.3f, 0.35f, 0.35f, 0.6f, 0);
        c.bloco(VisualBloco.POSTE, 12.5f, 7.8f, 0.3f, 0.3f, 3.6f, 0);
        c.bloco(VisualBloco.POSTE, 3.5f, 16.5f, 0.3f, 0.3f, 3.6f, 0);
        c.bloco(VisualBloco.BANCA, 1, 19.5f, 2.6f, 2.6f, 1.7f, 0);

        c.enfeite(TipoEnfeite.BUEIRO, 8.5f, 14, 0);
        c.enfeite(TipoEnfeite.BUEIRO, 24, 11, 0);
        c.enfeite(TipoEnfeite.PICHACAO, 7, 6, 0);
        c.enfeite(TipoEnfeite.RACHADURA, 11, 11, 0);
        c.enfeite(TipoEnfeite.RACHADURA, 18, 10.5f, 1);
        c.enfeite(TipoEnfeite.POMBO, 15.5f, 18, 0);
        c.enfeite(TipoEnfeite.POMBO, 16.5f, 18.6f, 1);

        Fase f = new Fase(1, "RUA DA PADARIA", "CUIDADO COM O PÃO QUENTE", Fase.Tema.RUA, c,
                80f, 10000, 0.95f, 5.5f, 5f, 6f);
        f.robos(TipoRobo.ROLIMA, 3);
        f.entradas(24, 3, 15, 13, 26, 15, 2, 12, 12, 18, 18, 3);
        f.item(TipoItem.CAFE, 9, 12);
        f.item(TipoItem.PASTEL, 17, 5);
        f.item(TipoItem.CAFE, 22, 21f);
        f.item(TipoItem.PASTEL, 3.5f, 17.5f);
        f.item(TipoItem.CURATIVO, 27, 7);
        return f;
    }

    // ---------------------------------------------------------------------------- 2: parque

    private static Fase parque() {
        Cenario c = new Cenario(30, 30, GRAMA, GRAMA);
        c.preencher(13, 0, 4, 30, PISO_PARQUE);
        c.preencher(0, 13, 30, 4, PISO_PARQUE);
        c.preencher(10, 10, 10, 10, PISO_PARQUE);
        c.preencher(3, 19, 8, 7, TERRA);

        c.fundo(VisualBloco.PREDIO, 0, 30, 10, 2.5f, 4.5f, "CORETO", 2);
        c.fundo(VisualBloco.PREDIO, 20, 30, 10, 2.5f, 5.5f, "PIPOCA", 3);
        c.fundo(VisualBloco.PREDIO, 30, 0, 2.5f, 12, 5, "ZOO?", 1);
        c.fundo(VisualBloco.PREDIO, 30, 20, 2.5f, 10, 6, "PAZ", 0);

        c.bloco(VisualBloco.FONTE, 13.5f, 13.5f, 3, 3, 1.0f, 0);

        // Bancos na praça central
        c.banco(11, 18.6f, 3.2f, 0.7f, 0.55f);
        c.banco(16, 10.7f, 3.2f, 0.7f, 0.55f);
        c.banco(10.7f, 11, 0.7f, 2.2f, 0.55f);
        c.banco(18.6f, 16.5f, 0.7f, 2.4f, 0.55f);

        // Canteiros com mureta
        c.mureta(21, 19, 7, 0.6f, 0.65f);
        c.mureta(21, 25, 7, 0.6f, 0.65f);
        c.mureta(2, 7, 8, 0.6f, 0.7f);
        c.mureta(20, 3, 0.6f, 7, 0.65f);

        // Rampas nas alamedas
        c.rampa(21, 14, 2.8f, 2, MAIS_X, 1.0f);
        c.rampa(6.2f, 14, 2.8f, 2, MENOS_X, 1.0f);
        c.rampa(14, 21, 2, 2.8f, MAIS_Y, 1.0f);
        c.rampa(14, 6, 2, 2.8f, MENOS_Y, 1.0f);

        // Corrimãos longos
        c.corrimao(13.2f, 24.5f, 0.85f, 13.2f, 29, 0.85f);
        c.corrimao(23, 12.6f, 0.85f, 29, 12.6f, 0.85f);
        c.corrimao(1, 17.4f, 0.85f, 6, 17.4f, 0.85f);

        // Escadinha do coreto
        c.escada(4, 20, 4, 2, MAIS_Y, 1f, true);
        c.plataforma(4, 22, 4, 3, 1f);
        c.rampa(8, 22, 3, 3, MENOS_X, 1f);

        // Árvores e arbustos
        float[][] arvores = {{3, 3}, {8, 2}, {24, 5}, {27, 9}, {4, 11}, {24, 22}, {26, 28}, {2, 27}, {10, 27},
            {18, 3}, {27, 2}, {19, 26}};
        for (float[] a : arvores) c.bloco(VisualBloco.ARVORE, a[0], a[1], 0.6f, 0.6f, 2.8f, (int) a[0] % 3);
        c.bloco(VisualBloco.ARBUSTO, 22, 21, 1.4f, 1.2f, 0.7f, 0);
        c.bloco(VisualBloco.ARBUSTO, 25, 21.6f, 1.4f, 1.2f, 0.7f, 1);
        c.bloco(VisualBloco.LIXEIRA, 19.8f, 19.8f, 0.5f, 0.5f, 0.8f, 0);

        for (int i = 0; i < 16; i++) {
            c.enfeite(TipoEnfeite.FLORES, 1 + (i * 7.3f) % 28, 1 + (i * 11.7f) % 28, i % 3);
        }
        for (int i = 0; i < 24; i++) {
            c.enfeite(TipoEnfeite.TUFO, 0.5f + (i * 5.9f) % 29, 0.5f + (i * 3.7f) % 29, i % 2);
        }
        float[][] pombos = {{11.5f, 12.5f}, {12, 12}, {17.5f, 17}, {18, 12.5f}, {12.5f, 17.5f}, {17, 18.5f}};
        for (int i = 0; i < pombos.length; i++) {
            c.enfeite(TipoEnfeite.POMBO, pombos[i][0], pombos[i][1], i);
        }

        Fase f = new Fase(2, "PARQUE DO POMBO", "NA GRAMA O SKATE EMPACA", Fase.Tema.PARQUE, c,
                85f, 18000, 1.0f, 4.8f, 15f, 4f);
        f.robos(TipoRobo.ROLIMA, 3).robos(TipoRobo.FAISCA, 2);
        f.entradas(15, 26, 26, 15, 3, 15, 24, 15, 15, 19, 18, 12);
        f.item(TipoItem.TENIS_MOLA, 12, 15);
        f.item(TipoItem.RELOGIO, 26, 16);
        f.item(TipoItem.TENIS_MOLA, 16, 27);
        f.item(TipoItem.RELOGIO, 6, 23.5f);
        f.item(TipoItem.CURATIVO, 8, 4);
        f.item(TipoItem.CURATIVO, 25, 17);
        return f;
    }

    // ------------------------------------------------------------------------- 3: skatepark

    private static Fase skatepark() {
        Cenario c = new Cenario(30, 26, CONCRETO, CONCRETO);
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 26; y++) {
                if ((x / 6 + y / 6) % 2 == 0) c.pintar(x, y, PINTADO);
            }
        }

        c.fundo(VisualBloco.PREDIO, 0, 26, 15, 2, 3.5f, "SEM COLETE", 0);
        c.fundo(VisualBloco.PREDIO, 15, 26, 15, 2, 3.5f, "ROBÔ NÃO ENTRA", 3);
        c.fundo(VisualBloco.PREDIO, 30, 0, 2, 26, 3.5f, "BOWL DO ZÉ", 1);

        // Funbox central com corrimão no topo
        c.rampa(10, 10, 2.6f, 4, MAIS_X, 1.1f);
        c.plataforma(12.6f, 10, 3, 4, 1.1f);
        c.rampa(15.6f, 10, 2.6f, 4, MENOS_X, 1.1f);
        c.corrimao(12.6f, 12, 1.75f, 15.6f, 12, 1.75f);

        // Segunda funbox, no eixo Y
        c.rampa(20, 15.5f, 4, 2.6f, MAIS_Y, 1.1f);
        c.plataforma(20, 18.1f, 4, 2, 1.1f);
        c.rampa(20, 20.1f, 4, 2.6f, MENOS_Y, 1.1f);

        // Transferência: duas rampas frente a frente
        c.rampa(2, 3, 3, 3, MAIS_X, 1.25f);
        c.rampa(10, 3, 3, 3, MENOS_X, 1.25f);

        // Escada grande com corrimãos
        c.escada(22, 4, 4, 2.6f, MAIS_Y, 1.5f, true);
        c.plataforma(22, 6.6f, 4, 3, 1.5f);
        c.rampa(26, 6.6f, 3.6f, 3, MENOS_X, 1.5f);

        // Ledges, banco e corrimãos planos
        c.mureta(2, 11, 6, 0.8f, 0.6f);
        c.mureta(2, 15, 6, 0.8f, 0.8f);
        c.mureta(27, 13, 0.8f, 8, 0.65f);
        c.banco(12, 18.5f, 4, 0.7f, 0.55f);
        c.banco(12, 22.2f, 4, 0.7f, 0.55f);
        c.corrimao(3, 20, 0.85f, 9.5f, 20, 0.85f);
        c.corrimao(15, 7.8f, 0.85f, 21, 7.8f, 0.85f);
        c.corrimao(17, 16, 0.85f, 17, 22, 0.85f);

        c.enfeite(TipoEnfeite.PICHACAO, 5, 8, 1);
        c.enfeite(TipoEnfeite.PICHACAO, 18, 3, 2);
        c.enfeite(TipoEnfeite.PICHACAO, 6, 23, 0);

        Fase f = new Fase(3, "BOWL DO ZÉ", "AQUI ATÉ O ROBÔ USA BONÉ", Fase.Tema.SKATEPARK, c,
                90f, 30000, 1.05f, 4.2f, 7f, 8f);
        f.robos(TipoRobo.ROLIMA, 2).robos(TipoRobo.FAISCA, 3).robos(TipoRobo.TANQUE, 1);
        f.entradas(14, 24, 25, 12, 8, 24, 19, 24, 6, 18, 18, 3);
        f.item(TipoItem.ADESIVO, 14, 12);
        f.item(TipoItem.CAPACETE, 22, 19);
        f.item(TipoItem.TENIS_MOLA, 24, 8);
        f.item(TipoItem.ADESIVO, 7.5f, 4.5f);
        f.item(TipoItem.CURATIVO, 19.5f, 12);
        f.item(TipoItem.CURATIVO, 6, 13);
        return f;
    }

    // -------------------------------------------------------------------------- 4: subúrbio

    private static Fase suburbio() {
        Cenario c = new Cenario(34, 28, GRAMA, GRAMA);
        c.preencher(14, 0, 7, 28, ASFALTO);
        c.preencher(0, 10, 34, 6, ASFALTO);
        for (int y = 0; y < 28; y += 3) {
            if (y < 10 || y > 15) c.pintar(17, y, FAIXA);
        }
        for (int x = 0; x < 34; x += 3) {
            if (x < 14 || x > 20) c.pintar(x, 13, FAIXA);
        }
        c.preencher(12, 0, 2, 10, CALCADA);
        c.preencher(21, 0, 2, 10, CALCADA);
        c.preencher(12, 16, 2, 12, CALCADA);
        c.preencher(21, 16, 2, 12, CALCADA);
        c.preencher(0, 8, 12, 2, CALCADA);
        c.preencher(23, 8, 11, 2, CALCADA);
        c.preencher(0, 16, 12, 2, CALCADA);
        c.preencher(23, 16, 11, 2, CALCADA);

        c.fundo(VisualBloco.CASA, 0, 28, 10, 3, 3.2f, "VÓ LURDES", 0);
        c.fundo(VisualBloco.CASA, 11, 28, 10, 3, 3.6f, "CONDOMÍNIO", 1);
        c.fundo(VisualBloco.CASA, 22, 28, 12, 3, 3.2f, "SEM BOLA", 2);
        c.fundo(VisualBloco.CASA, 34, 18, 3, 13, 3.2f, "ALUGA-SE", 3);
        c.fundo(VisualBloco.CASA, 34, 2, 3, 15, 3.6f, "SÍNDICO", 1);

        // Casa com varanda: escada, plataforma e rampa de garagem
        c.bloco(VisualBloco.CASA, 3, 23.5f, 7, 4, 3f, 2);
        c.escada(5, 19.5f, 3, 1.5f, MAIS_Y, 0.75f, true);
        c.plataforma(3, 21, 7, 2.5f, 0.75f);
        c.rampa(10, 21, 2, 2.5f, MENOS_X, 0.75f);

        c.bloco(VisualBloco.CASA, 26, 20.5f, 6.5f, 4.5f, 3f, 0);
        c.escada(28, 18.2f, 3, 1.3f, MAIS_Y, 0.75f, true);
        c.plataforma(25, 19.5f, 8, 1, 0.75f);

        // Rampas de garagem nas vias
        c.rampa(24.5f, 4, 3, 2.5f, MAIS_X, 1.1f);
        c.rampa(7, 2.5f, 3, 2.5f, MENOS_X, 1.1f);
        c.rampa(15.5f, 19, 2.2f, 2.8f, MAIS_Y, 1.0f);
        c.rampa(2, 11.5f, 2.8f, 2.2f, MAIS_X, 1.0f);
        c.rampa(29, 11.5f, 2.8f, 2.2f, MENOS_X, 1.0f);

        // Muretas dos jardins e corrimãos
        c.mureta(1, 7.2f, 10, 0.6f, 0.7f);
        c.mureta(24, 7.2f, 9, 0.6f, 0.7f);
        c.mureta(11.3f, 18, 0.6f, 5, 0.65f);
        c.banco(13, 3, 0.7f, 3, 0.55f);
        c.banco(21.5f, 22, 0.7f, 3, 0.55f);
        c.corrimao(15, 25.5f, 0.85f, 20, 25.5f, 0.85f);
        c.corrimao(21.6f, 11, 0.85f, 28, 11, 0.85f);

        // Cercas (obstáculos), carros e bobagens de jardim
        c.bloco(VisualBloco.CERCA, 1, 1, 5.5f, 0.2f, 0.7f, 0);
        c.bloco(VisualBloco.CERCA, 28, 1, 5, 0.2f, 0.7f, 0);
        c.bloco(VisualBloco.CARRO, 5.5f, 14, 2.6f, 1.2f, 0.9f, 2);
        c.bloco(VisualBloco.CARRO, 18.5f, 5.5f, 1.2f, 2.6f, 0.9f, 3);
        c.bloco(VisualBloco.ANAO_JARDIM, 2, 18.7f, 0.35f, 0.35f, 0.65f, 0);
        c.bloco(VisualBloco.ANAO_JARDIM, 24, 18.6f, 0.35f, 0.35f, 0.65f, 1);
        c.bloco(VisualBloco.ANAO_JARDIM, 30, 5.8f, 0.35f, 0.35f, 0.65f, 2);
        c.bloco(VisualBloco.CAIXA_CORREIO, 11, 21.2f, 0.3f, 0.3f, 0.9f, 0);
        c.bloco(VisualBloco.CAIXA_CORREIO, 23.5f, 17, 0.3f, 0.3f, 0.9f, 1);
        c.bloco(VisualBloco.ARVORE, 3, 5, 0.6f, 0.6f, 2.8f, 0);
        c.bloco(VisualBloco.ARVORE, 32, 5, 0.6f, 0.6f, 2.8f, 1);
        c.bloco(VisualBloco.ARBUSTO, 30, 2.5f, 1.4f, 1.2f, 0.7f, 0);
        c.bloco(VisualBloco.ARBUSTO, 1, 18.6f, 0.8f, 1.2f, 0.7f, 1);

        for (int i = 0; i < 14; i++) {
            c.enfeite(TipoEnfeite.FLORES, 1 + (i * 3.1f) % 11, 1 + (i * 4.3f) % 6, i % 3);
        }
        c.enfeite(TipoEnfeite.BUEIRO, 16, 12, 0);
        c.enfeite(TipoEnfeite.RACHADURA, 9, 12, 0);

        Fase f = new Fase(4, "VILA DO SÍNDICO", "PROIBIDO SKATE. E ROBÔ TAMBÉM.", Fase.Tema.SUBURBIO, c,
                95f, 40000, 1.12f, 4.4f, 17f, 2.5f);
        f.robos(TipoRobo.FAISCA, 3).robos(TipoRobo.TANQUE, 2).robos(TipoRobo.SINDICO, 1);
        f.entradas(17, 23, 26, 13, 8, 12, 17, 13, 31, 14.8f, 10, 15);
        f.item(TipoItem.BOLO, 6.5f, 22);
        f.item(TipoItem.CAPACETE, 28, 3.5f);
        f.item(TipoItem.RELOGIO, 18, 22);
        f.item(TipoItem.BOLO, 22, 13);
        f.item(TipoItem.CAPACETE, 5, 9);
        f.item(TipoItem.CURATIVO, 19, 17);
        f.item(TipoItem.CURATIVO, 30, 9);
        return f;
    }
}
