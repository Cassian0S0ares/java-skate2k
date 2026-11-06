package com.skate2k.render;

import com.badlogic.gdx.graphics.Color;

/**
 * Fonte 5x7 embutida no código, sem arquivo de fonte e com acentos do português. Cada glifo tem
 * 5 colunas; em cada coluna, o bit 0 é a linha de cima.
 */
public final class FontePixel {
    /** Recebe cada pixel aceso; permite desenhar texto também nas faces isométricas dos prédios. */
    public interface Pixel {
        void acender(int coluna, int linha);
    }

    public static final int LARGURA_CELULA = 6;
    public static final int ALTURA_LINHA = 7;

    private static final int[][] GLIFOS = new int[128][];

    static {
        g('A', 0x7C, 0x12, 0x11, 0x12, 0x7C); g('B', 0x7F, 0x49, 0x49, 0x49, 0x36);
        g('C', 0x3E, 0x41, 0x41, 0x41, 0x22); g('D', 0x7F, 0x41, 0x41, 0x41, 0x3E);
        g('E', 0x7F, 0x49, 0x49, 0x49, 0x41); g('F', 0x7F, 0x09, 0x09, 0x09, 0x01);
        g('G', 0x3E, 0x41, 0x49, 0x49, 0x7A); g('H', 0x7F, 0x08, 0x08, 0x08, 0x7F);
        g('I', 0x00, 0x41, 0x7F, 0x41, 0x00); g('J', 0x20, 0x40, 0x41, 0x3F, 0x01);
        g('K', 0x7F, 0x08, 0x14, 0x22, 0x41); g('L', 0x7F, 0x40, 0x40, 0x40, 0x40);
        g('M', 0x7F, 0x02, 0x0C, 0x02, 0x7F); g('N', 0x7F, 0x04, 0x08, 0x10, 0x7F);
        g('O', 0x3E, 0x41, 0x41, 0x41, 0x3E); g('P', 0x7F, 0x09, 0x09, 0x09, 0x06);
        g('Q', 0x3E, 0x41, 0x51, 0x21, 0x5E); g('R', 0x7F, 0x09, 0x19, 0x29, 0x46);
        g('S', 0x26, 0x49, 0x49, 0x49, 0x32); g('T', 0x01, 0x01, 0x7F, 0x01, 0x01);
        g('U', 0x3F, 0x40, 0x40, 0x40, 0x3F); g('V', 0x1F, 0x20, 0x40, 0x20, 0x1F);
        g('W', 0x3F, 0x40, 0x38, 0x40, 0x3F); g('X', 0x63, 0x14, 0x08, 0x14, 0x63);
        g('Y', 0x07, 0x08, 0x70, 0x08, 0x07); g('Z', 0x61, 0x51, 0x49, 0x45, 0x43);
        g('0', 0x3E, 0x51, 0x49, 0x45, 0x3E); g('1', 0x00, 0x42, 0x7F, 0x40, 0x00);
        g('2', 0x62, 0x51, 0x49, 0x49, 0x46); g('3', 0x22, 0x41, 0x49, 0x49, 0x36);
        g('4', 0x18, 0x14, 0x12, 0x7F, 0x10); g('5', 0x27, 0x45, 0x45, 0x45, 0x39);
        g('6', 0x3C, 0x4A, 0x49, 0x49, 0x30); g('7', 0x01, 0x71, 0x09, 0x05, 0x03);
        g('8', 0x36, 0x49, 0x49, 0x49, 0x36); g('9', 0x06, 0x49, 0x49, 0x29, 0x1E);
        g('!', 0x00, 0x00, 0x5F, 0x00, 0x00); g('.', 0x00, 0x60, 0x60, 0x00, 0x00);
        g(',', 0x00, 0x50, 0x30, 0x00, 0x00); g(':', 0x00, 0x36, 0x36, 0x00, 0x00);
        g(';', 0x00, 0x56, 0x36, 0x00, 0x00); g('-', 0x08, 0x08, 0x08, 0x08, 0x08);
        g('+', 0x08, 0x08, 0x3E, 0x08, 0x08); g('/', 0x20, 0x10, 0x08, 0x04, 0x02);
        g('?', 0x02, 0x01, 0x51, 0x09, 0x06); g('\'', 0x00, 0x00, 0x07, 0x00, 0x00);
        g('%', 0x23, 0x13, 0x08, 0x64, 0x62); g('(', 0x00, 0x1C, 0x22, 0x41, 0x00);
        g(')', 0x00, 0x41, 0x22, 0x1C, 0x00); g('=', 0x14, 0x14, 0x14, 0x14, 0x14);
        g('>', 0x41, 0x22, 0x14, 0x08, 0x00); g('<', 0x00, 0x08, 0x14, 0x22, 0x41);
        g('#', 0x14, 0x7F, 0x14, 0x7F, 0x14); g('"', 0x00, 0x07, 0x00, 0x07, 0x00);
        g('_', 0x40, 0x40, 0x40, 0x40, 0x40); g('*', 0x14, 0x08, 0x3E, 0x08, 0x14);
    }

