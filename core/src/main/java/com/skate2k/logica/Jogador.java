package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

import java.util.List;
import java.util.Random;

/** O skatista: máquina de estados (chão, ar, grind, caído) e física. Não desenha nem lê teclado. */
public final class Jogador {
    public enum Estado { NO_CHAO, NO_AR, GRIND, CAIDO }

    /** Avisos para o mundo do jogo. */
    public interface Ouvinte {
        void aoPular(Jogador j);
        void aoPousar(Jogador j, List<Manobra> manobras, boolean limpo);
        void aoCair(Jogador j, String motivo);
        void aoUsarCapacete(Jogador j);
        void aoIniciarGrind(Jogador j, TipoElemento tipo);
        void aoSairGrind(Jogador j);
        void aoUsarElemento(Jogador j, TipoElemento tipo);
        void aoNegar(Jogador j, String mensagem);
    }

    private static final String[] MOTIVOS_BATIDA = {
        "BEIJOU O OBSTÁCULO!", "A PAREDE VENCEU!", "FREOU COM A CARA!"
    };

    public float x;
    public float y;
    public float z;
    public float vx;
    public float vy;
    public float vz;
    public float rumo;

    private final Ouvinte ouvinte;
    private final Random aleatorio;
    private final DetectorManobras detector = new DetectorManobras();

    private Estado estado = Estado.NO_CHAO;
    private float tempoEstado;
    private float cargaPulo;
    private boolean carregandoPulo;
    private boolean puloAnterior;
    private boolean fakie;
    private int nivelEstilo = 1;

    private float potencial = 1f;
    private String nomePotencial;
    private float distanciaSobreEscada;

    private TrilhoGrind trilho;
    private float grindT;
    private int grindSentido;
    private float grindVelocidade;
    private float grindTempo;
    private boolean boardslide;
    private float equilibrio;
    private float velEquilibrio;
    private float esperaGrind;

    private ElementoCenario elementoChao;
    private float vzChao;
    private float tempoInvulneravel;
    private float tempoCafe;
    private float tempoMola;
    private boolean capacete;
    private float esperaNegacao;
    private int vida = VIDA_MAXIMA;

    public Jogador(float x, float y, Ouvinte ouvinte, Random aleatorio) {
        this.x = x;
        this.y = y;
        this.ouvinte = ouvinte;
        this.aleatorio = aleatorio;
        this.rumo = (float) (Math.PI / 4);
    }

    public void atualizar(float dt, Comandos c, Cenario cen, Restricao r, boolean cameraMovendo) {
        tempoEstado += dt;
        tempoInvulneravel = Math.max(0, tempoInvulneravel - dt);
        tempoCafe = Math.max(0, tempoCafe - dt);
        tempoMola = Math.max(0, tempoMola - dt);
        esperaGrind = Math.max(0, esperaGrind - dt);
        esperaNegacao = Math.max(0, esperaNegacao - dt);

        switch (estado) {
            case NO_CHAO: atualizarChao(dt, c, cen, r, cameraMovendo); break;
            case NO_AR: atualizarAr(dt, c, cen, r, cameraMovendo); break;
            case GRIND: atualizarGrind(dt, c, cen, r, cameraMovendo); break;
            default: atualizarCaido(dt, cen); break;
        }
        puloAnterior = c.pulo;
    }

    // ------------------------------------------------------------------------------------ chão

