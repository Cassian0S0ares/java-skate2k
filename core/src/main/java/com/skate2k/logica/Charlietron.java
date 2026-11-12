package com.skate2k.logica;

import java.util.List;

/** Cérebro do chefe: manobras pontuam no pouso; o pisão tem alvo anunciado e trajetória fixa. */
public final class Charlietron {
    public enum Estado { RODANDO, PREPARO_MANOBRA, MANOBRA, PREPARO_PISAO, PISAO, GRIND, RECUPERANDO, CAIDO, DERROTADO }
    private static final TipoManobra[] REPERTORIO = {TipoManobra.KICKFLIP, TipoManobra.IMPOSSIBLE,
        TipoManobra.GIRO_360, TipoManobra.GRAB, TipoManobra.HARDFLIP, TipoManobra.VARIAL,
        TipoManobra.DOUBLE_KICKFLIP, TipoManobra.DOUBLE_IMPOSSIBLE, TipoManobra.GIRO_540,
        TipoManobra.TRIPLE_KICKFLIP, TipoManobra.GIRO_720, TipoManobra.GIRO_180,
        TipoManobra.GRIND_5050, TipoManobra.BOARDSLIDE, TipoManobra.GAP_ESCADA, TipoManobra.VOO_LONGO};
    private Estado estado = Estado.RODANDO;
    private TipoManobra manobra = TipoManobra.KICKFLIP;
    private float tempo, z, vz, zAnterior, alvoX, alvoY, vx, vy, duracaoVoo, tempoVoo;
    private int pontos, rodada, proximaManobra;
    private TrilhoGrind trilho;
    private float progressoTrilho;
    private float alvoZ;
    private boolean passouEscada;
    /** 0 = inteiro, 1 = ferido (2/3 da vida), 2 = furioso (1/3 da vida): fica mais rápido e agressivo. */
    private int furia;
    private float preparo = 0.85f;
    private int encadeados;
    private int estiloJogador;

