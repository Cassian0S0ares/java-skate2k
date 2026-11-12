package com.skate2k.render;

/** Isola a maior silhueta de um recorte RGBA, excluindo pedaços de poses vizinhas. */
final class RecorteSprite {
    private RecorteSprite() {}

    static boolean[] mascara(int[] rgba, int w, int h) {
        return mascara(rgba, w, h, null);
    }

    /** Sementes permitem preservar o skate separado do corpo durante uma manobra aérea. */
    static boolean[] mascara(int[] rgba, int w, int h, int[] sementes) {
        int[] rotulos = new int[rgba.length];
        int[] fila = new int[rgba.length];
        int rotulo = 0, maior = 0, tamanhoMaior = 0;
        for (int inicio = 0; inicio < rgba.length; inicio++) {
            if (rotulos[inicio] != 0 || (rgba[inicio] & 255) <= 128) continue;
            int fim = 1;
            fila[0] = inicio;
            rotulos[inicio] = ++rotulo;
            for (int i = 0; i < fim; i++) {
                int p = fila[i], x = p % w, y = p / w;
                for (int lado = 0; lado < 4; lado++) {
                    int nx = x + (lado == 0 ? -1 : lado == 1 ? 1 : 0);
                    int ny = y + (lado == 2 ? -1 : lado == 3 ? 1 : 0);
                    if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                    int n = ny * w + nx;
                    if (rotulos[n] == 0 && (rgba[n] & 255) > 128) {
                        rotulos[n] = rotulo;
                        fila[fim++] = n;
                    }
                }
            }
            if (fim > tamanhoMaior) { maior = rotulo; tamanhoMaior = fim; }
        }
        boolean[] mascara = new boolean[rgba.length];
        if (maior == 0) return mascara;
        boolean[] escolhido = new boolean[rotulo + 1];
        if (sementes == null) escolhido[maior] = true;
        else for (int s : sementes) {
            if (s >= 0 && s < rotulos.length && rotulos[s] > 0) escolhido[rotulos[s]] = true;
        }
        for (int p = 0; p < rgba.length; p++) {
            if (escolhido[rotulos[p]]) mascara[p] = true;
            else if (rotulos[p] == 0 && (rgba[p] & 255) > 0) {
                // Preserva apenas a borda suavizada da própria silhueta.
                for (int dy = -2; dy <= 2 && !mascara[p]; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        int x = p % w + dx, y = p / w + dy;
                        if (x >= 0 && x < w && y >= 0 && y < h && escolhido[rotulos[y * w + x]]) {
                            mascara[p] = true;
                            break;
                        }
                    }
                }
            }
        }
        return mascara;
    }
}