    private void atualizarChao(float dt, Comandos c, Cenario cen, Restricao r, boolean cameraMovendo) {
        float velMax = velocidadeMaxima() * cen.pisoEm(x, y).fatorVelocidade;
        float vel = (float) Math.hypot(vx, vy);

        float wx = c.eixoX + c.eixoY;
        float wy = c.eixoY - c.eixoX;
        boolean temEntrada = wx != 0 || wy != 0;
        float aderencia = r.fatorAderencia;
        // A aderência reduzida afeta menos o freio e o giro do que o escorregão lateral.
        float fatorControle = 0.45f + 0.55f * aderencia;

        if (temEntrada) {
            float alvo = (float) Math.atan2(wy, wx);
            float dif = diferencaAngulo(rumo, alvo);
            if (vel > 2f && Math.abs(dif) > 2.2f) {
                vel -= FREIO * fatorControle * dt;
            } else {
                vel += ACELERACAO * dt;
            }
            float passoGiro = GIRO_CHAO_RAD * fatorControle * dt;
            rumo += Math.max(-passoGiro, Math.min(passoGiro, dif));
        } else {
            vel -= ATRITO * (0.4f + 0.6f * aderencia) * dt;
        }
        if (vel > velMax) vel = Math.max(velMax, vel - 8f * dt);
        if (vel < 0) vel = 0;

        ElementoCenario abaixo = cen.elementoEm(x, y);
        if (abaixo != null && abaixo.tipo == TipoElemento.ESCADA) {
            vel = Math.max(0, vel - ATRITO_ESCADA * dt);
        }

        float k = 1f - (float) Math.exp(-ADERENCIA * aderencia * dt);
        vx += ((float) Math.cos(rumo) * vel - vx) * k;
        vy += ((float) Math.sin(rumo) * vel - vy) * k;
        if (vel == 0 && !temEntrada) {
            vx *= 1f - k;
            vy *= 1f - k;
        }

        boolean podePular = r.permitePuloLivre() || cen.temTrilhoPerto(x, y, ALCANCE_PULO_SEM_PULO);
        if (c.pulo && !puloAnterior && !podePular) {
            negar("SEM PULO! SÓ PERTO DE TRILHO");
        }
        if (c.pulo && podePular) {
            carregandoPulo = true;
            cargaPulo = Math.min(1f, cargaPulo + dt / TEMPO_CARGA_PULO);
        }

        moverHorizontal(dt, cen);
        if (estado != Estado.NO_CHAO) return;

        float h = cen.alturaEm(x, y);
        if (h < z - QUEDA_VIRA_VOO) {
            // Saiu pela borda: a rampa arremessa; segurar o pulo soma ao lançamento.
            float lancamento = vzChao;
            if (carregandoPulo) lancamento += forcaPulo();
            carregandoPulo = false;
            cargaPulo = 0;
            iniciarVoo(lancamento, elementoChao);
            return;
        }
        z = h;
        elementoChao = cen.elementoEm(x, y);
        vzChao = 0;
        if (elementoChao != null && elementoChao.tipo == TipoElemento.RAMPA) {
            float aFavor = vx * elementoChao.direcao.dx + vy * elementoChao.direcao.dy;
            vzChao = Math.max(0, aFavor * elementoChao.inclinacao() * CHUTE_RAMPA);
        }

        if (!c.pulo && carregandoPulo) {
            float forca = forcaPulo() + vzChao;
            carregandoPulo = false;
            cargaPulo = 0;
            iniciarVoo(forca, elementoChao);
            ouvinte.aoPular(this);
            return;
        }

        // Manobra no chão: kickflip/impossible sem segurar o pulo dão um saltinho já com o flip.
        boolean querFlip = c.kickflip || c.impossible;
        if (querFlip && !carregandoPulo && !c.pulo && r.permiteManobraAerea()
                && (!r.exigeCameraEmMovimento() || cameraMovendo)) {
            iniciarVoo(FORCA_PULO_BAIXO + vzChao, elementoChao);
            ouvinte.aoPular(this);
            if (c.kickflip) detector.comandarKickflip();
            if (c.impossible) detector.comandarImpossible();
        }
    }

    private float forcaPulo() {
        float forca = FORCA_PULO + (FORCA_PULO_MAX - FORCA_PULO) * cargaPulo;
        return tempoMola > 0 ? forca * BONUS_TENIS_MOLA : forca;
    }

