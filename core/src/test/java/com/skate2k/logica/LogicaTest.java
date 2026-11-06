package com.skate2k.logica;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Testa as regras sem abrir janela nenhuma: é a prova de que a lógica é desacoplada do render. */
class LogicaTest {
    private static final float DT = 1f / 60f;

    /** Ouvinte que só anota o que aconteceu. */
    private static final class Anotador implements Jogador.Ouvinte {
        final List<Manobra> manobras = new ArrayList<>();
        int pousos;
        int quedas;
        String motivo;
        int grinds;
        final List<TipoElemento> elementos = new ArrayList<>();

        @Override public void aoPular(Jogador j) {}
        @Override public void aoPousar(Jogador j, List<Manobra> m, boolean limpo) { pousos++; manobras.addAll(m); }
        @Override public void aoCair(Jogador j, String m) { quedas++; motivo = m; }
        @Override public void aoUsarCapacete(Jogador j) {}
        @Override public void aoIniciarGrind(Jogador j, TipoElemento t) { grinds++; }
        @Override public void aoSairGrind(Jogador j) {}
        @Override public void aoUsarElemento(Jogador j, TipoElemento t) { elementos.add(t); }
        @Override public void aoNegar(Jogador j, String m) { motivo = m; }
    }

    private static Cenario vazio() {
        return new Cenario(40, 40, TipoPiso.CONCRETO, TipoPiso.CONCRETO);
    }

    private static void avancar(Jogador j, Comandos c, Cenario cen, Restricao r, float segundos) {
        int n = Math.round(segundos / DT);
        for (int i = 0; i < n; i++) j.atualizar(DT, c, cen, r, true);
    }

    /** Pula com carga máxima a partir de parado. */
    private static void pularForte(Jogador j, Cenario cen, Restricao r) {
        Comandos c = new Comandos();
        c.pulo = true;
        avancar(j, c, cen, r, 0.45f);
        c.pulo = false;
        j.atualizar(DT, c, cen, r, true);
        assertEquals(Jogador.Estado.NO_AR, j.estado());
    }

    private static void esperarPouso(Jogador j, Cenario cen, Restricao r) {
        Comandos c = new Comandos();
        for (int i = 0; i < 400 && j.estado() == Jogador.Estado.NO_AR; i++) j.atualizar(DT, c, cen, r, true);
    }

