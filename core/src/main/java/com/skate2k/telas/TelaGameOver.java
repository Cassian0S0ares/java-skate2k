package com.skate2k.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.skate2k.SkateGame;
import com.skate2k.logica.Pontuacao;
import com.skate2k.render.Desenho;
import com.skate2k.render.FontePixel;
import com.skate2k.render.Paleta;

/** Fim de jogo, tanto na derrota quanto ao vencer as quatro fases. Mostra o recorde salvo. */
public class TelaGameOver extends TelaBase {
    private static final String[] PIADAS_DERROTA = {
        "OS ROBÔS AGORA TÊM O SEU LUGAR NA PADARIA.",
        "SUA MÃE AVISOU SOBRE ESSE SKATE.",
        "ATÉ O POMBO FICOU COM VERGONHA.",
        "O SÍNDICO-9000 MANDOU UMA ADVERTÊNCIA POR ESCRITO."
    };

    private final Pontuacao pontuacao;
    private final int indiceFase;
    private final boolean venceu;
    private final boolean nocaute;
    private final boolean novoRecorde;
    private final int recordeAnterior;
    private final int meta;
    private final String piada;

    public TelaGameOver(SkateGame jogo, Pontuacao pontuacao, int indiceFase, boolean venceu, boolean nocaute,
                        boolean novoRecorde, int recordeAnterior, int meta) {
        super(jogo);
        this.nocaute = nocaute;
        this.pontuacao = pontuacao;
        this.indiceFase = indiceFase;
        this.venceu = venceu;
        jogo.audio.tocarMusica(venceu ? "ending" : "resumo");
        this.novoRecorde = novoRecorde;
        this.recordeAnterior = recordeAnterior;
        this.meta = meta;
        this.piada = venceu ? "O BAIRRO É SEU. OS ROBÔS VIRARAM TORRADEIRA DE NOVO."
                : PIADAS_DERROTA[(pontuacao.pontosTotal() + indiceFase) % PIADAS_DERROTA.length];
    }

    static void fundoListrado(Desenho d, float w, float h, float tempo, Color cor) {
        Color listra = Paleta.copia(cor, 0.18f);
        float largura = 90f;
        float deslocamento = (tempo * 30f) % (largura * 2f);
        for (float x = -h - largura * 2f + deslocamento; x < w + largura; x += largura * 2f) {
            d.quad(x, 0, x + largura, 0, x + largura + h, h, x + h, h, listra);
        }
    }

    @Override
    public void render(float delta) {
        tempo += Gdx.graphics.getDeltaTime();
        if (tempo > 0.4f) {
            if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                jogo.reiniciar();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                jogo.irParaMenu();
                return;
            }
        }

        Gdx.gl.glClearColor(0.11f, 0.05f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cameraUi.update();
        d.iniciar(cameraUi.combined);
        float w = larguraUi();
        float h = alturaUi();
        float cx = w / 2f;
        fundoListrado(d, w, h, tempo, venceu ? Paleta.LIMA : Paleta.VERMELHO);

        float tremor = venceu ? 0f : (float) Math.sin(tempo * 30f) * Math.max(0f, 1f - tempo) * 8f;
        if (venceu) {
            FontePixel.titulo(d, "LENDA DO BAIRRO!", cx, h - 170, 10f, Paleta.AMARELO, Paleta.LIMA);
        } else {
            FontePixel.titulo(d, "GAME OVER", cx + tremor, h - 170, 15f, Paleta.VERMELHO, Paleta.ROSA);
        }
        FontePixel.centro(d, piada, cx, h - 212, 2.5f, Paleta.CIANO);

        float y = h - 290;
        if (!venceu) {
            y = linha("FASE", (indiceFase + 1) + (nocaute ? " - NOCAUTE" : " - SEM TEMPO"), cx, y, Paleta.BRANCO);
            y = linha("PONTOS NA FASE / META", pontuacao.pontosFase() + " / " + meta, cx, y, Paleta.BRANCO);
        }
        y = linha("PONTUAÇÃO FINAL", String.valueOf(pontuacao.pontosTotal()), cx, y, Paleta.AMARELO);
        y = linha("ROBÔS DERROTADOS", String.valueOf(pontuacao.robosDerrotados()), cx, y, Paleta.BRANCO);
        y = linha("MAIOR MULTIPLICADOR", "X" + pontuacao.melhorMultiplicador(), cx, y, Paleta.ROSA);
        y = linha("RECORDE", String.valueOf(Math.max(recordeAnterior, pontuacao.pontosTotal())), cx, y, Paleta.LIMA);

        if (novoRecorde && (int) (tempo * 5f) % 2 == 0) {
            FontePixel.centro(d, "NOVO RECORDE!", cx, y - 26, 6f, Paleta.AMARELO);
        }
        FontePixel.centro(d, "ENTER JOGA DE NOVO - ESC VOLTA AO MENU", cx, 54, 3f, Paleta.BRANCO);
        d.terminar();
    }

    private float linha(String rotulo, String valor, float cx, float y, Color cor) {
        FontePixel.direita(d, rotulo, cx - 16, y, 3f, Paleta.BRANCO);
        FontePixel.comSombra(d, valor, cx + 16, y, 3f, cor);
        return y - 38;
    }
}
