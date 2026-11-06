package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Detecta manobras a partir do estado de voo e da rotação real do skate. Os comandos só iniciam
 * rotações; o que vale é o ângulo acumulado no momento do pouso: rotação incompleta vira tombo.
 */
public final class DetectorManobras {

    /** Resultado da avaliação do pouso. */
    public static final class Pouso {
        public final boolean valido;
        public final boolean limpo;
        public final String motivoQueda;
        public final List<Manobra> manobras;

        Pouso(boolean valido, boolean limpo, String motivoQueda, List<Manobra> manobras) {
            this.valido = valido;
            this.limpo = limpo;
            this.motivoQueda = motivoQueda;
            this.manobras = manobras;
        }
    }

    private float flipGraus;
    private float flipAlvo;
    private float impossibleGraus;
    private float impossibleAlvo;
    private float giroGraus;
    private int giroSentido;
    private float grabTempo;
    private boolean grabAtivo;
    private float tempoVoo;
    private boolean passouEscada;
    private final List<Manobra> grinds = new ArrayList<>();

    public void iniciarVoo() {
        flipGraus = 0;
        flipAlvo = 0;
        impossibleGraus = 0;
        impossibleAlvo = 0;
        giroGraus = 0;
        giroSentido = 0;
        grabTempo = 0;
        grabAtivo = false;
        tempoVoo = 0;
        passouEscada = false;
    }

    /** Descarta tudo o que estava pendente (usado no tombo). */
    public void descartar() {
        iniciarVoo();
        grinds.clear();
    }

    public void comandarKickflip() {
        flipAlvo += 360f;
    }

    public void comandarImpossible() {
        impossibleAlvo += 360f;
    }

    public void registrarGrind(Manobra grind) {
        grinds.add(grind);
    }

    public void marcarPassagemEscada() {
        passouEscada = true;
    }

    public boolean passouEscada() {
        return passouEscada;
    }

    /** Avança as rotações do skate e do corpo. */
    public void atualizar(float dt, int giroEntrada, boolean grab) {
        tempoVoo += dt;
        flipGraus = aproximar(flipGraus, flipAlvo, VELOCIDADE_KICKFLIP * dt);
        impossibleGraus = aproximar(impossibleGraus, impossibleAlvo, VELOCIDADE_IMPOSSIBLE * dt);

        if (giroEntrada != 0) {
            giroSentido = giroEntrada;
            giroGraus += giroEntrada * VELOCIDADE_GIRO * dt;
        } else if (giroSentido != 0) {
            // Soltou: o corpo completa sozinho até o meio-giro mais próximo no mesmo sentido.
            giroGraus = aproximar(giroGraus, alvoGiro(), VELOCIDADE_GIRO * dt);
        }

        grabAtivo = grab;
        if (grab) {
            grabTempo += dt;
        }
    }

    private float alvoGiro() {
        float abs = Math.abs(giroGraus);
        float meios = (float) Math.floor(abs / 180f);
        float resto = abs - meios * 180f;
        float alvoAbs = resto < 30f ? meios * 180f : (meios + 1) * 180f;
        return Math.signum(giroGraus == 0 ? giroSentido : giroGraus) * alvoAbs;
    }

    /** Avalia o pouso olhando os ângulos atuais. Só no sucesso as manobras são reconhecidas. */
    public Pouso avaliarPouso(float fatorTolerancia, float potencial) {
        float restoFlip = restoAngulo(flipGraus, 360f);
        float restoImpossible = restoAngulo(impossibleGraus, 360f);
        float restoGiro = restoAngulo(giroGraus, 180f);

        if (restoFlip > TOLERANCIA_FLIP * fatorTolerancia
                || restoImpossible > TOLERANCIA_FLIP * fatorTolerancia) {
            return new Pouso(false, false, "POUSOU DE SHAPE VIRADO!", List.of());
        }
        if (restoGiro > TOLERANCIA_GIRO * fatorTolerancia) {
            return new Pouso(false, false, "POUSOU DE LADO!", List.of());
        }
        if (grabAtivo) {
            return new Pouso(false, false, "SOLTA O SKATE, CRIATURA!", List.of());
        }

        List<Manobra> manobras = montarLista(Math.round(flipGraus / 360f),
                Math.round(impossibleGraus / 360f), Math.round(Math.abs(giroGraus) / 180f), potencial);
        boolean limpo = !manobras.isEmpty()
                && restoFlip <= TOLERANCIA_POUSO_LIMPO
                && restoImpossible <= TOLERANCIA_POUSO_LIMPO
                && restoGiro <= TOLERANCIA_POUSO_LIMPO;
        return new Pouso(true, limpo, null, manobras);
    }

