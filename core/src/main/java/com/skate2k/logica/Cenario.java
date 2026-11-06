package com.skate2k.logica;

import java.util.ArrayList;
import java.util.List;

/** Mapa de uma fase: piso, elementos com altura, trilhos de grind e enfeites sem colisão. */
public final class Cenario {

    /** Enfeite no chão, sem colisão. */
    public enum TipoEnfeite { BUEIRO, FLORES, POMBO, PICHACAO, TUFO, RACHADURA }

    public static final class Enfeite {
        public final TipoEnfeite tipo;
        public final float x;
        public final float y;
        public final int variacao;

        Enfeite(TipoEnfeite tipo, float x, float y, int variacao) {
            this.tipo = tipo;
            this.x = x;
            this.y = y;
            this.variacao = variacao;
        }
    }

    public final int largura;
    public final int profundidade;
    public final TipoPiso pisoExterno;
    public final List<ElementoCenario> elementos = new ArrayList<>();
    public final List<TrilhoGrind> trilhos = new ArrayList<>();
    public final List<Enfeite> enfeites = new ArrayList<>();
    private final TipoPiso[][] piso;

    public Cenario(int largura, int profundidade, TipoPiso pisoBase, TipoPiso pisoExterno) {
        this.largura = largura;
        this.profundidade = profundidade;
        this.pisoExterno = pisoExterno;
        this.piso = new TipoPiso[largura][profundidade];
        preencher(0, 0, largura, profundidade, pisoBase);
    }

    // ---------------------------------------------------------------------- consultas de física

    public float alturaEm(float px, float py) {
        float maior = 0f;
        for (int i = 0; i < elementos.size(); i++) {
            float h = elementos.get(i).alturaEm(px, py);
            if (h > maior) maior = h;
        }
        return maior;
    }

    /** O elemento sobre o qual uma pessoa estaria neste ponto, ou null se for o chão. */
    public ElementoCenario elementoEm(float px, float py) {
        ElementoCenario melhor = null;
        float maior = 0.01f;
        for (int i = 0; i < elementos.size(); i++) {
            ElementoCenario e = elementos.get(i);
            float h = e.alturaEm(px, py);
            if (h > maior) {
                maior = h;
                melhor = e;
            }
        }
        return melhor;
    }

    public TipoPiso pisoEm(float px, float py) {
        int ix = (int) Math.floor(px);
        int iy = (int) Math.floor(py);
        if (ix < 0 || iy < 0 || ix >= largura || iy >= profundidade) return pisoExterno;
        return piso[ix][iy];
    }

    public boolean temTrilhoPerto(float px, float py, float raio) {
        for (int i = 0; i < trilhos.size(); i++) {
            if (trilhos.get(i).distancia(px, py) <= raio) return true;
        }
        return false;
    }

    public TrilhoGrind trilhoMaisProximo(float px, float py) {
        TrilhoGrind melhor = null;
        float menor = Float.MAX_VALUE;
        for (int i = 0; i < trilhos.size(); i++) {
            float d = trilhos.get(i).distancia(px, py);
            if (d < menor) {
                menor = d;
                melhor = trilhos.get(i);
            }
        }
        return melhor;
    }

    // ------------------------------------------------------------------- construção das fases

    public final void preencher(int x, int y, int w, int d, TipoPiso tipo) {
        for (int ix = Math.max(0, x); ix < Math.min(largura, x + w); ix++) {
            for (int iy = Math.max(0, y); iy < Math.min(profundidade, y + d); iy++) {
                piso[ix][iy] = tipo;
            }
        }
    }

    public void pintar(int x, int y, TipoPiso tipo) {
        if (x >= 0 && y >= 0 && x < largura && y < profundidade) piso[x][y] = tipo;
    }

    public void enfeite(TipoEnfeite tipo, float x, float y, int variacao) {
        enfeites.add(new Enfeite(tipo, x, y, variacao));
    }

    public void rampa(float x, float y, float w, float d, Direcao sobe, float altura) {
        add(TipoElemento.RAMPA, x, y, w, d, altura, sobe, VisualBloco.NENHUM, null, 0, false);
    }

