package com.skate2k.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

/** Folhas 8 × 4: recortes próprios, carregamento sob demanda e escala constante por folha. */
final class FolhaCharlietron implements Disposable {
    private final Map<String, Texture> texturas = new HashMap<>();
    private final Map<String, TextureRegion[][]> folhas = new HashMap<>();

    TextureRegion quadro(String nome, int direcao, int indice) {
        if (!folhas.containsKey(nome)) carregar(nome);
        return folhas.get(nome)[direcao][indice];
    }

    private void carregar(String nome) {
        Pixmap fonte = new Pixmap(Gdx.files.internal("charlietron/" + nome + ".png"));
        Pixmap atlas = new Pixmap(8 * 256, 4 * 272, Pixmap.Format.RGBA8888);
        atlas.setBlending(Pixmap.Blending.None);
        JsonValue frames = new JsonReader().parse(Gdx.files.internal("charlietron/" + nome + ".json")).get("frames");
        int[][] retangulos = new int[32][];
        try {
            for (int i = 0; i < 32; i++) {
                JsonValue frame = frames.get(i);
                int[] r = frame.get("rect").asIntArray();
                int w = r[2], h = r[3];
                if (w > 252 || h > 268) throw new IllegalStateException("Pose grande demais: " + nome + i);
                int[] pixels = new int[w * h];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
                    pixels[y * w + x] = fonte.getPixel(r[0] + x, r[1] + y);
                JsonValue origens = frame.get("seeds");
                int[] sementes = new int[origens.size];
                for (int j = 0; j < sementes.length; j++) {
                    int[] xy = origens.get(j).asIntArray();
                    sementes[j] = xy[1] * w + xy[0];
                }
                boolean[] mascara = RecorteSprite.mascara(pixels, w, h, sementes);
                int ax = i % 8 * 256 + 2, ay = i / 8 * 272 + 2;
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
                    if (mascara[y * w + x]) atlas.drawPixel(ax + x, ay + y, pixels[y * w + x]);
                retangulos[i] = new int[]{ax, ay, w, h};
            }
            Texture textura = new Texture(atlas);
            textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            TextureRegion[][] quadros = new TextureRegion[4][8];
            for (int i = 0; i < 32; i++) {
                int[] r = retangulos[i];
                quadros[i / 8][i % 8] = new TextureRegion(textura, r[0], r[1], r[2], r[3]);
            }
            texturas.put(nome, textura);
            folhas.put(nome, quadros);
        } finally { fonte.dispose(); atlas.dispose(); }
    }

    float escala(String nome) { return 110f / 256f; }

    @Override public void dispose() { for (Texture t : texturas.values()) t.dispose(); }
}
