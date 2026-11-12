package com.skate2k.logica;

import java.util.Random;

/**
 * Restrição sorteada que muda as regras do jogo. O sorteio oficial usa uma semente fixa, por isso
 * o resultado documentado no README é sempre o mesmo; no menu, a tecla R sorteia outra.
 */
public enum Restricao {
    SEM_REGRA("SEM REGRA", "Jogo livre, sem restrição. Aperte R no menu para sortear uma regra.",
            1f, 1f, 1f, 1f),
    CHUVA("CHUVA NA PISTA", "A chuva reduz a aderência: o skate escorrega e o pouso exige mais precisão.",
            0.30f, 0.65f, 1.45f, 0.7f),
    CAMERA_EM_MOVIMENTO("SÓ VALE SE FILMAR", "Só dá para manobrar com a câmera em movimento. Câmera parada, ninguém viu!",
            1f, 1f, 1f, 1f),
    SEM_PULO("SEM PULO", "Sem ollie livre: só dá para pular perto de trilhos. No ar, só valem as manobras nos trilhos.",
            1f, 1f, 1f, 0.6f),
    DUELO("BATALHA DE ESTILO", "Supere o estilo do Charlietron e zere sua vida. Pule livremente e desvie da mira!",
            1f, 1f, 1f, 1f);

    private static final Restricao[] SORTEAVEIS = {CHUVA, CAMERA_EM_MOVIMENTO, SEM_PULO};

    /** Semente oficial do sorteio ("2k" do nome do jogo). */
    public static final long SEMENTE_OFICIAL = 2000L;

    public final String titulo;
    public final String descricao;
    /** Multiplica a aderência dos pneus ao chão. */
    public final float fatorAderencia;
    /** Multiplica a tolerância de ângulo no pouso. */
    public final float fatorTolerancia;
    /** Multiplica a perda de equilíbrio no grind. */
    public final float fatorInstabilidade;
    /** Multiplica a meta de pontos: restrição mais dura pede menos pontos. */
    public final float fatorMeta;

    Restricao(String titulo, String descricao, float fatorAderencia, float fatorTolerancia,
              float fatorInstabilidade, float fatorMeta) {
        this.fatorMeta = fatorMeta;
        this.titulo = titulo;
        this.descricao = descricao;
        this.fatorAderencia = fatorAderencia;
        this.fatorTolerancia = fatorTolerancia;
        this.fatorInstabilidade = fatorInstabilidade;
    }

    /** O sorteio oficial documentado no README. */
    public static Restricao sorteioOficial() {
        return sortear(new Random(SEMENTE_OFICIAL));
    }

    public static Restricao sortear(Random aleatorio) {
        Restricao[] todas = SORTEAVEIS;
        return todas[aleatorio.nextInt(todas.length)];
    }

    /** Sorteia uma restrição garantidamente diferente da atual. */
    public static Restricao sortearOutra(Restricao atual, Random aleatorio) {
        Restricao[] todas = SORTEAVEIS;
        if (atual == DUELO || atual == SEM_REGRA) return sortear(aleatorio);
        int posicao = java.util.Arrays.asList(todas).indexOf(atual);
        int indice = (posicao + 1 + aleatorio.nextInt(todas.length - 1)) % todas.length;
        return todas[indice];
    }

    public boolean exigeCameraEmMovimento() {
        return this == CAMERA_EM_MOVIMENTO;
    }

    public boolean permitePuloLivre() {
        return this != SEM_PULO;
    }

    /** Flips, giros e grabs no ar: proibidos quando só valem manobras em corrimão. */
    public boolean permiteManobraAerea() {
        return this != SEM_PULO;
    }
}
