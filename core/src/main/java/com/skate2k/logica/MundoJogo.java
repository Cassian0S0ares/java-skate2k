package com.skate2k.logica;

import static com.skate2k.logica.ConstantesFisica.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simulação completa de uma fase. Não conhece tela nem teclado: recebe delta time e comandos,
 * e publica o que aconteceu como uma fila de eventos.
 */
public final class MundoJogo implements Jogador.Ouvinte {
    private final Fase fase;
    private final Restricao restricao;
    private final Pontuacao pontuacao;
    private final Random aleatorio;
    private final Jogador jogador;
    private final CameraIso camera;
    private final List<RoboSkatista> robos = new ArrayList<>();
    private final List<EventoJogo> eventos = new ArrayList<>();
    private final List<TipoRobo> filaRenascer = new ArrayList<>();
    private final List<Float> esperaRenascer = new ArrayList<>();
    private final List<Manobra> ultimasManobras = new ArrayList<>();

    private float tempoRestante;
    private float tempoExibicaoManobras;
    private boolean metaAvisada;
    private int ultimoTique = -1;
    private RoboSkatista chefeFinal;
    private int estiloJogador;

    public MundoJogo(Fase fase, Restricao restricao, Pontuacao pontuacao, long semente) {
        this.fase = fase;
        this.restricao = fase.robos.contains(TipoRobo.CHARLIETRON) ? Restricao.DUELO : restricao;
        this.pontuacao = pontuacao;
        this.aleatorio = new Random(semente);
        this.jogador = new Jogador(fase.inicioX, fase.inicioY, this, aleatorio);
        this.camera = new CameraIso(fase.inicioX, fase.inicioY);
        this.tempoRestante = fase.tempoLimite;
        pontuacao.iniciarFase();
        for (int i = 0; i < fase.robos.size(); i++) {
            float[] p = fase.pontosDeEntrada.get(i % fase.pontosDeEntrada.size());
            RoboSkatista robo = new RoboSkatista(fase.robos.get(i), p[0], p[1], aleatorio);
            robos.add(robo);
            if (robo.chefe() != null) chefeFinal = robo;
        }
    }

    /** Avança a simulação. O delta time vem de fora (Gdx.graphics na TelaJogo, fixo nos testes). */
    public void atualizar(float dt, Comandos comandos) {
        if (acabouTempo() || (batalhaFinal() && metaBatida())) return;
        if (!jogador.vivo()) {
            // Nocaute: o relógio e os robôs param, só a queda termina de acontecer.
            jogador.atualizar(dt, comandos, fase.cenario, restricao, camera.emMovimento());
            camera.atualizar(dt, jogador, restricao);
            return;
        }
        tempoRestante = Math.max(0, tempoRestante - dt);
        tempoExibicaoManobras = Math.max(0, tempoExibicaoManobras - dt);

        jogador.definirNivelEstilo(pontuacao.nivel());
        jogador.atualizar(dt, comandos, fase.cenario, restricao, camera.emMovimento());
        camera.atualizar(dt, jogador, restricao);

        for (int i = 0; i < robos.size(); i++) {
            if (chefeFinal != null) chefeFinal.chefe().informarEstilo(estiloJogador);
            robos.get(i).atualizar(dt, jogador, fase.cenario, fase.fatorVelocidadeRobo, fase.intervaloInvestida,
                    aleatorio, eventos);
        }
        resolverColisoesRobos(dt);
        atualizarItens(dt);
        atualizarRenascimentos(dt);
        checarAvisosDeTempo();
    }

