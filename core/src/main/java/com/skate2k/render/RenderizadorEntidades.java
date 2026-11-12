package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.graphics.Color;
import com.skate2k.logica.Item;
import com.skate2k.logica.Jogador;
import com.skate2k.logica.RoboSkatista;
import com.skate2k.logica.TipoItem;
import com.skate2k.logica.TipoRobo;
import com.skate2k.logica.Charlietron;

/**
 * Personagens em sprites, com poses selecionadas pela física e skate solto projetado em 3D.
 */
final class RenderizadorEntidades {
    private static final Color SHAPE_TOPO = Paleta.hex("2a2240");
    private static final Color SHAPE_FUNDO = Paleta.hex("ffd92e");
    private static final Color SHAPE_DETALHE = Paleta.hex("2a2240");
    private static final Color RODA = Paleta.hex("fff8ec");

    /** Direção que aponta para o observador; a face é visível se a normal concordar com ela. */
    private static final float VX = -0.577f;
    private static final float VY = -0.577f;
    private static final float VZ = 0.577f;

    private final Desenho d;
    private FolhaPersonagem folha;
    private final AnimacaoMovimento animacaoMovimento = new AnimacaoMovimento();

    // Resultado da última projeção
    private float ox, oy;

    RenderizadorEntidades(Desenho d) {
        this.d = d;
    }

    // -------------------------------------------------------------------------------- jogador

    void sombra(float x, float y, float alturaChao, float alturaObjeto, float raio) {
        float f = Math.max(0.45f, 1f - (alturaObjeto - alturaChao) * 0.22f);
        d.elipse(sx(x, y), sy(x, y, alturaChao), raio * 2.1f * f, raio * 1.05f * f, Paleta.SOMBRA_CHAO);
    }

    void jogador(Jogador j, float alturaChao, float tempo) {
        float velocidade = (float) Math.hypot(j.vx, j.vy);
        animacaoMovimento.atualizar(tempo, velocidade / Math.max(0.01f, j.velocidadeMaxima()),
                velocidade >= 0.25f && j.estado() == Jogador.Estado.NO_CHAO && !j.carregandoPulo());
        sombra(j.x, j.y, alturaChao, j.z, 34f);
        if (j.invulneravel() && j.estado() != Jogador.Estado.CAIDO && ((int) (tempo * 14f)) % 2 == 0) return;

        float yaw = FolhaPersonagem.rumoDoSprite(j.rumo, j.vx, j.vy, j.noAr(),
                j.detector().giroGraus());
        boolean caido = j.estado() == Jogador.Estado.CAIDO;

        if (caido) {
            jogadorCaido(j, yaw, tempo);
            return;
        }

        jogadorSprite(j, yaw, tempo);
    }

    /** Seleciona poses pelos estados e pelos ângulos reais das manobras. */
    private void jogadorSprite(Jogador j, float yaw, float tempo) {
        folha = d.personagem();
        int direcao = FolhaPersonagem.linhaDoRumo(yaw);
        FolhaPersonagem.Quadro especial = null;
        if (j.estado() == Jogador.Estado.GRIND) {
            int quadro = j.equilibrio() < -0.25f ? 3 : j.equilibrio() > 0.25f ? 5 : 4;
            especial = folha.manobra(j.boardslide() ? "boardslide" : "grind_5050", direcao, quadro);
        } else if (j.noAr()) {
            float flip = j.detector().flipGraus();
            float impossible = j.detector().impossibleGraus();
            if (j.detector().temRotacaoEmAndamento()) {
                boolean temFlip = flip % 360f > 0.01f;
                boolean temImpossible = impossible % 360f > 0.01f;
                String nome = temFlip && temImpossible ? "hardflip_ciclo"
                        : temImpossible ? "impossible_ciclo" : "kickflip_ciclo";
                float angulo = temImpossible ? impossible : flip;
                especial = folha.manobra(nome, direcao, (int) ((angulo % 360f) / 45f));
            } else if (j.detector().grabAtivo()) {
                especial = folha.manobra("grab", direcao, 3);
            } else {
                especial = folha.manobra("salto_pouso", direcao, j.vz > 0f ? 3 : 4);
            }
        } else if (j.carregandoPulo()) {
            especial = folha.manobra("salto_pouso", direcao, j.cargaPulo() > 0.45f ? 1 : 0);
        } else if (j.tempoEstado() < 0.18f) {
            especial = folha.manobra("salto_pouso", direcao, 6);
        }
        float px = sx(j.x, j.y);
        float py = sy(j.x, j.y, j.z);
        if (especial != null) {
            desenharPose(especial, px, py - 3f);
            return;
        }
        int grupo = animacaoMovimento.rapido() ? FolhaPersonagem.RAPIDO : FolhaPersonagem.ANDANDO;
        int quadro = animacaoMovimento.quadro();
        // Mesmo parado, permanece sobre a prancha. O grupo antigo parado segurava o skate na mão.
        desenharPose(new FolhaPersonagem.Quadro(folha.quadro(direcao, grupo, quadro),
                FolhaPersonagem.ESCALA), px, py - 3f - AtlasMovimento.MARGEM_BASE * FolhaPersonagem.ESCALA);
    }

