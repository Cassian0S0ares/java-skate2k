package com.skate2k.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Monta uma grade limpa em memória a partir dos recortes medidos da arte original. */
final class AtlasMovimento {
    static final int LARGURA = 192;
    static final int ALTURA = 176;
    static final int MARGEM_BASE = 8;

    private AtlasMovimento() {}

    static Texture carregar() {
        JsonValue dados = new JsonReader().parse(Gdx.files.internal("personagem/movimento.json"));
        Pixmap fonte = new Pixmap(Gdx.files.internal(dados.getString("source")));
        Pixmap atlas = new Pixmap(8 * LARGURA, 4 * ALTURA, Pixmap.Format.RGBA8888);
        atlas.setBlending(Pixmap.Blending.None);
        try {
            for (int i = 0; i < 32; i++) {
                int[] r = dados.get("rects").get(i).asIntArray();
                int w = r[2], h = r[3];
                int[] pixels = new int[w * h];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
                    pixels[y * w + x] = fonte.getPixel(r[0] + x, r[1] + y);
                boolean[] mascara = RecorteSprite.mascara(pixels, w, h);
                int base = 0;
                for (int p = 0; p < pixels.length; p++)
                    if (mascara[p] && (pixels[p] & 255) > 128) base = Math.max(base, p / w);
                int esquerda = w, direita = 0;
                for (int y = Math.max(0, base - 10); y <= base; y++) for (int x = 0; x < w; x++) {
                    if (mascara[y * w + x] && (pixels[y * w + x] & 255) > 128) {
                        esquerda = Math.min(esquerda, x); direita = Math.max(direita, x);
                    }
                }
                int offsetX = LARGURA / 2 - (esquerda + direita) / 2;
                int offsetY = ALTURA - MARGEM_BASE - 1 - base;
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                    if (!mascara[y * w + x]) continue;
                    int ax = offsetX + x, ay = offsetY + y;
                    if (ax < 0 || ax >= LARGURA || ay < 0 || ay >= ALTURA)
                        throw new IllegalStateException("Pose fora da célula: " + i);
                    atlas.drawPixel((i % 8) * LARGURA + ax, (i / 8) * ALTURA + ay, pixels[y * w + x]);
                }
            }
            Texture textura = new Texture(atlas);
            textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return textura;
        } finally {
            fonte.dispose();
            atlas.dispose();
        }
    }
}
