package com.skate2k.logica;

/** Onde o recorde fica guardado. A lógica não conhece o meio de armazenamento. */
public interface RepositorioRecorde {
    int carregar();

    void salvar(int pontos);
}
