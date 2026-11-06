package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.graphics.Color;
import com.skate2k.logica.Item;
import com.skate2k.logica.Jogador;
import com.skate2k.logica.RoboSkatista;
import com.skate2k.logica.TipoItem;
import com.skate2k.logica.TipoRobo;

/**
 * Skatista, robôs e itens. Os personagens são modelos 3D simples projetados na isométrica, então
 * giros e flips do skate são rotações reais: o que o detector de manobras mede é o que se vê.
 */
final class RenderizadorEntidades {
    private static final Color PELE = Paleta.hex("c98a5e");
    private static final Color MOLETOM = Paleta.hex("ffd92e");
    private static final Color CALCA = Paleta.hex("27b6c9");
    private static final Color TENIS = Paleta.hex("fff8ec");
    private static final Color BONE = Paleta.hex("ff2e88");
    private static final Color SHAPE_TOPO = Paleta.hex("2a2240");
    private static final Color SHAPE_FUNDO = Paleta.hex("ff5a2e");
    private static final Color SHAPE_DETALHE = Paleta.hex("ffd92e");
    private static final Color RODA = Paleta.hex("19e3ff");

    /** Direção que aponta para o observador; a face é visível se a normal concordar com ela. */
    private static final float VX = -0.577f;
    private static final float VY = -0.577f;
    private static final float VZ = 0.577f;

    private final Desenho d;

    // Referencial local atual: base, frente, lado, inclinação
    private float bx, by, bz;
    private float fx, fy, lx, ly;
    private float tombo;
    private float escala = 1f;
    // Resultado da última projeção
    private float ox, oy;

    RenderizadorEntidades(Desenho d) {
        this.d = d;
    }

    private void referencial(float x, float y, float z, float yaw, float tomboRad) {
        bx = x;
        by = y;
        bz = z;
        fx = (float) Math.cos(yaw);
        fy = (float) Math.sin(yaw);
        lx = -fy;
        ly = fx;
        tombo = tomboRad;
    }

    /** Projeta um ponto local: l para a frente, s para o lado, h para cima. */
    private void ponto(float l, float s, float h) {
        l *= escala;
        s *= escala;
        h *= escala;
        if (tombo != 0) {
            float c = (float) Math.cos(tombo);
            float n = (float) Math.sin(tombo);
            float nl = l * c + h * n;
            float nh = -l * n + h * c;
            l = nl;
            h = nh;
        }
        float wx = bx + fx * l + lx * s;
        float wy = by + fy * l + ly * s;
        float wz = bz + h;
        ox = sx(wx, wy);
        oy = sy(wx, wy, wz);
    }

    // -------------------------------------------------------------------------------- jogador

    void sombra(float x, float y, float alturaChao, float alturaObjeto, float raio) {
        float f = Math.max(0.45f, 1f - (alturaObjeto - alturaChao) * 0.22f);
        d.elipse(sx(x, y), sy(x, y, alturaChao), raio * 2.1f * f, raio * 1.05f * f, Paleta.SOMBRA_CHAO);
    }

    void jogador(Jogador j, float alturaChao, float tempo) {
        sombra(j.x, j.y, alturaChao, j.z, 34f);
        if (j.invulneravel() && j.estado() != Jogador.Estado.CAIDO && ((int) (tempo * 14f)) % 2 == 0) return;

        float giro = (float) Math.toRadians(j.detector().giroGraus());
        float yaw = j.rumo + giro + (j.fakie() ? (float) Math.PI : 0f);
        boolean caido = j.estado() == Jogador.Estado.CAIDO;

        if (caido) {
            jogadorCaido(j, yaw, tempo);
            return;
        }

        float yawShape = j.boardslide() ? yaw + (float) Math.PI / 2f : yaw;
        boolean voando = j.noAr();
        float flip = (float) Math.toRadians(j.detector().flipGraus());
        float impossible = (float) Math.toRadians(j.detector().impossibleGraus());

        // Base do skate: sobe um pouco durante os flips para o corpo "abrir espaço".
        float alturaShape = 0.17f;
        boolean rodando = voando && j.detector().temRotacaoEmAndamento();
        float encolhe = rodando ? 0.30f : 0f;

        referencial(j.x, j.y, j.z, yaw, 0f);
        shape(j.x, j.y, j.z + alturaShape, yawShape, flip, impossible);

        float agacha = j.carregandoPulo() ? 0.25f + 0.35f * j.cargaPulo() : 0f;
        if (voando) agacha = Math.max(agacha, rodando ? 0.45f : 0.25f);
        if (j.detector().grabAtivo() && voando) agacha = 0.75f;
        if (j.estado() == Jogador.Estado.GRIND) agacha = 0.42f;

        corpo(j, tempo, agacha, encolhe, alturaShape);
    }