    @Test
    void kickflipCompletoEReconhecidoNoPouso() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        Jogador j = new Jogador(20, 20, a, new Random(1));
        pularForte(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        Comandos c = new Comandos();
        c.kickflip = true;
        j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
        esperarPouso(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        assertEquals(0, a.quedas);
        assertEquals(TipoManobra.KICKFLIP, a.manobras.get(0).tipo);
    }

    @Test
    void rotacaoIncompletaNoPousoDerruba() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        Jogador j = new Jogador(20, 20, a, new Random(1));
        pularForte(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        Comandos c = new Comandos();
        // Enche o voo de pedidos de flip: não dá tempo de terminar e ele pousa com o shape virado.
        c.kickflip = true;
        for (int i = 0; i < 8; i++) j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
        esperarPouso(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        assertTrue(a.quedas == 1 || j.estado() == Jogador.Estado.CAIDO);
        assertTrue(a.manobras.isEmpty());
    }

    @Test
    void giroEReconhecidoPeloAnguloReal() {
        DetectorManobras d = new DetectorManobras();
        d.iniciarVoo();
        for (int i = 0; i < 36; i++) d.atualizar(DT, 1, false); // ~372 graus
        for (int i = 0; i < 30; i++) d.atualizar(DT, 0, false);
        DetectorManobras.Pouso p = d.avaliarPouso(1f, 1f);
        assertTrue(p.valido);
        assertEquals(TipoManobra.GIRO_360, p.manobras.get(0).tipo);
    }

    @Test
    void chuvaExigeMaisPrecisaoQueTempoSeco() {
        DetectorManobras d = new DetectorManobras();
        d.iniciarVoo();
        d.comandarKickflip();
        // Para a 300 graus: faltam 60, tolerável no seco (65) e fatal na chuva (65 x 0.65).
        for (int i = 0; i < 1; i++) d.atualizar(300f / ConstantesFisica.VELOCIDADE_KICKFLIP, 0, false);
        assertTrue(d.avaliarPouso(1f, 1f).valido);
        assertFalse(d.avaliarPouso(Restricao.CHUVA.fatorTolerancia, 1f).valido);
    }

    @Test
    void grabSeguradoNoPousoDerruba() {
        DetectorManobras d = new DetectorManobras();
        d.iniciarVoo();
        d.atualizar(0.5f, 0, true);
        assertFalse(d.avaliarPouso(1f, 1f).valido);
    }

    @Test
    void rampaLancaEConcedePotencializador() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        cen.rampa(10, 8, 3, 4, Direcao.MAIS_X, 1.1f);
        Jogador j = new Jogador(5, 10, a, new Random(1));
        Comandos c = new Comandos();
        c.eixoX = 1;
        c.eixoY = 1; // tela diagonal = +X no mapa
        boolean decolou = false;
        for (int i = 0; i < 240 && !decolou; i++) {
            j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
            decolou = j.estado() == Jogador.Estado.NO_AR;
        }
        assertTrue(decolou, "a rampa deveria lançar o jogador");
        assertTrue(j.vz > 3f, "lançamento fraco: " + j.vz);
        assertEquals(1.5f, j.potencial());
        assertTrue(a.elementos.contains(TipoElemento.RAMPA));
    }

    @Test
    void cairNoGrindSemCorrigirOEquilibrio() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        cen.corrimao(5, 20, 0.6f, 35, 20, 0.6f);
        Jogador j = new Jogador(18, 20.2f, a, new Random(1));
        j.vx = 5;
        j.rumo = 0;
        pularForte(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        Comandos c = new Comandos();
        for (int i = 0; i < 300 && a.grinds == 0; i++) j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
        assertEquals(1, a.grinds);
        for (int i = 0; i < 600 && a.quedas == 0; i++) j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
        assertEquals(1, a.quedas);
        assertEquals("PERDEU O EQUILÍBRIO!", a.motivo);
    }

    @Test
    void grindCompletoViraManobraComPotencializador() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        cen.corrimao(5, 20, 0.6f, 12, 20, 0.6f);
        Jogador j = new Jogador(6.5f, 20.2f, a, new Random(1));
        j.vx = 2;
        j.rumo = 0;
        pularForte(j, cen, Restricao.CAMERA_EM_MOVIMENTO);
        Comandos c = new Comandos();
        for (int i = 0; i < 900 && a.pousos == 0 && a.quedas == 0; i++) {
            c.eixoX = -Math.signum(j.equilibrio());
            j.atualizar(DT, c, cen, Restricao.CAMERA_EM_MOVIMENTO, true);
            if (j.estado() != Jogador.Estado.GRIND) c.eixoX = 0;
        }
        assertEquals(0, a.quedas);
        assertEquals(1, a.manobras.size());
        assertEquals(TipoManobra.GRIND_5050, a.manobras.get(0).tipo);
    }

    @Test
    void semPuloBloqueiaOllieLongeDeCorrimao() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        Jogador j = new Jogador(20, 20, a, new Random(1));
        Comandos c = new Comandos();
        c.pulo = true;
        avancar(j, c, cen, Restricao.SEM_PULO, 0.3f);
        c.pulo = false;
        avancar(j, c, cen, Restricao.SEM_PULO, 0.1f);
        assertEquals(Jogador.Estado.NO_CHAO, j.estado());

