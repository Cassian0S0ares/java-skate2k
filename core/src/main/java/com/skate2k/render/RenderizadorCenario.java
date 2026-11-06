package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.graphics.Color;
import com.skate2k.logica.Cenario;
import com.skate2k.logica.Direcao;
import com.skate2k.logica.ElementoCenario;
import com.skate2k.logica.TipoElemento;
import com.skate2k.logica.TipoPiso;
import com.skate2k.logica.TrilhoGrind;

/** Desenha o piso e os elementos do cenário na projeção isométrica. */
final class RenderizadorCenario {
    private static final Color[] CORES_PREDIO = {
        Paleta.hex("e8567f"), Paleta.hex("4fb6d8"), Paleta.hex("f2b544"), Paleta.hex("8e6ee8")
    };
    private static final Color[] CORES_CASA = {
        Paleta.hex("f4c2d0"), Paleta.hex("b8e0d2"), Paleta.hex("fce38a"), Paleta.hex("c6c9f7")
    };
    private static final Color[] CORES_CARRO = {
        Paleta.hex("ff3b5c"), Paleta.hex("2e9bff"), Paleta.hex("ffc21f"), Paleta.hex("3ddc97")
    };
    private static final Color VIDRO = Paleta.hex("9ff0ff");
    private static final Color JANELA_ESCURA = Paleta.hex("31285a");
    private static final Color JANELA_ACESA = Paleta.hex("ffe78a");
    private static final Color METAL = Paleta.hex("d5d9ee");
    private static final Color FOLHA = Paleta.hex("2bb673");
    private static final Color FOLHA_CLARA = Paleta.hex("5fe08f");
    private static final Color TRONCO = Paleta.hex("8a5a33");

    private final Desenho d;
    private final Paleta.Tema tema;
    private final Color tmp = new Color();

    RenderizadorCenario(Desenho d, Paleta.Tema tema) {
        this.d = d;
        this.tema = tema;
    }

    // ---------------------------------------------------------------------------------- piso

    void desenharPiso(Cenario c, float minX, float maxX, float minY, float maxY, boolean chuva, float tempo) {
        int margem = 30;
        for (int soma = c.largura + c.profundidade + margem; soma >= -margem; soma--) {
            for (int ix = -margem; ix < c.largura + margem; ix++) {
                int iy = soma - ix;
                if (iy < -margem || iy >= c.profundidade + margem) continue;
                float cx = sx(ix + 0.5f, iy + 0.5f);
                float cy = sy(ix + 0.5f, iy + 0.5f, 0);
                if (cx < minX - 50 || cx > maxX + 50 || cy < minY - 40 || cy > maxY + 40) continue;
                desenharTile(c, ix, iy, chuva);
            }
        }
    }

    private void desenharTile(Cenario c, int ix, int iy, boolean chuva) {
        boolean dentro = ix >= 0 && iy >= 0 && ix < c.largura && iy < c.profundidade;
        TipoPiso tipo = c.pisoEm(ix + 0.5f, iy + 0.5f);
        Color base = tema.piso(tipo);
        int h = (ix * 73 + iy * 151) & 7;
        float luz = ((ix + iy) & 1) == 0 ? 0.035f : 0f;
        tmp.set(base).lerp(Color.WHITE, luz + h * 0.003f);
        if (!dentro) tmp.lerp(tema.fundo, 0.72f);
        if (chuva) tmp.lerp(Paleta.TINTA, 0.22f);
        losango(ix, iy, ix + 1, iy + 1, 0, tmp);

        if (!dentro) return;
        switch (tipo) {
            case FAIXA:
                losango(ix + 0.38f, iy + 0.1f, ix + 0.62f, iy + 0.9f, 0, tema.faixa);
                break;
            case FAIXA_PEDESTRE:
                if (iy % 2 == 0) losango(ix + 0.05f, iy + 0.12f, ix + 0.95f, iy + 0.88f, 0, Paleta.BRANCO);
                break;
            case CALCADA:
            case CONCRETO:
            case PINTADO:
            case PISO_PARQUE:
                bordaTile(ix, iy, Paleta.copia(Paleta.TINTA, 0.10f));
                break;
            case GRAMA:
                if (h < 2) {
                    tmp.set(base).lerp(Color.WHITE, 0.2f);
                    losango(ix + 0.3f, iy + 0.45f, ix + 0.42f, iy + 0.52f, 0, tmp);
                }
                break;
            default:
                break;
        }
        if (chuva && (ix * 13 + iy * 7) % 11 == 0 && tipo != TipoPiso.GRAMA) {
            float px = sx(ix + 0.5f, iy + 0.5f);
            float py = sy(ix + 0.5f, iy + 0.5f, 0);
            d.elipse(px, py, 58, 26, Paleta.copia(Paleta.CIANO, 0.18f));
            d.elipse(px - 6, py + 1, 32, 12, Paleta.copia(Paleta.BRANCO, 0.14f));
        }
    }

