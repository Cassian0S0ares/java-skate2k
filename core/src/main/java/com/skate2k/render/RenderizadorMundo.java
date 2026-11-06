package com.skate2k.render;

import static com.skate2k.render.Projecao.sx;
import static com.skate2k.render.Projecao.sy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.skate2k.logica.Cenario;
import com.skate2k.logica.ElementoCenario;
import com.skate2k.logica.Item;
import com.skate2k.logica.Jogador;
import com.skate2k.logica.MundoJogo;
import com.skate2k.logica.Restricao;
import com.skate2k.logica.RoboSkatista;
import com.skate2k.logica.TrilhoGrind;
import java.util.ArrayList;
import java.util.List;

/**
 * Desenha o mundo isométrico. Elementos fixos ganham uma ordem de pintura topológica; cada entidade
 * móvel é então encaixada logo depois do último elemento que está atrás dela.
 */
public final class RenderizadorMundo {
    private static final class Entidade {
        Object alvo;
        int posicao;
        float profundidade;
    }

    private final Desenho d;
    private final Cenario cenario;
    private final RenderizadorCenario rCenario;
    private final RenderizadorEntidades rEntidades;
    private final List<ElementoCenario> fundo = new ArrayList<>();
    private final List<ElementoCenario> ordenados;
    private final List<Entidade> entidades = new ArrayList<>();
    private final Paleta.Tema tema;
    private final float[] vooPombo;
    private final float[] gotaX = new float[140];
    private final float[] gotaY = new float[140];

    public RenderizadorMundo(Desenho d, MundoJogo mundo) {
        this.d = d;
        this.cenario = mundo.fase().cenario;
        this.tema = Paleta.tema(mundo.fase().tema);
        this.rCenario = new RenderizadorCenario(d, tema);
        this.rEntidades = new RenderizadorEntidades(d);
        this.ordenados = ordenar(cenario.elementos);
        this.vooPombo = new float[cenario.enfeites.size()];
        for (ElementoCenario e : cenario.elementos) {
            if (e.fundo) fundo.add(e);
        }
        fundo.sort((a, b) -> Float.compare(b.x + b.y, a.x + a.y));
        for (int i = 0; i < gotaX.length; i++) {
            gotaX[i] = (float) Math.random();
            gotaY[i] = (float) Math.random();
        }
    }

    /** "a" deve ser pintado antes de "b" quando está inteiramente mais distante da câmera. */
    private static boolean antes(ElementoCenario a, ElementoCenario b) {
        return a.x >= b.x1() - 0.001f || a.y >= b.y1() - 0.001f;
    }

    private static List<ElementoCenario> ordenar(List<ElementoCenario> todos) {
        List<ElementoCenario> restantes = new ArrayList<>();
        for (ElementoCenario e : todos) {
            if (!e.fundo) restantes.add(e);
        }
        List<ElementoCenario> saida = new ArrayList<>();
        while (!restantes.isEmpty()) {
            ElementoCenario escolhido = null;
            for (ElementoCenario candidato : restantes) {
                boolean livre = true;
                for (ElementoCenario outro : restantes) {
                    if (outro != candidato && antes(outro, candidato) && !antes(candidato, outro)) {
                        livre = false;
                        break;
                    }
                }
                if (livre && (escolhido == null
                        || candidato.x + candidato.y > escolhido.x + escolhido.y)) {
                    escolhido = candidato;
                }
            }
            // Em um ciclo improvável, cai para o mais distante.
            if (escolhido == null) {
                escolhido = restantes.get(0);
                for (ElementoCenario e : restantes) {
                    if (e.x + e.y > escolhido.x + escolhido.y) escolhido = e;
                }
            }
            saida.add(escolhido);
            restantes.remove(escolhido);
        }
        return saida;
    }

