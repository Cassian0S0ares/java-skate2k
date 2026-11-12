package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

import java.util.List;
import java.util.Random;

/** Inimigo: patrulha, persegue e dá investidas avisadas. Só a lógica; quem desenha é o render. */
public final class RoboSkatista {
    public enum Estado { PATRULHA, PERSEGUICAO, PREPARO, INVESTIDA, ATORDOADO }

    private static final String[] FALAS = {
        "BIP BOP! SAI DA PISTA!", "ERRO 404: MANOBRA", "SEU KICKFLIP É BETA",
        "CALCULANDO TOMBO...", "ATUALIZANDO ÓDIO...", "MEU PAI É UMA TORRADEIRA"
    };
    private static final float[] DESVIOS = {0f, 0.8f, -0.8f, 1.6f, -1.6f, 2.4f, -2.4f};

    public final TipoRobo tipo;
    public float x;
    public float y;
    public float rumo;
    private int vida;
    private Estado estado = Estado.PATRULHA;
    private float tempoEstado;
    private float esperaInvestida;
    private float alvoX;
    private float alvoY;
    private float esperaAlvo;
    private float esperaFala;
    private float dirInvestidaX;
    private float dirInvestidaY;
    private float flashDano;
    private boolean movendo;
    private final Charlietron chefe;

    public RoboSkatista(TipoRobo tipo, float x, float y, Random r) {
        this.tipo = tipo;
        this.chefe = tipo == TipoRobo.CHARLIETRON ? new Charlietron() : null;
        this.x = x;
        this.y = y;
        this.vida = tipo.vida;
        this.alvoX = x;
        this.alvoY = y;
        this.rumo = r.nextFloat() * 6.28f;
        this.esperaInvestida = 2f + r.nextFloat() * 2f;
        this.esperaFala = 3f + r.nextFloat() * 9f;
    }