    private void iniciarVoo(float velocidadeVertical, ElementoCenario origem) {
        estado = Estado.NO_AR;
        tempoEstado = 0;
        vz = velocidadeVertical;
        detector.iniciarVoo();
        distanciaSobreEscada = 0;
        if (origem != null && origem.tipo.concedePotencial()) {
            ativarPotencial(origem.tipo);
        }
    }

    private void ativarPotencial(TipoElemento tipo) {
        potencial = Math.max(potencial, tipo.potencial);
        nomePotencial = tipo.nomePotencializador;
        ouvinte.aoUsarElemento(this, tipo);
    }

    // -------------------------------------------------------------------------------------- ar

    private void atualizarAr(float dt, Comandos c, Cenario cen, Restricao r, boolean cameraMovendo) {
        float wx = c.eixoX + c.eixoY;
        float wy = c.eixoY - c.eixoX;
        if (wx != 0 || wy != 0) {
            float n = (float) Math.hypot(wx, wy);
            vx += wx / n * CONTROLE_AR * dt;
            vy += wy / n * CONTROLE_AR * dt;
            float vel = (float) Math.hypot(vx, vy);
            float limite = velocidadeMaxima() * 1.3f;
            if (vel > limite) {
                vx *= limite / vel;
                vy *= limite / vel;
            }
        }
        if (vx != 0 || vy != 0) rumo = (float) Math.atan2(vy, vx);

        boolean querManobrar = c.kickflip || c.impossible || c.giro != 0 || c.grab;
        boolean aereoPermitido = r.permiteManobraAerea();
        boolean filmando = !r.exigeCameraEmMovimento() || cameraMovendo;
        if (querManobrar && !aereoPermitido) {
            negar("NO AR NÃO VALE! VAI PRO TRILHO");
        } else if (querManobrar && !filmando) {
            negar("A CÂMERA NÃO VIU!");
        }
        boolean liberado = aereoPermitido && filmando;
        if (liberado && c.kickflip) detector.comandarKickflip();
        if (liberado && c.impossible) detector.comandarImpossible();
        detector.atualizar(dt, liberado ? c.giro : 0, liberado && c.grab);

        vz -= GRAVIDADE * dt;
        z += vz * dt;
        moverHorizontal(dt, cen);
        if (estado != Estado.NO_AR) return;

        checarPassagemEscada(dt, cen, r);

        if (esperaGrind <= 0 && vz <= 1.5f && tentarEncaixarGrind(cen, r)) return;

        float h = cen.alturaEm(x, y);
        if (z <= h && vz <= 0) pousar(h, r);
    }

    private void checarPassagemEscada(float dt, Cenario cen, Restricao r) {
        ElementoCenario abaixo = cen.elementoEm(x, y);
        if (abaixo == null || abaixo.tipo != TipoElemento.ESCADA || detector.passouEscada()) return;
        if (z <= abaixo.alturaEm(x, y) + 0.2f) return;
        distanciaSobreEscada += (float) Math.hypot(vx, vy) * dt;
        if (distanciaSobreEscada >= abaixo.comprimentoNaDirecao() * 0.6f && r.permiteManobraAerea()) {
            detector.marcarPassagemEscada();
            ativarPotencial(TipoElemento.ESCADA);
        }
    }

    private void pousar(float altura, Restricao r) {
        DetectorManobras.Pouso pouso = detector.avaliarPouso(r.fatorTolerancia, potencial);
        z = altura;
        vz = 0;
        if (!pouso.valido) {
            cair(pouso.motivoQueda);
            return;
        }
        estado = Estado.NO_CHAO;
        tempoEstado = 0;
        if (Math.round(Math.abs(detector.giroGraus()) / 180f) % 2 == 1) fakie = !fakie;
        detector.descartar();
        ouvinte.aoPousar(this, pouso.manobras, pouso.limpo);
        limparPotencial();
    }

    private void limparPotencial() {
        potencial = 1f;
        nomePotencial = null;
    }

