package com.skate2k.logica;

/** Caixa alinhada aos eixos do mapa. Define altura de terreno e, para o renderizador, a aparência. */
public final class ElementoCenario {
    public final TipoElemento tipo;
    public final float x;
    public final float y;
    public final float largX;
    public final float largY;
    public final float altura;
    public final Direcao direcao;
    public final VisualBloco visual;
    public final String letreiro;
    public final int variacao;
    /** Elementos de fundo ficam fora da área jogável e são desenhados antes de tudo. */
    public final boolean fundo;
    /** Altura de cada ponta do corrimão, apenas para desenho. */
    public final float alturaInicio;
    public final float alturaFim;

    ElementoCenario(TipoElemento tipo, float x, float y, float largX, float largY, float altura,
                    Direcao direcao, VisualBloco visual, String letreiro, int variacao, boolean fundo,
                    float alturaInicio, float alturaFim) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.largX = largX;
        this.largY = largY;
        this.altura = altura;
        this.direcao = direcao;
        this.visual = visual;
        this.letreiro = letreiro;
        this.variacao = variacao;
        this.fundo = fundo;
        this.alturaInicio = alturaInicio;
        this.alturaFim = alturaFim;
    }

    public float x1() { return x + largX; }
    public float y1() { return y + largY; }

    public boolean contem(float px, float py) {
        return px >= x && px <= x + largX && py >= y && py <= y + largY;
    }

    /** Progresso de 0 (base) a 1 (topo) na direção de subida. */
    public float progresso(float px, float py) {
        switch (direcao) {
            case MAIS_X: return (px - x) / largX;
            case MENOS_X: return (x + largX - px) / largX;
            case MAIS_Y: return (py - y) / largY;
            default: return (y + largY - py) / largY;
        }
    }

    /** Altura do terreno neste ponto; -1 se o ponto está fora deste elemento. */
    public float alturaEm(float px, float py) {
        if (!contem(px, py)) return -1f;
        switch (tipo) {
            case RAMPA:
                return altura * progresso(px, py);
            case ESCADA: {
                int degraus = numeroDegraus();
                float t = Math.min(0.9999f, progresso(px, py));
                return altura * ((int) (t * degraus) + 1) / degraus;
            }
            case CORRIMAO:
                return 0f; // cano fino: o skate passa por baixo; o encaixe vem do TrilhoGrind
            default:
                return altura;
        }
    }

    public int numeroDegraus() {
        return Math.max(2, Math.round(altura / 0.25f));
    }

    public float comprimentoNaDirecao() {
        return direcao == Direcao.MAIS_X || direcao == Direcao.MENOS_X ? largX : largY;
    }

    /** Subida por unidade de distância em uma rampa. */
    public float inclinacao() {
        return altura / comprimentoNaDirecao();
    }
}