    void atualizar(float dt, RoboSkatista robo, Jogador jogador, Cenario c, List<EventoJogo> eventos) {
        zAnterior = z;
        tempo += dt;
        atualizarFuria(robo, eventos);
        if (estado == Estado.DERROTADO || estado == Estado.CAIDO) {
            cairAteChao(dt, robo, c);
            if (estado == Estado.CAIDO && tempo > 1.6f) trocar(Estado.RODANDO);
            return;
        }
        if (estado == Estado.RODANDO) {
            moverNoChao(dt, robo, jogador, c);
            float distancia = (float) Math.hypot(robo.x - jogador.x, robo.y - jogador.y);
            if (tempo > 2.4f - 0.6f * furia && distancia < 10f + 3f * furia) {
                int vez = rodada++;
                // Esperto: quem fica colado nele é punido na hora; furioso quase só pisa.
                boolean padrao = furia == 0 ? vez % 2 == 1 : furia == 1 ? vez % 3 != 0 : vez % 4 != 0;
                boolean punir = furia >= 1 && distancia < 5f;
                // Lê o placar: atrás no estilo, foca em manobrar; na frente, aperta o jogador com pisões.
                boolean atrasado = pontos + 200 < estiloJogador;
                boolean adiantado = pontos > estiloJogador + 300 && furia >= 1;
                if (atrasado && !punir) padrao = false;
                else if (adiantado) padrao = true;
                boolean ataque = (padrao || punir) && jogador.vivo() && jogador.estado() != Jogador.Estado.CAIDO;
                mirar(robo, jogador, c);
                encadeados = 0;
                preparo = 0.85f - 0.13f * furia;
                if (!ataque) manobra = candidata(true);
                trocar(ataque ? Estado.PREPARO_PISAO : Estado.PREPARO_MANOBRA);
                eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_ALERTA, robo.x, robo.y, z + 2f,
                        ataque ? "PISÃO! SAIA DA MIRA!" : "CHARLIETRON: " + manobra.nome, 1));
            }
        } else if (estado == Estado.PREPARO_PISAO || estado == Estado.PREPARO_MANOBRA) {
            if (tempo < preparo) return;
            if (estado == Estado.PREPARO_MANOBRA && tentarGrind(robo, c)) return;
            boolean ataque = estado == Estado.PREPARO_PISAO;
            vz = ataque ? 11.5f : manobra == TipoManobra.VOO_LONGO ? 14f : 10.5f;
            duracaoVoo = 2f * vz / ConstantesFisica.GRAVIDADE;
            tempoVoo = 0;
            passouEscada = false;
            vx = ataque ? (alvoX - robo.x) / (duracaoVoo - 0.17f) : (float) Math.cos(robo.rumo) * 2f;
            vy = ataque ? (alvoY - robo.y) / (duracaoVoo - 0.17f) : (float) Math.sin(robo.rumo) * 2f;
            if (ataque) robo.rumo = (float) Math.atan2(vy, vx);
            trocar(ataque ? Estado.PISAO : Estado.MANOBRA);
        } else if (noAr()) {
            tempoVoo += dt;
            vz -= ConstantesFisica.GRAVIDADE * dt;
            z += vz * dt;
            float nx = limitar(robo.x + vx * dt, c.largura), ny = limitar(robo.y + vy * dt, c.profundidade);
            if (c.alturaEm(nx, ny) <= z + 0.2f) { robo.x = nx; robo.y = ny; }
            for (ElementoCenario e : c.elementos)
                if (e.tipo == TipoElemento.ESCADA && e.contem(robo.x, robo.y)) passouEscada = true;
            float chao = c.alturaEm(robo.x, robo.y);
            if (vz < 0 && z <= chao) {
                z = chao; vz = 0;
                if (estado == Estado.PISAO && furia >= 1 && encadeados < furia && jogador.vivo()
                        && Math.hypot(robo.x - jogador.x, robo.y - jogador.y) < 9f) {
                    // Errou o pisão? Quem não se afastou leva outro, com aviso mais curto.
                    encadeados++;
                    mirar(robo, jogador, c);
                    preparo = 0.45f;
                    trocar(Estado.PREPARO_PISAO);
                    eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_ALERTA, robo.x, robo.y, z + 2f, "DE NOVO! SAIA DA MIRA!", 1));
                    return;
                }
                if (estado == Estado.MANOBRA
                        && (manobra != TipoManobra.GAP_ESCADA || passouEscada)
                        && (manobra != TipoManobra.VOO_LONGO || tempoVoo >= ConstantesFisica.TEMPO_VOO_LONGO))
                    pontuar(robo, eventos);
                trocar(Estado.RECUPERANDO);
            }
        } else if (estado == Estado.GRIND) {
            progressoTrilho = Math.min(1f, progressoTrilho + dt * 4f / trilho.comprimento);
            robo.x = trilho.xEm(progressoTrilho); robo.y = trilho.yEm(progressoTrilho);
            z = trilho.zEm(progressoTrilho);
            if (progressoTrilho >= 1) {
                pontuar(robo, eventos);
                trocar(Estado.RECUPERANDO);
            }
        } else {
            cairAteChao(dt, robo, c);
            if (tempo > 1.2f - 0.25f * furia) trocar(Estado.RODANDO);
        }
    }

    private void atualizarFuria(RoboSkatista robo, List<EventoJogo> eventos) {
        float fracao = robo.vida() / (float) robo.tipo.vida;
        int nova = fracao <= 0.34f ? 2 : fracao <= 0.67f ? 1 : 0;
        if (nova > furia && robo.vida() > 0) {
            furia = nova;
            eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_ALERTA, robo.x, robo.y, z + 2.6f,
                    furia == 1 ? "CHARLIETRON ESTÁ SÉRIO!" : "CHARLIETRON FICOU FURIOSO!", 1));
        }
    }

    /** Antecipa o jogador: quanto mais furioso, mais à frente ele mira. O alvo trava ao avisar. */
    private void mirar(RoboSkatista robo, Jogador jogador, Cenario c) {
        float antecipacao = 0.25f + 0.3f * furia;
        if (jogador.noAr()) {
            // No ar o trajeto é previsível: mira onde ele vai pousar.
            float queda = (jogador.vz + (float) Math.sqrt(Math.max(0f, jogador.vz * jogador.vz
                    + 2f * ConstantesFisica.GRAVIDADE * Math.max(0f, jogador.z)))) / ConstantesFisica.GRAVIDADE;
            antecipacao = Math.max(antecipacao, Math.min(1.3f, queda));
        }
        alvoX = limitar(jogador.x + jogador.vx * antecipacao, c.largura);
        alvoY = limitar(jogador.y + jogador.vy * antecipacao, c.profundidade);
        alvoZ = c.alturaEm(alvoX, alvoY);
    }

    /** Repertório por fase da luta: inteiro faz o básico; furioso só faz as manobras de mais pontos. */
    private TipoManobra candidata(boolean avancar) {
        int inicio = furia == 0 ? 0 : furia == 1 ? 3 : 7;
        int n = REPERTORIO.length - inicio;
        TipoManobra m = REPERTORIO[inicio + proximaManobra % n];
        if (avancar) proximaManobra++;
        return m;
    }

    private boolean tentarGrind(RoboSkatista r, Cenario c) {
        if (manobra != TipoManobra.GRIND_5050 && manobra != TipoManobra.BOARDSLIDE) return false;
        for (TrilhoGrind t : c.trilhos) {
            if (Math.hypot(r.x - t.ax, r.y - t.ay) < 1.2f) {
                trilho = t; progressoTrilho = 0;
                r.x = t.ax; r.y = t.ay; z = t.az; r.rumo = t.anguloRad();
                trocar(Estado.GRIND);
                return true;
            }
        }
        manobra = TipoManobra.KICKFLIP; // Sem trilho próximo, faz uma manobra de solo válida.
        return false;
    }

    private void moverNoChao(float dt, RoboSkatista r, Jogador j, Cenario c) {
        float dx = j.x - r.x, dy = j.y - r.y;
        // Esquiva: se o jogador despenca em cima dele, sai da frente em vez de levar o pisão.
        boolean foge = j.noAr() && j.vz < 0 && j.z > 0.6f && Math.hypot(dx, dy) < 3.2f;
        if (foge) { dx = -dx; dy = -dy; dt *= 1.7f; }
        else if (candidata(false) == TipoManobra.GRIND_5050 || candidata(false) == TipoManobra.BOARDSLIDE) {
            TrilhoGrind perto = null; float distancia = Float.MAX_VALUE;
            for (TrilhoGrind t : c.trilhos) {
                float d = (float) Math.hypot(t.ax - r.x, t.ay - r.y);
                if (d < distancia) { perto = t; distancia = d; }
            }
            if (perto != null && distancia < 8) { dx = perto.ax - r.x; dy = perto.ay - r.y; }
        } else if (Math.hypot(dx, dy) < 4f) { float aux = dx; dx = -dy; dy = aux; }
        float angulo = (float) Math.atan2(dy, dx);
        for (float desvio : new float[]{0, 0.7f, -0.7f, 1.5f, -1.5f, 3.14f}) {
            float a = angulo + desvio;
            float nx = limitar(r.x + (float) Math.cos(a) * (3.7f + 0.6f * furia) * dt, c.largura);
            float ny = limitar(r.y + (float) Math.sin(a) * (3.7f + 0.6f * furia) * dt, c.profundidade);
            if (c.alturaEm(nx, ny) <= z + ConstantesFisica.DEGRAU) {
                r.x = nx; r.y = ny; r.rumo = a; z = c.alturaEm(nx, ny); break;
            }
        }
    }

    private void pontuar(RoboSkatista r, List<EventoJogo> eventos) {
        int ganho = Math.round(manobra.pontosBase * (1f + 0.25f * furia));
        pontos += ganho;
        eventos.add(new EventoJogo(EventoJogo.Tipo.ROBO_FALA, r.x, r.y, z + 2f,
                "CHARLIETRON +" + ganho + " " + manobra.nome, 0));
    }
    private void cairAteChao(float dt, RoboSkatista r, Cenario c) {
        vz -= ConstantesFisica.GRAVIDADE * dt;
        z = Math.max(c.alturaEm(r.x, r.y), z + vz * dt);
        if (z <= c.alturaEm(r.x, r.y)) vz = 0;
    }
    private static float limitar(float valor, float limite) { return Math.max(1f, Math.min(limite - 1f, valor)); }
    private void trocar(Estado novo) { estado = novo; tempo = 0; }
    void derrubar(boolean derrotado) { trocar(derrotado ? Estado.DERROTADO : Estado.CAIDO); vz = Math.min(0, vz); }
    boolean acertaCabeca(Jogador j) {
        float cabeca = j.z + 1.5f;
        return estado == Estado.PISAO && vz < 0 && zAnterior >= cabeca && z <= cabeca;
    }
    void acertouPisao(boolean pontua) {
        if (pontua) pontos += 200;
        trocar(Estado.RECUPERANDO);
    }
    public Estado estado() { return estado; }
    public int furia() { return furia; }
    void informarEstilo(int estilo) { estiloJogador = estilo; }
    public TipoManobra manobra() { return manobra; }
    public float z() { return z; }
    public float vz() { return vz; }
    public float tempo() { return tempo; }
    public float progressoVoo() { return Math.min(1f, tempoVoo / Math.max(0.01f, duracaoVoo)); }
    public float alvoX() { return alvoX; }
    public float alvoY() { return alvoY; }
    public float alvoZ() { return alvoZ; }
    public int pontos() { return pontos; }
    public boolean noAr() { return estado == Estado.MANOBRA || estado == Estado.PISAO; }
    public boolean vulneravel() { return estado == Estado.CAIDO || estado == Estado.RECUPERANDO; }
}