    private void desenharPose(FolhaPersonagem.Quadro quadro, float x, float y) {
        float w = quadro.regiao.getRegionWidth() * quadro.escala;
        float h = quadro.regiao.getRegionHeight() * quadro.escala;
        d.sprite(quadro.regiao, x - w / 2f, y, w, h);
    }

    private void jogadorCaido(Jogador j, float yaw, float tempo) {
        folha = d.personagem();
        float t = j.tempoEstado();
        float dist = Math.min(1.5f, t * 2.2f);
        shape(j.x + (float) Math.cos(yaw) * dist, j.y + (float) Math.sin(yaw) * dist,
                j.z + 0.15f, yaw + Math.min(t, 0.6f) * 7f, Math.min(t, 0.6f) * 4f, 0f);
        FolhaPersonagem.Quadro pose = folha.caido(FolhaPersonagem.linhaDoRumo(yaw));
        float px = sx(j.x, j.y);
        float py = sy(j.x, j.y, j.z);
        float impacto = Math.max(0f, 1f - t / 0.18f) * 8f;
        desenharPose(pose, px, py - pose.regiao.getRegionHeight() * pose.escala * 0.45f + impacto);
        for (int i = 0; i < 3; i++) {
            float a = tempo * 5f + i * 2.09f;
            estrela(px + (float) Math.cos(a) * 24f, py + 36f + (float) Math.sin(a) * 8f,
                    5f, Paleta.AMARELO);
        }
    }

    void estrela(float x, float y, float r, Color c) {
        d.tri(x - r, y, x + r, y, x, y + r * 1.4f, c);
        d.tri(x - r, y, x + r, y, x, y - r * 1.4f, c);
        d.tri(x, y - r, x, y + r, x - r * 1.4f, y, c);
        d.tri(x, y - r, x, y + r, x + r * 1.4f, y, c);
    }

    /** Desenha o shape em 3D de verdade: a rotação de flip e impossible vem do detector. */
    private void shape(float x, float y, float z, float yaw, float roll, float pitch) {
        float cy = (float) Math.cos(yaw), sy2 = (float) Math.sin(yaw);
        float cr = (float) Math.cos(roll), sr = (float) Math.sin(roll);
        float cp = (float) Math.cos(pitch), sp = (float) Math.sin(pitch);

        // Normal do tampo do shape, para saber qual lado está virado para a câmera.
        float nS = -sr;
        float nH = cr;
        float nL = nH * sp;
        nH = nH * cp;
        float nx = cy * nL - sy2 * nS;
        float ny = sy2 * nL + cy * nS;
        boolean topoVisivel = nx * VX + ny * VY + nH * VZ > 0;

        final float[][] contorno = {
            {0.48f, 0.08f, 0.05f}, {0.36f, 0.15f, 0f}, {-0.36f, 0.15f, 0f}, {-0.48f, 0.08f, 0.05f},
            {-0.48f, -0.08f, 0.05f}, {-0.36f, -0.15f, 0f}, {0.36f, -0.15f, 0f}, {0.48f, -0.08f, 0.05f}
        };
        float[] px = new float[contorno.length];
        float[] py = new float[contorno.length];
        for (int i = 0; i < contorno.length; i++) {
            transformarShape(contorno[i][0], contorno[i][1], contorno[i][2], x, y, z, cy, sy2, cr, sr, cp, sp);
            px[i] = ox;
            py[i] = oy;
        }
        transformarShape(0, 0, 0, x, y, z, cy, sy2, cr, sr, cp, sp);
        float cx0 = ox, cy0 = oy;

        if (topoVisivel) rodas(x, y, z, cy, sy2, cr, sr, cp, sp);

        // Contorno
        for (int i = 0; i < px.length; i++) {
            int n = (i + 1) % px.length;
            d.membro(px[i], py[i], px[n], py[n], 5f, Paleta.TINTA);
        }
        Color cor = topoVisivel ? SHAPE_TOPO : SHAPE_FUNDO;
        for (int i = 0; i < px.length; i++) {
            int n = (i + 1) % px.length;
            d.tri(cx0, cy0, px[i], py[i], px[n], py[n], cor);
        }
        // Risco no meio do shape: deixa a rotação bem legível
        transformarShape(0.3f, 0, 0.005f, x, y, z, cy, sy2, cr, sr, cp, sp);
        float ax = ox, ay = oy;
        transformarShape(-0.3f, 0, 0.005f, x, y, z, cy, sy2, cr, sr, cp, sp);
        d.linha(ax, ay, ox, oy, 3.5f, topoVisivel ? Paleta.ROSA : SHAPE_DETALHE);

        if (!topoVisivel) rodas(x, y, z, cy, sy2, cr, sr, cp, sp);
    }

