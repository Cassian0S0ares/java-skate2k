package com.skate2k.logica;

import java.util.List;
import java.util.Random;

/**
 * Um jogador automático simples. Anima o fundo do menu e serve para testar se as fases são
 * jogáveis e calibrar as metas. Gera os mesmos Comandos que o teclado geraria.
 */
public final class BotDemo {
    private final Random aleatorio;
    private float esperaPulo = 1f;
    private float tempoSegurandoPulo;
    private boolean segurandoPulo;
    private float tempoPreso;
    private float tempoFuga;
    private float fugaX;
    private float fugaY;
    private float tempoGiro;
    private int sentidoGiro;
    private int truquesNoVoo;
    private boolean vooAnterior;

    public BotDemo(long semente) {
        this.aleatorio = new Random(semente);
    }

    public void preencher(MundoJogo mundo, Comandos c, float dt) {
        c.limpar();
        Jogador j = mundo.jogador();
        Cenario cen = mundo.fase().cenario;

        RoboSkatista alvo = roboMaisProximo(mundo.robos(), j);
        boolean semPulo = !mundo.restricao().permitePuloLivre();

        if (j.estado() == Jogador.Estado.GRIND) {
            c.eixoX = j.equilibrio() > 0 ? -1f : 1f;
            // Sem pulo livre, o grind é a única decolagem: salta quando há robô por perto.
            if (semPulo && alvo != null && Math.hypot(alvo.x - j.x, alvo.y - j.y) < 3.5f) c.pulo = true;
            return;
        }

        float wx;
        float wy;
        float distAlvo = 99f;
        boolean mirarTrilho = semPulo && j.estado() == Jogador.Estado.NO_CHAO;
        TrilhoGrind trilho = mirarTrilho ? cen.trilhoMaisProximo(j.x, j.y) : null;
        if (tempoFuga > 0) {
            tempoFuga -= dt;
            wx = fugaX;
            wy = fugaY;
        } else if (trilho != null) {
            float t = trilho.projetar(j.x, j.y);
            wx = trilho.xEm(t) - j.x;
            wy = trilho.yEm(t) - j.y;
            // Com a Carga baixa vai para o trilho; ao chegar perto, salta sobre ele.
            distAlvo = Math.hypot(wx, wy) < 2.2f ? 3f : 99f;
        } else if (alvo != null) {
            wx = alvo.x - j.x;
            wy = alvo.y - j.y;
            distAlvo = (float) Math.hypot(wx, wy);
        } else {
            wx = cen.largura / 2f - j.x;
            wy = cen.profundidade / 2f - j.y;
        }
        definirEixos(c, wx, wy);

        float vel = (float) Math.hypot(j.vx, j.vy);
        if (j.estado() == Jogador.Estado.NO_CHAO) {
            tempoPreso = vel < 1f ? tempoPreso + dt : 0;
            if (tempoPreso > 0.6f) {
                tempoPreso = 0;
                tempoFuga = 0.9f;
                float a = aleatorio.nextFloat() * 6.28f;
                fugaX = (float) Math.cos(a);
                fugaY = (float) Math.sin(a);
            }
            decidirPulo(c, dt, vel, distAlvo);
        } else {
            segurandoPulo = false;
        }

        if (j.noAr()) {
            decidirTruques(j, cen, c, dt, mundo.restricao());
        } else {
            vooAnterior = false;
        }
    }

    private void decidirPulo(Comandos c, float dt, float vel, float distAlvo) {
        esperaPulo -= dt;
        if (segurandoPulo) {
            tempoSegurandoPulo -= dt;
            if (tempoSegurandoPulo > 0) {
                c.pulo = true;
            } else {
                segurandoPulo = false;
                esperaPulo = 0.25f + aleatorio.nextFloat();
            }
            return;
        }
        boolean perto = distAlvo < 4.0f;
        if (perto || (vel > 3f && esperaPulo <= 0)) {
            segurandoPulo = true;
            tempoSegurandoPulo = perto ? 0.10f : 0.25f;
            c.pulo = true;
        }
    }

    private void decidirTruques(Jogador j, Cenario cen, Comandos c, float dt, Restricao r) {
        if (!vooAnterior) {
            vooAnterior = true;
            truquesNoVoo = 0;
            tempoGiro = 0;
        }
        if (!r.permiteManobraAerea()) return;
        float h = cen.alturaEm(j.x, j.y);
        float restante = (j.vz + (float) Math.sqrt(Math.max(0, j.vz * j.vz + 2 * 18f * (j.z - h)))) / 18f;
        boolean livre = !j.detector().temRotacaoEmAndamento();

        if (livre && restante > 0.52f && truquesNoVoo < 2 && aleatorio.nextFloat() < 0.2f) {
            if (restante > 0.68f && aleatorio.nextBoolean()) {
                c.impossible = true;
            } else {
                c.kickflip = true;
            }
            truquesNoVoo++;
        }
        if (tempoGiro <= 0 && restante > 0.75f && j.detector().giroGraus() == 0 && aleatorio.nextFloat() < 0.04f) {
            tempoGiro = 0.28f;
            sentidoGiro = aleatorio.nextBoolean() ? 1 : -1;
        }
        if (tempoGiro > 0) {
            tempoGiro -= dt;
            c.giro = sentidoGiro;
        }
    }

    private static void definirEixos(Comandos c, float wx, float wy) {
        // Inverso do mapeamento tela -> mapa usado em Jogador.
        float ex = wx - wy;
        float ey = wx + wy;
        float n = Math.max(Math.abs(ex), Math.abs(ey));
        if (n < 0.001f) return;
        c.eixoX = ex / n;
        c.eixoY = ey / n;
    }

    private static RoboSkatista roboMaisProximo(List<RoboSkatista> robos, Jogador j) {
        RoboSkatista melhor = null;
        float menor = Float.MAX_VALUE;
        for (RoboSkatista r : robos) {
            float d = (float) Math.hypot(r.x - j.x, r.y - j.y);
            if (d < menor) {
                menor = d;
                melhor = r;
            }
        }
        return melhor;
    }
}
