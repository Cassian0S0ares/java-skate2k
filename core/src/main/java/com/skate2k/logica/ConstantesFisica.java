package com.skate2k.logica;

/**
 * Todas as constantes de física e de regras do jogo em um só lugar, para facilitar ajustes.
 * Unidades: distância em "tiles" do mapa, tempo em segundos, ângulos em graus (salvo indicação).
 */
public final class ConstantesFisica {
    private ConstantesFisica() {}

    // --- Gravidade e pulo ---
    public static final float GRAVIDADE = 18f;
    public static final float FORCA_PULO = 6.4f;
    public static final float FORCA_PULO_MAX = 9.2f;
    /** Saltinho que acompanha uma manobra feita no chão (kickflip/impossible sem pulo). */
    public static final float FORCA_PULO_BAIXO = 5.0f;
    public static final float TEMPO_CARGA_PULO = 0.40f;
    public static final float BONUS_TENIS_MOLA = 1.3f;
    public static final float QUIQUE_PISAO = 8.2f;
    public static final float LANCAMENTO_FIM_GRIND = 2.8f;
    /** Multiplica a velocidade vertical obtida na inclinação da rampa ao sair pela borda. */
    public static final float CHUTE_RAMPA = 2.1f;

    // --- Movimento no chão ---
    public static final float VELOCIDADE_MAX = 6.6f;
    public static final float BONUS_VELOCIDADE_NIVEL = 0.28f;
    public static final float BONUS_VELOCIDADE_CAFE = 2.4f;
    public static final float ACELERACAO = 15f;
    public static final float ATRITO = 1.2f;
    public static final float FREIO = 15f;
    public static final float GIRO_CHAO_RAD = 6.0f;
    public static final float ADERENCIA = 11f;
    public static final float ATRITO_ESCADA = 2.0f;
    public static final float CONTROLE_AR = 7f;

    // --- Colisão com o cenário ---
    /** Diferença de altura que o skate vence sem bater. */
    public static final float DEGRAU = 0.30f;
    /** Queda de altura a partir da qual o jogador sai do chão. */
    public static final float QUEDA_VIRA_VOO = 0.35f;
    public static final float RAIO_COLISAO = 0.22f;
    /** Velocidade numa batida de frente que derruba o jogador. */
    public static final float VELOCIDADE_BATIDA = 5.6f;

    // --- Manobras: velocidades de rotação (graus/s) ---
    public static final float VELOCIDADE_KICKFLIP = 950f;
    public static final float VELOCIDADE_IMPOSSIBLE = 760f;
    public static final float VELOCIDADE_GIRO = 620f;

    // --- Manobras: tolerância no pouso (graus) ---
    public static final float TOLERANCIA_FLIP = 65f;
    public static final float TOLERANCIA_GIRO = 42f;
    public static final float TOLERANCIA_POUSO_LIMPO = 14f;
    public static final float TEMPO_VOO_LONGO = 1.45f;

    // --- Grind ---
    public static final float RAIO_ENCAIXE_GRIND = 0.60f;
    public static final float VELOCIDADE_MIN_GRIND = 4.2f;
    public static final float ANGULO_BOARDSLIDE_RAD = 0.75f;
    public static final float DERIVA_EQUILIBRIO = 1.15f;
    public static final float DERIVA_POR_SEGUNDO = 0.30f;
    public static final float FORCA_EQUILIBRIO = 5.5f;
    public static final float AMORTECE_EQUILIBRIO = 1.2f;
    public static final float ESPERA_REENCAIXE = 0.30f;
    /** Restrição SEM_PULO: distância máxima de um trilho para ainda poder pular. */
    public static final float ALCANCE_PULO_SEM_PULO = 2.8f;

    // --- Queda ---
    public static final float TEMPO_CAIDO = 1.15f;
    public static final float TEMPO_INVULNERAVEL = 1.7f;

    // --- Robôs ---
    public static final float RAIO_PISAO_EXTRA = 0.85f;
    public static final float IMA_PISAO = 5.5f;
    public static final float ALCANCE_IMA_PISAO = 2.6f;
    public static final float TEMPO_PREPARO_INVESTIDA = 0.70f;
    public static final float TEMPO_INVESTIDA = 0.95f;
    public static final float MULTIPLICADOR_INVESTIDA = 2.2f;
    public static final float TEMPO_ATORDOADO = 1.6f;
    public static final float ESPERA_RENASCER = 3.5f;
    /** Acima desta altura o jogador já passa por cima do robô sem encostar. */
    public static final float ALTURA_PASSA_POR_CIMA = 0.30f;
    public static final float FRACAO_ROUBO_CARGA = 0.30f;
    public static final float FORCA_EMPURRAO = 5f;
    public static final float INVULNERAVEL_EMPURRAO = 2.2f;
    /** Folga somada ao raio do robô para contar um toque. */
    public static final float RAIO_TOQUE_EXTRA = 0.2f;
    /** Quantos pontos de vida o jogador tem em cada fase. */
    public static final int VIDA_MAXIMA = 3;

    // --- Pontuação ---
    public static final int MULTIPLICADOR_MAXIMO = 16;
    public static final float BONUS_POUSO_LIMPO = 1.25f;
    public static final int CARGA_DANO_EXTRA = 600;
    public static final int XP_POR_DIFICULDADE = 25;

    // --- Câmera ---
    public static final float CAMERA_SEGUIR = 5.5f;
    public static final float CAMERA_ZONA_MORTA = 2.4f;
    public static final float CAMERA_REENQUADRA = 0.4f;
    public static final float CAMERA_VELOCIDADE_DOLLY = 8.5f;
    public static final float CAMERA_LIMIAR_MOVIMENTO = 0.6f;
}