    private void rodas(float x, float y, float z, float cy, float sy2, float cr, float sr, float cp, float sp) {
        float[][] r = {{0.3f, 0.12f}, {0.3f, -0.12f}, {-0.3f, 0.12f}, {-0.3f, -0.12f}};
        for (float[] p : r) {
            transformarShape(p[0], p[1], -0.09f, x, y, z, cy, sy2, cr, sr, cp, sp);
            d.circulo(ox, oy, 5f, Paleta.TINTA);
            d.circulo(ox, oy, 3f, RODA);
        }
    }

    private void transformarShape(float l, float s, float h, float x, float y, float z,
                                  float cy, float sy2, float cr, float sr, float cp, float sp) {
        // Flip gira em volta do eixo do comprimento; impossible, em volta do eixo da largura.
        float s1 = s * cr - h * sr;
        float h1 = s * sr + h * cr;
        float l2 = l * cp + h1 * sp;
        float h2 = -l * sp + h1 * cp;
        float wx = x + cy * l2 - sy2 * s1;
        float wy = y + sy2 * l2 + cy * s1;
        ox = sx(wx, wy);
        oy = sy(wx, wy, z + h2);
    }

    // ---------------------------------------------------------------------------------- robôs

    void robo(RoboSkatista r, float alturaChao, float tempo) {
        if (r.chefe() != null) {
            charlietron(r, alturaChao, tempo);
            return;
        }
        TipoRobo tipo = r.tipo;
        sombra(r.x, r.y, alturaChao, alturaChao, 24f + tipo.raio * 28f);
        boolean atordoado = r.estado() == RoboSkatista.Estado.ATORDOADO;
        boolean preparando = r.estado() == RoboSkatista.Estado.PREPARO;
        boolean investindo = r.estado() == RoboSkatista.Estado.INVESTIDA;
        float tremor = preparando ? (float) Math.sin(tempo * 70f) * 3f : 0f;
        float pulinho = r.movendo() ? Math.abs((float) Math.sin(tempo * 12f)) * 2f : 0f;
        float yaw = r.rumo + (atordoado ? (float) Math.sin(tempo * 9f) * 0.25f : 0f);
        float tamanho = 102f * tipo.altura / 1.15f;
        float largura = tamanho * (investindo ? 1.08f : 1f);
        float altura = tamanho * (preparando ? 0.94f : 1f);
        float x = sx(r.x, r.y) + tremor;
        float y = sy(r.x, r.y, alturaChao) + pulinho;
        Color cor = preparando || investindo ? Paleta.hex("ffaaaa")
                : atordoado ? Paleta.hex("bbbbdd") : Paleta.BRANCO;
        // Pisca ao tomar dano; as cores normais retornam entre os pulsos.
        if (r.flashDano() > 0 && ((int) (tempo * 20f)) % 2 == 0) cor = Paleta.hex("ff7777");
        d.sprite(d.robos().quadro(tipo, yaw), x - largura / 2f, y - altura * 0.04f,
                largura, altura, cor);
        if (atordoado) {
            for (int i = 0; i < 3; i++) {
                float angulo = tempo * 5f + i * 2.0944f;
                estrela(x + (float) Math.cos(angulo) * 19f,
                        y + altura * 0.85f + (float) Math.sin(angulo) * 5f, 4f, Paleta.AMARELO);
            }
        }
    }

