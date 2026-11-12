package com.skate2k.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Sprites do skatista: atlas isolado de movimento, poses de manobras e queda.
 */
final class FolhaPersonagem implements Disposable {
    static final int ANDANDO = 1;
    static final int RAPIDO = 2;
    static final float ESCALA = 0.62f;

    static final class Quadro {
        final TextureRegion regiao;
        final float escala;
        Quadro(TextureRegion regiao, float escala) {
            this.regiao = regiao;
            this.escala = escala;
        }
    }

    private final Map<String, Texture> texturas = new HashMap<>();
    private final Map<String, Quadro[]> manobras = new HashMap<>();
    private final JsonValue catalogo = new JsonReader().parse(Gdx.files.internal("manobras/manifest.json")).get("sheets");
    private final Texture textura;
    private final TextureRegion[][][] quadros = new TextureRegion[4][3][4];

    FolhaPersonagem() {
        textura = AtlasMovimento.carregar();
        for (int linha = 0; linha < 4; linha++) {
            for (int grupo = ANDANDO; grupo <= RAPIDO; grupo++) {
                for (int q = 0; q < 4; q++) {
                    quadros[linha][grupo][q] = new TextureRegion(textura,
                            ((grupo - 1) * 4 + q) * AtlasMovimento.LARGURA,
                            linha * AtlasMovimento.ALTURA, AtlasMovimento.LARGURA, AtlasMovimento.ALTURA);
                }
            }
        }
    }

    /** Descarta margens transparentes para manter os pés ancorados entre quadros. */
    private static TextureRegion recortar(Texture textura, Pixmap pixels, int x, int y, int w, int h) {
        int minX = x + w, minY = y + h, maxX = x, maxY = y;
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if ((pixels.getPixel(px, py) & 255) > 128) {
                    minX = Math.min(minX, px); minY = Math.min(minY, py);
                    maxX = Math.max(maxX, px); maxY = Math.max(maxY, py);
                }
            }
        }
        if (minX > maxX) return new TextureRegion(textura, x, y, w, h);
        minX = Math.max(x, minX - 2); minY = Math.max(y, minY - 2);
        maxX = Math.min(x + w - 1, maxX + 2); maxY = Math.min(y + h - 1, maxY + 2);
        return new TextureRegion(textura, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    Quadro manobra(String nome, int linha, int indice) {
        if (!manobras.containsKey(nome)) {
            JsonValue dados = catalogo.get(nome);
            Texture t = new Texture(Gdx.files.internal("manobras/" + dados.getString("image")));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturas.put(nome, t);
            JsonValue frames = dados.get("frames");
            Quadro[] lista = new Quadro[frames.size];
            for (int i = 0; i < lista.length; i++) {
                int[] r = frames.get(i).get("rect").asIntArray();
                lista[i] = new Quadro(new TextureRegion(t, r[0], r[1], r[2], r[3]),
                        dados.getFloat("previewScale") * 0.43f);
            }
            manobras.put(nome, lista);
        }
        return manobras.get(nome)[linha * 8 + indice];
    }

    Quadro caido(int direcao) {
        if (!manobras.containsKey("caido")) {
            String arquivo = "personagem/caido.png";
            Texture t = new Texture(Gdx.files.internal(arquivo));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            texturas.put("caido", t);
            Pixmap pixels = new Pixmap(Gdx.files.internal(arquivo));
            Quadro[] lista = new Quadro[4];
            for (int i = 0; i < 4; i++) {
                int x = (i % 2) * t.getWidth() / 2, y = (i / 2) * t.getHeight() / 2;
                TextureRegion r = recortar(t, pixels, x, y, t.getWidth() / 2, t.getHeight() / 2);
                lista[i] = new Quadro(r, 100f / Math.max(r.getRegionWidth(), r.getRegionHeight()));
            }
            pixels.dispose();
            manobras.put("caido", lista);
        }
        return manobras.get("caido")[direcao];
    }

    /** As poses de deslocamento olham para onde o personagem anda, inclusive após um 180. */
    static float rumoDoSprite(float rumo, float vx, float vy, boolean noAr, float giroGraus) {
        float movimento = vx * vx + vy * vy >= 0.25f * 0.25f
                ? (float) Math.atan2(vy, vx) : rumo;
        return movimento + (noAr ? (float) Math.toRadians(giroGraus) : 0f);
    }

    /** Linha da folha para um rumo no mapa: SE=0, SW=1, NW=2, NE=3 (direções na tela). */
    static int linhaDoRumo(float rumo) {
        float tela = (float) Math.toDegrees(rumo) + 45f; // ângulo na tela, y para cima
        tela = ((tela + 180f) % 360f + 360f) % 360f - 180f;
        if (tela < -90f) return 1;
        if (tela < 0f) return 0;
        if (tela < 90f) return 3;
        return 2;
    }

    TextureRegion quadro(int linha, int grupo, int indice) {
        return quadros[linha][grupo][indice & 3];
    }

    @Override
    public void dispose() {
        for (Texture t : texturas.values()) t.dispose();
        textura.dispose();
    }
}
