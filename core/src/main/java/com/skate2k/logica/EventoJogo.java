package com.skate2k.logica;

/** Algo que aconteceu na lógica. Render e áudio consomem estes eventos sem afetar a simulação. */
public final class EventoJogo {
    public enum Tipo {
        PULO, POUSO, POUSO_LIMPO, QUEDA, DANO, ESCUDO_USADO, GRIND_INICIO, GRIND_FIM, ROBO_ATINGIDO, ROBO_DERROTADO,
        ROBO_ALERTA, ROBO_FALA, ROUBO_CARGA, ITEM, SUBIU_NIVEL, POTENCIALIZADOR, META_BATIDA, TIQUE_TEMPO, NEGADO
    }

    public final Tipo tipo;
    public final float x;
    public final float y;
    public final float z;
    public final String texto;
    public final int valor;

    public EventoJogo(Tipo tipo, float x, float y, float z, String texto, int valor) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.z = z;
        this.texto = texto;
        this.valor = valor;
    }
}
