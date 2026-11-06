package com.skate2k.logica;

/** Itens coletáveis. Cada fase oferece uma seleção diferente. */
public enum TipoItem {
    CAFE("CAFÉ DA PADARIA", "VELOCIDADE!"),
    PASTEL("PASTEL DE FEIRA", "+400 DE CARGA"),
    TENIS_MOLA("TÊNIS DE MOLA", "PULO ALTO!"),
    RELOGIO("RELÓGIO DE CAMELÔ", "+10 SEGUNDOS"),
    ADESIVO("ADESIVO RARO", "+2 NO MULTIPLICADOR"),
    CAPACETE("CAPACETE DA MÃE", "PROTEGE DE UM TOMBO"),
    BOLO("BOLO DA VÓ", "+800 DE CARGA"),
    CURATIVO("BAND-AID DA VÓ", "+1 DE VIDA");

    public final String nome;
    public final String efeito;

    TipoItem(String nome, String efeito) {
        this.nome = nome;
        this.efeito = efeito;
    }
}