    private void resolverColisoesRobos(float dt) {
        for (int i = robos.size() - 1; i >= 0; i--) {
            RoboSkatista r = robos.get(i);
            if (r.vida() <= 0) continue;
            float d = (float) Math.hypot(jogador.x - r.x, jogador.y - r.y);
            float alturaRobo = r.z() + r.tipo.altura;

            if (r.chefe() != null && r.chefe().acertaCabeca(jogador)
                    && d < r.tipo.raio + 0.35f && !jogador.invulneravel()
                    && jogador.estado() != Jogador.Estado.CAIDO) {
                boolean capacete = jogador.temCapacete();
                golpear(r, true);
                r.chefe().acertouPisao(!capacete);
                continue;
            }

            if (jogador.noAr() && jogador.vz < 2f && d < ALCANCE_IMA_PISAO
                    && jogador.z > alturaRobo * 0.5f && jogador.z < alturaRobo + 2.5f) {
                jogador.atrairPara(r.x, r.y, dt);
            }

            boolean naAlturaDoPisao = jogador.z >= alturaRobo * 0.55f && jogador.z <= alturaRobo + 0.9f;
            if (jogador.noAr() && jogador.vz <= 3f && naAlturaDoPisao && d < r.tipo.raio + RAIO_PISAO_EXTRA) {
                pisar(r, i);
            } else if (d < r.tipo.raio + RAIO_TOQUE_EXTRA && r.perigoso() && !jogador.invulneravel()
                    && jogador.estado() != Jogador.Estado.CAIDO && jogador.estado() != Jogador.Estado.GRIND
                    && jogador.z < r.z() + ALTURA_PASSA_POR_CIMA
                    && jogador.z + 1.5f > r.z()) {
                golpear(r, r.estado() == RoboSkatista.Estado.INVESTIDA);
            }
        }
    }

    /** O robô acertou o jogador: o dano depende do modelo e de ser toque ou investida. */
    void golpear(RoboSkatista r, boolean investida) {
        if (jogador.temCapacete()) {
            jogador.cair("CAPACETE");
            jogador.empurrar(r.x, r.y);
            r.atordoarPorTrombada();
            return;
        }
        int dano = investida ? r.tipo.danoInvestida : r.tipo.danoToque;
        jogador.perderVida(dano);
        emitir(EventoJogo.Tipo.DANO, jogador.x, jogador.y, jogador.z + 2.2f, "-" + dano + " DE VIDA", dano);
        if (!jogador.vivo()) {
            jogador.cair("NOCAUTE!");
        } else if (investida) {
            jogador.cair(r.chefe() != null ? "PISÃO DO CHARLIETRON!" : "ATROPELADO POR UMA TORRADEIRA!");
        } else {
            trombar(r);
        }
    }

    /** Encostar em robô sem investida não derruba, mas ele rouba um pedaço da Carga. */
    private void trombar(RoboSkatista r) {
        int roubado = pontuacao.roubarCarga(FRACAO_ROUBO_CARGA);
        jogador.empurrar(r.x, r.y);
        r.atordoarPorTrombada();
        emitir(EventoJogo.Tipo.ROUBO_CARGA, jogador.x, jogador.y, jogador.z + 1.8f,
                roubado > 0 ? "ROBÔ LADRÃO! -" + roubado + " DE CARGA" : "SAI, LATA VELHA!", roubado);
    }

    private void pisar(RoboSkatista r, int indice) {
        List<Manobra> pendentes = jogador.quicarNoRobo();
        if (!pendentes.isEmpty()) registrarManobras(pendentes, false);
        boolean destruido = r.receberPisao(pontuacao.dano());
        if (r.chefe() != null) estiloJogador += 200;
        if (destruido) {
            int carga = pontuacao.carga();
            int mult = pontuacao.multiplicador();
            int ganho = pontuacao.derrotarRobo(r.tipo.valor);
            emitir(EventoJogo.Tipo.ROBO_DERROTADO, r.x, r.y, r.tipo.altura,
                    "(" + r.tipo.valor + " + " + carga + ") X" + mult, ganho);
            if (r.chefe() == null) {
                robos.remove(indice);
                filaRenascer.add(r.tipo);
                esperaRenascer.add(ESPERA_RENASCER);
            }
            checarMeta();
        } else {
            emitir(EventoJogo.Tipo.ROBO_ATINGIDO, r.x, r.y, r.tipo.altura, "BLINDAGEM " + r.vida() + "/" + r.tipo.vida, 0);
        }
    }