    private void corpo(Jogador j, float tempo, float agacha, float encolhe, float alturaShape) {
        boolean voando = j.noAr();
        boolean grab = j.detector().grabAtivo() && voando;
        float equilibrio = j.estado() == Jogador.Estado.GRIND ? j.equilibrio() : 0f;
        float vel = (float) Math.hypot(j.vx, j.vy);
        float balanco = (float) Math.sin(tempo * 7f) * 0.03f * Math.min(1f, vel / 4f);

        float pePiso = alturaShape + 0.07f + encolhe;
        float quadril = 0.92f - agacha * 0.3f + encolhe * 0.5f;
        float peito = quadril + 0.44f;
        float cabeca = peito + 0.32f;
        float inclina = equilibrio * 0.28f;

        // Pontos em coordenadas de tela
        ponto(0.26f, 0.02f, pePiso); float peFx = ox, peFy = oy;
        ponto(-0.26f, -0.02f, pePiso); float peTx = ox, peTy = oy;
        ponto(0.24f, 0.13f, (pePiso + quadril) / 2f + 0.04f); float joFx = ox, joFy = oy;
        ponto(-0.24f, 0.13f, (pePiso + quadril) / 2f + 0.04f); float joTx = ox, joTy = oy;
        ponto(0.05f, inclina * 0.3f, quadril); float quX = ox, quY = oy;
        ponto(0.0f + balanco, inclina, peito); float peX = ox, peY = oy;
        ponto(0.0f + balanco, inclina * 1.3f, cabeca); float caX = ox, caY = oy;
        ponto(0.17f + balanco, inclina, peito - 0.02f); float omFx = ox, omFy = oy;
        ponto(-0.17f + balanco, inclina, peito - 0.02f); float omTx = ox, omTy = oy;

        float abre = voando ? 0.62f : j.estado() == Jogador.Estado.GRIND ? 0.68f : 0.42f;
        float sobe = voando ? 0.18f : j.estado() == Jogador.Estado.GRIND ? 0.12f : -0.15f;
        ponto(abre, inclina - 0.05f + balanco, peito + sobe + equilibrio * 0.25f); float maFx = ox, maFy = oy;
        if (grab) {
            ponto(0.05f, 0.14f, pePiso - 0.04f);
            maFx = ox;
            maFy = oy;
        }
        ponto(-abre, inclina - 0.05f - balanco, peito + sobe - equilibrio * 0.25f); float maTx = ox, maTy = oy;

        float ePerna = 13f, eBraco = 9.5f, eTronco = 24f, eTenis = 13f;
        float c = 4.5f;
        Color t = Paleta.TINTA;
        // Primeiro todos os contornos, para o corpo parecer uma peça só.
        d.membro(quX, quY, joFx, joFy, ePerna + c, t);
        d.membro(joFx, joFy, peFx, peFy, ePerna + c, t);
        d.membro(quX, quY, joTx, joTy, ePerna + c, t);
        d.membro(joTx, joTy, peTx, peTy, ePerna + c, t);
        d.membro(quX, quY, peX, peY, eTronco + c, t);
        d.membro(omFx, omFy, maFx, maFy, eBraco + c, t);
        d.membro(omTx, omTy, maTx, maTy, eBraco + c, t);
        d.circulo(caX, caY, 12f + c / 2f, t);
        d.membro(peFx - 3, peFy, peFx + 3, peFy, eTenis + c, t);
        d.membro(peTx - 3, peTy, peTx + 3, peTy, eTenis + c, t);

        d.membro(peFx - 3, peFy, peFx + 3, peFy, eTenis, TENIS);
        d.membro(peTx - 3, peTy, peTx + 3, peTy, eTenis, TENIS);
        d.membro(quX, quY, joTx, joTy, ePerna, Paleta.escuro(CALCA, 0.15f));
        d.membro(joTx, joTy, peTx, peTy, ePerna, Paleta.escuro(CALCA, 0.15f));
        d.membro(quX, quY, joFx, joFy, ePerna, CALCA);
        d.membro(joFx, joFy, peFx, peFy, ePerna, CALCA);
        d.membro(omTx, omTy, maTx, maTy, eBraco, Paleta.escuro(MOLETOM, 0.15f));
        d.membro(quX, quY, peX, peY, eTronco, MOLETOM);
        d.membro(omFx, omFy, maFx, maFy, eBraco, MOLETOM);
        d.circulo(maFx, maFy, 5.5f, PELE);
        d.circulo(maTx, maTy, 5.5f, PELE);

        // Número do moletom
        d.ret(peX - 4, peY - 8, 8, 10, Paleta.ROSA);

        // Cabeça e boné, com a aba apontando para onde o corpo olha
        d.circulo(caX, caY, 12f, PELE);
        d.circulo(caX, caY + 4f, 12f, BONE);
        d.ret(caX - 12, caY - 1, 24, 5, BONE);
        ponto(0.26f + balanco, inclina * 1.3f, cabeca + 0.04f);
        d.membro(caX, caY + 2, ox, oy + 2, 6.5f, t);
        d.membro(caX, caY + 2, ox, oy + 2, 3.5f, Paleta.escuro(BONE, 0.2f));
    }

