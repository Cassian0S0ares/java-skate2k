package com.skate2k.logica;

/**
 * Modelos de robô skatista. Vida é a blindagem; o pisão desconta o dano do nível de estilo.
 * Cada modelo tira uma quantidade própria da vida do jogador, ao encostar e na investida.
 */
public enum TipoRobo {
    ROLIMA("ROLIMÃ-3000", 1, 3.0f, 100, 0.42f, 1.15f, 1, 1),
    FAISCA("FAÍSCA", 2, 3.9f, 200, 0.42f, 1.25f, 1, 2),
    TANQUE("TANQUE DE GUERRA", 4, 2.8f, 400, 0.60f, 1.55f, 2, 2),
    SINDICO("SÍNDICO-9000", 8, 3.3f, 1500, 0.80f, 2.0f, 2, 3),
    CHARLIETRON("CHARLIETRON", 12, 3.7f, 3000, 0.7f, 1.7f, 1, 2);

    public final String nome;
    public final int vida;
    public final float velocidade;
    public final int valor;
    public final float raio;
    public final float altura;
    public final int danoToque;
    public final int danoInvestida;

    TipoRobo(String nome, int vida, float velocidade, int valor, float raio, float altura,
             int danoToque, int danoInvestida) {
        this.nome = nome;
        this.vida = vida;
        this.velocidade = velocidade;
        this.valor = valor;
        this.raio = raio;
        this.altura = altura;
        this.danoToque = danoToque;
        this.danoInvestida = danoInvestida;
    }
}
