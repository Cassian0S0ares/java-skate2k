package com.skate2k.logica;

/** Tipos de piso. Grama e terra freiam o skate. */
public enum TipoPiso {
    ASFALTO(1f), FAIXA(1f), FAIXA_PEDESTRE(1f), CALCADA(1f), CONCRETO(1.05f),
    GRAMA(0.62f), TERRA(0.8f), PISO_PARQUE(1f), PINTADO(1.05f);

    public final float fatorVelocidade;

    TipoPiso(float fatorVelocidade) {
        this.fatorVelocidade = fatorVelocidade;
    }
}
