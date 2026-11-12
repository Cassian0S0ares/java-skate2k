package com.skate2k.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.skate2k.SkateGame;
import com.skate2k.logica.BotDemo;
import com.skate2k.logica.CatalogoFases;
import com.skate2k.logica.Comandos;
import com.skate2k.logica.EventoJogo;
import com.skate2k.logica.MundoJogo;
import com.skate2k.logica.Pontuacao;
import com.skate2k.logica.Restricao;
import com.skate2k.render.Efeitos;
import com.skate2k.render.FontePixel;
import com.skate2k.render.Paleta;
import com.skate2k.render.Projecao;
import com.skate2k.render.RenderizadorHud;
import com.skate2k.render.RenderizadorMundo;

/** A partida: lê o teclado, avança a lógica e manda desenhar. Nenhuma regra mora aqui. */
public class TelaJogo extends TelaBase {
    private static final float TEMPO_ABERTURA = 2.6f;
    private static final float TEMPO_ENCERRAMENTO = 1.8f;

    private final OrthographicCamera cameraMundo = new OrthographicCamera();
    private final ExtendViewport viewportMundo = new ExtendViewport(LARGURA_BASE, ALTURA_BASE, cameraMundo);
    private final int indiceFase;
    private final Pontuacao pontuacao;
    private final MundoJogo mundo;
    private final RenderizadorMundo render;
    private final RenderizadorHud hud;
    private final Efeitos efeitos = new Efeitos();
    private final Comandos comandos = new Comandos();
    private final int recorde;

    private float abertura = TEMPO_ABERTURA;
    private float encerramento;
    private boolean encerrando;
    private boolean pausado;
    private int ultimoNumeroAbertura = -1;
    private BotDemo bot;

    public TelaJogo(SkateGame jogo, int indiceFase, Pontuacao pontuacao) {
        super(jogo);
        this.indiceFase = indiceFase;
        jogo.audio.tocarMusica("fase" + Math.min(indiceFase + 1, 4));
        this.pontuacao = pontuacao;
        this.mundo = new MundoJogo(CatalogoFases.criar(indiceFase), jogo.restricao, pontuacao, System.nanoTime());
        this.render = new RenderizadorMundo(d, mundo);
        this.hud = new RenderizadorHud(d);
        this.recorde = jogo.recorde.carregar();
    }

    /** Coloca o bot no controle e pula a abertura (usado pelo modo de captura de tela). */
    public void ligarBot() {
        bot = new BotDemo(99);
        abertura = 0;
    }

    @Override
    public void resize(int largura, int altura) {
        super.resize(largura, altura);
        viewportMundo.update(largura, altura, false);
    }