    private void atualizarItens(float dt) {
        for (int i = 0; i < fase.itens.size(); i++) {
            Item item = fase.itens.get(i);
            item.atualizar(dt);
            // Com a vida cheia o curativo fica no lugar, guardado para quando precisar.
            boolean dispensavel = item.tipo == TipoItem.CURATIVO && jogador.vida() >= VIDA_MAXIMA;
            if (!dispensavel && jogador.estado() != Jogador.Estado.CAIDO
                    && item.alcancado(jogador.x, jogador.y, jogador.z)) {
                item.coletar();
                aplicarItem(item.tipo);
                emitir(EventoJogo.Tipo.ITEM, item.x, item.y, item.z, item.tipo.nome + ": " + item.tipo.efeito, 0);
            }
        }
    }

    private void aplicarItem(TipoItem tipo) {
        switch (tipo) {
            case CAFE: jogador.beberCafe(); break;
            case PASTEL: pontuacao.adicionarCarga(400); break;
            case BOLO: pontuacao.adicionarCarga(800); break;
            case TENIS_MOLA: jogador.calcarTenisMola(); break;
            case RELOGIO: tempoRestante += 10f; break;
            case ADESIVO: pontuacao.adicionarBonusMultiplicador(2); break;
            case CURATIVO: jogador.curar(1); break;
            default: jogador.colocarCapacete(); break;
        }
    }

    private void atualizarRenascimentos(float dt) {
        for (int i = filaRenascer.size() - 1; i >= 0; i--) {
            float espera = esperaRenascer.get(i) - dt;
            if (espera > 0) {
                esperaRenascer.set(i, espera);
                continue;
            }
            float[] p = pontoDeEntradaLonge();
            robos.add(new RoboSkatista(filaRenascer.get(i), p[0], p[1], aleatorio));
            filaRenascer.remove(i);
            esperaRenascer.remove(i);
        }
    }

    private float[] pontoDeEntradaLonge() {
        float[] melhor = fase.pontosDeEntrada.get(0);
        float maiorDist = -1f;
        for (float[] p : fase.pontosDeEntrada) {
            float d = (float) Math.hypot(p[0] - jogador.x, p[1] - jogador.y) * (0.6f + aleatorio.nextFloat());
            if (d > maiorDist) {
                maiorDist = d;
                melhor = p;
            }
        }
        return melhor;
    }

    private void checarAvisosDeTempo() {
        int segundos = (int) Math.ceil(tempoRestante);
        if (segundos <= 10 && segundos != ultimoTique && segundos > 0) {
            ultimoTique = segundos;
            emitir(EventoJogo.Tipo.TIQUE_TEMPO, 0, 0, 0, null, segundos);
        }
    }

    /** Meta desta fase já ajustada pelo peso da restrição sorteada. */
    public int meta() {
        if (batalhaFinal()) return Math.max(fase.meta, chefeFinal.chefe().pontos() + 1);
        return Math.round(fase.meta * restricao.fatorMeta);
    }

    private void checarMeta() {
        if (!metaAvisada && metaBatida()) {
            metaAvisada = true;
            emitir(EventoJogo.Tipo.META_BATIDA, jogador.x, jogador.y, jogador.z, "META BATIDA!", 0);
        }
    }

    private int ultimoGanho;

    private void registrarManobras(List<Manobra> manobras, boolean limpo) {
        int cargaAntes = pontuacao.carga();
        boolean subiu = pontuacao.registrarManobras(manobras, limpo);
        ultimoGanho = pontuacao.carga() - cargaAntes;
        if (batalhaFinal()) estiloJogador += ultimoGanho;
        ultimasManobras.clear();
        ultimasManobras.addAll(manobras);
        tempoExibicaoManobras = 2.2f;
        checarMeta();
        if (subiu) {
            emitir(EventoJogo.Tipo.SUBIU_NIVEL, jogador.x, jogador.y, jogador.z + 1.8f, pontuacao.nomeNivel(), pontuacao.nivel());
        }
    }