    // ---------------------------------------------------------------------------------- itens

    private void charlietron(RoboSkatista r, float chao, float tempo) {
        Charlietron c = r.chefe();
        sombra(r.x, r.y, chao, c.z(), 38f);
        String folha = "movimento";
        int quadro = 2 + (int) (c.tempo() * 8f) % 4;
        float rumo = r.rumo;
        boolean caido = false;
        switch (c.estado()) {
            case PREPARO_PISAO: folha = "combate"; quadro = 0; break;
            case PISAO: folha = "combate"; quadro = c.vz() > 0 ? 1 : 2; break;
            case PREPARO_MANOBRA: folha = "acoes"; quadro = 0; break;
            case RECUPERANDO: folha = "acoes"; quadro = 5; break;
            case GRIND:
                folha = "grinds";
                quadro = (c.manobra() == com.skate2k.logica.TipoManobra.BOARDSLIDE ? 4 : 0)
                        + (int) (c.tempo() * 8f) % 4;
                break;
            case CAIDO: folha = "combate"; quadro = c.tempo() < 1f ? 5 : 6; caido = quadro == 5; break;
            case DERROTADO: folha = "combate"; quadro = 7; caido = true; break;
            case MANOBRA:
                float progresso = c.progressoVoo();
                int ciclos = 1;
                switch (c.manobra()) {
                    case TRIPLE_KICKFLIP: ciclos = 3; // continua
                    case DOUBLE_KICKFLIP:
                        if (ciclos == 1) ciclos = 2;
                    case KICKFLIP: folha = "kickflip"; break;
                    case DOUBLE_IMPOSSIBLE: ciclos = 2;
                    case IMPOSSIBLE: folha = "impossible"; break;
                    case HARDFLIP: folha = progresso < 0.5f ? "kickflip" : "impossible"; ciclos = 2; break;
                    case VARIAL: folha = "kickflip"; rumo += progresso * (float) Math.PI; break;
                    case GIRO_180: rumo += progresso * (float) Math.PI; folha = "acoes"; break;
                    case GIRO_360: rumo += progresso * (float) Math.PI * 2; folha = "acoes"; break;
                    case GIRO_540: rumo += progresso * (float) Math.PI * 3; folha = "acoes"; break;
                    case GIRO_720: rumo += progresso * (float) Math.PI * 4; folha = "acoes"; break;
                    default: folha = "acoes"; break;
                }
                quadro = folha.equals("acoes") ? (c.manobra() == com.skate2k.logica.TipoManobra.GRAB ? 4 : 3)
                        : Math.min(7, (int) (((progresso * ciclos) % 1f) * 8f));
                break;
            default: break;
        }
        if (c.estado() == Charlietron.Estado.PREPARO_PISAO || c.estado() == Charlietron.Estado.PISAO) {
            float ax = sx(c.alvoX(), c.alvoY()), ay = sy(c.alvoX(), c.alvoY(), c.alvoZ());
            d.elipse(ax, ay, 68, 34, Paleta.copia(Paleta.VERMELHO, 0.35f));
            d.linha(ax - 23, ay - 12, ax + 23, ay + 12, 3, Paleta.VERMELHO);
            d.linha(ax - 23, ay + 12, ax + 23, ay - 12, 3, Paleta.VERMELHO);
        }
        var imagem = d.charlietron().quadro(folha, FolhaPersonagem.linhaDoRumo(rumo), quadro);
        float escala = d.charlietron().escala(folha);
        float largura = imagem.getRegionWidth() * escala, altura = imagem.getRegionHeight() * escala;
        float x = sx(r.x, r.y), y = sy(r.x, r.y, c.z());
        d.sprite(imagem, x - largura / 2, y - (caido ? altura * 0.4f : 3f), largura, altura,
                r.flashDano() > 0 ? Paleta.hex("ff9999") : Paleta.BRANCO);
        if (c.vulneravel() && c.estado() == Charlietron.Estado.CAIDO) {
            for (int i = 0; i < 3; i++) estrela(x + (float) Math.cos(tempo * 4 + i * 2.094f) * 22,
                    y + 34 + (float) Math.sin(tempo * 4 + i * 2.094f) * 6, 4, Paleta.AMARELO);
        }
    }

