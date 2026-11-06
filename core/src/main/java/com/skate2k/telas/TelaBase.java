package com.skate2k.telas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.skate2k.SkateGame;
import com.skate2k.render.Desenho;

/** Base das telas: viewport 2D para interface, com altura de referência de 720. */
abstract class TelaBase extends ScreenAdapter {
    protected static final float LARGURA_BASE = 1280f;
    protected static final float ALTURA_BASE = 720f;

    protected final SkateGame jogo;
    protected final Desenho d;
    protected final OrthographicCamera cameraUi = new OrthographicCamera();
    protected final ExtendViewport viewportUi = new ExtendViewport(LARGURA_BASE, ALTURA_BASE, cameraUi);
    protected float tempo;

    protected TelaBase(SkateGame jogo) {
        this.jogo = jogo;
        this.d = jogo.desenho;
    }

    @Override
    public void resize(int largura, int altura) {
        viewportUi.update(largura, altura, true);
    }

    protected float larguraUi() {
        return viewportUi.getWorldWidth();
    }

    protected float alturaUi() {
        return viewportUi.getWorldHeight();
    }
}
