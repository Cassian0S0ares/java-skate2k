package com.skate2k.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.skate2k.SkateGame;

/** Abre a janela do jogo. Com --captura DIRETORIO, roda o tour automático de capturas de tela. */
public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Rolê x Robôs");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowSizeLimits(960, 540, -1, -1);
        // Suavização 4x: as formas vetoriais ficam sem serrilhado.
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);

        SkateGame jogo = args.length >= 2 && "--captura".equals(args[0])
                ? new CapturaTelas(args[1], args.length >= 3 ? args[2] : null)
                : new SkateGame();
        new Lwjgl3Application(jogo, config);
    }
}
