package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Regras de pontuação: manobras enchem a Carga, e a Carga só vira ponto ao derrotar um robô.
 * Cair zera a Carga e o multiplicador, e derruba um nível de estilo.
 */
public final class Pontuacao {
    public static final int[] XP_POR_NIVEL = {0, 150, 400, 800, 1500};
    public static final String[] NOMES_NIVEL = {
        "ZÉ NINGUÉM", "MANOBREIRO", "ESTILOSO", "BRABO", "LENDA DO BAIRRO"
    };

    private int pontosTotal;
    private int pontosFase;
    private int carga;
    private int manobrasConsecutivas;
    private int bonusMultiplicador;
    private int xp;
    private int nivel = 1;
    private int melhorMultiplicador = 1;
    private int robosDerrotados;
    private final Set<TipoElemento> elementosNoCombo = EnumSet.noneOf(TipoElemento.class);

    public void iniciarFase() {
        pontosFase = 0;
        carga = 0;
    }

    /** Pouso bem-sucedido: cada manobra vai para a Carga e alimenta o estilo. */
    public boolean registrarManobras(List<Manobra> manobras, boolean limpo) {
        int nivelAntes = nivel;
        float fator = limpo ? BONUS_POUSO_LIMPO : 1f;
        for (Manobra m : manobras) {
            carga += Math.round(m.pontos * fator);
            manobrasConsecutivas++;
            xp += m.dificuldade * XP_POR_DIFICULDADE;
        }
        atualizarNivel();
        atualizarMelhorMultiplicador();
        return nivel > nivelAntes;
    }

    /** Usar um tipo novo de elemento no combo soma +1 ao multiplicador. */
    public boolean usarElemento(TipoElemento tipo) {
        boolean novo = elementosNoCombo.add(tipo);
        if (novo) {
            bonusMultiplicador++;
            atualizarMelhorMultiplicador();
        }
        return novo;
    }

    /** Dano do pisão: cresce com o nível de estilo e com uma Carga alta. */
    public int dano() {
        return nivel + (carga >= CARGA_DANO_EXTRA ? 1 : 0);
    }

    /** Robô derrotado: descarrega a Carga inteira, multiplicada, no placar. */
    public int derrotarRobo(int valorRobo) {
        int ganho = (valorRobo + carga) * multiplicador();
        pontosTotal += ganho;
        pontosFase += ganho;
        carga = 0;
        manobrasConsecutivas++;
        robosDerrotados++;
        atualizarMelhorMultiplicador();
        return ganho;
    }

    /** Cair zera o multiplicador e a Carga, e tira um nível de estilo. */
    public void cair() {
        carga = 0;
        manobrasConsecutivas = 0;
        bonusMultiplicador = 0;
        elementosNoCombo.clear();
        nivel = Math.max(1, nivel - 1);
        xp = XP_POR_NIVEL[nivel - 1];
    }

    /** Trombada leve: o robô leva uma fração da Carga. Devolve quanto foi roubado. */
    public int roubarCarga(float fracao) {
        int roubado = Math.round(carga * fracao);
        carga -= roubado;
        return roubado;
    }

    public void adicionarCarga(int valor) {
        carga += valor;
    }

    public void adicionarBonusMultiplicador(int valor) {
        bonusMultiplicador += valor;
        atualizarMelhorMultiplicador();
    }

    public void adicionarBonusFase(int valor) {
        pontosTotal += valor;
    }

    /** Devolve true se a pontuação total bateu o recorde (e o salva). */
    public boolean salvarSeRecorde(RepositorioRecorde repositorio) {
        if (pontosTotal > repositorio.carregar()) {
            repositorio.salvar(pontosTotal);
            return true;
        }
        return false;
    }

    private void atualizarNivel() {
        while (nivel < XP_POR_NIVEL.length && xp >= XP_POR_NIVEL[nivel]) {
            nivel++;
        }
    }

    private void atualizarMelhorMultiplicador() {
        melhorMultiplicador = Math.max(melhorMultiplicador, multiplicador());
    }

    public int multiplicador() {
        return Math.min(MULTIPLICADOR_MAXIMO, 1 + manobrasConsecutivas / 2 + bonusMultiplicador);
    }

    /** Progresso de 0 a 1 dentro do nível atual de estilo. */
    public float progressoNivel() {
        if (nivel >= XP_POR_NIVEL.length) return 1f;
        int inicio = XP_POR_NIVEL[nivel - 1];
        int fim = XP_POR_NIVEL[nivel];
        return (xp - inicio) / (float) (fim - inicio);
    }

    public String nomeNivel() { return NOMES_NIVEL[nivel - 1]; }
    public int pontosTotal() { return pontosTotal; }
    public int pontosFase() { return pontosFase; }
    public int carga() { return carga; }
    public int nivel() { return nivel; }
    public int manobrasConsecutivas() { return manobrasConsecutivas; }
    public int melhorMultiplicador() { return melhorMultiplicador; }
    public int robosDerrotados() { return robosDerrotados; }
}
