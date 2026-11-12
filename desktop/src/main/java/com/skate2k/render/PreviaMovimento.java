package com.skate2k.render;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

/** Inspeção dos 32 recortes usados pelo jogo, disponível no tour de capturas. */
public final class PreviaMovimento extends ScreenAdapter {
    private final Desenho desenho;
    private final OrthographicCamera camera = new OrthographicCamera(1280, 720);

    public PreviaMovimento(Desenho desenho) {
        this.desenho = desenho;
        camera.position.set(640, 360, 0);
        camera.update();
    }

    @Override public void render(float dt) {
        Gdx.gl.glClearColor(0.22f, 0.26f, 0.32f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        desenho.iniciar(camera.combined);
        String[] direcoes = {"SE", "SW", "NW", "NE"};
        FontePixel.centro(desenho, "MOVIMENTO NORMAL                         MOVIMENTO RAPIDO", 640, 690, 2f, Paleta.BRANCO);
        for (int linha = 0; linha < 4; linha++) {
            float base = 525 - linha * 165;
            FontePixel.texto(desenho, direcoes[linha], 8, base + 55, 2f, Paleta.BRANCO);
            for (int coluna = 0; coluna < 8; coluna++) {
                float x = 70 + coluna * 150;
                desenho.linha(x - 8, base + 6, x + 144, base + 6, 1f, Paleta.CIANO);
                desenho.sprite(desenho.personagem().quadro(linha, coluna < 4 ? 1 : 2, coluna % 4),
                        x, base, AtlasMovimento.LARGURA * 0.75f, AtlasMovimento.ALTURA * 0.75f);
            }
        }
        desenho.terminar();
    }
}
