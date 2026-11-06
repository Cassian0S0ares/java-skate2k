package com.skate2k.persistencia;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.skate2k.logica.RepositorioRecorde;

/** Guarda o recorde nas Preferences do libGDX, em ~/.prefs/skate2k no desktop. */
public final class RecordePreferences implements RepositorioRecorde {
    private static final String CHAVE = "recorde";
    private final Preferences preferencias = Gdx.app.getPreferences("skate2k");

    @Override
    public int carregar() {
        return preferencias.getInteger(CHAVE, 0);
    }

    @Override
    public void salvar(int pontos) {
        preferencias.putInteger(CHAVE, pontos);
        preferencias.flush();
    }
}
