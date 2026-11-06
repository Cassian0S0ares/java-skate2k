package com.skate2k.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/** Atalhos sobre o ShapeRenderer. Todo o visual do jogo é desenhado com formas, sem imagens. */
public final class Desenho implements Disposable {
    private final ShapeRenderer sr = new ShapeRenderer(30000);

    public void iniciar(Matrix4 projecao) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.setProjectionMatrix(projecao);
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void terminar() {
        sr.end();
    }

    public void tri(float x1, float y1, float x2, float y2, float x3, float y3, Color c) {
        sr.setColor(c);
        sr.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, Color c) {
        sr.setColor(c);
        sr.triangle(x1, y1, x2, y2, x3, y3);
        sr.triangle(x1, y1, x3, y3, x4, y4);
    }

    public void ret(float x, float y, float w, float h, Color c) {
        sr.setColor(c);
        sr.rect(x, y, w, h);
    }

    public void circulo(float x, float y, float r, Color c) {
        sr.setColor(c);
        sr.circle(x, y, r, Math.max(10, (int) (r * 1.2f)));
    }

    public void elipse(float x, float y, float w, float h, Color c) {
        sr.setColor(c);
        sr.ellipse(x - w / 2f, y - h / 2f, w, h, 18);
    }

    public void linha(float x1, float y1, float x2, float y2, float esp, Color c) {
        sr.setColor(c);
        sr.rectLine(x1, y1, x2, y2, esp);
    }

    /** Linha de pontas arredondadas, usada nos membros dos personagens. */
    public void membro(float x1, float y1, float x2, float y2, float esp, Color c) {
        sr.setColor(c);
        sr.rectLine(x1, y1, x2, y2, esp);
        int seg = 10;
        sr.circle(x1, y1, esp / 2f, seg);
        sr.circle(x2, y2, esp / 2f, seg);
    }

    public void contornoRet(float x, float y, float w, float h, float esp, Color c) {
        ret(x, y, w, esp, c);
        ret(x, y + h - esp, w, esp, c);
        ret(x, y, esp, h, c);
        ret(x + w - esp, y, esp, h, c);
    }

    /** Painel de HUD com sombra dura, no estilo adesivo. */
    public void painel(float x, float y, float w, float h, Color fundo, Color borda) {
        ret(x + 5, y - 5, w, h, Paleta.TINTA_SOMBRA);
        ret(x, y, w, h, fundo);
        contornoRet(x, y, w, h, 3, borda);
    }

    @Override
    public void dispose() {
        sr.dispose();
    }
}
