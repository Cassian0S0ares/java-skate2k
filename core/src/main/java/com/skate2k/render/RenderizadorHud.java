package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.skate2k.logica.ConstantesFisica;
import com.skate2k.logica.DetectorManobras;
import com.skate2k.logica.Jogador;
import com.skate2k.logica.Manobra;
import com.skate2k.logica.MundoJogo;
import com.skate2k.logica.Pontuacao;
import com.skate2k.logica.Restricao;
import com.skate2k.logica.RoboSkatista;
import com.skate2k.logica.TipoItem;
import java.util.List;

/** Interface na tela: tempo, pontos, Carga, multiplicador, estilo e manobras. */
public final class RenderizadorHud {
    private static final int CARGA_CHEIA = 2000;

    private final Desenho d;
    private final RenderizadorEntidades icones;
    private final Color tmp = new Color();
    private float pulsoCarga;
    private int ultimaCarga;
    private int ultimaVida = ConstantesFisica.VIDA_MAXIMA;
    private float pulsoDano;

    public RenderizadorHud(Desenho d) {
        this.d = d;
        this.icones = new RenderizadorEntidades(d);
    }

    public void desenhar(MundoJogo m, float w, float h, float tempo, int recorde, float dt) {
        desenharTempo(m, w, h, tempo);
        desenharPontos(m, h, recorde);
        desenharCombo(m, h, tempo, dt);
        desenharVida(m, h, tempo, dt);
        desenharRestricao(m, w, h, tempo);
        desenharItensAtivos(m, w, h);
        desenharManobras(m, w);
    }

    private void desenharTempo(MundoJogo m, float w, float h, float tempo) {
        float t = m.tempoRestante();
        int seg = (int) Math.ceil(t);
        String texto = seg / 60 + ":" + (seg % 60 < 10 ? "0" : "") + seg % 60;
        boolean urgente = t <= 10f;
        float escala = 7f;
        float pw = 190;
        float px = w / 2f - pw / 2f;
        float py = h - 78;
        d.painel(px, py, pw, 66, Paleta.PAINEL, urgente ? Paleta.VERMELHO : Paleta.CIANO);
        Color cor = urgente ? ((int) (tempo * 6f) % 2 == 0 ? Paleta.VERMELHO : Paleta.BRANCO) : Paleta.BRANCO;
        FontePixel.centro(d, texto, w / 2f, py + 15, escala, cor);
    }

    private void desenharPontos(MundoJogo m, float h, int recorde) {
        Pontuacao p = m.pontuacao();
        float px = 16;
        float pw = 330;
        float py = h - 100;
        d.painel(px, py, pw, 88, Paleta.PAINEL, Paleta.ROSA);
        FontePixel.comSombra(d, m.fase().nome, px + 12, py + 68, 2f, Paleta.CIANO);
        FontePixel.comSombra(d, String.valueOf(p.pontosFase()), px + 12, py + 33, 4f, Paleta.BRANCO);
        FontePixel.direita(d, "META " + m.meta(), px + pw - 12, py + 38, 2f, m.metaBatida() ? Paleta.LIMA : Paleta.AMARELO);
        // Barra de progresso até a meta
        float bx = px + 12;
        float bw = pw - 24;
        d.ret(bx, py + 16, bw, 11, Paleta.TINTA);
        float prog = Math.min(1f, p.pontosFase() / (float) m.meta());
        d.ret(bx + 2, py + 18, (bw - 4) * prog, 7, m.metaBatida() ? Paleta.LIMA : Paleta.AMARELO);
        FontePixel.comSombra(d, "TOTAL " + p.pontosTotal(), px + 12, py + 3, 1.5f, Paleta.BRANCO);
        FontePixel.direita(d, "RECORDE " + Math.max(recorde, p.pontosTotal()), px + pw - 12, py + 3, 1.5f, Paleta.CINZA);
    }