    private static void g(char c, int a, int b, int d, int e, int f) {
        GLIFOS[c] = new int[] {a, b, d, e, f};
    }

    private FontePixel() {}

    /** Remove o acento e devolve a letra base (sempre maiúscula). */
    private static char base(char c) {
        switch (Character.toUpperCase(c)) {
            case 'Á': case 'À': case 'Ã': case 'Â': return 'A';
            case 'É': case 'Ê': return 'E';
            case 'Í': return 'I';
            case 'Ó': case 'Õ': case 'Ô': return 'O';
            case 'Ú': case 'Ü': return 'U';
            case 'Ç': return 'C';
            default: return Character.toUpperCase(c);
        }
    }

    /** Emite os pixels de um texto. Acentos usam as linhas -2 e -1; a cedilha, 7 e 8. */
    public static void varrer(String texto, Pixel saida) {
        int cursor = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            char b = base(c);
            int[] glifo = b < 128 ? GLIFOS[b] : null;
            if (glifo != null) {
                for (int col = 0; col < 5; col++) {
                    for (int lin = 0; lin < 7; lin++) {
                        if ((glifo[col] >> lin & 1) != 0) saida.acender(cursor + col, lin);
                    }
                }
                acento(Character.toUpperCase(c), cursor, saida);
            }
            cursor += LARGURA_CELULA;
        }
    }

    private static void acento(char c, int x, Pixel s) {
        switch (c) {
            case 'Á': case 'É': case 'Í': case 'Ó': case 'Ú':
                s.acender(x + 3, -2); s.acender(x + 2, -1);
                break;
            case 'À':
                s.acender(x + 1, -2); s.acender(x + 2, -1);
                break;
            case 'Ã': case 'Õ':
                s.acender(x + 1, -2); s.acender(x + 2, -1); s.acender(x + 3, -2); s.acender(x + 4, -1);
                s.acender(x, -1);
                break;
            case 'Â': case 'Ê': case 'Ô':
                s.acender(x + 2, -2); s.acender(x + 1, -1); s.acender(x + 3, -1);
                break;
            case 'Ü':
                s.acender(x + 1, -1); s.acender(x + 3, -1);
                break;
            case 'Ç':
                s.acender(x + 2, 7); s.acender(x + 3, 8); s.acender(x + 2, 8);
                break;
            default:
                break;
        }
    }

    public static float largura(String texto, float escala) {
        return Math.max(0, texto.length() * LARGURA_CELULA - 1) * escala;
    }

    /** Desenha com a base das letras na altura y. */
    public static void texto(Desenho d, String texto, float x, float y, float escala, Color cor) {
        varrer(texto, (col, lin) -> d.ret(x + col * escala, y + (6 - lin) * escala, escala + 0.2f, escala + 0.2f, cor));
    }

    /** Texto com sombra dura. */
    public static void comSombra(Desenho d, String t, float x, float y, float escala, Color cor) {
        float s = Math.max(1.5f, escala * 0.6f);
        texto(d, t, x + s, y - s, escala, Paleta.TINTA);
        texto(d, t, x, y, escala, cor);
    }

    public static void centro(Desenho d, String t, float cx, float y, float escala, Color cor) {
        comSombra(d, t, cx - largura(t, escala) / 2f, y, escala, cor);
    }

    public static void direita(Desenho d, String t, float xFim, float y, float escala, Color cor) {
        comSombra(d, t, xFim - largura(t, escala), y, escala, cor);
    }

    /** Título grande: contorno grosso, sombra e degradê vertical. */
    public static void titulo(Desenho d, String t, float cx, float y, float escala, Color cima, Color baixo) {
        float x = cx - largura(t, escala) / 2f;
        float c = escala * 0.45f;
        varrer(t, (col, lin) -> d.ret(x + col * escala - c + escala * 0.5f, y + (6 - lin) * escala - c - escala * 0.7f,
                escala + c * 2, escala + c * 2, Paleta.TINTA));
        Color tmp = new Color();
        varrer(t, (col, lin) -> {
            float f = Math.max(0f, Math.min(1f, lin / 6f));
            tmp.set(cima).lerp(baixo, f);
            d.ret(x + col * escala, y + (6 - lin) * escala, escala + 0.3f, escala + 0.3f, tmp);
        });
    }

    /** Quebra um texto em linhas de até "colunas" caracteres. */
    public static java.util.List<String> quebrar(String texto, int colunas) {
        java.util.List<String> linhas = new java.util.ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            if (atual.length() > 0 && atual.length() + 1 + palavra.length() > colunas) {
                linhas.add(atual.toString());
                atual.setLength(0);
            }
            if (atual.length() > 0) atual.append(' ');
            atual.append(palavra);
        }
        if (atual.length() > 0) linhas.add(atual.toString());
        return linhas;
    }
}
