package com.skate2k.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.skate2k.SkateGame;
import com.skate2k.logica.Pontuacao;
import com.skate2k.logica.RepositorioRecorde;
import com.skate2k.logica.Restricao;
import com.skate2k.telas.TelaGameOver;
import com.skate2k.telas.TelaJogo;
import com.skate2k.telas.TelaMenu;
import com.skate2k.telas.TelaResultadoFase;
import java.util.ArrayList;
import java.util.List;

/**
 * Ferramenta de desenvolvimento: percorre as telas com o bot no controle e salva PNGs, para
 * conferir o visual sem precisar jogar. Só roda com o argumento --captura.
 */
final class CapturaTelas extends SkateGame {
    private interface Passo {
        void abrir();
    }

    private final String diretorio;
    private final String filtro;
    private final List<String> nomes = new ArrayList<>();
    private final List<Passo> passos = new ArrayList<>();
    private final List<Float> duracoes = new ArrayList<>();
    private int atual = -1;
    private float restante;

    CapturaTelas(String diretorio, String filtro) {
        this.diretorio = diretorio;
        this.filtro = filtro;
    }

    private void passo(String nome, float duracao, Passo p) {
        if (filtro != null && !java.util.Arrays.asList(filtro.split(",")).contains(nome)) return;
        nomes.add(nome);
        duracoes.add(duracao);
        passos.add(p);
    }

    @Override
    protected void abrirPrimeiraTela() {
        audio.alternarMudo();
        // O bot pode ser nocauteado durante o tour: o recorde real não pode ser alterado.
        recorde = new RepositorioRecorde() {
            private int valor;
            @Override public int carregar() { return valor; }
            @Override public void salvar(int pontos) { valor = pontos; }
        };
        passo("menu", 2.5f, () -> setScreen(new TelaMenu(this)));
        passo("ajuda", 0.6f, () -> {
            TelaMenu menu = new TelaMenu(this);
            menu.abrirAjuda();
            setScreen(menu);
        });
        for (int fase = 0; fase < 4; fase++) {
            final int f = fase;
            passo("fase" + (f + 1) + "-a", 4f, () -> abrirFase(f, Restricao.sorteioOficial()));
            passo("fase" + (f + 1) + "-b", 11f, () -> abrirFase(f, Restricao.CAMERA_EM_MOVIMENTO));
        }
        passo("chuva", 7f, () -> abrirFase(1, Restricao.CHUVA));
        passo("queda", 0.5f, this::abrirQueda);
        passo("sprites-movimento", 0.5f, () -> setScreen(new com.skate2k.render.PreviaMovimento(desenho)));
        passo("abertura", 0.8f, () -> {
            restricao = Restricao.sorteioOficial();
            setScreen(new TelaJogo(this, 0, new Pontuacao()));
        });
        passo("resultado", 0.5f, () -> setScreen(new TelaResultadoFase(this, 0, exemplo(), 310, 6000)));
        passo("gameover", 0.5f, () -> setScreen(new TelaGameOver(this, exemplo(), 1, false, true, true, 1200, 10800)));
        passo("vitoria", 0.5f, () -> setScreen(new TelaGameOver(this, exemplo(), 3, true, false, false, 99999, 24000)));
        avancar();
    }

    private void abrirFase(int indice, Restricao r) {
        restricao = r;
        TelaJogo tela = new TelaJogo(this, indice, new Pontuacao());
        tela.ligarBot();
        setScreen(tela);
    }

    /** Cena estável para conferir a pose caída sem depender das colisões do bot. */
    private void abrirQueda() {
        var mundo = new com.skate2k.logica.MundoJogo(com.skate2k.logica.CatalogoFases.criar(0),
                Restricao.SEM_PULO, new Pontuacao(), 42L);
        var jogador = mundo.jogador();
        jogador.rumo = -(float) Math.PI / 2f;
        jogador.cair("CAPTURA DE QUEDA");
        for (int i = 0; i < 24; i++) mundo.atualizar(1f / 60f, new com.skate2k.logica.Comandos());
        var camera = new com.badlogic.gdx.graphics.OrthographicCamera(640, 360);
        camera.position.set(com.skate2k.render.Projecao.sx(jogador.x, jogador.y),
                com.skate2k.render.Projecao.sy(jogador.x, jogador.y, jogador.z) + 30f, 0);
        camera.update();
        var render = new com.skate2k.render.RenderizadorMundo(desenho, mundo);
        var efeitos = new com.skate2k.render.Efeitos();
        setScreen(new com.badlogic.gdx.ScreenAdapter() {
            @Override public void render(float dt) {
                render.limparTela();
                render.desenhar(mundo, camera, efeitos, 0.5f, 0f);
            }
        });
    }

    private Pontuacao exemplo() {
        Pontuacao p = new Pontuacao();
        p.adicionarCarga(800);
        p.adicionarBonusMultiplicador(3);
        p.derrotarRobo(200);
        return p;
    }

    private void avancar() {
        atual++;
        if (atual >= passos.size()) {
            Gdx.app.exit();
            return;
        }
        restante = duracoes.get(atual);
        passos.get(atual).abrir();
    }

    @Override
    public void render() {
        super.render();
        restante -= Gdx.graphics.getDeltaTime();
        if (restante <= 0 && atual < passos.size()) {
            salvar(nomes.get(atual));
            avancar();
        }
    }

    private void salvar(String nome) {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
        Pixmap imagem = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, imagem.getPixels(), pixels.length);
        FileHandle arquivo = Gdx.files.absolute(diretorio).child(nome + ".png");
        PixmapIO.writePNG(arquivo, imagem);
        imagem.dispose();
    }
}
