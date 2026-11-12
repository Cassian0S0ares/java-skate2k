package com.skate2k.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.skate2k.SkateGame;
import com.skate2k.logica.CatalogoFases;
import com.skate2k.logica.Pontuacao;
import com.skate2k.render.FontePixel;
import com.skate2k.render.Paleta;

/** Resumo entre fases. */
public class TelaResultadoFase extends TelaBase {
    private final int indice;
    private final Pontuacao pontuacao;
    private final int bonus;
    private final int meta;
    private final String proxima;

    public TelaResultadoFase(SkateGame jogo, int indice, Pontuacao pontuacao, int bonus, int meta) {
        super(jogo);
        jogo.audio.tocarMusica("resumo", false);
        this.indice = indice;
        this.pontuacao = pontuacao;
        this.bonus = bonus;
        this.meta = meta;
        this.proxima = CatalogoFases.criar(indice + 1).nome;
    }

    @Override
    public void render(float delta) {
        tempo += Gdx.graphics.getDeltaTime();
        if (tempo > 0.4f && (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE))) {
            jogo.proximaFase(indice, pontuacao);
            return;
        }
        Gdx.gl.glClearColor(0.11f, 0.06f, 0.2f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
        cameraUi.update();
        d.iniciar(cameraUi.combined);
        float w = larguraUi();
        float h = alturaUi();
        TelaGameOver.fundoListrado(d, w, h, tempo, Paleta.ROXO);
        float cx = w / 2f;
        tituloOndulante("FASE CONCLUÍDA!", cx, h - 170, 9f);
        float y = h - 260;
        y = linha("PONTOS NA FASE", String.valueOf(pontuacao.pontosFase()), cx, y, Paleta.BRANCO);
        y = linha("META", String.valueOf(meta), cx, y, Paleta.CINZA);
        y = linha("BÔNUS DA CARGA QUE SOBROU", "+" + bonus, cx, y, Paleta.CIANO);
        y = linha("TOTAL", String.valueOf(pontuacao.pontosTotal()), cx, y, Paleta.AMARELO);
        y = linha("ESTILO", pontuacao.nomeNivel(), cx, y, Paleta.ROSA);
        y -= 36;
        FontePixel.centro(d, "PRÓXIMA: " + proxima, cx, y, 3.5f, Paleta.AMARELO);
        if ((int) (tempo * 2.5f) % 2 == 0) {
            FontePixel.centro(d, "ENTER PARA CONTINUAR", cx, 70, 3f, Paleta.BRANCO);
        }
        d.terminar();
    }

    /** Cada letra balança em onda e o título pulsa de leve. */
    private void tituloOndulante(String texto, float cx, float y, float escala) {
        float celula = FontePixel.largura("AA", 1f) - FontePixel.largura("A", 1f);
        float larguraLetra = FontePixel.largura("A", escala);
        float inicio = cx - FontePixel.largura(texto, escala) / 2f;
        for (int i = 0; i < texto.length(); i++) {
            float onda = (float) Math.sin(tempo * 4f - i * 0.5f);
            float escalaLetra = escala * (1f + 0.04f * onda);
            float centro = inicio + i * celula * escala + larguraLetra / 2f;
            FontePixel.titulo(d, String.valueOf(texto.charAt(i)), centro, y + onda * 10f, escalaLetra,
                    Paleta.LIMA, Paleta.CIANO);
        }
    }

    private float linha(String rotulo, String valor, float cx, float y, com.badlogic.gdx.graphics.Color cor) {
        FontePixel.direita(d, rotulo, cx - 16, y, 3f, Paleta.BRANCO);
        FontePixel.comSombra(d, valor, cx + 16, y, 3f, cor);
        return y - 38;
    }
}