    // ------------------------------------------------------------------------------------ grind

    private boolean tentarEncaixarGrind(Cenario cen, Restricao r) {
        TrilhoGrind melhor = null;
        float menor = RAIO_ENCAIXE_GRIND;
        for (TrilhoGrind t : cen.trilhos) {
            float d = t.distancia(x, y);
            if (d > menor) continue;
            float zT = t.zEm(t.projetar(x, y));
            if (z < zT - 0.45f || z > zT + 0.55f) continue;
            menor = d;
            melhor = t;
        }
        if (melhor == null) return false;

        // Encaixar no trilho vale como pouso: rotação pela metade também derruba.
        DetectorManobras.Pouso pouso = detector.avaliarPouso(r.fatorTolerancia, potencial);
        if (!pouso.valido) {
            cair(pouso.motivoQueda);
            return true;
        }
        detector.descartar();
        if (!pouso.manobras.isEmpty()) ouvinte.aoPousar(this, pouso.manobras, pouso.limpo);

        trilho = melhor;
        grindT = melhor.projetar(x, y);
        float dirX = (melhor.bx - melhor.ax) / melhor.comprimento;
        float dirY = (melhor.by - melhor.ay) / melhor.comprimento;
        float aFavor = vx * dirX + vy * dirY;
        float vel = (float) Math.hypot(vx, vy);
        float anguloRelativo = vel > 0.1f ? (float) Math.acos(Math.min(1f, Math.abs(aFavor) / vel)) : 0f;
        boardslide = anguloRelativo > ANGULO_BOARDSLIDE_RAD;
        grindSentido = aFavor > 0.05f ? 1 : aFavor < -0.05f ? -1 : (grindT < 0.5f ? 1 : -1);
        grindVelocidade = Math.max(VELOCIDADE_MIN_GRIND, vel * 0.9f);
        grindTempo = 0;
        equilibrio = aleatorio.nextBoolean() ? 0.06f : -0.06f;
        velEquilibrio = 0;
        estado = Estado.GRIND;
        tempoEstado = 0;
        vz = 0;
        limparPotencial();
        ativarPotencial(melhor.tipo);
        ouvinte.aoIniciarGrind(this, melhor.tipo);
        posicionarNoTrilho();
        return true;
    }

    private void atualizarGrind(float dt, Comandos c, Cenario cen, Restricao r, boolean cameraMovendo) {
        boolean filmando = !r.exigeCameraEmMovimento() || cameraMovendo;
        if (filmando) grindTempo += dt;

        float sinal = equilibrio >= 0 ? 1f : -1f;
        float deriva = (DERIVA_EQUILIBRIO + DERIVA_POR_SEGUNDO * tempoEstado) * r.fatorInstabilidade;
        velEquilibrio += (sinal * deriva + c.eixoX * FORCA_EQUILIBRIO) * dt;
        velEquilibrio *= (float) Math.exp(-AMORTECE_EQUILIBRIO * dt);
        equilibrio += velEquilibrio * dt;

        grindVelocidade = Math.max(VELOCIDADE_MIN_GRIND, grindVelocidade - 0.5f * dt);
        grindT += grindSentido * grindVelocidade * dt / trilho.comprimento;
        posicionarNoTrilho();

        if (Math.abs(equilibrio) >= 1f) {
            trilho = null;
            ouvinte.aoSairGrind(this);
            cair("PERDEU O EQUILÍBRIO!");
            return;
        }
        if (c.pulo && !puloAnterior) {
            sairDoGrind(FORCA_PULO);
            ouvinte.aoPular(this);
        } else if (grindT <= 0f || grindT >= 1f) {
            sairDoGrind(LANCAMENTO_FIM_GRIND);
        }
    }

