package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.graphics.Color;
import com.skate2k.logica.EventoJogo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Partículas, textos flutuantes e tremor de tela. Puro enfeite: nada aqui afeta a simulação. */
public final class Efeitos {
    private static final class Particula {
        float x, y, vx, vy, vida, vidaTotal, tamanho;
        Color cor;
        boolean gravidade;
    }

    private static final class Texto {
        float x, y, vida, vidaTotal, escala;
        String texto;
        Color cor;
    }

    private static final Color[] CONFETE = {Paleta.ROSA, Paleta.CIANO, Paleta.AMARELO, Paleta.LIMA, Paleta.BRANCO};

    private final List<Particula> particulas = new ArrayList<>();
    private final List<Texto> textos = new ArrayList<>();
    private final Random aleatorio = new Random();
    private final Color tmp = new Color();
    private float tremor;
    private float flash;
    private Color corFlash = Paleta.BRANCO;

    public void processar(EventoJogo e) {
        float x = sx(e.x, e.y);
        float y = sy(e.x, e.y, e.z);
        switch (e.tipo) {
            case PULO:
                poeira(x, y, 6);
                break;
            case POUSO:
                poeira(x, y, 8);
                if (e.valor > 0) texto("+" + e.valor, x, y + 80, 3f, Paleta.CIANO);
                break;
            case POUSO_LIMPO:
                explosao(x, y + 10, 14, Paleta.AMARELO, 200);
                texto(e.texto + " +" + e.valor, x, y + 90, 3f, Paleta.AMARELO);
                break;
            case QUEDA:
                tremor = Math.max(tremor, 11f);
                poeira(x, y, 16);
                texto(e.texto, x, y + 110, 3f, Paleta.VERMELHO);
                flash(Paleta.VERMELHO, 0.25f);
                break;
            case DANO:
                tremor = Math.max(tremor, 10f + e.valor * 4f);
                flash(Paleta.VERMELHO, 0.2f + e.valor * 0.08f);
                explosao(x, y - 40, 8 + e.valor * 6, Paleta.VERMELHO, 240);
                texto(e.texto, x, y, 2.5f + e.valor * 0.5f, Paleta.VERMELHO);
                break;
            case ESCUDO_USADO:
                tremor = Math.max(tremor, 7f);
                explosao(x, y + 20, 18, Paleta.LIMA, 240);
                texto(e.texto, x, y + 100, 3f, Paleta.LIMA);
                break;
            case GRIND_INICIO:
                explosao(x, y, 10, Paleta.AMARELO, 170);
                break;
            case ROBO_ATINGIDO:
                tremor = Math.max(tremor, 7f);
                explosao(x, y, 16, Paleta.LARANJA, 260);
                texto(e.texto, x, y + 40, 2.5f, Paleta.LARANJA);
                break;
            case ROBO_DERROTADO:
                tremor = Math.max(tremor, 15f);
                flash(Paleta.BRANCO, 0.28f);
                for (Color c : CONFETE) explosao(x, y, 12, c, 420);
                texto("+" + e.valor, x, y + 70, 5f, Paleta.AMARELO);
                texto(e.texto, x, y + 30, 2.5f, Paleta.BRANCO);
                break;
            case ROBO_ALERTA:
                texto(e.texto, x, y + 30, 5f, Paleta.VERMELHO);
                break;
            case ROBO_FALA:
                texto(e.texto, x, y + 30, 2f, Paleta.BRANCO);
                break;
            case ROUBO_CARGA:
                tremor = Math.max(tremor, 5f);
                explosao(x, y - 50, 8, Paleta.CIANO, 200);
                texto(e.texto, x, y, 2.5f, Paleta.CIANO);
                break;
            case ITEM:
                explosao(x, y, 16, Paleta.LIMA, 240);
                texto(e.texto, x, y + 50, 2.5f, Paleta.LIMA);
                break;
            case SUBIU_NIVEL:
                flash(Paleta.AMARELO, 0.2f);
                explosao(x, y, 24, Paleta.AMARELO, 320);
                texto("ESTILO: " + e.texto, x, y + 40, 4f, Paleta.ROSA);
                break;
            case POTENCIALIZADOR:
                texto(e.texto, x, y, 2.5f, Paleta.LIMA);
                break;
            case META_BATIDA:
                flash(Paleta.LIMA, 0.25f);
                texto(e.texto, x, y + 140, 5f, Paleta.LIMA);
                break;
            case NEGADO:
                texto(e.texto, x, y, 2.5f, Paleta.LARANJA);
                break;
            default:
                break;
        }
    }

    private void flash(Color cor, float alfa) {
        corFlash = cor;
        flash = alfa;
    }

