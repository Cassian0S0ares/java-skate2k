package com.skate2k.logica;

/** Tipos de elemento do cenário e o potencializador de estilo que cada um concede. */
public enum TipoElemento {
    RAMPA("IMPULSO DE RAMPA", 1.5f),
    ESCADA("GAP DE ESCADA", 2.0f),
    CORRIMAO("TRILHO QUENTE", 1.75f),
    BANCO("BANCO MALANDRO", 1.25f),
    MURETA("MURETA MAROTA", 1.25f),
    PLATAFORMA(null, 1f),
    BLOCO(null, 1f);

    public final String nomePotencializador;
    public final float potencial;

    TipoElemento(String nomePotencializador, float potencial) {
        this.nomePotencializador = nomePotencializador;
        this.potencial = potencial;
    }

    public boolean concedePotencial() {
        return nomePotencializador != null;
    }
}