    private void jogadorCaido(Jogador j, float yaw, float tempo) {
        float t = j.tempoEstado();
        float gira = Math.min(1f, t / 0.3f);
        // O skate segue sozinho, rodando, para longe do dono.
        float dist = Math.min(1.5f, t * 2.2f);
        float bxS = j.x + (float) Math.cos(yaw) * dist;
        float byS = j.y + (float) Math.sin(yaw) * dist;
        shape(bxS, byS, j.z + 0.15f, yaw + t * 7f, t * 4f, 0f);

        referencial(j.x, j.y, j.z + 0.15f, yaw, gira * 1.5f);
        Color c = Paleta.TINTA;
        ponto(0, 0, 0.1f); float qx = ox, qy = oy;
        ponto(0, 0, 0.6f); float px = ox, py = oy;
        ponto(0, 0, 0.92f); float cx = ox, cy = oy;
        ponto(0.2f, 0.25f, -0.4f); float p1x = ox, p1y = oy;
        ponto(-0.2f, -0.2f, -0.4f); float p2x = ox, p2y = oy;
        ponto(0.5f, 0.2f, 0.65f); float m1x = ox, m1y = oy;
        ponto(-0.5f, -0.25f, 0.5f); float m2x = ox, m2y = oy;
        d.membro(qx, qy, p1x, p1y, 17, c);
        d.membro(qx, qy, p2x, p2y, 17, c);
        d.membro(qx, qy, px, py, 28, c);
        d.membro(px, py, m1x, m1y, 13, c);
        d.membro(px, py, m2x, m2y, 13, c);
        d.circulo(cx, cy, 15, c);
        d.membro(qx, qy, p1x, p1y, 12, CALCA);
        d.membro(qx, qy, p2x, p2y, 12, CALCA);
        d.membro(qx, qy, px, py, 23, MOLETOM);
        d.membro(px, py, m1x, m1y, 9, MOLETOM);
        d.membro(px, py, m2x, m2y, 9, MOLETOM);
        d.circulo(cx, cy, 12, PELE);
        d.circulo(cx + 2, cy + 3, 10, BONE);

        // Passarinhos de desenho animado
        for (int i = 0; i < 3; i++) {
            float a = tempo * 5f + i * 2.09f;
            float ex = cx + (float) Math.cos(a) * 24f;
            float ey = cy + 20f + (float) Math.sin(a) * 8f;
            estrela(ex, ey, 6f, Paleta.AMARELO);
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
        TipoRobo tipo = r.tipo;
        sombra(r.x, r.y, alturaChao, 0, 24f + tipo.raio * 28f);

        boolean atordoado = r.estado() == RoboSkatista.Estado.ATORDOADO;
        boolean preparando = r.estado() == RoboSkatista.Estado.PREPARO;
        boolean investindo = r.estado() == RoboSkatista.Estado.INVESTIDA;

        float tremor = preparando ? (float) Math.sin(tempo * 70f) * 0.05f : 0f;
        float pulinho = r.movendo() ? Math.abs((float) Math.sin(tempo * 12f)) * 0.03f : 0f;
        float inclinar = investindo ? 0.26f : atordoado ? -0.08f : 0f;

        escala = tipo.altura / 1.15f;
        float yaw = r.rumo + (atordoado ? (float) Math.sin(tempo * 9f) * 0.25f : 0f);
        referencial(r.x + tremor, r.y - tremor, pulinho, yaw, inclinar);

        Color corpo = corRobo(tipo);
        if (r.flashDano() > 0) corpo = Paleta.BRANCO;
        Color secundaria = Paleta.escuro(corpo, 0.35f);

        // Skate-robô: prancha larga com faíscas embaixo
        caixaLocal(0, 0, 0.14f, 0.52f, 0.2f, 0.05f, Paleta.hex("3a3355"));
        rodaRobo(0.34f, 0.17f); rodaRobo(0.34f, -0.17f); rodaRobo(-0.34f, 0.17f); rodaRobo(-0.34f, -0.17f);

        // Pernas curtas e corpo em lata
        caixaLocal(0, 0, 0.30f, 0.14f, 0.16f, 0.12f, secundaria);
        caixaLocal(0, 0, 0.62f, 0.27f, 0.30f, 0.22f, corpo);
        // Peito com luz
        ponto(0.28f, 0, 0.66f);
        if (fx * VX + fy * VY > -0.2f) {
            d.circulo(ox, oy, 7f * escala, Paleta.TINTA);
            d.circulo(ox, oy, 4.6f * escala, atordoado ? Paleta.CINZA : Paleta.CIANO);
        }

        // Braços
        ponto(0, 0.34f, 0.74f); float o1x = ox, o1y = oy;
        ponto(0.16f, 0.5f, 0.5f); float m1x = ox, m1y = oy;
        ponto(0, -0.34f, 0.74f); float o2x = ox, o2y = oy;
        ponto(0.16f, -0.5f, 0.5f); float m2x = ox, m2y = oy;
        if (preparando) {
            ponto(0.1f, 0.5f, 1.05f); m1x = ox; m1y = oy;
            ponto(0.1f, -0.5f, 1.05f); m2x = ox; m2y = oy;
        }
        d.membro(o1x, o1y, m1x, m1y, 9 * escala, Paleta.TINTA);
        d.membro(o2x, o2y, m2x, m2y, 9 * escala, Paleta.TINTA);
        d.membro(o1x, o1y, m1x, m1y, 5 * escala, Paleta.hex("c5c9e6"));
        d.membro(o2x, o2y, m2x, m2y, 5 * escala, Paleta.hex("c5c9e6"));

        // Cabeça com tela
        caixaLocal(0, 0, 1.0f, 0.22f, 0.24f, 0.16f, Paleta.claro(corpo, 0.15f));
        rosto(r, tempo, corpo);

        // Antena
        ponto(0, 0, 1.16f); float a0x = ox, a0y = oy;
        ponto(0, 0, 1.42f);
        d.linha(a0x, a0y, ox, oy, 3, Paleta.TINTA);
        boolean pisca = ((int) (tempo * 6f)) % 2 == 0;
        Color luz = preparando ? Paleta.VERMELHO : pisca ? Paleta.ROSA : Paleta.AMARELO;
        d.circulo(ox, oy, 5.5f, Paleta.TINTA);
        d.circulo(ox, oy, 3.8f, luz);

        if (tipo == TipoRobo.SINDICO) {
            // Gravata e prancheta de síndico
            ponto(0.28f, 0, 0.84f); float gx = ox, gy = oy;
            ponto(0.28f, 0, 0.5f);
            d.linha(gx, gy, ox, oy, 5, Paleta.VERMELHO);
            ponto(0.2f, -0.56f, 0.55f);
            d.ret(ox - 9, oy - 7, 18, 22, Paleta.TINTA);
            d.ret(ox - 7, oy - 5, 14, 18, Paleta.BRANCO);
            d.ret(ox - 5, oy + 6, 10, 2, Paleta.TINTA);
            d.ret(ox - 5, oy + 1, 10, 2, Paleta.TINTA);
        }
        if (tipo == TipoRobo.TANQUE) {
            // Ombreiras de blindagem
            caixaLocal(0, 0.36f, 0.82f, 0.13f, 0.08f, 0.08f, Paleta.AMARELO);
            caixaLocal(0, -0.36f, 0.82f, 0.13f, 0.08f, 0.08f, Paleta.AMARELO);
        }

        escala = 1f;
        tombo = 0f;
    }

    private void rodaRobo(float l, float s) {
        ponto(l, s, 0.06f);
        d.circulo(ox, oy, 5.5f * escala, Paleta.TINTA);
        d.circulo(ox, oy, 3.4f * escala, Paleta.LARANJA);
    }

    private void rosto(RoboSkatista r, float tempo, Color corpo) {
        boolean frenteVisivel = fx * VX + fy * VY > 0.02f;
        if (!frenteVisivel) return;
        // Tela do rosto na face frontal
        pQuad(0.225f, -0.18f, 0.9f, 0.225f, 0.18f, 0.9f, 0.225f, 0.18f, 1.1f, 0.225f, -0.18f, 1.1f, Paleta.TINTA);
        boolean atordoado = r.estado() == RoboSkatista.Estado.ATORDOADO;
        boolean bravo = r.estado() == RoboSkatista.Estado.PREPARO || r.estado() == RoboSkatista.Estado.INVESTIDA;
        Color olho = bravo ? Paleta.VERMELHO : atordoado ? Paleta.AMARELO : Paleta.LIMA;
        for (int lado = -1; lado <= 1; lado += 2) {
            float s = lado * 0.09f;
            ponto(0.23f, s, 1.0f);
            float x = ox, y = oy;
            if (atordoado) {
                d.linha(x - 4, y - 4, x + 4, y + 4, 2.5f, olho);
                d.linha(x - 4, y + 4, x + 4, y - 4, 2.5f, olho);
            } else {
                d.circulo(x, y, 3.8f * escala, olho);
            }
        }
    }

    private void pQuad(float l1, float s1, float h1, float l2, float s2, float h2,
                       float l3, float s3, float h3, float l4, float s4, float h4, Color c) {
        ponto(l1, s1, h1); float ax = ox, ay = oy;
        ponto(l2, s2, h2); float bx2 = ox, by2 = oy;
        ponto(l3, s3, h3); float cx = ox, cy = oy;
        ponto(l4, s4, h4);
        d.quad(ax, ay, bx2, by2, cx, cy, ox, oy, c);
    }

    /** Caixa orientada pelo referencial atual. (l,s,h) é o centro; a,b,c são as meias-medidas. */
    private void caixaLocal(float l, float s, float h, float a, float b, float c, Color cor) {
        // Normais dos quatro lados no plano do chão (o tombo é pequeno e pode ser ignorado aqui).
        desenharFace(l, s, h, a, b, c, fx, fy, 1, cor);
        desenharFace(l, s, h, a, b, c, -fx, -fy, 2, cor);
        desenharFace(l, s, h, a, b, c, lx, ly, 3, cor);
        desenharFace(l, s, h, a, b, c, -lx, -ly, 4, cor);
        // Topo
        ponto(l + a, s + b, h + c); float x1 = ox, y1 = oy;
        ponto(l + a, s - b, h + c); float x2 = ox, y2 = oy;
        ponto(l - a, s - b, h + c); float x3 = ox, y3 = oy;
        ponto(l - a, s + b, h + c); float x4 = ox, y4 = oy;
        d.quad(x1, y1, x2, y2, x3, y3, x4, y4, Paleta.claro(cor, 0.15f));
        float e = 2.2f;
        d.linha(x1, y1, x2, y2, e, Paleta.TINTA);
        d.linha(x2, y2, x3, y3, e, Paleta.TINTA);
        d.linha(x3, y3, x4, y4, e, Paleta.TINTA);
        d.linha(x4, y4, x1, y1, e, Paleta.TINTA);
    }

    private void desenharFace(float l, float s, float h, float a, float b, float c, float nx, float ny, int face, Color cor) {
        if (nx * VX + ny * VY <= 0.01f) return;
        float l1, s1, l2, s2;
        switch (face) {
            case 1: l1 = l + a; s1 = s - b; l2 = l + a; s2 = s + b; break;
            case 2: l1 = l - a; s1 = s - b; l2 = l - a; s2 = s + b; break;
            case 3: l1 = l - a; s1 = s + b; l2 = l + a; s2 = s + b; break;
            default: l1 = l - a; s1 = s - b; l2 = l + a; s2 = s - b; break;
        }
        ponto(l1, s1, h - c); float x1 = ox, y1 = oy;
        ponto(l2, s2, h - c); float x2 = ox, y2 = oy;
        ponto(l2, s2, h + c); float x3 = ox, y3 = oy;
        ponto(l1, s1, h + c); float x4 = ox, y4 = oy;
        // Sombreado simples pela orientação da face
        float luz = 0.12f + 0.25f * Math.abs(nx);
        d.quad(x1, y1, x2, y2, x3, y3, x4, y4, Paleta.escuro(cor, luz));
        float e = 2.2f;
        d.linha(x1, y1, x2, y2, e, Paleta.TINTA);
        d.linha(x2, y2, x3, y3, e, Paleta.TINTA);
        d.linha(x3, y3, x4, y4, e, Paleta.TINTA);
        d.linha(x4, y4, x1, y1, e, Paleta.TINTA);
    }

    private static Color corRobo(TipoRobo tipo) {
        switch (tipo) {
            case ROLIMA: return Paleta.hex("ffb02e");
            case FAISCA: return Paleta.hex("3fd0ff");
            case TANQUE: return Paleta.hex("c04fff");
            default: return Paleta.hex("8f9bb8");
        }
    }

    // ---------------------------------------------------------------------------------- itens

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