    private void desenharCombo(MundoJogo m, float h, float tempo, float dt) {
        Pontuacao p = m.pontuacao();
        if (p.carga() != ultimaCarga) {
            if (p.carga() > ultimaCarga) pulsoCarga = 1f;
            ultimaCarga = p.carga();
        }
        pulsoCarga = Math.max(0, pulsoCarga - dt * 3f);

        float px = 16;
        float pw = 330;
        float py = h - 256;
        d.painel(px, py, pw, 144, Paleta.PAINEL, Paleta.AMARELO);

        // Bateria da Carga
        FontePixel.comSombra(d, "CARGA", px + 12, py + 122, 2f, Paleta.AMARELO);
        FontePixel.comSombra(d, String.valueOf(p.carga()), px + 80, py + 122, 2f, Paleta.BRANCO);
        float bx = px + 12;
        float by = py + 92;
        float bw = 216;
        float bh = 22;
        d.ret(bx, by, bw, bh, Paleta.TINTA);
        d.ret(bx + bw, by + 6, 6, bh - 12, Paleta.TINTA);
        float f = Math.min(1f, p.carga() / (float) CARGA_CHEIA);
        tmp.set(Paleta.CIANO).lerp(Paleta.ROSA, f);
        if (pulsoCarga > 0) tmp.lerp(Color.WHITE, pulsoCarga * 0.6f);
        d.ret(bx + 3, by + 3, (bw - 6) * f, bh - 6, tmp);
        for (int i = 1; i < 5; i++) d.ret(bx + bw * i / 5f, by + 3, 2, bh - 6, Paleta.TINTA);

        // Multiplicador
        String mult = "X" + p.multiplicador();
        float esc = p.multiplicador() >= 10 ? 5f : 6f;
        FontePixel.direita(d, mult, px + pw - 10, py + 84, esc, p.multiplicador() >= 6 ? Paleta.ROSA : Paleta.AMARELO);

        // Nível de estilo
        FontePixel.comSombra(d, "ESTILO " + p.nivel() + "/5", px + 12, py + 62, 2f, Paleta.LIMA);
        FontePixel.comSombra(d, p.nomeNivel(), px + 12, py + 40, 2.5f, Paleta.BRANCO);
        float sx0 = px + 12;
        float sw = pw - 24;
        d.ret(sx0, py + 14, sw, 16, Paleta.TINTA);
        d.ret(sx0 + 2, py + 16, (sw - 4) * p.progressoNivel(), 12, Paleta.LIMA);
        FontePixel.comSombra(d, "DANO DO PISÃO " + p.dano(), sx0 + 6, py + 18, 1.5f, Paleta.BRANCO);
    }

    private void desenharVida(MundoJogo m, float h, float tempo, float dt) {
        int vida = m.jogador().vida();
        if (vida < ultimaVida) pulsoDano = 1f;
        ultimaVida = vida;
        pulsoDano = Math.max(0, pulsoDano - dt * 2f);

        float px = 16;
        float pw = 330;
        float py = h - 310;
        boolean critico = vida == 1;
        d.painel(px, py, pw, 42, Paleta.PAINEL, critico ? Paleta.VERMELHO : Paleta.LIMA);
        FontePixel.comSombra(d, "VIDA", px + 12, py + 13, 3f, critico ? Paleta.VERMELHO : Paleta.LIMA);

        float tremorDano = pulsoDano > 0 ? (float) Math.sin(tempo * 60f) * 4f * pulsoDano : 0f;
        for (int i = 0; i < ConstantesFisica.VIDA_MAXIMA; i++) {
            float x = px + 120 + i * 44 + tremorDano;
            boolean cheio = i < vida;
            float batida = cheio && critico ? 1f + 0.12f * (float) Math.sin(tempo * 10f) : 1f;
            icones.coracao(x, py + 21, batida, cheio ? Paleta.VERMELHO : Paleta.copia(Paleta.CINZA, 0.45f));
        }
    }

    private void desenharRestricao(MundoJogo m, float w, float h, float tempo) {
        Restricao r = m.restricao();
        float pw = 350;
        float px = w - pw - 16;
        float py = h - 84;
        d.painel(px, py, pw, 72, Paleta.PAINEL, Paleta.LARANJA);
        FontePixel.comSombra(d, "REGRA: " + r.titulo, px + 12, py + 52, 2f, Paleta.LARANJA);
        String detalhe;
        switch (r) {
            case CHUVA:
                detalhe = "PISTA MOLHADA, POUSO PRECISO";
                break;
            case CAMERA_EM_MOVIMENTO:
                detalhe = "MANOBRA COM A CÂMERA ANDANDO";
                break;
            default:
                detalhe = m.fase().cenario.temTrilhoPerto(m.jogador().x, m.jogador().y,
                        com.skate2k.logica.ConstantesFisica.ALCANCE_PULO_SEM_PULO)
                        ? "TRILHO PERTO: PODE PULAR!" : "PROCURE UM TRILHO PARA PULAR";
                break;
        }
        FontePixel.comSombra(d, detalhe, px + 12, py + 32, 1.5f, Paleta.BRANCO);
        if (r.exigeCameraEmMovimento()) {
            boolean filmando = m.camera().emMovimento();
            d.circulo(px + 22, py + 14, 6, Paleta.TINTA);
            d.circulo(px + 22, py + 14, 4.5f, filmando && (int) (tempo * 4f) % 2 == 0 ? Paleta.VERMELHO : filmando ? Paleta.ROSA : Paleta.CINZA);
            FontePixel.comSombra(d, filmando ? "GRAVANDO" : "CÂMERA PARADA", px + 36, py + 10, 1.5f, filmando ? Paleta.LIMA : Paleta.CINZA);
        }
    }