    public void limparTela() {
        Gdx.gl.glClearColor(tema.fundo.r, tema.fundo.g, tema.fundo.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void desenhar(MundoJogo m, OrthographicCamera cam, Efeitos efeitos, float tempo, float dt) {
        float meiaL = cam.viewportWidth * cam.zoom / 2f;
        float meiaA = cam.viewportHeight * cam.zoom / 2f;
        float minX = cam.position.x - meiaL;
        float maxX = cam.position.x + meiaL;
        float minY = cam.position.y - meiaA;
        float maxY = cam.position.y + meiaA;

        boolean chuva = m.restricao() == Restricao.CHUVA;
        d.iniciar(cam.combined);
        rCenario.desenharPiso(cenario, minX, maxX, minY, maxY, chuva, tempo);
        bordaDoMapa();
        Jogador j = m.jogador();
        desenharEnfeites(j, tempo, dt);

        for (ElementoCenario e : fundo) rCenario.desenhar(e, tempo, false);

        montarEntidades(m);
        TrilhoGrind trilhoProximo = cenario.trilhoMaisProximo(j.x, j.y);
        if (trilhoProximo != null && trilhoProximo.distancia(j.x, j.y) > 2.8f) trilhoProximo = null;

        cursorEntidade = 0;
        desenharEntidadesAte(-1, tempo);
        for (int i = 0; i < ordenados.size(); i++) {
            ElementoCenario e = ordenados.get(i);
            rCenario.desenhar(e, tempo, RenderizadorCenario.elementoDoTrilho(e, trilhoProximo));
            desenharEntidadesAte(i, tempo);
        }

        if (j.estado() == Jogador.Estado.GRIND) {
            efeitos.faiscasDeGrind(sx(j.x, j.y), sy(j.x, j.y, j.z + 0.08f), dt);
        }
        efeitos.desenhar(d);
        desenharMarcadorJogador(j, tempo);
        desenharBarras(m);
        d.terminar();
    }

    private void bordaDoMapa() {
        float w = cenario.largura;
        float h = cenario.profundidade;
        Color borda = Paleta.AMARELO;
        d.linha(sx(0, 0), sy(0, 0, 0), sx(w, 0), sy(w, 0, 0), 5f, Paleta.TINTA);
        d.linha(sx(0, 0), sy(0, 0, 0), sx(0, h), sy(0, h, 0), 5f, Paleta.TINTA);
        // Tracejado amarelo: é aqui que o rolê termina.
        for (float x = 0; x < w; x += 2f) {
            d.linha(sx(x, 0), sy(x, 0, 0), sx(x + 1f, 0), sy(x + 1f, 0, 0), 3f, borda);
        }
        for (float y = 0; y < h; y += 2f) {
            d.linha(sx(0, y), sy(0, y, 0), sx(0, y + 1f), sy(0, y + 1f, 0), 3f, borda);
        }
    }

    private void desenharEnfeites(Jogador j, float tempo, float dt) {
        for (int i = 0; i < cenario.enfeites.size(); i++) {
            Cenario.Enfeite e = cenario.enfeites.get(i);
            if (e.tipo == Cenario.TipoEnfeite.POMBO) {
                pombo(e, i, j, tempo, dt);
            } else {
                rCenario.desenharEnfeite(e);
            }
        }
    }

    private void pombo(Cenario.Enfeite e, int i, Jogador j, float tempo, float dt) {
        if (vooPombo[i] == 0 && Math.hypot(j.x - e.x, j.y - e.y) < 2.4f) vooPombo[i] = 0.001f;
        if (vooPombo[i] > 0) vooPombo[i] += dt;
        // Depois de um tempo o pombo volta para o mesmo lugar, como todo pombo.
        if (vooPombo[i] > 9f) vooPombo[i] = 0f;
        float voo = vooPombo[i];
        float subida = voo > 0 ? Math.min(voo, 3f) * 160f : 0f;
        float lado = voo > 0 ? Math.min(voo, 3f) * ((e.variacao % 2 == 0) ? 110f : -110f) : 0f;
        if (subida > 400f) return;

        float x = sx(e.x, e.y) + lado;
        float y = sy(e.x, e.y, 0) + subida;
        float bicada = voo > 0 ? 0f : Math.max(0f, (float) Math.sin(tempo * 4f + e.variacao * 2f)) * 4f;
        if (voo == 0) d.elipse(x, y - 1, 18, 7, Paleta.SOMBRA_CHAO);
        Color corpo = Paleta.hex("9aa3b8");
        d.elipse(x, y + 7, 21, 15, Paleta.TINTA);
        d.elipse(x, y + 7, 17, 11, corpo);
        d.circulo(x + 7, y + 13 - bicada, 6, Paleta.TINTA);
        d.circulo(x + 7, y + 13 - bicada, 4.2f, Paleta.hex("6c7a94"));
        d.tri(x + 10, y + 15 - bicada, x + 10, y + 11 - bicada, x + 15, y + 12 - bicada, Paleta.LARANJA);
        if (voo > 0) {
            float asa = (float) Math.sin(tempo * 40f) * 12f;
            d.linha(x, y + 9, x - 14, y + 11 + asa, 4, corpo);
            d.linha(x, y + 9, x + 12, y + 11 + asa, 4, corpo);
        }
    }

    private void montarEntidades(MundoJogo m) {
        entidades.clear();
        Jogador j = m.jogador();
        adicionar(j, j.x, j.y, j.z);
        for (RoboSkatista r : m.robos()) adicionar(r, r.x, r.y, 0);
        for (Item item : m.fase().itens) {
            if (item.disponivel()) adicionar(item, item.x, item.y, item.z);
        }
        entidades.sort((a, b) -> {
            if (a.posicao != b.posicao) return Integer.compare(a.posicao, b.posicao);
            return Float.compare(b.profundidade, a.profundidade);
        });
    }

    private void adicionar(Object alvo, float x, float y, float z) {
        Entidade ent = new Entidade();
        ent.alvo = alvo;
        ent.profundidade = x + y;
        ent.posicao = -1;
        for (int i = 0; i < ordenados.size(); i++) {
            if (estaNaFrente(x, y, z, ordenados.get(i))) ent.posicao = i;
        }
        entidades.add(ent);
    }

    /** A entidade fica na frente do elemento se está acima dele ou não está além da sua face oposta. */
    private static boolean estaNaFrente(float x, float y, float z, ElementoCenario e) {
        float topo = e.tipo == com.skate2k.logica.TipoElemento.CORRIMAO ? Math.max(e.alturaInicio, e.alturaFim) : e.altura;
        if (e.contem(x, y) && z >= e.alturaEm(x, y) - 0.05f) return true;
        if (z >= topo - 0.05f) return true;
        return x < e.x1() && y < e.y1();
    }

    private int cursorEntidade;

    private void desenharEntidadesAte(int posicao, float tempo) {
        while (cursorEntidade < entidades.size() && entidades.get(cursorEntidade).posicao <= posicao) {
            Object alvo = entidades.get(cursorEntidade).alvo;
            if (alvo instanceof Jogador) {
                Jogador j = (Jogador) alvo;
                rEntidades.jogador(j, cenario.alturaEm(j.x, j.y), tempo);
            } else if (alvo instanceof RoboSkatista) {
                RoboSkatista r = (RoboSkatista) alvo;
                rEntidades.robo(r, cenario.alturaEm(r.x, r.y), tempo);
            } else {
                rEntidades.item((Item) alvo, tempo);
            }
            cursorEntidade++;
        }
    }

    private void desenharMarcadorJogador(Jogador j, float tempo) {
        float x = sx(j.x, j.y);
        float y = sy(j.x, j.y, j.z) + 104 + (float) Math.sin(tempo * 5f) * 4f;
        d.tri(x - 10, y + 12, x + 10, y + 12, x, y - 3, Paleta.TINTA);
        d.tri(x - 6.5f, y + 10, x + 6.5f, y + 10, x, y, Paleta.LIMA);
    }

    private void desenharBarras(MundoJogo m) {
        Jogador j = m.jogador();
        float x = sx(j.x, j.y);
        float y = sy(j.x, j.y, j.z);

        if (j.carregandoPulo()) {
            float w = 58;
            d.ret(x - w / 2 - 2, y - 24, w + 4, 11, Paleta.TINTA);
            d.ret(x - w / 2, y - 22, w * j.cargaPulo(), 7, Paleta.LIMA);
        }
        if (j.estado() == Jogador.Estado.GRIND) {
            // Barra de equilíbrio: o marcador não pode encostar nas pontas.
            float w = 150;
            float bx = x - w / 2f;
            float by = y + 118;
            d.ret(bx - 3, by - 3, w + 6, 18, Paleta.TINTA);
            d.ret(bx, by, w, 12, Paleta.VERMELHO);
            d.ret(bx + w * 0.2f, by, w * 0.6f, 12, Paleta.AMARELO);
            d.ret(bx + w * 0.36f, by, w * 0.28f, 12, Paleta.LIMA);
            float mx = bx + w / 2f + j.equilibrio() * w / 2f;
            d.ret(mx - 4, by - 5, 8, 22, Paleta.BRANCO);
            d.contornoRet(mx - 4, by - 5, 8, 22, 2, Paleta.TINTA);
            FontePixel.centro(d, "< A   D >", x, by + 22, 2f, Paleta.BRANCO);
        }
        for (RoboSkatista r : m.robos()) {
            if (r.tipo.vida <= 1) continue;
            float rx = sx(r.x, r.y);
            float ry = sy(r.x, r.y, r.tipo.altura + 0.72f);
            float w = 12f * r.tipo.vida + 4;
            d.ret(rx - w / 2, ry, w, 10, Paleta.TINTA);
            for (int i = 0; i < r.tipo.vida; i++) {
                if (i < r.vida()) d.ret(rx - w / 2 + 3 + i * 12f, ry + 2, 10, 6, Paleta.ROSA);
            }
        }
    }

    /** Chuva em espaço de tela, chamada depois do mundo. */
    public void desenharChuva(float largura, float altura, float tempo) {
        Color cor = Paleta.copia(Paleta.hex("bfe9ff"), 0.45f);
        for (int i = 0; i < gotaX.length; i++) {
            float velocidade = 900f + (i % 5) * 110f;
            float y = altura - ((gotaY[i] * altura + tempo * velocidade) % (altura + 40f));
            float x = (gotaX[i] * (largura + 200f) - 100f + (altura - y) * -0.22f + largura) % (largura + 60f);
            d.linha(x, y, x + 5f, y + 24f, 2f, cor);
        }
    }
}
