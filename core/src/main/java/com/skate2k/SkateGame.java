package com.skate2k;

import com.badlogic.gdx.Game;
import com.skate2k.audio.Audio;
import com.skate2k.logica.CatalogoFases;
import com.skate2k.logica.Pontuacao;
import com.skate2k.logica.RepositorioRecorde;
import com.skate2k.logica.Restricao;
import com.skate2k.persistencia.RecordePreferences;
import com.skate2k.render.Desenho;
import com.skate2k.telas.TelaGameOver;
import com.skate2k.telas.TelaJogo;
import com.skate2k.telas.TelaMenu;
import com.skate2k.telas.TelaResultadoFase;

/** Ponto de entrada: guarda os recursos compartilhados e faz as trocas de tela. */
public class SkateGame extends Game {
    public Desenho desenho;
    public Audio audio;
    public RepositorioRecorde recorde;
    /** Começa no sorteio oficial documentado no README; a tecla R no menu sorteia outra. */
    public Restricao restricao = Restricao.sorteioOficial();

    @Override
    public void create() {
        desenho = new Desenho();
        audio = new Audio();
        audio.carregar();
        recorde = new RecordePreferences();
        abrirPrimeiraTela();
    }

    /** Pode ser sobrescrito por ferramentas (por exemplo, o modo de captura de tela). */
    protected void abrirPrimeiraTela() {
        irParaMenu();
    }

    public void irParaMenu() {
        trocar(new TelaMenu(this));
    }

    public void novoJogo() {
        trocar(new TelaJogo(this, 0, new Pontuacao()));
    }

    public void proximaFase(int indiceAtual, Pontuacao pontuacao) {
        trocar(new TelaJogo(this, indiceAtual + 1, pontuacao));
    }

    public void mostrarResultadoFase(int indice, Pontuacao pontuacao, int bonus, int meta) {
        trocar(new TelaResultadoFase(this, indice, pontuacao, bonus, meta));
    }

    /** Fim de jogo: salva o recorde se foi batido e mostra o resumo. */
    public void encerrar(Pontuacao pontuacao, int indiceFase, boolean venceu, boolean nocaute, int meta) {
        int recordeAnterior = recorde.carregar();
        boolean novoRecorde = pontuacao.salvarSeRecorde(recorde);
        trocar(new TelaGameOver(this, pontuacao, indiceFase, venceu, nocaute, novoRecorde, recordeAnterior, meta));
    }

    public boolean ultimaFase(int indice) {
        return indice >= CatalogoFases.TOTAL - 1;
    }

    private void trocar(com.badlogic.gdx.Screen nova) {
        com.badlogic.gdx.Screen anterior = getScreen();
        setScreen(nova);
        if (anterior != null) anterior.dispose();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        desenho.dispose();
        audio.dispose();
    }
}
