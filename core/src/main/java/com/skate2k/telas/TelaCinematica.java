package com.skate2k.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.skate2k.SkateGame;
import com.skate2k.render.FontePixel;
import com.skate2k.render.Paleta;

/** Sequência de quadros estáticos: ENTER/ESPAÇO avança, ESC pula tudo. */
public class TelaCinematica extends TelaBase {
    public static final String[] ABERTURA = {
        "cinematicas/01-fabrica.png",
        "cinematicas/01b-linha-de-montagem.png",
        "cinematicas/02-robos.png",
        "cinematicas/02b-bairro-ocupado.png",
        "cinematicas/03-outra-ideia-sem-texto.png"};
    public static final String[] PRE_FASE4 = {
        "cinematicas/pre-fase4/01-ceo-e-assistente.png",
        "cinematicas/pre-fase4/02-plano-de-contingencia.png",
        "cinematicas/pre-fase4/03-construcao-charlietron.png",
        "cinematicas/pre-fase4/04-skate-e-montagem-final.png",
        "cinematicas/pre-fase4/05-charlietron-ativado.png"};
    public static final String[] FINAL = {
        "cinematicas/final/legendado/01-charlie-tron.png",
        "cinematicas/final/legendado/02-cidade-liberta.png",
        "cinematicas/final/legendado/03-ceo-preso.png",
        "cinematicas/final/legendado/04-empresa-destruida.png",
        "cinematicas/final/legendado/05-role-da-vitoria.png"};
    private static final float FADE = 0.6f;

    private final TextureRegion[] quadros;
    private final Texture[] texturas;
    private final Runnable aoTerminar;
    private int atual;
    private float tempoQuadro;
    private boolean concluida;

    /** musica: nome da trilha em musicas/, ou null para manter a que já está tocando. */
    public TelaCinematica(SkateGame jogo, String[] arquivos, String musica, Runnable aoTerminar) {
        super(jogo);
        if (musica != null) jogo.audio.tocarMusica(musica);
        this.aoTerminar = aoTerminar;
        texturas = new Texture[arquivos.length];
        quadros = new TextureRegion[arquivos.length];
        for (int i = 0; i < arquivos.length; i++) {
            texturas[i] = new Texture(Gdx.files.internal(arquivos[i]));
            texturas[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            quadros[i] = new TextureRegion(texturas[i]);
        }
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        tempo += dt;
        tempoQuadro += dt;
        if (Gdx.input.isKeyJustPressed(Keys.M)) jogo.audio.alternarMudo();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) { terminar(); return; }
        if (tempoQuadro > 0.4f && (Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Keys.SPACE))) {
            if (++atual >= quadros.length) { terminar(); return; }
            tempoQuadro = 0;
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cameraUi.update();
        d.iniciar(cameraUi.combined);
        float w = larguraUi(), h = alturaUi();
        TextureRegion q = quadros[atual];
        float escala = Math.min(w / q.getRegionWidth(), h / q.getRegionHeight());
        float lw = q.getRegionWidth() * escala, lh = q.getRegionHeight() * escala;
        float brilho = Math.min(1f, tempoQuadro / FADE);
        d.sprite(q, (w - lw) / 2f, (h - lh) / 2f, lw, lh, new Color(brilho, brilho, brilho, 1f));
        if ((int) (tempo * 2.5f) % 2 == 0) {
            FontePixel.direita(d, atual == quadros.length - 1 ? "ENTER PARA JOGAR" : "ENTER PARA CONTINUAR",
                    w - 16, 12, 2f, Paleta.BRANCO);
        }
        FontePixel.direita(d, "ESC PULA", w - 16, 36, 1.5f, Paleta.CINZA);
        d.terminar();
    }

    private void terminar() {
        if (concluida) return;
        concluida = true;
        aoTerminar.run();
    }

    @Override
    public void dispose() {
        for (Texture t : texturas) t.dispose();
    }
}