    private void emitir(EventoJogo.Tipo tipo, float x, float y, float z, String texto, int valor) {
        eventos.add(new EventoJogo(tipo, x, y, z, texto, valor));
    }

    // ---------------------------------------------------------------------- avisos do jogador

    @Override
    public void aoPular(Jogador j) {
        emitir(EventoJogo.Tipo.PULO, j.x, j.y, j.z, null, 0);
    }

    @Override
    public void aoPousar(Jogador j, List<Manobra> manobras, boolean limpo) {
        if (manobras.isEmpty()) {
            emitir(EventoJogo.Tipo.POUSO, j.x, j.y, j.z, null, 0);
            return;
        }
        registrarManobras(manobras, limpo);
        int soma = ultimoGanho;
        emitir(limpo ? EventoJogo.Tipo.POUSO_LIMPO : EventoJogo.Tipo.POUSO, j.x, j.y, j.z,
                limpo ? "POUSO LIMPO!" : null, soma);
    }

    @Override
    public void aoCair(Jogador j, String motivo) {
        pontuacao.cair();
        emitir(EventoJogo.Tipo.QUEDA, j.x, j.y, j.z, motivo, 0);
    }

    @Override
    public void aoUsarCapacete(Jogador j) {
        emitir(EventoJogo.Tipo.ESCUDO_USADO, j.x, j.y, j.z, "O CAPACETE DA MÃE SALVOU!", 0);
    }

    @Override
    public void aoIniciarGrind(Jogador j, TipoElemento tipo) {
        emitir(EventoJogo.Tipo.GRIND_INICIO, j.x, j.y, j.z, null, 0);
    }

    @Override
    public void aoSairGrind(Jogador j) {
        emitir(EventoJogo.Tipo.GRIND_FIM, j.x, j.y, j.z, null, 0);
    }

    @Override
    public void aoUsarElemento(Jogador j, TipoElemento tipo) {
        boolean novoNoCombo = pontuacao.usarElemento(tipo);
        if (novoNoCombo) {
            emitir(EventoJogo.Tipo.POTENCIALIZADOR, j.x, j.y, j.z + 1.8f,
                    tipo.nomePotencializador + " X" + formatar(tipo.potencial), 0);
        }
    }

    @Override
    public void aoNegar(Jogador j, String mensagem) {
        emitir(EventoJogo.Tipo.NEGADO, j.x, j.y, j.z + 1.8f, mensagem, 0);
    }

    private static String formatar(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.valueOf(v);
    }

    // -------------------------------------------------------------------------------- leitura

    /** Devolve os eventos acumulados e limpa a fila. */
    public List<EventoJogo> consumirEventos() {
        List<EventoJogo> copia = new ArrayList<>(eventos);
        eventos.clear();
        return copia;
    }

    public boolean acabouTempo() { return tempoRestante <= 0; }
    /** A fase acabou, por tempo ou por nocaute. */
    public boolean terminou() { return acabouTempo() || !jogador.vivo() || (batalhaFinal() && metaBatida()); }
    public boolean metaBatida() {
        return batalhaFinal() ? chefeFinal.vida() <= 0 && estiloJogador >= meta() : pontuacao.pontosFase() >= meta();
    }
    public boolean batalhaFinal() { return chefeFinal != null; }
    public RoboSkatista chefeFinal() { return chefeFinal; }
    public int estiloJogador() { return estiloJogador; }
    public float tempoRestante() { return tempoRestante; }
    public Fase fase() { return fase; }
    public Restricao restricao() { return restricao; }
    public Pontuacao pontuacao() { return pontuacao; }
    public Jogador jogador() { return jogador; }
    public CameraIso camera() { return camera; }
    public List<RoboSkatista> robos() { return robos; }
    public List<Manobra> ultimasManobras() { return ultimasManobras; }
    public float tempoExibicaoManobras() { return tempoExibicaoManobras; }
}