    public void atualizar(float dt, Jogador j, Cenario cen, float fatorVelocidade, float intervaloInvestida,
                          Random r, List<EventoJogo> eventos) {
        tempoEstado += dt;
        esperaInvestida -= dt;
        esperaFala -= dt;
        flashDano = Math.max(0, flashDano - dt);
        movendo = false;
        if (chefe != null) {
            chefe.atualizar(dt, this, j, cen, eventos);
            movendo = chefe.estado() == Charlietron.Estado.RODANDO;
            return;
        }

        float dx = j.x - x;
        float dy = j.y - y;
        float dist = (float) Math.hypot(dx, dy);
        boolean jogadorDisponivel = j.estado() != Jogador.Estado.CAIDO && !j.invulneravel();
        float vel = tipo.velocidade * fatorVelocidade;

        if (esperaFala <= 0) {
            esperaFala = 7f + r.nextFloat() * 10f;
            if (dist < 12f) {
                eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_FALA, x, y, tipo.altura + 0.5f,
                        FALAS[r.nextInt(FALAS.length)], 0));
            }
        }

        switch (estado) {
            case PATRULHA:
                patrulhar(dt, cen, vel * 0.6f, r);
                if (dist < 11f && jogadorDisponivel) trocar(Estado.PERSEGUICAO);
                break;
            case PERSEGUICAO:
                if (!jogadorDisponivel || dist > 15f) {
                    trocar(Estado.PATRULHA);
                    break;
                }
                mover(dt, cen, dx / Math.max(dist, 0.01f), dy / Math.max(dist, 0.01f), vel);
                if (dist < 5.5f && esperaInvestida <= 0) {
                    trocar(Estado.PREPARO);
                    eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_ALERTA, x, y, tipo.altura,
                            "!".repeat(tipo.danoInvestida), tipo.danoInvestida));
                }
                break;
            case PREPARO:
                rumo = (float) Math.atan2(dy, dx);
                if (tempoEstado >= TEMPO_PREPARO_INVESTIDA) {
                    dirInvestidaX = (float) Math.cos(rumo);
                    dirInvestidaY = (float) Math.sin(rumo);
                    trocar(Estado.INVESTIDA);
                }
                break;
            case INVESTIDA:
                if (!avancar(dt, cen, dirInvestidaX, dirInvestidaY, vel * MULTIPLICADOR_INVESTIDA)) {
                    atordoar(0.9f);
                } else if (tempoEstado >= TEMPO_INVESTIDA) {
                    atordoar(0.7f);
                }
                esperaInvestida = intervaloInvestida;
                break;
            default:
                if (tempoEstado >= duracaoAtordoado) trocar(Estado.PATRULHA);
                break;
        }
    }

    private float duracaoAtordoado = TEMPO_ATORDOADO;

    private void atordoar(float duracao) {
        duracaoAtordoado = duracao;
        trocar(Estado.ATORDOADO);
    }

    private void patrulhar(float dt, Cenario cen, float vel, Random r) {
        esperaAlvo -= dt;
        float dx = alvoX - x;
        float dy = alvoY - y;
        float d = (float) Math.hypot(dx, dy);
        if (d < 0.6f || esperaAlvo <= 0) {
            alvoX = Math.max(1f, Math.min(cen.largura - 1f, x + (r.nextFloat() - 0.5f) * 12f));
            alvoY = Math.max(1f, Math.min(cen.profundidade - 1f, y + (r.nextFloat() - 0.5f) * 12f));
            esperaAlvo = 4f;
            return;
        }
        mover(dt, cen, dx / d, dy / d, vel);
    }

    /** Anda desviando dos obstáculos: tenta a direção desejada e depois ângulos abertos. */
    private void mover(float dt, Cenario cen, float dirX, float dirY, float vel) {
        float base = (float) Math.atan2(dirY, dirX);
        for (float desvio : DESVIOS) {
            float a = base + desvio;
            if (avancar(dt, cen, (float) Math.cos(a), (float) Math.sin(a), vel)) return;
        }
    }

    private boolean avancar(float dt, Cenario cen, float dirX, float dirY, float vel) {
        float nx = x + dirX * vel * dt;
        float ny = y + dirY * vel * dt;
        float frenteX = nx + dirX * tipo.raio;
        float frenteY = ny + dirY * tipo.raio;
        if (frenteX < 0.5f || frenteY < 0.5f || frenteX > cen.largura - 0.5f || frenteY > cen.profundidade - 0.5f) {
            return false;
        }
        if (cen.alturaEm(frenteX, frenteY) > DEGRAU) return false;
        x = nx;
        y = ny;
        rumo = (float) Math.atan2(dirY, dirX);
        movendo = true;
        return true;
    }

    private void trocar(Estado novo) {
        estado = novo;
        tempoEstado = 0;
    }

    /** Aplica o dano do pisão. Devolve true se o robô foi destruído. */
    public boolean receberPisao(int dano) {
        vida -= dano;
        vida = Math.max(0, vida);
        flashDano = 0.3f;
        atordoar(TEMPO_ATORDOADO);
        esperaInvestida = Math.max(esperaInvestida, 2f);
        if (chefe != null) chefe.derrubar(vida <= 0);
        return vida <= 0;
    }

    public void atordoarPorTrombada() {
        atordoar(0.8f);
        if (chefe != null) chefe.derrubar(false);
    }

    public boolean perigoso() {
        if (chefe != null) return vida > 0 && !chefe.noAr() && !chefe.vulneravel();
        return estado != Estado.ATORDOADO;
    }

    public Charlietron chefe() { return chefe; }
    public float z() { return chefe == null ? 0f : chefe.z(); }

    public Estado estado() {
        if (chefe == null) return estado;
        switch (chefe.estado()) {
            case PREPARO_MANOBRA: case PREPARO_PISAO: return Estado.PREPARO;
            case PISAO: return Estado.INVESTIDA;
            case CAIDO: case DERROTADO: case RECUPERANDO: return Estado.ATORDOADO;
            default: return Estado.PERSEGUICAO;
        }
    }
    public float tempoEstado() { return tempoEstado; }
    public int vida() { return vida; }
    public boolean movendo() { return movendo; }
    public float flashDano() { return flashDano; }
}