    public void plataforma(float x, float y, float w, float d, float altura) {
        add(TipoElemento.PLATAFORMA, x, y, w, d, altura, Direcao.MAIS_X, VisualBloco.NENHUM, null, 0, false);
    }

    /** Escada que sobe na direção indicada, com corrimão opcional nas laterais. */
    public void escada(float x, float y, float w, float d, Direcao sobe, float altura, boolean corrimaos) {
        add(TipoElemento.ESCADA, x, y, w, d, altura, sobe, VisualBloco.NENHUM, null, 0, false);
        if (!corrimaos) return;
        final float alt = 0.75f;
        final float m = 0.22f;
        switch (sobe) {
            case MAIS_Y:
                corrimao(x + m, y, alt, x + m, y + d, altura + alt);
                corrimao(x + w - m, y, alt, x + w - m, y + d, altura + alt);
                break;
            case MENOS_Y:
                corrimao(x + m, y, altura + alt, x + m, y + d, alt);
                corrimao(x + w - m, y, altura + alt, x + w - m, y + d, alt);
                break;
            case MAIS_X:
                corrimao(x, y + m, alt, x + w, y + m, altura + alt);
                corrimao(x, y + d - m, alt, x + w, y + d - m, altura + alt);
                break;
            default:
                corrimao(x, y + m, altura + alt, x + w, y + m, alt);
                corrimao(x, y + d - m, altura + alt, x + w, y + d - m, alt);
                break;
        }
    }

    /** Banco: tem altura para rolar em cima e um trilho no eixo mais longo. */
    public void banco(float x, float y, float w, float d, float altura) {
        add(TipoElemento.BANCO, x, y, w, d, altura, Direcao.MAIS_X, VisualBloco.NENHUM, null, 0, false);
        trilhoNoEixo(x, y, w, d, altura, TipoElemento.BANCO);
    }

    public void mureta(float x, float y, float w, float d, float altura) {
        add(TipoElemento.MURETA, x, y, w, d, altura, Direcao.MAIS_X, VisualBloco.NENHUM, null, 0, false);
        trilhoNoEixo(x, y, w, d, altura, TipoElemento.MURETA);
    }

    /** Corrimão alinhado a um dos eixos do mapa. */
    public void corrimao(float ax, float ay, float az, float bx, float by, float bz) {
        trilhos.add(new TrilhoGrind(ax, ay, az, bx, by, bz, TipoElemento.CORRIMAO));
        float x = Math.min(ax, bx);
        float y = Math.min(ay, by);
        float w = Math.max(0.12f, Math.abs(bx - ax));
        float d = Math.max(0.12f, Math.abs(by - ay));
        // A ordem das alturas segue o sentido crescente do eixo, para o desenho.
        boolean invertido = ax > bx || ay > by;
        elementos.add(new ElementoCenario(TipoElemento.CORRIMAO, x, y, w, d, Math.max(az, bz), Direcao.MAIS_X,
                VisualBloco.NENHUM, null, 0, false, invertido ? bz : az, invertido ? az : bz));
    }

    public void bloco(VisualBloco visual, float x, float y, float w, float d, float altura, int variacao) {
        add(TipoElemento.BLOCO, x, y, w, d, altura, Direcao.MAIS_X, visual, null, variacao, false);
    }

    /** Construção decorativa atrás das bordas distantes do mapa. */
    public void fundo(VisualBloco visual, float x, float y, float w, float d, float altura, String letreiro, int variacao) {
        add(TipoElemento.BLOCO, x, y, w, d, altura, Direcao.MAIS_X, visual, letreiro, variacao, true);
    }

    private void trilhoNoEixo(float x, float y, float w, float d, float altura, TipoElemento tipo) {
        if (w >= d) {
            trilhos.add(new TrilhoGrind(x, y + d / 2f, altura, x + w, y + d / 2f, altura, tipo));
        } else {
            trilhos.add(new TrilhoGrind(x + w / 2f, y, altura, x + w / 2f, y + d, altura, tipo));
        }
    }

    private void add(TipoElemento tipo, float x, float y, float w, float d, float altura, Direcao dir,
                     VisualBloco visual, String letreiro, int variacao, boolean fundo) {
        elementos.add(new ElementoCenario(tipo, x, y, w, d, altura, dir, visual, letreiro, variacao, fundo, 0, altura));
    }
}
