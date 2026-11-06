package com.skate2k.logica;

import java.util.ArrayList;
import java.util.List;

/** Dados de uma fase: cenário, itens, inimigos, tempo, meta e dificuldade. */
public final class Fase {
    /** Identidade visual: o renderizador escolhe a paleta a partir daqui. */
    public enum Tema { RUA, PARQUE, SKATEPARK, SUBURBIO }

    public final int numero;
    public final String nome;
    public final String subtitulo;
    public final Tema tema;
    public final Cenario cenario;
    public final float tempoLimite;
    public final int meta;
    public final float fatorVelocidadeRobo;
    public final float intervaloInvestida;
    public final float inicioX;
    public final float inicioY;
    public final List<Item> itens = new ArrayList<>();
    public final List<TipoRobo> robos = new ArrayList<>();
    public final List<float[]> pontosDeEntrada = new ArrayList<>();

    Fase(int numero, String nome, String subtitulo, Tema tema, Cenario cenario, float tempoLimite, int meta,
         float fatorVelocidadeRobo, float intervaloInvestida, float inicioX, float inicioY) {
        this.numero = numero;
        this.nome = nome;
        this.subtitulo = subtitulo;
        this.tema = tema;
        this.cenario = cenario;
        this.tempoLimite = tempoLimite;
        this.meta = meta;
        this.fatorVelocidadeRobo = fatorVelocidadeRobo;
        this.intervaloInvestida = intervaloInvestida;
        this.inicioX = inicioX;
        this.inicioY = inicioY;
    }

    Fase item(TipoItem tipo, float x, float y) {
        itens.add(new Item(tipo, x, y, cenario.alturaEm(x, y) + 0.6f));
        return this;
    }

    Fase robos(TipoRobo tipo, int quantidade) {
        for (int i = 0; i < quantidade; i++) robos.add(tipo);
        return this;
    }

    Fase entradas(float... coordenadas) {
        for (int i = 0; i + 1 < coordenadas.length; i += 2) {
            pontosDeEntrada.add(new float[] {coordenadas[i], coordenadas[i + 1]});
        }
        return this;
    }
}