    private void bordaTile(int ix, int iy, Color c) {
        d.linha(sx(ix, iy), sy(ix, iy, 0), sx(ix + 1, iy), sy(ix + 1, iy, 0), 1.5f, c);
        d.linha(sx(ix, iy), sy(ix, iy, 0), sx(ix, iy + 1), sy(ix, iy + 1, 0), 1.5f, c);
    }

    private void losango(float x0, float y0, float x1, float y1, float z, Color c) {
        d.quad(sx(x0, y0), sy(x0, y0, z), sx(x1, y0), sy(x1, y0, z), sx(x1, y1), sy(x1, y1, z),
                sx(x0, y1), sy(x0, y1, z), c);
    }

    void desenharEnfeite(Cenario.Enfeite e) {
        float x = sx(e.x, e.y);
        float y = sy(e.x, e.y, 0);
        switch (e.tipo) {
            case BUEIRO:
                d.elipse(x, y, 40, 20, Paleta.TINTA);
                d.elipse(x, y, 30, 14, Paleta.CINZA);
                d.linha(x - 12, y, x + 12, y, 2, Paleta.TINTA);
                d.linha(x, y - 5, x, y + 5, 2, Paleta.TINTA);
                break;
            case FLORES:
                Color cor = e.variacao == 0 ? Paleta.ROSA : e.variacao == 1 ? Paleta.AMARELO : Paleta.BRANCO;
                d.circulo(x - 9, y, 4, cor);
                d.circulo(x + 7, y + 4, 4, cor);
                d.circulo(x + 2, y - 5, 4, cor);
                break;
            case TUFO:
                tmp.set(tema.grama).lerp(Paleta.TINTA, 0.25f);
                d.linha(x - 5, y, x - 8, y + 9, 2.5f, tmp);
                d.linha(x, y, x, y + 11, 2.5f, tmp);
                d.linha(x + 5, y, x + 8, y + 9, 2.5f, tmp);
                break;
            case RACHADURA:
                Color r = Paleta.copia(Paleta.TINTA, 0.5f);
                d.linha(x - 20, y, x - 6, y + 4, 2, r);
                d.linha(x - 6, y + 4, x + 2, y - 3, 2, r);
                d.linha(x + 2, y - 3, x + 18, y + 2, 2, r);
                break;
            case PICHACAO:
                pichacaoNoChao(e, x, y);
                break;
            default:
                break;
        }
    }

    private void pichacaoNoChao(Cenario.Enfeite e, float x, float y) {
        Color cor = e.variacao == 0 ? Paleta.ROSA : e.variacao == 1 ? Paleta.CIANO : Paleta.AMARELO;
        String texto = e.variacao == 0 ? "ROLE" : e.variacao == 1 ? "2K" : "SK8";
        FontePixel.varrer(texto, (col, lin) -> {
            float px = e.x + col * 0.16f;
            float py = e.y - lin * 0.16f;
            losango(px, py, px + 0.16f, py + 0.16f, 0, cor);
        });
    }

    // ------------------------------------------------------------------------------ elementos

