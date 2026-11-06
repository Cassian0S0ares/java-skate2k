package com.skate2k.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.skate2k.logica.EventoJogo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * PLACEHOLDER DE ÁUDIO: efeitos e música são sintetizados por código ao abrir o jogo, gravados em
 * arquivos WAV temporários e tocados pelo libGDX. Para usar sons reais, troque os Efeito.X na
 * carga abaixo por Gdx.audio.newSound(Gdx.files.internal("sons/x.wav")).
 */
public final class Audio implements Disposable {
    public enum Efeito { PULO, POUSO, POUSO_LIMPO, QUEDA, GRIND, PISAO, EXPLOSAO, ITEM, NIVEL, TIQUE, ALERTA, MENU, NEGADO }

    private static final int TAXA = 22050;

    private final Map<Efeito, Sound> sons = new EnumMap<>(Efeito.class);
    private final Random aleatorio = new Random(7);
    private Music musica;
    private boolean mudo;
    private long idGrind = -1;

    public void carregar() {
        try {
            for (Efeito e : Efeito.values()) {
                sons.put(e, Gdx.audio.newSound(Gdx.files.absolute(wavTemporario(gerar(e)).getAbsolutePath())));
            }
            musica = Gdx.audio.newMusic(Gdx.files.absolute(wavTemporario(gerarMusica()).getAbsolutePath()));
            musica.setLooping(true);
            musica.setVolume(0.32f);
            musica.play();
        } catch (Exception | LinkageError erro) {
            // Sem dispositivo de áudio o jogo continua normalmente, em silêncio.
            Gdx.app.error("Audio", "Áudio indisponível: " + erro.getMessage());
        }
    }

    public void tocar(Efeito e) {
        tocar(e, 1f);
    }

    public void tocar(Efeito e, float volume) {
        Sound s = sons.get(e);
        if (s != null && !mudo) s.play(volume * 0.8f);
    }

    public void processar(EventoJogo evento) {
        switch (evento.tipo) {
            case PULO: tocar(Efeito.PULO); break;
            case POUSO: tocar(Efeito.POUSO); break;
            case POUSO_LIMPO: tocar(Efeito.POUSO_LIMPO); break;
            case QUEDA: tocar(Efeito.QUEDA); pararGrind(); break;
            case DANO: tocar(Efeito.PISAO); break;
            case ESCUDO_USADO: tocar(Efeito.PISAO); break;
            case GRIND_INICIO: iniciarGrind(); break;
            case GRIND_FIM: pararGrind(); break;
            case ROBO_ATINGIDO: tocar(Efeito.PISAO); break;
            case ROBO_DERROTADO: tocar(Efeito.EXPLOSAO); break;
            case ROBO_ALERTA: tocar(Efeito.ALERTA, 0.5f); break;
            case ROUBO_CARGA: tocar(Efeito.NEGADO); break;
            case ITEM: tocar(Efeito.ITEM); break;
            case SUBIU_NIVEL: case META_BATIDA: tocar(Efeito.NIVEL); break;
            case TIQUE_TEMPO: tocar(Efeito.TIQUE); break;
            case NEGADO: tocar(Efeito.NEGADO, 0.6f); break;
            default: break;
        }
    }

    private void iniciarGrind() {
        Sound s = sons.get(Efeito.GRIND);
        if (s != null && !mudo && idGrind == -1) idGrind = s.loop(0.45f);
    }

    public void pararGrind() {
        Sound s = sons.get(Efeito.GRIND);
        if (s != null && idGrind != -1) s.stop(idGrind);
        idGrind = -1;
    }

    public void alternarMudo() {
        mudo = !mudo;
        pararGrind();
        if (musica != null) {
            if (mudo) musica.pause();
            else musica.play();
        }
    }

    public boolean mudo() { return mudo; }

    // ------------------------------------------------------------------------------- síntese

