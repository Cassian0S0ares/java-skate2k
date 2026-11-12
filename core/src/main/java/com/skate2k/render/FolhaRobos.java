package com.skate2k.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.skate2k.logica.TipoRobo;

/** Linhas: Rolimã, Faísca, Tanque, Síndico. Colunas: SE, SW, NW, NE. */
final class FolhaRobos implements Disposable {
    private final Texture textura;
    private final TextureRegion[][] quadros;

    FolhaRobos() {
        textura = new Texture(Gdx.files.internal("inimigos/robos.png"));
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        quadros = new TextureRegion[4][4];
        // Limites proporcionais também aceitam folhas cuja dimensão não é múltipla de quatro.
        for (int linha = 0; linha < 4; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                int x = Math.round(coluna * textura.getWidth() / 4f);
                int y = Math.round(linha * textura.getHeight() / 4f);
                int direita = Math.round((coluna + 1) * textura.getWidth() / 4f);
                int baixo = Math.round((linha + 1) * textura.getHeight() / 4f);
                quadros[linha][coluna] = new TextureRegion(textura, x, y, direita - x, baixo - y);
            }
        }
    }

    TextureRegion quadro(TipoRobo tipo, float rumo) {
        return quadros[tipo.ordinal()][FolhaPersonagem.linhaDoRumo(rumo)];
    }

    @Override
    public void dispose() {
        textura.dispose();
    }
}