    void desenhar(ElementoCenario e, float tempo, boolean trilhoAtivo) {
        switch (e.tipo) {
            case RAMPA: rampa(e); break;
            case ESCADA: escada(e); break;
            case PLATAFORMA: caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, tema.plataforma); break;
            case BANCO: banco(e, trilhoAtivo, tempo); break;
            case MURETA: mureta(e, trilhoAtivo, tempo); break;
            case CORRIMAO: corrimao(e, trilhoAtivo, tempo); break;
            default: bloco(e, tempo); break;
        }
    }

    /** Caixa com as três faces visíveis e contorno de desenho animado. */
    void caixa(float x0, float y0, float x1, float y1, float z0, float z1, Color c) {
        Color esq = Paleta.escuro(c, 0.18f);
        Color dir = Paleta.escuro(c, 0.34f);
        Color topo = Paleta.claro(c, 0.08f);
        faceEsq(x0, y0, y1, z0, z1, z1, esq);
        faceDir(x0, x1, y0, z0, z1, z1, dir);
        d.quad(sx(x0, y0), sy(x0, y0, z1), sx(x1, y0), sy(x1, y0, z1), sx(x1, y1), sy(x1, y1, z1),
                sx(x0, y1), sy(x0, y1, z1), topo);
        contorno(x0, y0, x1, y1, z0, z1);
    }

    private void contorno(float x0, float y0, float x1, float y1, float z0, float z1) {
        Color t = Paleta.TINTA;
        float e = 2.4f;
        d.linha(sx(x0, y1), sy(x0, y1, z1), sx(x1, y1), sy(x1, y1, z1), e, t);
        d.linha(sx(x1, y0), sy(x1, y0, z1), sx(x1, y1), sy(x1, y1, z1), e, t);
        d.linha(sx(x0, y0), sy(x0, y0, z1), sx(x1, y0), sy(x1, y0, z1), e, t);
        d.linha(sx(x0, y0), sy(x0, y0, z1), sx(x0, y1), sy(x0, y1, z1), e, t);
        d.linha(sx(x0, y0), sy(x0, y0, z0), sx(x0, y0), sy(x0, y0, z1), e, t);
        d.linha(sx(x0, y1), sy(x0, y1, z0), sx(x0, y1), sy(x0, y1, z1), e, t);
        d.linha(sx(x1, y0), sy(x1, y0, z0), sx(x1, y0), sy(x1, y0, z1), e, t);
        d.linha(sx(x0, y0), sy(x0, y0, z0), sx(x0, y1), sy(x0, y1, z0), e, t);
        d.linha(sx(x0, y0), sy(x0, y0, z0), sx(x1, y0), sy(x1, y0, z0), e, t);
    }

    /** Face visível à esquerda da tela (plano x = x0). zA é a altura em y0; zB, em y1. */
    private void faceEsq(float x0, float y0, float y1, float z0, float zA, float zB, Color c) {
        d.quad(sx(x0, y0), sy(x0, y0, z0), sx(x0, y1), sy(x0, y1, z0), sx(x0, y1), sy(x0, y1, zB),
                sx(x0, y0), sy(x0, y0, zA), c);
    }

    /** Face visível à direita da tela (plano y = y0). zA é a altura em x0; zB, em x1. */
    private void faceDir(float x0, float x1, float y0, float z0, float zA, float zB, Color c) {
        d.quad(sx(x0, y0), sy(x0, y0, z0), sx(x1, y0), sy(x1, y0, z0), sx(x1, y0), sy(x1, y0, zB),
                sx(x0, y0), sy(x0, y0, zA), c);
    }

    private void rampa(ElementoCenario e) {
        float x0 = e.x, y0 = e.y, x1 = e.x1(), y1 = e.y1();
        float h00 = e.alturaEm(x0, y0);
        float h10 = e.alturaEm(x1, y0);
        float h11 = e.alturaEm(x1, y1);
        float h01 = e.alturaEm(x0, y1);
        Color c = tema.rampa;
        faceEsq(x0, y0, y1, 0, h00, h01, Paleta.escuro(c, 0.28f));
        faceDir(x0, x1, y0, 0, h00, h10, Paleta.escuro(c, 0.42f));
        d.quad(sx(x0, y0), sy(x0, y0, h00), sx(x1, y0), sy(x1, y0, h10), sx(x1, y1), sy(x1, y1, h11),
                sx(x0, y1), sy(x0, y1, h01), c);

        // Listras na direção da subida indicam para onde a rampa lança.
        int n = 3;
        for (int i = 0; i < n; i++) {
            float t0 = (i + 0.25f) / n;
            float t1 = (i + 0.60f) / n;
            listraRampa(e, t0, t1);
        }
        Color t = Paleta.TINTA;
        float esp = 2.4f;
        d.linha(sx(x0, y0), sy(x0, y0, h00), sx(x1, y0), sy(x1, y0, h10), esp, t);
        d.linha(sx(x1, y0), sy(x1, y0, h10), sx(x1, y1), sy(x1, y1, h11), esp, t);
        d.linha(sx(x1, y1), sy(x1, y1, h11), sx(x0, y1), sy(x0, y1, h01), esp, t);
        d.linha(sx(x0, y1), sy(x0, y1, h01), sx(x0, y0), sy(x0, y0, h00), esp, t);
        d.linha(sx(x0, y0), sy(x0, y0, 0), sx(x0, y1), sy(x0, y1, 0), esp, t);
        d.linha(sx(x0, y0), sy(x0, y0, 0), sx(x1, y0), sy(x1, y0, 0), esp, t);
        d.linha(sx(x0, y0), sy(x0, y0, 0), sx(x0, y0), sy(x0, y0, h00), esp, t);
        d.linha(sx(x0, y1), sy(x0, y1, 0), sx(x0, y1), sy(x0, y1, h01), esp, t);
        d.linha(sx(x1, y0), sy(x1, y0, 0), sx(x1, y0), sy(x1, y0, h10), esp, t);
    }

    private void listraRampa(ElementoCenario e, float t0, float t1) {
        float m = 0.14f;
        float ax, ay, bx, by, cx, cy, dx, dy;
        Direcao dir = e.direcao;
        if (dir == Direcao.MAIS_X || dir == Direcao.MENOS_X) {
            float p0 = dir == Direcao.MAIS_X ? e.x + e.largX * t0 : e.x1() - e.largX * t0;
            float p1 = dir == Direcao.MAIS_X ? e.x + e.largX * t1 : e.x1() - e.largX * t1;
            ax = p0; ay = e.y + m; bx = p1; by = e.y + m; cx = p1; cy = e.y1() - m; dx = p0; dy = e.y1() - m;
        } else {
            float p0 = dir == Direcao.MAIS_Y ? e.y + e.largY * t0 : e.y1() - e.largY * t0;
            float p1 = dir == Direcao.MAIS_Y ? e.y + e.largY * t1 : e.y1() - e.largY * t1;
            ax = e.x + m; ay = p0; bx = e.x + m; by = p1; cx = e.x1() - m; cy = p1; dx = e.x1() - m; dy = p0;
        }
        d.quad(sx(ax, ay), sy(ax, ay, e.altura * t0), sx(bx, by), sy(bx, by, e.altura * t1),
                sx(cx, cy), sy(cx, cy, e.altura * t1), sx(dx, dy), sy(dx, dy, e.altura * t0), tema.listraRampa);
    }

    private void escada(ElementoCenario e) {
        int n = e.numeroDegraus();
        // Do degrau mais distante para o mais próximo, conforme o sentido da subida.
        boolean subindoParaLonge = e.direcao == Direcao.MAIS_X || e.direcao == Direcao.MAIS_Y;
        for (int k = 0; k < n; k++) {
            int i = subindoParaLonge ? n - 1 - k : k;
            float t0 = i / (float) n;
            float t1 = (i + 1) / (float) n;
            float h = e.altura * (i + 1) / n;
            Color c = i % 2 == 0 ? tema.escada : Paleta.escuro(tema.escada, 0.06f);
            switch (e.direcao) {
                case MAIS_X: caixa(e.x + e.largX * t0, e.y, e.x + e.largX * t1, e.y1(), 0, h, c); break;
                case MENOS_X: caixa(e.x1() - e.largX * t1, e.y, e.x1() - e.largX * t0, e.y1(), 0, h, c); break;
                case MAIS_Y: caixa(e.x, e.y + e.largY * t0, e.x1(), e.y + e.largY * t1, 0, h, c); break;
                default: caixa(e.x, e.y1() - e.largY * t1, e.x1(), e.y1() - e.largY * t0, 0, h, c); break;
            }
        }
    }

    private void banco(ElementoCenario e, boolean ativo, float tempo) {
        boolean noX = e.largX >= e.largY;
        float h = e.altura;
        float pe = 0.1f;
        // Pés
        Color metal = Paleta.TINTA;
        if (noX) {
            caixa(e.x + 0.2f, e.y + 0.15f, e.x + 0.2f + pe, e.y1() - 0.15f, 0, h - 0.12f, metal);
            caixa(e.x1() - 0.3f, e.y + 0.15f, e.x1() - 0.3f + pe, e.y1() - 0.15f, 0, h - 0.12f, metal);
        } else {
            caixa(e.x + 0.15f, e.y + 0.2f, e.x1() - 0.15f, e.y + 0.2f + pe, 0, h - 0.12f, metal);
            caixa(e.x + 0.15f, e.y1() - 0.3f, e.x1() - 0.15f, e.y1() - 0.3f + pe, 0, h - 0.12f, metal);
        }
        caixa(e.x, e.y, e.x1(), e.y1(), h - 0.12f, h, tema.banco);
        // Ripas do assento
        Color ripa = Paleta.escuro(tema.banco, 0.28f);
        if (noX) {
            float m = e.y + e.largY / 3f;
            float m2 = e.y + e.largY * 2f / 3f;
            d.linha(sx(e.x, m), sy(e.x, m, h), sx(e.x1(), m), sy(e.x1(), m, h), 1.8f, ripa);
            d.linha(sx(e.x, m2), sy(e.x, m2, h), sx(e.x1(), m2), sy(e.x1(), m2, h), 1.8f, ripa);
        } else {
            float m = e.x + e.largX / 3f;
            float m2 = e.x + e.largX * 2f / 3f;
            d.linha(sx(m, e.y), sy(m, e.y, h), sx(m, e.y1()), sy(m, e.y1(), h), 1.8f, ripa);
            d.linha(sx(m2, e.y), sy(m2, e.y, h), sx(m2, e.y1()), sy(m2, e.y1(), h), 1.8f, ripa);
        }
        realceTrilho(e, h, ativo, tempo);
    }

    private void mureta(ElementoCenario e, boolean ativo, float tempo) {
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, tema.mureta);
        // Tinta descascada na face voltada para a câmera
        Color tinta = Paleta.copia(Paleta.ROSA, 0.75f);
        int n = (int) Math.max(e.largX, e.largY);
        for (int i = 0; i < n; i += 2) {
            if (e.largX >= e.largY) {
                float a = e.x + i + 0.2f;
                float b = Math.min(e.x1() - 0.1f, a + 0.7f);
                faceDir(a, b, e.y, e.altura * 0.25f, e.altura * 0.65f, e.altura * 0.65f, tinta);
            } else {
                float a = e.y + i + 0.2f;
                float b = Math.min(e.y1() - 0.1f, a + 0.7f);
                faceEsq(e.x, a, b, e.altura * 0.25f, e.altura * 0.65f, e.altura * 0.65f, tinta);
            }
        }
        realceTrilho(e, e.altura, ativo, tempo);
    }

    /** Linha amarela no topo avisa que dá para encaixar um grind ali. */
    private void realceTrilho(ElementoCenario e, float h, boolean ativo, float tempo) {
        float ax, ay, bx, by;
        if (e.largX >= e.largY) {
            ax = e.x; bx = e.x1(); ay = by = e.y + e.largY / 2f;
        } else {
            ay = e.y; by = e.y1(); ax = bx = e.x + e.largX / 2f;
        }
        float brilho = ativo ? 0.6f + 0.4f * (float) Math.sin(tempo * 9f) : 0.55f;
        d.linha(sx(ax, ay), sy(ax, ay, h), sx(bx, by), sy(bx, by, h), ativo ? 5f : 3f,
                Paleta.copia(Paleta.AMARELO, brilho));
    }

    private void corrimao(ElementoCenario e, boolean ativo, float tempo) {
        boolean noX = e.largX >= e.largY;
        float ax = e.x, ay = e.y;
        float bx = noX ? e.x1() : e.x;
        float by = noX ? e.y : e.y1();
        if (noX) { ay = by = e.y + e.largY / 2f; } else { ax = bx = e.x + e.largX / 2f; }
        float za = e.alturaInicio;
        float zb = e.alturaFim;
        float comp = Math.max(e.largX, e.largY);

        // Postes: da ponta mais distante para a mais próxima.
        int postes = Math.max(2, Math.round(comp / 1.8f) + 1);
        Color poste = Paleta.hex("6b6a8a");
        for (int i = postes - 1; i >= 0; i--) {
            float t = i / (float) (postes - 1);
            float px = ax + (bx - ax) * t;
            float py = ay + (by - ay) * t;
            float pz = za + (zb - za) * t;
            d.linha(sx(px, py), sy(px, py, 0), sx(px, py), sy(px, py, pz), 6f, Paleta.TINTA);
            d.linha(sx(px, py), sy(px, py, 0), sx(px, py), sy(px, py, pz), 2.8f, poste);
        }
        d.membro(sx(ax, ay), sy(ax, ay, za), sx(bx, by), sy(bx, by, zb), 8f, Paleta.TINTA);
        Color cano = ativo ? Paleta.AMARELO : METAL;
        d.membro(sx(ax, ay), sy(ax, ay, za), sx(bx, by), sy(bx, by, zb), 4.2f, cano);
        float brilho = 0.5f + 0.5f * (float) Math.sin(tempo * 4f);
        d.linha(sx(ax, ay), sy(ax, ay, za) + 1, sx(bx, by), sy(bx, by, zb) + 1, 1.4f,
                Paleta.copia(Paleta.BRANCO, 0.5f + 0.4f * brilho));
    }

    // ---------------------------------------------------------------------------------- blocos

    private void bloco(ElementoCenario e, float tempo) {
        switch (e.visual) {
            case PREDIO: predio(e); break;
            case CASA: casa(e); break;
            case CARRO: carro(e); break;
            case ARVORE: arvore(e, tempo); break;
            case LIXEIRA: lixeira(e); break;
            case HIDRANTE: hidrante(e); break;
            case FONTE: fonte(e, tempo); break;
            case BANCA: banca(e); break;
            case CERCA: cerca(e); break;
            case ANAO_JARDIM: anao(e); break;
            case CAIXA_CORREIO: caixaCorreio(e); break;
            case ARBUSTO: arbusto(e); break;
            case POSTE: poste(e); break;
            default: caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, Paleta.CINZA); break;
        }
    }

    private void predio(ElementoCenario e) {
        Color c = CORES_PREDIO[e.variacao % CORES_PREDIO.length];
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, c);
        janelasEsq(e, 1.1f);
        janelasDir(e, 1.1f);
        letreiro(e);
    }

    private void janelasEsq(ElementoCenario e, float inicio) {
        for (float z = inicio; z + 0.7f < e.altura; z += 1.3f) {
            for (float y = e.y + 0.45f; y + 0.6f < e.y1(); y += 1.2f) {
                boolean acesa = ((int) (y * 3) + (int) (z * 5) + e.variacao) % 3 == 0;
                faceEsq(e.x, y, y + 0.6f, z, z + 0.7f, z + 0.7f, acesa ? JANELA_ACESA : JANELA_ESCURA);
            }
        }
    }

    private void janelasDir(ElementoCenario e, float inicio) {
        for (float z = inicio; z + 0.7f < e.altura; z += 1.3f) {
            for (float x = e.x + 0.45f; x + 0.6f < e.x1(); x += 1.2f) {
                boolean acesa = ((int) (x * 3) + (int) (z * 5) + e.variacao) % 3 == 1;
                faceDir(x, x + 0.6f, e.y, z, z + 0.7f, z + 0.7f, acesa ? JANELA_ACESA : JANELA_ESCURA);
            }
        }
    }

    /** Letreiro pintado na maior face visível do prédio, respeitando a perspectiva. */
    private void letreiro(ElementoCenario e) {
        if (e.letreiro == null) return;
        String t = e.letreiro;
        float tam = 0.115f;
        float comp = FontePixel.largura(t, tam);
        float z = 0.2f;
        float alturaLetra = 7 * tam;
        if (e.largY >= e.largX) {
            // Na face x = x0: a leitura na tela vai de y1 para y0.
            float meio = e.y + e.largY / 2f;
            float inicio = meio + comp / 2f;
            float fundoA = inicio + 0.2f;
            float fundoB = meio - comp / 2f - 0.2f;
            faceEsq(e.x, fundoB, fundoA, z - 0.08f, z + alturaLetra + 0.08f, z + alturaLetra + 0.08f, Paleta.TINTA);
            FontePixel.varrer(t, (col, lin) -> {
                float py1 = inicio - col * tam;
                float py0 = py1 - tam;
                float pz = z + (6 - lin) * tam;
                faceEsq(e.x, py0, py1, pz, pz + tam, pz + tam, Paleta.AMARELO);
            });
        } else {
            float meio = e.x + e.largX / 2f;
            float inicio = meio - comp / 2f;
            faceDir(inicio - 0.2f, inicio + comp + 0.2f, e.y, z - 0.08f, z + alturaLetra + 0.08f, z + alturaLetra + 0.08f, Paleta.TINTA);
            FontePixel.varrer(t, (col, lin) -> {
                float px0 = inicio + col * tam;
                float pz = z + (6 - lin) * tam;
                faceDir(px0, px0 + tam, e.y, pz, pz + tam, pz + tam, Paleta.AMARELO);
            });
        }
    }

    private void casa(ElementoCenario e) {
        Color c = CORES_CASA[e.variacao % CORES_CASA.length];
        float h = e.altura;
        caixa(e.x, e.y, e.x1(), e.y1(), 0, h, c);
        // Telhado de duas águas, com a cumeeira ao longo do lado maior
        Color telha = Paleta.hex("c9503c");
        Color telha2 = Paleta.hex("a33d2c");
        float t = Math.min(e.largX, e.largY) * 0.32f;
        if (e.largX >= e.largY) {
            float m = e.y + e.largY / 2f;
            d.tri(sx(e.x, e.y), sy(e.x, e.y, h), sx(e.x, e.y1()), sy(e.x, e.y1(), h), sx(e.x, m), sy(e.x, m, h + t), Paleta.claro(c, 0.2f));
            d.quad(sx(e.x, e.y), sy(e.x, e.y, h), sx(e.x1(), e.y), sy(e.x1(), e.y, h), sx(e.x1(), m), sy(e.x1(), m, h + t), sx(e.x, m), sy(e.x, m, h + t), telha);
            d.quad(sx(e.x, e.y1()), sy(e.x, e.y1(), h), sx(e.x, m), sy(e.x, m, h + t), sx(e.x1(), m), sy(e.x1(), m, h + t), sx(e.x1(), e.y1()), sy(e.x1(), e.y1(), h), telha2);
            d.linha(sx(e.x, m), sy(e.x, m, h + t), sx(e.x1(), m), sy(e.x1(), m, h + t), 2.5f, Paleta.TINTA);
            d.linha(sx(e.x, e.y), sy(e.x, e.y, h), sx(e.x, m), sy(e.x, m, h + t), 2.5f, Paleta.TINTA);
            d.linha(sx(e.x, e.y1()), sy(e.x, e.y1(), h), sx(e.x, m), sy(e.x, m, h + t), 2.5f, Paleta.TINTA);
        } else {
            float m = e.x + e.largX / 2f;
            d.tri(sx(e.x, e.y), sy(e.x, e.y, h), sx(e.x1(), e.y), sy(e.x1(), e.y, h), sx(m, e.y), sy(m, e.y, h + t), Paleta.claro(c, 0.2f));
            d.quad(sx(e.x, e.y), sy(e.x, e.y, h), sx(m, e.y), sy(m, e.y, h + t), sx(m, e.y1()), sy(m, e.y1(), h + t), sx(e.x, e.y1()), sy(e.x, e.y1(), h), telha);
            d.quad(sx(e.x1(), e.y), sy(e.x1(), e.y, h), sx(e.x1(), e.y1()), sy(e.x1(), e.y1(), h), sx(m, e.y1()), sy(m, e.y1(), h + t), sx(m, e.y), sy(m, e.y, h + t), telha2);
            d.linha(sx(m, e.y), sy(m, e.y, h + t), sx(m, e.y1()), sy(m, e.y1(), h + t), 2.5f, Paleta.TINTA);
            d.linha(sx(e.x, e.y), sy(e.x, e.y, h), sx(m, e.y), sy(m, e.y, h + t), 2.5f, Paleta.TINTA);
            d.linha(sx(e.x1(), e.y), sy(e.x1(), e.y, h), sx(m, e.y), sy(m, e.y, h + t), 2.5f, Paleta.TINTA);
        }
        // Portas e janelas
        float meioDir = e.x + e.largX * 0.5f;
        if (e.largX > 3f) {
            faceDir(meioDir - 0.4f, meioDir + 0.4f, e.y, 0, 1.5f, 1.5f, Paleta.hex("8a5a33"));
            janelasDirCasa(e);
        }
        if (e.largY > 3f) {
            for (float y = e.y + 0.8f; y + 0.8f < e.y1(); y += 2f) {
                faceEsq(e.x, y, y + 0.8f, 1.0f, 1.9f, 1.9f, VIDRO);
                d.linha(sx(e.x, y + 0.4f), sy(e.x, y + 0.4f, 1.0f), sx(e.x, y + 0.4f), sy(e.x, y + 0.4f, 1.9f), 2, Paleta.BRANCO);
            }
        }
        letreiro(e);
    }

    private void janelasDirCasa(ElementoCenario e) {
        for (float x = e.x + 0.7f; x + 0.8f < e.x1(); x += 2.4f) {
            if (Math.abs(x + 0.4f - (e.x + e.largX * 0.5f)) < 0.9f) continue;
            faceDir(x, x + 0.8f, e.y, 1.0f, 1.9f, 1.9f, VIDRO);
        }
    }

    private void carro(ElementoCenario e) {
        Color c = CORES_CARRO[e.variacao % CORES_CARRO.length];
        boolean noX = e.largX >= e.largY;
        // Rodas
        Color roda = Paleta.TINTA;
        if (noX) {
            d.circulo(sx(e.x + 0.5f, e.y), sy(e.x + 0.5f, e.y, 0.14f), 9, roda);
            d.circulo(sx(e.x1() - 0.5f, e.y), sy(e.x1() - 0.5f, e.y, 0.14f), 9, roda);
        } else {
            d.circulo(sx(e.x, e.y + 0.5f), sy(e.x, e.y + 0.5f, 0.14f), 9, roda);
            d.circulo(sx(e.x, e.y1() - 0.5f), sy(e.x, e.y1() - 0.5f, 0.14f), 9, roda);
        }
        caixa(e.x, e.y, e.x1(), e.y1(), 0.15f, 0.62f, c);
        float cab = 0.55f;
        float cx0 = noX ? e.x + cab : e.x + 0.1f;
        float cx1 = noX ? e.x1() - cab : e.x1() - 0.1f;
        float cy0 = noX ? e.y + 0.1f : e.y + cab;
        float cy1 = noX ? e.y1() - 0.1f : e.y1() - cab;
        caixa(cx0, cy0, cx1, cy1, 0.62f, 1.0f, VIDRO);
        d.quad(sx(cx0, cy0), sy(cx0, cy0, 1.0f), sx(cx1, cy0), sy(cx1, cy0, 1.0f), sx(cx1, cy1), sy(cx1, cy1, 1.0f),
                sx(cx0, cy1), sy(cx0, cy1, 1.0f), c);
    }

    private void arvore(ElementoCenario e, float tempo) {
        float cx = e.x + e.largX / 2f;
        float cy = e.y + e.largY / 2f;
        caixa(e.x + 0.12f, e.y + 0.12f, e.x1() - 0.12f, e.y1() - 0.12f, 0, 1.6f, TRONCO);
        float balanco = (float) Math.sin(tempo * 1.4f + e.x) * 2f;
        float tx = sx(cx, cy) + balanco;
        float ty = sy(cx, cy, 2.4f);
        d.circulo(tx - 26, ty - 8, 32, Paleta.TINTA);
        d.circulo(tx + 26, ty - 8, 32, Paleta.TINTA);
        d.circulo(tx, ty + 20, 38, Paleta.TINTA);
        d.circulo(tx - 26, ty - 8, 29, FOLHA);
        d.circulo(tx + 26, ty - 8, 29, FOLHA);
        d.circulo(tx, ty + 20, 35, FOLHA);
        d.circulo(tx - 8, ty + 28, 20, FOLHA_CLARA);
    }

    private void lixeira(ElementoCenario e) {
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, Paleta.hex("3ddc97"));
        caixa(e.x - 0.04f, e.y - 0.04f, e.x1() + 0.04f, e.y1() + 0.04f, e.altura, e.altura + 0.1f, Paleta.TINTA);
    }

    private void hidrante(ElementoCenario e) {
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, Paleta.VERMELHO);
        float cx = sx(e.x + e.largX / 2f, e.y + e.largY / 2f);
        float cy = sy(e.x + e.largX / 2f, e.y + e.largY / 2f, e.altura);
        d.circulo(cx, cy + 3, 8, Paleta.TINTA);
        d.circulo(cx, cy + 3, 6, Paleta.VERMELHO);
    }

    private void fonte(ElementoCenario e, float tempo) {
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, Paleta.hex("c9d2f0"));
        float cx = e.x + e.largX / 2f;
        float cy = e.y + e.largY / 2f;
        float m = 0.25f;
        losango(e.x + m, e.y + m, e.x1() - m, e.y1() - m, e.altura, Paleta.hex("3fc6f5"));
        float px = sx(cx, cy);
        float pz = sy(cx, cy, e.altura);
        d.linha(px, pz, px, pz + 60, 10, Paleta.TINTA);
        d.linha(px, pz, px, pz + 60, 6, Paleta.hex("eefcff"));
        for (int i = 0; i < 7; i++) {
            float fase = (tempo * 0.8f + i * 0.14f) % 1f;
            float ang = i * 0.9f;
            float dx = (float) Math.cos(ang) * 60f * fase;
            float dy = (float) Math.sin(ang) * 26f * fase;
            float subida = 60f + 26f * (float) Math.sin(fase * Math.PI) - 70f * fase;
            d.circulo(px + dx, pz + dy + subida, 3.5f, Paleta.hex("c9f5ff"));
        }
    }

    private void banca(ElementoCenario e) {
        caixa(e.x, e.y, e.x1(), e.y1(), 0, e.altura, Paleta.hex("3f8cff"));
        caixa(e.x - 0.15f, e.y - 0.15f, e.x1() + 0.15f, e.y1() + 0.15f, e.altura, e.altura + 0.14f, Paleta.AMARELO);
        faceDir(e.x + 0.3f, e.x1() - 0.3f, e.y, 0.5f, 1.3f, 1.3f, Paleta.BRANCO);
        for (int i = 0; i < 4; i++) {
            float x = e.x + 0.4f + i * 0.46f;
            Color c = i % 2 == 0 ? Paleta.ROSA : Paleta.LIMA;
            faceDir(x, x + 0.34f, e.y, 0.65f, 1.15f, 1.15f, c);
        }
    }

    private void cerca(ElementoCenario e) {
        Color madeira = Paleta.hex("fff3d9");
        boolean noX = e.largX >= e.largY;
        float comp = Math.max(e.largX, e.largY);
        int n = Math.round(comp / 0.32f);
        for (int k = 0; k < n; k++) {
            // Sempre da tábua mais distante para a mais próxima.
            int i = n - 1 - k;
            float t = i / (float) n;
            float l = 0.22f / comp;
            if (noX) {
                caixa(e.x + comp * t, e.y, e.x + comp * (t + l), e.y1(), 0, e.altura, madeira);
            } else {
                caixa(e.x, e.y + comp * t, e.x1(), e.y + comp * (t + l), 0, e.altura, madeira);
            }
        }
    }

    private void anao(ElementoCenario e) {
        float cx = sx(e.x + e.largX / 2f, e.y + e.largY / 2f);
        float y = sy(e.x + e.largX / 2f, e.y + e.largY / 2f, 0);
        d.elipse(cx, y + 8, 24, 20, Paleta.TINTA);
        d.elipse(cx, y + 8, 20, 16, e.variacao == 0 ? Paleta.hex("2e9bff") : Paleta.hex("3ddc97"));
        d.circulo(cx, y + 21, 8, Paleta.hex("ffcfa8"));
        d.tri(cx - 9, y + 24, cx + 9, y + 24, cx, y + 44, Paleta.VERMELHO);
        d.elipse(cx, y + 17, 14, 9, Paleta.BRANCO);
    }

    private void caixaCorreio(ElementoCenario e) {
        float cx = e.x + e.largX / 2f;
        float cy = e.y + e.largY / 2f;
        d.linha(sx(cx, cy), sy(cx, cy, 0), sx(cx, cy), sy(cx, cy, 0.6f), 5, Paleta.hex("8a5a33"));
        caixa(cx - 0.22f, cy - 0.12f, cx + 0.22f, cy + 0.12f, 0.6f, e.altura, Paleta.BRANCO);
        d.linha(sx(cx + 0.22f, cy), sy(cx + 0.22f, cy, e.altura), sx(cx + 0.22f, cy), sy(cx + 0.22f, cy, e.altura + 0.3f), 3, Paleta.VERMELHO);
    }

    private void arbusto(ElementoCenario e) {
        float cx = sx(e.x + e.largX / 2f, e.y + e.largY / 2f);
        float cy = sy(e.x + e.largX / 2f, e.y + e.largY / 2f, 0.32f);
        float r = 18f * Math.max(e.largX, e.largY);
        d.circulo(cx - r * 0.65f, cy, r * 0.8f + 2.5f, Paleta.TINTA);
        d.circulo(cx + r * 0.65f, cy, r * 0.8f + 2.5f, Paleta.TINTA);
        d.circulo(cx, cy + r * 0.45f, r + 2.5f, Paleta.TINTA);
        d.circulo(cx - r * 0.65f, cy, r * 0.8f, FOLHA);
        d.circulo(cx + r * 0.65f, cy, r * 0.8f, FOLHA);
        d.circulo(cx, cy + r * 0.45f, r, FOLHA_CLARA);
    }

    private void poste(ElementoCenario e) {
        float cx = e.x + e.largX / 2f;
        float cy = e.y + e.largY / 2f;
        float base = sy(cx, cy, 0);
        float topo = sy(cx, cy, e.altura);
        float x = sx(cx, cy);
        d.linha(x, base, x, topo, 9, Paleta.TINTA);
        d.linha(x, base, x, topo, 5, Paleta.hex("6b6a8a"));
        d.linha(x, topo, x + 34, topo + 10, 7, Paleta.TINTA);
        d.linha(x, topo, x + 34, topo + 10, 3.5f, Paleta.hex("6b6a8a"));
        d.circulo(x + 34, topo + 6, 9, Paleta.TINTA);
        d.circulo(x + 34, topo + 6, 6.5f, JANELA_ACESA);
    }

    /** Indica se o trilho mais próximo do jogador pertence a este elemento, para o realce. */
    static boolean elementoDoTrilho(ElementoCenario e, TrilhoGrind t) {
        if (t == null) return false;
        if (e.tipo != TipoElemento.CORRIMAO && e.tipo != TipoElemento.BANCO && e.tipo != TipoElemento.MURETA) return false;
        return e.contem(t.xEm(0.5f), t.yEm(0.5f));
    }
}