    private short[] gerar(Efeito e) {
        switch (e) {
            case PULO: return varredura(0.13f, 170f, 560f, 0.5f, 0.15f);
            case POUSO: return ruidoComGrave(0.14f, 90f, 0.7f);
            case POUSO_LIMPO: return arpejo(new float[] {659f, 880f, 1319f}, 0.07f, 0.5f);
            case QUEDA: return ruidoComGrave(0.5f, 60f, 1f);
            case GRIND: return lixa(0.5f);
            case PISAO: return ruidoComGrave(0.2f, 120f, 1f);
            case EXPLOSAO: return juntar(ruidoComGrave(0.45f, 70f, 1f), arpejo(new float[] {523f, 784f, 1047f, 1568f}, 0.08f, 0.45f));
            case ITEM: return arpejo(new float[] {784f, 1175f}, 0.08f, 0.5f);
            case NIVEL: return arpejo(new float[] {523f, 659f, 784f, 1047f, 1319f}, 0.08f, 0.5f);
            case TIQUE: return varredura(0.06f, 1000f, 1000f, 0.45f, 0f);
            case ALERTA: return arpejo(new float[] {880f, 660f}, 0.09f, 0.5f);
            case MENU: return varredura(0.06f, 500f, 800f, 0.4f, 0f);
            default: return varredura(0.18f, 220f, 110f, 0.5f, 0.05f);
        }
    }

    private short[] varredura(float dur, float f0, float f1, float vol, float ruido) {
        int n = (int) (dur * TAXA);
        short[] s = new short[n];
        float fase = 0;
        for (int i = 0; i < n; i++) {
            float t = i / (float) n;
            fase += (f0 + (f1 - f0) * t) / TAXA;
            float onda = (fase % 1f) < 0.5f ? 1f : -1f;
            float r = (aleatorio.nextFloat() * 2f - 1f) * ruido;
            s[i] = amostra((onda * (1f - ruido) + r) * vol * (1f - t));
        }
        return s;
    }

    private short[] ruidoComGrave(float dur, float grave, float vol) {
        int n = (int) (dur * TAXA);
        short[] s = new short[n];
        float filtro = 0;
        for (int i = 0; i < n; i++) {
            float t = i / (float) n;
            filtro += ((aleatorio.nextFloat() * 2f - 1f) - filtro) * 0.35f;
            float tom = (float) Math.sin(2 * Math.PI * grave * (1f - t * 0.6f) * i / TAXA);
            float env = (1f - t) * (1f - t);
            s[i] = amostra((filtro * 0.65f + tom * 0.6f) * env * vol);
        }
        return s;
    }

    private short[] lixa(float dur) {
        int n = (int) (dur * TAXA);
        short[] s = new short[n];
        float a = 0;
        for (int i = 0; i < n; i++) {
            float ruido = aleatorio.nextFloat() * 2f - 1f;
            a += (ruido - a) * 0.8f;
            float modula = 0.75f + 0.25f * (float) Math.sin(2 * Math.PI * 24 * i / TAXA);
            // Suaviza as pontas para o laço não estalar.
            float borda = Math.min(1f, Math.min(i, n - i) / 300f);
            s[i] = amostra((ruido - a) * 0.55f * modula * borda);
        }
        return s;
    }

    private short[] arpejo(float[] notas, float durNota, float vol) {
        int nNota = (int) (durNota * TAXA);
        short[] s = new short[nNota * notas.length + nNota];
        float fase = 0;
        for (int k = 0; k < s.length; k++) {
            int indice = Math.min(notas.length - 1, k / nNota);
            fase += notas[indice] / TAXA;
            float env = 1f - k / (float) s.length;
            s[k] = amostra(((fase % 1f) < 0.5f ? 1f : -1f) * vol * env);
        }
        return s;
    }