    void item(Item item, float tempo) {
        if (!item.disponivel()) return;
        float flutua = (float) Math.sin(tempo * 3f + item.x) * 0.08f;
        float x = sx(item.x, item.y);
        float y = sy(item.x, item.y, item.z + flutua);
        d.elipse(x, sy(item.x, item.y, item.z - 0.6f), 34, 16, Paleta.SOMBRA_CHAO);
        d.circulo(x, y, 21f, Paleta.TINTA);
        d.circulo(x, y, 18f, Paleta.copia(Paleta.BRANCO, 0.92f));
        float g = 0.5f + 0.5f * (float) Math.sin(tempo * 6f);
        d.circulo(x, y, 18f, Paleta.copia(Paleta.AMARELO, 0.15f + 0.2f * g));
        icone(item.tipo, x, y);
    }

    /** Coração com contorno: item de cura e medidor de vida do HUD. */
    void coracao(float x, float y, float e, Color cor) {
        d.circulo(x - 5.5f * e, y + 3 * e, 8.5f * e, Paleta.TINTA);
        d.circulo(x + 5.5f * e, y + 3 * e, 8.5f * e, Paleta.TINTA);
        d.tri(x - 13.5f * e, y + 1 * e, x + 13.5f * e, y + 1 * e, x, y - 15 * e, Paleta.TINTA);
        d.circulo(x - 5.5f * e, y + 3 * e, 6 * e, cor);
        d.circulo(x + 5.5f * e, y + 3 * e, 6 * e, cor);
        d.tri(x - 11 * e, y + 1 * e, x + 11 * e, y + 1 * e, x, y - 11 * e, cor);
    }

    /** Pequeno ícone de item, também usado no HUD. */
    void icone(TipoItem t, float x, float y) {
        switch (t) {
            case CAFE:
                d.ret(x - 8, y - 9, 13, 15, Paleta.TINTA);
                d.ret(x - 6, y - 7, 9, 11, Paleta.hex("8a5a33"));
                d.linha(x + 5, y + 2, x + 10, y + 2, 3, Paleta.TINTA);
                d.linha(x + 10, y + 2, x + 10, y - 5, 3, Paleta.TINTA);
                d.linha(x + 5, y - 5, x + 10, y - 5, 3, Paleta.TINTA);
                break;
            case PASTEL:
                d.tri(x - 12, y - 7, x + 12, y - 7, x, y + 9, Paleta.TINTA);
                d.tri(x - 9, y - 5, x + 9, y - 5, x, y + 6, Paleta.hex("f2b544"));
                break;
            case TENIS_MOLA:
                d.ret(x - 11, y + 1, 18, 8, Paleta.TINTA);
                d.ret(x - 9, y + 3, 14, 4, Paleta.ROSA);
                for (int i = 0; i < 3; i++) d.linha(x - 7, y - 1 - i * 3, x + 3, y - 3 - i * 3, 2, Paleta.TINTA);
                break;
            case RELOGIO:
                d.circulo(x, y, 12, Paleta.TINTA);
                d.circulo(x, y, 9.5f, Paleta.BRANCO);
                d.linha(x, y, x, y + 7, 2.5f, Paleta.TINTA);
                d.linha(x, y, x + 5, y - 2, 2.5f, Paleta.TINTA);
                break;
            case ADESIVO:
                estrela(x, y, 9f, Paleta.TINTA);
                estrela(x, y, 6.5f, Paleta.ROSA);
                break;
            case CAPACETE:
                d.circulo(x, y - 1, 12, Paleta.TINTA);
                d.circulo(x, y - 1, 9.5f, Paleta.hex("3ddc97"));
                d.ret(x - 14, y - 10, 28, 8, Paleta.BRANCO);
                break;
            case CURATIVO:
                coracao(x, y, 1f, Paleta.VERMELHO);
                break;
            default: // BOLO
                d.ret(x - 12, y - 9, 24, 14, Paleta.TINTA);
                d.ret(x - 10, y - 7, 20, 10, Paleta.hex("f4c2d0"));
                d.ret(x - 10, y - 2, 20, 3, Paleta.hex("8a5a33"));
                d.circulo(x, y + 8, 3.5f, Paleta.VERMELHO);
                break;
        }
    }
}