    private void desenharItensAtivos(MundoJogo m, float w, float h) {
        Jogador j = m.jogador();
        float x = w - 40;
        float y = h - 118;
        if (j.tempoCafe() > 0) x = itemAtivo(TipoItem.CAFE, x, y, j.tempoCafe() / 8f);
        if (j.tempoMola() > 0) x = itemAtivo(TipoItem.TENIS_MOLA, x, y, j.tempoMola() / 10f);
        if (j.temCapacete()) itemAtivo(TipoItem.CAPACETE, x, y, 1f);
    }

    private float itemAtivo(TipoItem tipo, float x, float y, float fracao) {
        d.circulo(x, y, 22, Paleta.TINTA);
        d.circulo(x, y, 19, Paleta.BRANCO);
        icones.icone(tipo, x, y);
        d.ret(x - 19, y - 30, 38, 6, Paleta.TINTA);
        d.ret(x - 18, y - 29, 36 * fracao, 4, Paleta.LIMA);
        return x - 56;
    }

    private void desenharManobras(MundoJogo m, float w) {
        Jogador j = m.jogador();
        DetectorManobras det = j.detector();
        float y = 40;

        if (j.nomePotencial() != null) {
            String pot = "POTENCIALIZADOR: " + j.nomePotencial() + " X" + String.format("%.2f", j.potencial()).replace(',', '.');
            FontePixel.centro(d, pot, w / 2f, y + 42, 2f, Paleta.LIMA);
        }

        StringBuilder sb = new StringBuilder();
        if (j.noAr() || j.estado() == Jogador.Estado.GRIND) {
            List<Manobra> previa = det.previa(j.potencial());
            for (Manobra mano : previa) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(mano.nome());
            }
            if (j.estado() == Jogador.Estado.GRIND) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(j.boardslide() ? "BOARDSLIDE" : "50-50");
            }
            if (sb.length() > 0) {
                FontePixel.centro(d, sb.toString(), w / 2f, y, 3f, Paleta.BRANCO);
            }
        } else if (m.tempoExibicaoManobras() > 0 && !m.ultimasManobras().isEmpty()) {
            int soma = 0;
            for (Manobra mano : m.ultimasManobras()) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(mano.nome());
                soma += mano.pontos;
            }
            FontePixel.centro(d, sb.toString(), w / 2f, y, 3f, Paleta.AMARELO);
            FontePixel.centro(d, "+" + soma + " NA CARGA", w / 2f, y - 24, 2f, Paleta.CIANO);
        }
    }

    /** Setas nas bordas da tela apontando para os robôs fora de quadro. */
    public void desenharSetasRobos(MundoJogo m, OrthographicCamera cam, float w, float h) {
        float meiaL = cam.viewportWidth / 2f;
        float meiaA = cam.viewportHeight / 2f;
        for (RoboSkatista r : m.robos()) {
            float rx = sx(r.x, r.y) - cam.position.x;
            float ry = sy(r.x, r.y, 0.5f) - cam.position.y;
            if (Math.abs(rx) < meiaL - 20 && Math.abs(ry) < meiaA - 20) continue;
            float n = (float) Math.hypot(rx, ry);
            float dx = rx / n;
            float dy = ry / n;
            float k = Math.min((meiaL - 34) / Math.max(0.001f, Math.abs(dx)), (meiaA - 34) / Math.max(0.001f, Math.abs(dy)));
            float cx = w / 2f + dx * k;
            float cy = h / 2f + dy * k;
            float px = -dy;
            float py = dx;
            d.tri(cx + dx * 18, cy + dy * 18, cx + px * 13, cy + py * 13, cx - px * 13, cy - py * 13, Paleta.TINTA);
            d.tri(cx + dx * 12, cy + dy * 12, cx + px * 8, cy + py * 8, cx - px * 8, cy - py * 8, Paleta.ROSA);
        }
    }

    public void desenharDicaInicial(float w, float tempoFase) {
        if (tempoFase > 12f) return;
        float a = Math.min(1f, (12f - tempoFase) * 2f);
        d.ret(0, 96, w, 30, Paleta.copia(Paleta.TINTA, 0.55f * a));
        FontePixel.centro(d, "WASD ANDA - ESPAÇO PULA - J/K FLIPS - L PEGA - Q/E GIRA - ESC PAUSA", w / 2f, 104, 2f, Paleta.copia(Paleta.BRANCO, a));
    }
}