    /** Pisão em robô: confirma apenas as rotações já completas, sem exigir alinhamento. */
    public List<Manobra> consumirNoPisao(float potencial) {
        List<Manobra> manobras = montarLista((int) (flipGraus / 360f), (int) (impossibleGraus / 360f),
                (int) (Math.abs(giroGraus) / 180f), potencial);
        iniciarVoo();
        grinds.clear();
        return manobras;
    }

    /** Manobras já completas neste voo, para mostrar no HUD enquanto o jogador está no ar. */
    public List<Manobra> previa(float potencial) {
        return montarLista((int) (flipGraus / 360f), (int) (impossibleGraus / 360f),
                (int) (Math.abs(giroGraus) / 180f), potencial);
    }

    private List<Manobra> montarLista(int flips, int impossibles, int meiosGiros, float potencial) {
        List<Manobra> lista = new ArrayList<>(grinds);

        if (flips == 1) lista.add(Manobra.de(TipoManobra.KICKFLIP, potencial));
        else if (flips == 2) lista.add(Manobra.de(TipoManobra.DOUBLE_KICKFLIP, potencial));
        else if (flips >= 3) lista.add(Manobra.de(TipoManobra.TRIPLE_KICKFLIP, potencial));

        if (impossibles == 1) lista.add(Manobra.de(TipoManobra.IMPOSSIBLE, potencial));
        else if (impossibles >= 2) lista.add(Manobra.de(TipoManobra.DOUBLE_IMPOSSIBLE, potencial));

        if (flips > 0 && impossibles > 0) lista.add(Manobra.de(TipoManobra.HARDFLIP, potencial));
        if (flips > 0 && meiosGiros > 0) lista.add(Manobra.de(TipoManobra.VARIAL, potencial));

        if (meiosGiros == 1) lista.add(Manobra.de(TipoManobra.GIRO_180, potencial));
        else if (meiosGiros == 2) lista.add(Manobra.de(TipoManobra.GIRO_360, potencial));
        else if (meiosGiros == 3) lista.add(Manobra.de(TipoManobra.GIRO_540, potencial));
        else if (meiosGiros >= 4) lista.add(Manobra.de(TipoManobra.GIRO_720, potencial));

        if (grabTempo >= 0.25f) lista.add(Manobra.grab(grabTempo, potencial));
        if (passouEscada) lista.add(Manobra.de(TipoManobra.GAP_ESCADA, potencial));
        if (tempoVoo >= TEMPO_VOO_LONGO && !lista.isEmpty()) {
            lista.add(Manobra.de(TipoManobra.VOO_LONGO, potencial));
        }
        return lista;
    }

    /** Distância do ângulo até o múltiplo de "passo" mais próximo. */
    static float restoAngulo(float graus, float passo) {
        float abs = Math.abs(graus) % passo;
        return Math.min(abs, passo - abs);
    }

    private static float aproximar(float atual, float alvo, float passo) {
        if (atual < alvo) return Math.min(atual + passo, alvo);
        if (atual > alvo) return Math.max(atual - passo, alvo);
        return atual;
    }

    public float flipGraus() { return flipGraus; }
    public float impossibleGraus() { return impossibleGraus; }
    public float giroGraus() { return giroGraus; }
    public boolean grabAtivo() { return grabAtivo; }
    public float tempoVoo() { return tempoVoo; }
    public boolean temRotacaoEmAndamento() {
        return flipGraus < flipAlvo || impossibleGraus < impossibleAlvo;
    }
}