    private void posicionarNoTrilho() {
        float t = Math.max(0f, Math.min(1f, grindT));
        x = trilho.xEm(t);
        y = trilho.yEm(t);
        z = trilho.zEm(t);
        float dirX = (trilho.bx - trilho.ax) / trilho.comprimento * grindSentido;
        float dirY = (trilho.by - trilho.ay) / trilho.comprimento * grindSentido;
        vx = dirX * grindVelocidade;
        vy = dirY * grindVelocidade;
        rumo = (float) Math.atan2(vy, vx);
    }

    private void sairDoGrind(float velocidadeVertical) {
        detector.registrarGrind(Manobra.grind(boardslide, grindTempo, potencial));
        TipoElemento tipo = trilho.tipo;
        trilho = null;
        esperaGrind = ESPERA_REENCAIXE;
        estado = Estado.NO_AR;
        tempoEstado = 0;
        vz = velocidadeVertical;
        detector.iniciarVoo();
        distanciaSobreEscada = 0;
        potencial = tipo.potencial;
        nomePotencial = tipo.nomePotencializador;
        ouvinte.aoSairGrind(this);
    }

    // ----------------------------------------------------------------------------------- queda

    /** Derruba o jogador. O capacete absorve um tombo e preserva o combo. */
    public void cair(String motivo) {
        carregandoPulo = false;
        cargaPulo = 0;
        detector.descartar();
        limparPotencial();
        trilho = null;
        if (capacete) {
            capacete = false;
            estado = z > 0.05f && vz != 0 ? Estado.NO_AR : Estado.NO_CHAO;
            tempoInvulneravel = TEMPO_INVULNERAVEL;
            tempoEstado = 0;
            ouvinte.aoUsarCapacete(this);
            return;
        }
        estado = Estado.CAIDO;
        tempoEstado = 0;
        ouvinte.aoCair(this, motivo);
    }

    private void atualizarCaido(float dt, Cenario cen) {
        float k = (float) Math.exp(-3.5f * dt);
        vx *= k;
        vy *= k;
        float h = cen.alturaEm(x, y);
        if (z > h || vz > 0) {
            vz -= GRAVIDADE * dt;
            z += vz * dt;
        }
        if (z <= h) {
            z = h;
            vz = 0;
        }
        moverHorizontal(dt, cen);
        if (tempoEstado >= TEMPO_CAIDO && vivo()) {
            estado = Estado.NO_CHAO;
            tempoEstado = 0;
            tempoInvulneravel = TEMPO_INVULNERAVEL;
            vx = 0;
            vy = 0;
        }
    }

    // ---------------------------------------------------------------------------------- pisão

    /** Pisão em robô: o que estava em andamento vale agora, e o jogador quica de volta ao ar. */
    public List<Manobra> quicarNoRobo() {
        List<Manobra> manobras = detector.consumirNoPisao(potencial);
        limparPotencial();
        estado = Estado.NO_AR;
        tempoEstado = 0;
        vz = QUIQUE_PISAO;
        esperaGrind = 0.15f;
        distanciaSobreEscada = 0;
        return manobras;
    }

    /** Empurrão de uma trombada leve: afasta o jogador do ponto de contato. */
    public void empurrar(float deX, float deY) {
        float dx = x - deX;
        float dy = y - deY;
        float d = Math.max(0.01f, (float) Math.hypot(dx, dy));
        vx = dx / d * FORCA_EMPURRAO;
        vy = dy / d * FORCA_EMPURRAO;
        rumo = (float) Math.atan2(vy, vx);
        tempoInvulneravel = Math.max(tempoInvulneravel, INVULNERAVEL_EMPURRAO);
    }

    /** Puxa de leve o jogador para o robô logo abaixo dele, deixando o pisão mais gostoso. */
    public void atrairPara(float alvoX, float alvoY, float dt) {
        float dx = alvoX - x;
        float dy = alvoY - y;
        float d = (float) Math.hypot(dx, dy);
        if (d < 0.05f) return;
        vx += dx / d * IMA_PISAO * dt;
        vy += dy / d * IMA_PISAO * dt;
    }

