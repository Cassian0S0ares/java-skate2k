package com.skate2k.telas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.skate2k.SkateGame;
import com.skate2k.audio.Audio;
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
import com.skate2k.render.RenderizadorMundo;
import java.util.List;
import java.util.Random;

/** Menu inicial, com um bot andando no skatepark ao fundo. */
public class TelaMenu extends TelaBase {
    private static final String[] OPCOES = {"JOGAR", "COMO JOGAR", "SORTEAR REGRA", "SAIR"};

    private final OrthographicCamera cameraMundo = new OrthographicCamera();
    private final ExtendViewport viewportMundo = new ExtendViewport(LARGURA_BASE, ALTURA_BASE, cameraMundo);
    private final Comandos comandos = new Comandos();
    private final Random aleatorio = new Random();
    private final Efeitos efeitos = new Efeitos();
    private MundoJogo demo;
    private BotDemo bot;
    private RenderizadorMundo renderDemo;
    private int selecionada;
    private boolean ajuda;

    public TelaMenu(SkateGame jogo) {
        super(jogo);
        jogo.audio.tocarMusica("menu");
        reiniciarDemo();
    }

    private void reiniciarDemo() {
        long semente = aleatorio.nextLong();
        demo = new MundoJogo(CatalogoFases.criar(2), Restricao.CAMERA_EM_MOVIMENTO, new Pontuacao(), semente);
        bot = new BotDemo(semente + 1);
        renderDemo = new RenderizadorMundo(d, demo);
    }

    @Override
    public void resize(int largura, int altura) {
        super.resize(largura, altura);
        viewportMundo.update(largura, altura, false);
    }

    /** Abre diretamente a tela de ajuda (usado pelo modo de captura de tela). */
    public void abrirAjuda() {
        ajuda = true;
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        tempo += dt;
        entrada();

        bot.preencher(demo, comandos, dt);
        demo.atualizar(dt, comandos);
        List<EventoJogo> eventos = demo.consumirEventos();
        for (EventoJogo e : eventos) {
            if (e.tipo != EventoJogo.Tipo.ROBO_FALA && e.tipo != EventoJogo.Tipo.NEGADO) efeitos.processar(e);
        }
        efeitos.atualizar(dt);
        if (demo.terminou()) reiniciarDemo();

        cameraMundo.position.set(Projecao.sx(demo.camera().x(), demo.camera().y()),
                Projecao.sy(demo.camera().x(), demo.camera().y(), demo.camera().z()) + 40f, 0);
        cameraMundo.update();
        renderDemo.limparTela();
        renderDemo.desenhar(demo, cameraMundo, efeitos, tempo, dt);

        cameraUi.update();
        d.iniciar(cameraUi.combined);
        float w = larguraUi();
        float h = alturaUi();
        d.ret(0, 0, w, h, Paleta.copia(Paleta.TINTA, 0.62f));
        if (ajuda) {
            desenharAjuda(w, h);
        } else {
            desenharPrincipal(w, h);
        }
        d.terminar();
    }

