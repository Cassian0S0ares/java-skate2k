package com.skate2k.logica;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class CharlietronTest {
    private static final float DT = 1f / 120f;
    private MundoJogo duelo() {
        Fase f = new Fase(4, "DUELO", "", Fase.Tema.SUBURBIO,
                new Cenario(40, 40, TipoPiso.CONCRETO, TipoPiso.CONCRETO), 150, 1200, 1, 4, 20, 20);
        f.robos(TipoRobo.CHARLIETRON, 1).entradas(20, 24);
        return new MundoJogo(f, Restricao.SEM_PULO, new Pontuacao(), 42);
    }
    @Test void chefeSoApareceNaUltimaFaseEPuloFicaLivre() {
        for (int i = 0; i < CatalogoFases.TOTAL; i++) {
            MundoJogo m = new MundoJogo(CatalogoFases.criar(i), Restricao.SEM_PULO, new Pontuacao(), 42);
            assertEquals(i == CatalogoFases.TOTAL - 1, m.batalhaFinal());
            if (m.batalhaFinal()) {
                assertTrue(m.restricao().permitePuloLivre());
                assertFalse(m.restricao().exigeCameraEmMovimento());
            }
        }
        for (int i = 0; i < 100; i++) assertNotEquals(Restricao.DUELO, Restricao.sortear(new Random(i)));
    }
    @Test void vitoriaExigeVidaZeradaEEstilo() {
        MundoJogo m = duelo();
        m.pontuacao().adicionarBonusFase(99999);
        assertFalse(m.metaBatida());
        m.aoPousar(m.jogador(), List.of(Manobra.de(TipoManobra.GIRO_720, 2)), true);
        assertTrue(m.estiloJogador() >= m.meta());
        assertFalse(m.metaBatida());
        m.chefeFinal().receberPisao(12);
        assertTrue(m.metaBatida());
        assertTrue(m.terminou());
    }
    @Test void chefeDerrotadoNaoRenasceNemPontua() {
        MundoJogo m = duelo(); RoboSkatista chefe = m.chefeFinal();
        chefe.receberPisao(12);
        for (int i = 0; i < 1200; i++) m.atualizar(DT, new Comandos());
        assertEquals(1, m.robos().size());
        assertSame(chefe, m.robos().get(0));
        assertEquals(0, chefe.vida());
        assertEquals(0, chefe.chefe().pontos());
        assertFalse(chefe.perigoso());
        assertFalse(m.metaBatida());
    }
    @Test void manobraSoPontuaAoPousarEInterrupcaoCancelaPontos() {
        MundoJogo m = duelo(); RoboSkatista r = m.chefeFinal();
        List<EventoJogo> eventos = new ArrayList<>(); Random rng = new Random(1);
        for (int i = 0; i < 800 && r.chefe().estado() != Charlietron.Estado.MANOBRA; i++)
            r.atualizar(DT, m.jogador(), m.fase().cenario, 1, 4, rng, eventos);
        assertEquals(Charlietron.Estado.MANOBRA, r.chefe().estado());
        assertEquals(0, r.chefe().pontos());
        r.receberPisao(1);
        for (int i = 0; i < 120; i++) r.atualizar(DT, m.jogador(), m.fase().cenario, 1, 4, rng, eventos);
        assertEquals(0, r.chefe().pontos());
        for (int i = 0; i < 2400 && r.chefe().pontos() == 0; i++)
            r.atualizar(DT, m.jogador(), m.fase().cenario, 1, 4, rng, eventos);
        assertTrue(r.chefe().pontos() > 0);
    }
    @Test void pisaoAvisadoAtingeCabecaUmaVezEPermiteContraAtaque() {
        MundoJogo m = duelo(); int vida = m.jogador().vida();
        boolean aviso = false, voo = false;
        for (int i = 0; i < 2400 && m.jogador().vida() == vida; i++) {
            m.atualizar(DT, new Comandos());
            aviso |= m.chefeFinal().chefe().estado() == Charlietron.Estado.PREPARO_PISAO;
            voo |= m.chefeFinal().z() > 1.5f;
        }
        assertTrue(aviso); assertTrue(voo);
        assertEquals(vida - 2, m.jogador().vida());
        assertEquals(Jogador.Estado.CAIDO, m.jogador().estado());
        assertTrue(m.chefeFinal().chefe().vulneravel());
        for (int i = 0; i < 30; i++) m.atualizar(DT, new Comandos());
        assertEquals(vida - 2, m.jogador().vida());
    }
    @Test void alvoDoPisaoNaoPersegueJogadorDepoisDoAviso() {
        MundoJogo m = duelo(); RoboSkatista r = m.chefeFinal();
        List<EventoJogo> eventos = new ArrayList<>(); Random rng = new Random(1);
        for (int i = 0; i < 2000 && r.chefe().estado() != Charlietron.Estado.PREPARO_PISAO; i++)
            r.atualizar(DT, m.jogador(), m.fase().cenario, 1, 4, rng, eventos);
        assertEquals(Charlietron.Estado.PREPARO_PISAO, r.chefe().estado());
        float x = r.chefe().alvoX(), y = r.chefe().alvoY();
        m.jogador().x += 6;
        for (int i = 0; i < 180; i++) r.atualizar(DT, m.jogador(), m.fase().cenario, 1, 4, rng, eventos);
        assertEquals(x, r.chefe().alvoX()); assertEquals(y, r.chefe().alvoY());
    }

    @Test void pisaoNaCabecaPerdeComboSemCapacete() {
        verificarComboAposPisao(false);
    }

    @Test void pisaoNaCabecaPreservaComboComCapacete() {
        verificarComboAposPisao(true);
    }

    private void verificarComboAposPisao(boolean capacete) {
        MundoJogo m = duelo();
        m.pontuacao().registrarManobras(List.of(
                Manobra.de(TipoManobra.GIRO_180, 1),
                Manobra.de(TipoManobra.GIRO_360, 1)), true);
        int carga = m.pontuacao().carga();
        int multiplicador = m.pontuacao().multiplicador();
        int vida = m.jogador().vida();
        assertTrue(carga > 0);
        assertTrue(multiplicador > 1);
        if (capacete) m.jogador().colocarCapacete();
        boolean aviso = false;
        for (int i = 0; i < 2400; i++) {
            m.atualizar(DT, new Comandos());
            aviso |= m.chefeFinal().chefe().estado() == Charlietron.Estado.PREPARO_PISAO;
            if (capacete ? !m.jogador().temCapacete() : m.jogador().vida() < vida) break;
        }
        assertTrue(aviso);
        assertEquals(capacete ? vida : vida - 2, m.jogador().vida());
        assertEquals(capacete ? carga : 0, m.pontuacao().carga());
        assertEquals(capacete ? multiplicador : 1, m.pontuacao().multiplicador());
        assertEquals(capacete ? 2 : 0, m.pontuacao().manobrasConsecutivas());
        assertFalse(m.jogador().temCapacete());
        if (capacete) assertNotEquals(Jogador.Estado.CAIDO, m.jogador().estado());
        else assertEquals(Jogador.Estado.CAIDO, m.jogador().estado());
    }

    @Test void chefeFicaFuriosoComAVidaBaixaEPontuaMais() {
        MundoJogo m = duelo(); RoboSkatista r = m.chefeFinal();
        assertEquals(0, r.chefe().furia());
        r.receberPisao(4);
        m.atualizar(DT, new Comandos());
        assertEquals(1, r.chefe().furia());
        r.receberPisao(4);
        m.atualizar(DT, new Comandos());
        assertEquals(2, r.chefe().furia());
        assertTrue(r.vida() > 0);
    }
    @Test void jogoComecaSemRegraESorteioNuncaVoltaParaEla() {
        assertEquals(Restricao.SEM_REGRA, new com.skate2k.SkateGame().restricao);
        Restricao atual = Restricao.SEM_REGRA; Random rng = new Random(3);
        for (int i = 0; i < 50; i++) {
            Restricao nova = Restricao.sortearOutra(atual, rng);
            assertNotEquals(Restricao.SEM_REGRA, nova); assertNotEquals(Restricao.DUELO, nova);
            if (atual != Restricao.SEM_REGRA) assertNotEquals(atual, nova);
            atual = nova;
        }
    }
}