    private void texto(String t, float x, float y, float escala, Color cor) {
        // Empilha sobre os textos recentes da mesma região para não embolar a leitura.
        boolean mexeu = true;
        for (int tentativa = 0; mexeu && tentativa < 6; tentativa++) {
            mexeu = false;
            for (Texto outro : textos) {
                if (Math.abs(outro.x - x) < 260f && Math.abs(outro.y - y) < 12f * Math.max(escala, outro.escala)) {
                    y = outro.y + 11f * Math.max(escala, outro.escala);
                    mexeu = true;
                }
            }
        }
        Texto tx = new Texto();
        tx.texto = t;
        tx.x = x;
        tx.y = y;
        tx.escala = escala;
        tx.cor = cor;
        tx.vida = tx.vidaTotal = 1.5f;
        textos.add(tx);
    }

    private void poeira(float x, float y, int n) {
        for (int i = 0; i < n; i++) {
            Particula p = nova(x, y, Paleta.copia(Paleta.BRANCO, 0.7f));
            float a = aleatorio.nextFloat() * 6.28f;
            p.vx = (float) Math.cos(a) * (40 + aleatorio.nextFloat() * 70);
            p.vy = (float) Math.sin(a) * 25 + 15;
            p.tamanho = 5 + aleatorio.nextFloat() * 7;
            p.vida = p.vidaTotal = 0.35f + aleatorio.nextFloat() * 0.25f;
        }
    }

    private void explosao(float x, float y, int n, Color cor, float forca) {
        for (int i = 0; i < n; i++) {
            Particula p = nova(x, y, cor);
            float a = aleatorio.nextFloat() * 6.28f;
            float f = forca * (0.35f + aleatorio.nextFloat() * 0.65f);
            p.vx = (float) Math.cos(a) * f;
            p.vy = (float) Math.sin(a) * f + 70;
            p.tamanho = 3 + aleatorio.nextFloat() * 5;
            p.gravidade = true;
            p.vida = p.vidaTotal = 0.45f + aleatorio.nextFloat() * 0.4f;
        }
    }

    private Particula nova(float x, float y, Color cor) {
        Particula p = new Particula();
        p.x = x;
        p.y = y;
        p.cor = cor;
        particulas.add(p);
        return p;
    }

    /** Faíscas contínuas enquanto o jogador está em grind. */
    public void faiscasDeGrind(float x, float y, float dt) {
        if (aleatorio.nextFloat() < dt * 45f) {
            Particula p = nova(x, y, aleatorio.nextBoolean() ? Paleta.AMARELO : Paleta.LARANJA);
            p.vx = (aleatorio.nextFloat() - 0.5f) * 220f;
            p.vy = 40 + aleatorio.nextFloat() * 110f;
            p.tamanho = 2 + aleatorio.nextFloat() * 3;
            p.gravidade = true;
            p.vida = p.vidaTotal = 0.3f + aleatorio.nextFloat() * 0.25f;
        }
    }

    public void atualizar(float dt) {
        tremor = Math.max(0, tremor - 34f * dt);
        flash = Math.max(0, flash - dt * 0.9f);
        for (Iterator<Particula> it = particulas.iterator(); it.hasNext(); ) {
            Particula p = it.next();
            p.vida -= dt;
            if (p.vida <= 0) {
                it.remove();
                continue;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.gravidade) p.vy -= 600f * dt;
        }
        for (Iterator<Texto> it = textos.iterator(); it.hasNext(); ) {
            Texto t = it.next();
            t.vida -= dt;
            t.y += 42f * dt;
            if (t.vida <= 0) it.remove();
        }
    }

    public void desenhar(Desenho d) {
        for (Particula p : particulas) {
            tmp.set(p.cor);
            tmp.a *= Math.min(1f, p.vida / p.vidaTotal * 1.7f);
            d.ret(p.x - p.tamanho / 2f, p.y - p.tamanho / 2f, p.tamanho, p.tamanho, tmp);
        }
        for (Texto t : textos) {
            float vidaPassada = t.vidaTotal - t.vida;
            float pop = vidaPassada < 0.12f ? 0.6f + vidaPassada / 0.12f * 0.4f : 1f;
            FontePixel.centro(d, t.texto, t.x, t.y, Math.round(t.escala * pop * 2f) / 2f, t.cor);
        }
    }

    public float deslocamentoX() {
        return tremor > 0 ? (aleatorio.nextFloat() - 0.5f) * tremor : 0f;
    }

    public float deslocamentoY() {
        return tremor > 0 ? (aleatorio.nextFloat() - 0.5f) * tremor : 0f;
    }

    public float flash() { return flash; }
    public Color corFlash() { return corFlash; }
}