    // -------------------------------------------------------------------------------- utilidades

    private void moverHorizontal(float dt, Cenario cen) {
        float nx = x + vx * dt;
        float sx = vx > 0 ? RAIO_COLISAO : -RAIO_COLISAO;
        if (cen.alturaEm(nx + sx, y) > z + DEGRAU) {
            float impacto = Math.abs(vx);
            vx *= -0.2f;
            baterSeForte(impacto);
            if (estado == Estado.CAIDO) return;
        } else {
            x = nx;
        }
        float ny = y + vy * dt;
        float sy = vy > 0 ? RAIO_COLISAO : -RAIO_COLISAO;
        if (cen.alturaEm(x, ny + sy) > z + DEGRAU) {
            float impacto = Math.abs(vy);
            vy *= -0.2f;
            baterSeForte(impacto);
        } else {
            y = ny;
        }
        float limX = cen.largura - 0.3f;
        float limY = cen.profundidade - 0.3f;
        if (x < 0.3f) { x = 0.3f; vx = 0; }
        if (y < 0.3f) { y = 0.3f; vy = 0; }
        if (x > limX) { x = limX; vx = 0; }
        if (y > limY) { y = limY; vy = 0; }
    }

    private void baterSeForte(float impacto) {
        if (estado == Estado.NO_CHAO && impacto >= VELOCIDADE_BATIDA) {
            cair(MOTIVOS_BATIDA[aleatorio.nextInt(MOTIVOS_BATIDA.length)]);
        }
    }

    private void negar(String mensagem) {
        if (esperaNegacao <= 0) {
            esperaNegacao = 1.2f;
            ouvinte.aoNegar(this, mensagem);
        }
    }

    public float velocidadeMaxima() {
        float v = VELOCIDADE_MAX + BONUS_VELOCIDADE_NIVEL * (nivelEstilo - 1);
        return tempoCafe > 0 ? v + BONUS_VELOCIDADE_CAFE : v;
    }

    public static float diferencaAngulo(float de, float para) {
        float d = (para - de) % ((float) Math.PI * 2f);
        if (d > Math.PI) d -= (float) Math.PI * 2f;
        if (d < -Math.PI) d += (float) Math.PI * 2f;
        return d;
    }

    // ---------------------------------------------------------------------------- efeitos de item

    public void perderVida(int dano) {
        vida = Math.max(0, vida - dano);
        tempoInvulneravel = Math.max(tempoInvulneravel, INVULNERAVEL_EMPURRAO);
    }

    public void curar(int pontos) {
        vida = Math.min(VIDA_MAXIMA, vida + pontos);
    }

    public int vida() { return vida; }
    public boolean vivo() { return vida > 0; }
    public void beberCafe() { tempoCafe = 8f; }
    public void calcarTenisMola() { tempoMola = 10f; }
    public void colocarCapacete() { capacete = true; }
    public void definirNivelEstilo(int nivel) { nivelEstilo = nivel; }

    // ------------------------------------------------------------------------- leitura de estado

    public Estado estado() { return estado; }
    public float tempoEstado() { return tempoEstado; }
    public float cargaPulo() { return cargaPulo; }
    public boolean carregandoPulo() { return carregandoPulo; }
    public boolean fakie() { return fakie; }
    public boolean boardslide() { return estado == Estado.GRIND && boardslide; }
    public float equilibrio() { return equilibrio; }
    public DetectorManobras detector() { return detector; }
    public float potencial() { return potencial; }
    public String nomePotencial() { return nomePotencial; }
    public boolean invulneravel() { return tempoInvulneravel > 0; }
    public float tempoCafe() { return tempoCafe; }
    public float tempoMola() { return tempoMola; }
    public boolean temCapacete() { return capacete; }
    public float grindTempo() { return grindTempo; }
    public boolean noAr() { return estado == Estado.NO_AR; }
}