    @Override
    public void hide() {
        jogo.audio.pararGrind();
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        tempo += dt;

        if (Gdx.input.isKeyJustPressed(Keys.M)) jogo.audio.alternarMudo();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) && !encerrando && abertura <= 0) {
            pausado = !pausado;
            jogo.audio.pararGrind();
        }

        if (!pausado && !encerrando && (Gdx.input.isKeyJustPressed(Keys.NUM_7)
                || Gdx.input.isKeyJustPressed(Keys.NUMPAD_7))) {
            pularFase();
            return;
        }

        if (pausado) {
            if (Gdx.input.isKeyJustPressed(Keys.Q)) {
                jogo.irParaMenu();
                return;
            }
        } else if (abertura > 0) {
            atualizarAbertura(dt);
        } else if (!encerrando) {
            lerComandos(dt);
            mundo.atualizar(dt, comandos);
            for (EventoJogo e : mundo.consumirEventos()) {
                efeitos.processar(e);
                jogo.audio.processar(e);
            }
            if (mundo.terminou()) iniciarEncerramento();
        } else {
            mundo.atualizar(dt, comandos);
            encerramento -= dt;
            if (encerramento <= 0) {
                concluir();
                return;
            }
        }
        if (!pausado) efeitos.atualizar(dt);

        desenharTudo(dt);
    }

    /** Atalho de teste: tecla 7 conclui a fase como vitória. */
    private void pularFase() {
        jogo.audio.pararGrind();
        if (jogo.ultimaFase(indiceFase)) {
            jogo.encerrar(pontuacao, indiceFase, true, false, mundo.meta());
        } else {
            jogo.mostrarResultadoFase(indiceFase, pontuacao, 0, mundo.meta());
        }
    }

    private void atualizarAbertura(float dt) {
        abertura -= dt;
        int numero = (int) Math.ceil(abertura);
        if (numero != ultimoNumeroAbertura && numero <= 2 && numero >= 0) {
            ultimoNumeroAbertura = numero;
            jogo.audio.tocar(com.skate2k.audio.Audio.Efeito.TIQUE);
        }
    }

    private void iniciarEncerramento() {
        encerrando = true;
        encerramento = TEMPO_ENCERRAMENTO;
        jogo.audio.pararGrind();
        jogo.audio.tocar(mundo.metaBatida() && mundo.jogador().vivo()
                ? com.skate2k.audio.Audio.Efeito.NIVEL : com.skate2k.audio.Audio.Efeito.QUEDA);
    }

    private void concluir() {
        // Nocaute é sempre derrota, mesmo com a meta já batida.
        if (!mundo.jogador().vivo() || !mundo.metaBatida()) {
            jogo.encerrar(pontuacao, indiceFase, false, !mundo.jogador().vivo(), mundo.meta());
            return;
        }
        // O que sobrou na Carga vale metade no fim da fase: robô é sempre o melhor negócio.
        int bonus = pontuacao.carga() / 2;
        pontuacao.adicionarBonusFase(bonus);
        if (jogo.ultimaFase(indiceFase)) {
            jogo.encerrar(pontuacao, indiceFase, true, false, mundo.meta());
        } else {
            jogo.mostrarResultadoFase(indiceFase, pontuacao, bonus, mundo.meta());
        }
    }

    /** Traduz o teclado para intenção de jogo. A lógica só enxerga o objeto Comandos. */
    private void lerComandos(float dt) {
        if (bot != null) {
            bot.preencher(mundo, comandos, dt);
            return;
        }
        comandos.limpar();
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) comandos.eixoX -= 1;
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) comandos.eixoX += 1;
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) comandos.eixoY += 1;
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) comandos.eixoY -= 1;
        comandos.pulo = Gdx.input.isKeyPressed(Keys.SPACE);
        comandos.kickflip = Gdx.input.isKeyJustPressed(Keys.J);
        comandos.impossible = Gdx.input.isKeyJustPressed(Keys.K);
        comandos.grab = Gdx.input.isKeyPressed(Keys.L);
        if (Gdx.input.isKeyPressed(Keys.Q)) comandos.giro -= 1;
        if (Gdx.input.isKeyPressed(Keys.E)) comandos.giro += 1;
    }

    private void desenharTudo(float dt) {
        cameraMundo.position.set(
                Projecao.sx(mundo.camera().x(), mundo.camera().y()) + efeitos.deslocamentoX(),
                Projecao.sy(mundo.camera().x(), mundo.camera().y(), mundo.camera().z()) + 50f + efeitos.deslocamentoY(), 0);
        cameraMundo.update();

        render.limparTela();
        render.desenhar(mundo, cameraMundo, efeitos, tempo, pausado ? 0f : dt);

        cameraUi.update();
        float w = larguraUi();
        float h = alturaUi();
        d.iniciar(cameraUi.combined);
        if (mundo.restricao() == Restricao.CHUVA) render.desenharChuva(w, h, tempo);
        hud.desenharSetasRobos(mundo, cameraMundo, w, h);
        hud.desenhar(mundo, w, h, tempo, recorde, pausado ? 0f : dt);

        if (efeitos.flash() > 0) d.ret(0, 0, w, h, Paleta.copia(efeitos.corFlash(), efeitos.flash()));
        if (abertura > 0) desenharAbertura(w, h);
        if (encerrando) desenharEncerramento(w, h);
        if (pausado) desenharPausa(w, h);
        hud.desenharControles(w);
        d.terminar();
    }

    private void desenharAbertura(float w, float h) {
        d.ret(0, h / 2f - 110, w, 220, Paleta.copia(Paleta.TINTA, 0.75f));
        FontePixel.titulo(d, "FASE " + mundo.fase().numero + ": " + mundo.fase().nome, w / 2f, h / 2f + 40, 6f, Paleta.AMARELO, Paleta.ROSA);
        FontePixel.centro(d, mundo.fase().subtitulo, w / 2f, h / 2f + 5, 2.5f, Paleta.CIANO);
        String objetivo = mundo.batalhaFinal() ? "SUPERE O ESTILO E ZERE A VIDA DO BOSS"
                : "META: " + mundo.meta() + " PONTOS EM " + (int) mundo.fase().tempoLimite + "S";
        FontePixel.centro(d, objetivo, w / 2f, h / 2f - 28, 2.5f, Paleta.BRANCO);
        int numero = (int) Math.ceil(abertura);
        String t = numero >= 3 ? "PREPARA..." : numero == 2 ? "APONTA..." : "VAI!";
        FontePixel.centro(d, t, w / 2f, h / 2f - 80, 5f, Paleta.LIMA);
    }

    private void desenharEncerramento(float w, float h) {
        d.ret(0, h / 2f - 70, w, 140, Paleta.copia(Paleta.TINTA, 0.75f));
        if (!mundo.jogador().vivo()) {
            FontePixel.titulo(d, "NOCAUTE!", w / 2f, h / 2f + 8, 8f, Paleta.VERMELHO, Paleta.AMARELO);
            FontePixel.centro(d, "OS ROBÔS TE DESMONTARAM",
                    w / 2f, h / 2f - 45, 3f, Paleta.BRANCO);
            return;
        }
        boolean ok = mundo.metaBatida();
        if (mundo.batalhaFinal()) {
            FontePixel.titulo(d, ok ? "VOCÊ VENCEU!" : "FIM DO DUELO", w / 2f, h / 2f + 8,
                    6f, ok ? Paleta.LIMA : Paleta.VERMELHO, Paleta.AMARELO);
            FontePixel.centro(d, ok ? "CHARLIETRON SUPERADO NO ESTILO!" : "SUPERE O ESTILO E ZERE A VIDA DO BOSS",
                    w / 2f, h / 2f - 45, 2.5f, Paleta.BRANCO);
            return;
        }
        FontePixel.titulo(d, "TEMPO!", w / 2f, h / 2f + 8, 8f, ok ? Paleta.LIMA : Paleta.VERMELHO, Paleta.AMARELO);
        FontePixel.centro(d, ok ? "META BATIDA!" : "FALTOU " + (mundo.meta() - pontuacao.pontosFase()) + " PONTOS",
                w / 2f, h / 2f - 45, 3f, Paleta.BRANCO);
    }

    private void desenharPausa(float w, float h) {
        d.ret(0, 0, w, h, Paleta.copia(Paleta.TINTA, 0.7f));
        FontePixel.titulo(d, "PAUSA", w / 2f, h / 2f + 40, 10f, Paleta.AMARELO, Paleta.ROSA);
        FontePixel.centro(d, "ESC CONTINUA - Q VOLTA AO MENU", w / 2f, h / 2f - 30, 3f, Paleta.BRANCO);
    }
}