    private short[] juntar(short[] a, short[] b) {
        short[] s = new short[Math.max(a.length, b.length)];
        for (int i = 0; i < s.length; i++) {
            int v = (i < a.length ? a[i] : 0) + (i < b.length ? b[i] / 2 : 0);
            s[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v));
        }
        return s;
    }

    /** Laço curto de punk-rock de garagem: baixo, bateria e uma guitarrinha em onda quadrada. */
    private short[] gerarMusica() {
        float bpm = 168f;
        float colcheia = 60f / bpm / 2f;
        int passos = 64;
        int nPasso = (int) (colcheia * TAXA);
        float[] baixo = {82.41f, 82.41f, 82.41f, 98f, 110f, 110f, 123.47f, 110f};
        float[] solo = {329.6f, 0, 392f, 0, 440f, 392f, 329.6f, 0, 293.7f, 0, 329.6f, 0, 392f, 440f, 493.9f, 0};
        short[] s = new short[passos * nPasso];
        float faseB = 0;
        float faseS = 0;
        for (int p = 0; p < passos; p++) {
            float fBaixo = baixo[(p / 4) % baixo.length];
            float fSolo = p >= 32 ? solo[p % solo.length] : 0f;
            boolean bumbo = p % 8 == 0 || p % 8 == 3;
            boolean caixa = p % 8 == 4;
            for (int i = 0; i < nPasso; i++) {
                float t = i / (float) nPasso;
                float seg = i / (float) TAXA;
                faseB += fBaixo / TAXA;
                faseS += fSolo / TAXA;
                float amostra = ((faseB % 1f) < 0.4f ? 0.33f : -0.33f) * (1f - t * 0.5f);
                if (fSolo > 0) amostra += ((faseS % 1f) < 0.5f ? 0.12f : -0.12f) * (1f - t * 0.6f);
                if (bumbo) amostra += (float) Math.sin(2 * Math.PI * (110f - 300f * seg) * seg) * 0.55f * (float) Math.exp(-seg * 22f);
                if (caixa) amostra += (aleatorio.nextFloat() * 2f - 1f) * 0.35f * (float) Math.exp(-seg * 22f);
                amostra += (aleatorio.nextFloat() * 2f - 1f) * 0.08f * (float) Math.exp(-seg * 90f);
                s[p * nPasso + i] = amostra(amostra);
            }
        }
        return s;
    }

    private static short amostra(float v) {
        return (short) (Math.max(-1f, Math.min(1f, v)) * 32000f);
    }

    private static File wavTemporario(short[] amostras) throws IOException {
        File f = File.createTempFile("skate2k", ".wav");
        f.deleteOnExit();
        ByteArrayOutputStream o = new ByteArrayOutputStream(44 + amostras.length * 2);
        escreverTexto(o, "RIFF");
        escreverInt(o, 36 + amostras.length * 2);
        escreverTexto(o, "WAVEfmt ");
        escreverInt(o, 16);
        escreverCurto(o, 1);
        escreverCurto(o, 1);
        escreverInt(o, TAXA);
        escreverInt(o, TAXA * 2);
        escreverCurto(o, 2);
        escreverCurto(o, 16);
        escreverTexto(o, "data");
        escreverInt(o, amostras.length * 2);
        for (short a : amostras) escreverCurto(o, a);
        Files.write(f.toPath(), o.toByteArray());
        return f;
    }

    private static void escreverTexto(ByteArrayOutputStream o, String s) {
        for (int i = 0; i < s.length(); i++) o.write(s.charAt(i));
    }

    private static void escreverInt(ByteArrayOutputStream o, int v) {
        o.write(v);
        o.write(v >> 8);
        o.write(v >> 16);
        o.write(v >> 24);
    }

    private static void escreverCurto(ByteArrayOutputStream o, int v) {
        o.write(v);
        o.write(v >> 8);
    }

    @Override
    public void dispose() {
        for (Sound s : sons.values()) s.dispose();
        if (musica != null) musica.dispose();
    }
}