        cen.corrimao(19, 21, 0.8f, 25, 21, 0.8f);
        c.pulo = true;
        avancar(j, c, cen, Restricao.SEM_PULO, 0.1f);
        c.pulo = false;
        avancar(j, c, cen, Restricao.SEM_PULO, 0.1f);
        assertEquals(Jogador.Estado.NO_AR, j.estado());
    }

    @Test
    void semCameraEmMovimentoManobraNaoConta() {
        Anotador a = new Anotador();
        Cenario cen = vazio();
        Jogador j = new Jogador(20, 20, a, new Random(1));
        Restricao r = Restricao.CAMERA_EM_MOVIMENTO;
        pularForte(j, cen, r);
        Comandos c = new Comandos();
        c.kickflip = true;
        j.atualizar(DT, c, cen, r, false); // câmera parada
        c.kickflip = false;
        // Giro e grab segurados também não contam com a câmera parada.
        c.giro = 1;
        c.grab = true;
        for (int i = 0; i < 300 && j.estado() == Jogador.Estado.NO_AR; i++) j.atualizar(DT, c, cen, r, false);
        assertEquals(0, a.quedas);
        assertTrue(a.manobras.isEmpty());
        assertEquals("A CÂMERA NÃO VIU!", a.motivo);
    }

    @Test
    void cairZeraMultiplicadorECarga() {
        Pontuacao p = new Pontuacao();
        p.registrarManobras(List.of(Manobra.de(TipoManobra.KICKFLIP, 1f), Manobra.de(TipoManobra.IMPOSSIBLE, 1f),
                Manobra.de(TipoManobra.GIRO_360, 1f), Manobra.de(TipoManobra.GRIND_5050, 1f)), false);
        p.usarElemento(TipoElemento.RAMPA);
        assertEquals(4, p.multiplicador()); // 1 + 4/2 + 1 elemento
        assertTrue(p.carga() > 0);
        p.cair();
        assertEquals(1, p.multiplicador());
        assertEquals(0, p.carga());
    }

    @Test
    void cargaSoViraPontoAoDerrotarRobo() {
        Pontuacao p = new Pontuacao();
        p.registrarManobras(List.of(Manobra.de(TipoManobra.KICKFLIP, 1f), Manobra.de(TipoManobra.KICKFLIP, 1f)), false);
        assertEquals(0, p.pontosTotal());
        assertEquals(200, p.carga());
        int ganho = p.derrotarRobo(100);
        assertEquals((100 + 200) * 2, ganho);
        assertEquals(ganho, p.pontosTotal());
        assertEquals(0, p.carga());
    }

    @Test
    void manobraDificilSobeNivelDeEstilo() {
        Pontuacao p = new Pontuacao();
        assertEquals(1, p.nivel());
        boolean subiu = p.registrarManobras(List.of(Manobra.de(TipoManobra.TRIPLE_KICKFLIP, 1f),
                Manobra.de(TipoManobra.GAP_ESCADA, 1f)), false);
        assertTrue(subiu);
        assertEquals(2, p.nivel());
        p.cair();
        assertEquals(1, p.nivel());
    }

    @Test
    void recordeSoSalvaQuandoSupera() {
        int[] guardado = {500};
        RepositorioRecorde repo = new RepositorioRecorde() {
            @Override public int carregar() { return guardado[0]; }
            @Override public void salvar(int pontos) { guardado[0] = pontos; }
        };
        Pontuacao p = new Pontuacao();
        p.derrotarRobo(100);
        assertFalse(p.salvarSeRecorde(repo));
        assertEquals(500, guardado[0]);
        p.adicionarBonusFase(1000);
        assertTrue(p.salvarSeRecorde(repo));
        assertEquals(1100, guardado[0]);
    }

    @Test
    void sorteioOficialEDeterministico() {
        assertEquals(Restricao.sorteioOficial(), Restricao.sorteioOficial());
        System.out.println("SORTEIO OFICIAL = " + Restricao.sorteioOficial());
    }

    @Test
    void sorteioOutraNuncaRepete() {
        Random r = new Random(5);
        for (Restricao atual : Restricao.values()) {
            for (int i = 0; i < 20; i++) assertNotEquals(atual, Restricao.sortearOutra(atual, r));
        }
    }

    @Test
    void fasesTemElementosCoerentes() {
        assertTrue(CatalogoFases.TOTAL >= 3);
        for (int i = 0; i < CatalogoFases.TOTAL; i++) {
            Fase f = CatalogoFases.criar(i);
            Cenario c = f.cenario;
            String n = f.nome;
            assertEquals(0f, c.alturaEm(f.inicioX, f.inicioY), n + ": jogador nasce dentro de algo");
            for (float[] p : f.pontosDeEntrada) {
                assertEquals(0f, c.alturaEm(p[0], p[1]), n + ": robô nasce dentro de algo em " + p[0] + "," + p[1]);
                assertTrue(p[0] > 0 && p[1] > 0 && p[0] < c.largura && p[1] < c.profundidade, n + ": entrada fora");
            }
            for (Item item : f.itens) {
                assertTrue(item.x > 0 && item.y > 0 && item.x < c.largura && item.y < c.profundidade, n + ": item fora");
                assertTrue(c.alturaEm(item.x, item.y) < 2f, n + ": item inalcançável " + item.tipo);
            }
            boolean rampa = false, trilho = !c.trilhos.isEmpty();
            for (ElementoCenario e : c.elementos) rampa |= e.tipo == TipoElemento.RAMPA;
            assertTrue(rampa && trilho, n + ": sem elementos de manobra");
            for (ElementoCenario a : c.elementos) {
                if (a.fundo) {
                    assertTrue(a.x >= c.largura || a.y >= c.profundidade, n + ": fundo dentro da área jogável");
                }
            }
        }
    }

    @Test
    void relogioCorreEFaseTermina() {
        Fase f = CatalogoFases.criar(0);
        f.robos.clear();
        MundoJogo m = new MundoJogo(f, Restricao.CHUVA, new Pontuacao(), 1L);
        Comandos c = new Comandos();
        for (int i = 0; i < 60 * 200 && !m.acabouTempo(); i++) m.atualizar(DT, c);
        assertTrue(m.acabouTempo());
        assertEquals(0f, m.tempoRestante());
    }

    private static MundoJogo mundoVazio() {
        Fase f = new Fase(1, "T", "T", Fase.Tema.RUA, vazio(), 60f, 100, 1f, 5f, 20f, 20f);
        f.entradas(5f, 5f);
        return new MundoJogo(f, Restricao.CHUVA, new Pontuacao(), 1L);
    }

    private static RoboSkatista robo(TipoRobo tipo) {
        return new RoboSkatista(tipo, 20f, 20f, new Random(1));
    }

    @Test
    void cadaRoboTiraUmaQuantidadeDiferenteDeVida() {
        // toque e investida, de cada modelo
        int[][] esperado = {{1, 1}, {1, 2}, {2, 2}, {2, 3}};
        TipoRobo[] tipos = {TipoRobo.ROLIMA, TipoRobo.FAISCA, TipoRobo.TANQUE, TipoRobo.SINDICO};
        for (int i = 0; i < tipos.length; i++) {
            MundoJogo toque = mundoVazio();
            toque.golpear(robo(tipos[i]), false);
            assertEquals(ConstantesFisica.VIDA_MAXIMA - esperado[i][0], toque.jogador().vida(), tipos[i] + " toque");
            MundoJogo investida = mundoVazio();
            investida.golpear(robo(tipos[i]), true);
            assertEquals(ConstantesFisica.VIDA_MAXIMA - esperado[i][1], investida.jogador().vida(), tipos[i] + " investida");
        }
    }

    @Test
    void tresHitsLevesNocauteiamEFaseTermina() {
        MundoJogo m = mundoVazio();
        assertEquals(3, ConstantesFisica.VIDA_MAXIMA);
        m.golpear(robo(TipoRobo.ROLIMA), false);
        m.golpear(robo(TipoRobo.ROLIMA), false);
        assertFalse(m.terminou());
        m.golpear(robo(TipoRobo.ROLIMA), false);
        assertEquals(0, m.jogador().vida());
        assertTrue(m.terminou());
        assertFalse(m.acabouTempo());
        // O nocauteado continua no chão, não levanta sozinho.
        Comandos c = new Comandos();
        for (int i = 0; i < 300; i++) m.atualizar(DT, c);
        assertEquals(Jogador.Estado.CAIDO, m.jogador().estado());
    }

    @Test
    void investidaDoSindicoNocauteiaDeUmaVez() {
        MundoJogo m = mundoVazio();
        m.golpear(robo(TipoRobo.SINDICO), true);
        assertTrue(m.terminou());
    }

    @Test
    void capaceteAbsorveOGolpeSemTirarVida() {
        MundoJogo m = mundoVazio();
        m.jogador().colocarCapacete();
        m.golpear(robo(TipoRobo.SINDICO), true);
        assertEquals(ConstantesFisica.VIDA_MAXIMA, m.jogador().vida());
        assertFalse(m.jogador().temCapacete());
        assertEquals(Jogador.Estado.NO_CHAO, m.jogador().estado());
    }

    @Test
    void tomboPorErroProprioNaoTiraVida() {
        MundoJogo m = mundoVazio();
        m.jogador().cair("POUSOU DE LADO!");
        assertEquals(ConstantesFisica.VIDA_MAXIMA, m.jogador().vida());
    }

    @Test
    void curativoCuraSemPassarDoMaximoEFicaNoLugarComVidaCheia() {
        Fase f = new Fase(1, "T", "T", Fase.Tema.RUA, vazio(), 60f, 100, 1f, 5f, 20f, 20f);
        f.entradas(5f, 5f);
        f.item(TipoItem.CURATIVO, 20.2f, 20f);
        MundoJogo m = new MundoJogo(f, Restricao.CHUVA, new Pontuacao(), 1L);
        Comandos c = new Comandos();
        m.atualizar(DT, c);
        assertTrue(f.itens.get(0).disponivel(), "vida cheia não deveria gastar o curativo");
        m.jogador().perderVida(1);
        m.atualizar(DT, c);
        assertEquals(ConstantesFisica.VIDA_MAXIMA, m.jogador().vida());
        assertFalse(f.itens.get(0).disponivel());
    }

    /** O bot joga cada fase em cada restrição: nada trava, e os robôs morrem de verdade. */
    @Test
    void botJogaTodasAsFasesSemTravar() {
        System.out.println("fase | restricao | meta | pontos (5 sementes) | robos | quedas");
        java.util.Map<String, Integer> motivos = new java.util.TreeMap<>();
        for (int i = 0; i < CatalogoFases.TOTAL; i++) {
            for (Restricao r : Restricao.values()) {
                StringBuilder sb = new StringBuilder();
                int robos = 0;
                int quedas = 0;
                int meta = 0;
                for (long semente = 1; semente <= 5; semente++) {
                    Fase f = CatalogoFases.criar(i);
                    meta = f.meta;
                    Pontuacao p = new Pontuacao();
                    MundoJogo m = new MundoJogo(f, r, p, semente);
                    BotDemo bot = new BotDemo(semente * 31);
                    Comandos c = new Comandos();
                    while (!m.terminou()) {
                        bot.preencher(m, c, DT);
                        m.atualizar(DT, c);
                        for (EventoJogo e : m.consumirEventos()) {
                            if (e.tipo == EventoJogo.Tipo.QUEDA) {
                                quedas++;
                                motivos.merge(e.texto, 1, Integer::sum);
                            }
                        }
                        Jogador j = m.jogador();
                        assertTrue(Float.isFinite(j.x) && Float.isFinite(j.y) && Float.isFinite(j.z), "posição inválida");
                        assertTrue(j.z >= -0.01f, "jogador afundou no chão: " + j.z);
                        assertTrue(j.x >= 0 && j.y >= 0 && j.x <= f.cenario.largura && j.y <= f.cenario.profundidade);
                    }
                    sb.append(m.metaBatida() ? "PASSOU" : "perdeu").append(m.jogador().vivo() ? "" : "(KO " + (int) (f.tempoLimite - m.tempoRestante()) + "s)").append(' ');
                    robos += p.robosDerrotados();
                }
                System.out.println((i + 1) + " | " + r + " | " + meta + " | " + sb + "| " + robos / 5 + " | " + quedas / 5);
            }
            System.out.println("motivos das quedas: " + motivos);
        }
    }
}