    private void entrada() {
        if (Gdx.input.isKeyJustPressed(Keys.M)) jogo.audio.alternarMudo();
        if (ajuda) {
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                ajuda = false;
                jogo.audio.tocar(Audio.Efeito.MENU);
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            selecionada = (selecionada + OPCOES.length - 1) % OPCOES.length;
            jogo.audio.tocar(Audio.Efeito.MENU);
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            selecionada = (selecionada + 1) % OPCOES.length;
            jogo.audio.tocar(Audio.Efeito.MENU);
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) sortear();
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) ativar();
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) Gdx.app.exit();
    }

    private void ativar() {
        jogo.audio.tocar(Audio.Efeito.POUSO_LIMPO);
        switch (selecionada) {
            case 0: jogo.novoJogo(); break;
            case 1: ajuda = true; break;
            case 2: sortear(); break;
            default: Gdx.app.exit(); break;
        }
    }

    private void sortear() {
        jogo.restricao = Restricao.sortearOutra(jogo.restricao, aleatorio);
        jogo.audio.tocar(Audio.Efeito.ITEM);
    }

    private void desenharPrincipal(float w, float h) {
        float cx = w / 2f;
        float pulo = (float) Math.sin(tempo * 3f) * 5f;
        FontePixel.titulo(d, "PUNK DISCO!", cx, h - 170 + pulo, 13f, Paleta.AMARELO, Paleta.ROSA);
        FontePixel.centro(d, "SKATE, PANCADA EM ROBÔ E ZERO RESPONSABILIDADE", cx, h - 208, 2.5f, Paleta.CIANO);

        float y = h - 300;
        for (int i = 0; i < OPCOES.length; i++) {
            boolean sel = i == selecionada;
            float largura = 330;
            Color borda = sel ? Paleta.AMARELO : Paleta.ROXO;
            d.painel(cx - largura / 2f, y - 13, largura, 44, sel ? Paleta.ROSA : Paleta.PAINEL, borda);
            FontePixel.centro(d, OPCOES[i], cx, y, 3f, sel ? Paleta.BRANCO : Paleta.CINZA);
            if (sel) {
                float seta = (float) Math.sin(tempo * 8f) * 5f;
                FontePixel.comSombra(d, ">", cx - largura / 2f - 34 + seta, y - 2, 4f, Paleta.AMARELO);
            }
            y -= 56;
        }

        // Regra sorteada e recorde
        Restricao r = jogo.restricao;
        float painelY = 62;
        d.painel(cx - 400, painelY, 800, 76, Paleta.PAINEL, Paleta.LARANJA);
        String oficial = r == Restricao.sorteioOficial() ? " (SORTEIO OFICIAL)" : "";
        FontePixel.centro(d, "REGRA: " + r.titulo + oficial, cx, painelY + 50, 2.5f, Paleta.LARANJA);
        List<String> linhas = FontePixel.quebrar(r.descricao, 62);
        float ly = painelY + 28;
        for (String l : linhas) {
            FontePixel.centro(d, l, cx, ly, 2f, Paleta.BRANCO);
            ly -= 20;
        }
        FontePixel.centro(d, "RECORDE: " + jogo.recorde.carregar(), cx, 32, 3f, Paleta.AMARELO);
        FontePixel.direita(d, "R SORTEIA REGRA - M SOM " + (jogo.audio.mudo() ? "OFF" : "ON"), w - 16, 12, 1.5f, Paleta.CINZA);
    }

    private void desenharAjuda(float w, float h) {
        float cx = w / 2f;
        float pw = 1120;
        float ph = 610;
        float px = cx - pw / 2f;
        float py = h / 2f - ph / 2f;
        d.painel(px, py, pw, ph, new Color(0.11f, 0.06f, 0.2f, 0.96f), Paleta.CIANO);
        FontePixel.centro(d, "COMO JOGAR", cx, py + ph - 50, 5f, Paleta.AMARELO);

        float x1 = px + 34;
        float y = py + ph - 90;
        y = bloco(x1, y, "O ROLÊ", Paleta.ROSA, new String[] {
            "1. FAÇA MANOBRAS PARA ENCHER A CARGA (BATERIA NO CANTO).",
            "2. PISE NOS ROBÔS: PULE E CAIA EM CIMA DELES.",
            "3. O ROBÔ DERROTADO DESCARREGA TUDO: (VALOR + CARGA) X MULTIPLICADOR.",
            "4. CAIU? CARGA E MULTIPLICADOR ZERAM E O ESTILO DESCE UM NÍVEL.",
            "BATA A META ANTES DO TEMPO ACABAR!"
        });
        y -= 12;
        y = bloco(x1, y, "CONTROLES", Paleta.CIANO, new String[] {
            "WASD OU SETAS: ANDAR E, NO AR, AJUSTAR A DIREÇÃO",
            "ESPAÇO: SEGURE PARA CARREGAR E SOLTE PARA PULAR",
            "J: KICKFLIP - K: IMPOSSIBLE - L (SEGURE): PEGA-PÃO - Q/E: GIRAR O CORPO",
            "NO GRIND: A/D EQUILIBRA - ESPAÇO SALTA DO TRILHO - ESC: PAUSA - M: SOM"
        });
        y -= 12;
        y = bloco(x1, y, "DICAS", Paleta.LIMA, new String[] {
            "CENÁRIO DÁ POTENCIALIZADOR: RAMPA X1.5, ESCADA X2, CORRIMÃO X1.75, BANCO E MURETA X1.25.",
            "ELEMENTO NOVO NO COMBO SOMA +1 NO MULTIPLICADOR. VARIE!",
            "VOCÊ TEM 3 DE VIDA POR FASE. ZEROU, É NOCAUTE E GAME OVER. BAND-AID DEVOLVE 1.",
            "ROBÔ QUE DÁ \"!\" VAI DAR INVESTIDA: DERRUBA, E CADA \"!\" É 1 DE DANO.",
            "ENCOSTAR TIRA VIDA E 30% DA CARGA: ROLIMÃ E FAÍSCA 1, TANQUE E SÍNDICO 2.",
            "POUSE COM O SHAPE COMPLETO: ROTAÇÃO PELA METADE É TOMBO (NÃO TIRA VIDA)."
        });
        FontePixel.centro(d, "ENTER PARA VOLTAR", cx, py + 18, 2.5f, Paleta.AMARELO);
    }

    private float bloco(float x, float y, String titulo, Color cor, String[] linhas) {
        FontePixel.comSombra(d, titulo, x, y, 3f, cor);
        y -= 26;
        for (String l : linhas) {
            FontePixel.comSombra(d, l, x, y, 2f, Paleta.BRANCO);
            y -= 22;
        }
        return y;
    }
}
