# Punk Disco!

Jogo de skate 2D em Java com libGDX, em câmera isométrica. Um skatista de bairro enfrenta os robôs de uma corporação que dominou a cidade, passando por três fases urbanas até o duelo final de estilo contra o **Charlietron**.

## Resumo do jogo

- **História:** uma cinemática de abertura mostra como as fábricas criaram um exército de robôs. Depois da fase 3, o CEO libera o "plano de contingência" e manda o Charlietron, um skatista ciborgue. Vencê-lo libera a cidade (cinemática final).
- **Mecânica central:** suas manobras enchem uma bateria, a **Carga**, que não vale nada até você pisar em um robô. Aí ela descarrega inteira, multiplicada pelo seu combo. Quanto mais você manobra sem pisar, maior a explosão de pontos, mas um tombo zera a Carga e o multiplicador.
- **Sequência:** cada manobra seguida sem cair vale 10% a mais que a anterior, até 3x.
- **Cenário:** rampas, escadas, corrimãos, bancos e muretas potencializam cada manobra e sobem seu estilo, que faz o pisão bater mais forte.
- **Fase final:** uma batalha de estilo. Você precisa zerar a vida do Charlietron **e** superar a pontuação de estilo dele. Ele faz manobras, mira pisões anunciados por um alvo vermelho no chão e pode **pular na sua cabeça: você cai e perde o combo** (o capacete absorve um pisão). Quanto mais ferido, mais rápido e agressivo ele fica.
- **Modo sem regra:** o jogo começa sem restrição. No menu, a tecla **R** sorteia uma regra (chuva, câmera em movimento ou sem pulo).

## Como rodar

Só é preciso ter o **JDK 17 ou mais novo** instalado e internet na primeira execução. Os scripts baixam o Gradle e as dependências sozinhos e abrem o jogo:

```
./run.sh        Linux / macOS
run.bat         Windows (funciona com duplo clique)
```

Se preferir usar o Gradle direto:

```
./gradlew desktop:run          Linux/macOS (se der "permissão negada": sh gradlew desktop:run)
gradlew.bat desktop:run        Windows
```

Outros comandos:

```
gradlew core:test              testes da lógica, sem abrir janela
gradlew desktop:dist           gera desktop/build/libs/skate2k.jar
gradlew desktop:run --args="--captura PASTA"    tour com bot que salva PNGs das telas em PASTA (caminho absoluto)
```

Para rodar o JAR em JDK recente: `java --enable-native-access=ALL-UNNAMED -jar skate2k.jar`.

## Como jogar

No menu, use as setas (ou W/S) e ENTER para escolher **JOGAR**. A cinemática de abertura passa com ENTER; **Esc** pula.

| Tecla | Ação |
|---|---|
| WASD ou setas | Andar; no ar, ajustar a direção; no grind, A/D equilibra |
| Espaço | Segure para carregar e solte para pular; no grind, salta do trilho |
| J | Kickflip (aperte de novo para double e triple) |
| K | Impossible |
| L (segure) | Pega-Pão; solte antes de pousar |
| Q / E | Girar o corpo |
| Esc | Pausa; Q volta ao menu durante a pausa |
| M | Liga e desliga o som |
| R (no menu) | Sorteia uma regra |
| 7 | Atalho de teste: conclui a fase atual |

**Dicas:**
1. Manobre no ar e pouse limpo para encher a Carga, depois pise em um robô para descarregá-la.
2. Use rampas, escadas e corrimãos: cada um multiplica a manobra.
3. Encadeie manobras sem cair para aumentar o bônus de sequência.
4. Contra o Charlietron, saia da mira vermelha assim que ela aparecer. Depois de um pisão errado ele fica vulnerável: é a hora de pisar nele.

## Regras

- **Pontos:** robô derrotado rende `(valor do robô + Carga) x multiplicador`. O que sobra na Carga ao fim de uma fase vencida vale metade, e só no total.
- **Multiplicador:** `1 + manobras consecutivas / 2 + elementos diferentes usados no combo`, até x16. **Cair zera.**
- **Bônus de sequência:** +10% por manobra seguida sem cair, até 3x, aplicado ao valor que entra na Carga.
- **Estilo:** manobras difíceis dão experiência. São 5 níveis, de Zé Ninguém a Lenda do Bairro; cada um aumenta o dano do pisão e a velocidade. Cair tira um nível.
- **Potencializadores:** rampa x1,5; escada x2; corrimão x1,75; banco e mureta x1,25.
- **Manobras:** o detector mede o ângulo real do skate no pouso. Rotação incompleta, pouso de lado ou grab ainda segurado viram tombo.
- **Vida:** o jogador tem 3 pontos de vida, que voltam ao máximo a cada fase. Só golpes de robô tiram vida; tombo por erro próprio não tira, mas zera o combo. O Band-Aid devolve 1 ponto, e o Capacete absorve um golpe inteiro.
- **Robôs:** cada modelo tira uma quantidade de vida (tabela abaixo). A investida é avisada com um "!" para cada ponto de dano e derruba. Encostar sem investida não derruba, mas rouba 30% da Carga.
- **Fim de jogo:** é game over se o tempo acabar ou a vida zerar abaixo da meta. Nocaute com a meta já batida só encerra a fase mais cedo, e ela conta como vencida. O recorde fica em `~/.prefs/skate2k`.

| Robô | Blindagem | Dano ao encostar | Dano na investida |
|---|---|---|---|
| Rolimã-3000 | 1 | 1 | 1 |
| Faísca | 2 | 1 | 2 |
| Tanque de Guerra | 4 | 2 | 2 |
| Síndico-9000 | 8 | 2 | 3 (nocaute direto) |
| Charlietron (chefe) | 12 | 1 | 2 (pisão) |

## Fases

Metas sem o fator da regra sorteada.

| # | Fase | Identidade | Itens | Inimigos | Tempo | Meta |
|---|---|---|---|---|---|---|
| 1 | Rua da Padaria | Rua com carros, faixa de pedestres, escadaria e banca | Café, Pastel, Band-Aid | 3 Rolimãs | 80 s | 10.000 |
| 2 | Parque do Pombo | Alamedas, fonte, pombos, árvores; a grama freia o skate | Tênis de Mola, Relógio, 2 Band-Aids | 3 Rolimãs, 2 Faíscas | 85 s | 18.000 |
| 3 | Bowl do Zé | Skatepark com funboxes, transferência e escada grande | Adesivo, Capacete, Tênis de Mola, 2 Band-Aids | 2 Rolimãs, 3 Faíscas, 1 Tanque | 90 s | 30.000 |
| 4 | Charlietron | Subúrbio, batalha final de estilo | Bolo da Vó | Charlietron | 150 s | estilo do chefe + vida zerada |

### O chefe: Charlietron

- **Fúria:** com 67% e 34% da vida ele muda de fase e avisa na tela. A cada fase fica mais rápido, ataca com mais frequência, mira mais à frente de você e avisa o pisão mais rápido.
- **Pisão:** o alvo vermelho no chão trava ao aparecer, então dá para desviar. Se errar e você estiver perto, ele pisa de novo (até 2 vezes na fúria máxima).
- **Estilo:** ele pontua manobras ao pousar (e perde os pontos se for derrubado no ar). Nas fases avançadas faz só as manobras de mais pontos, valendo até 50% a mais.
- **Brecha:** depois de pisar ou cair, fica vulnerável por pouco tempo. Pise nele nessa hora.

## Regras sorteáveis

No menu a tecla **R** troca a regra da partida. O jogo sempre abre em **SEM REGRA**. A fase do Charlietron ignora a regra e usa a batalha de estilo, com pulo livre.

| Regra | Efeito | Meta de pontos |
|---|---|---|
| `SEM_REGRA` | Jogo livre, sem restrição | x1 |
| `SEM_PULO` | Pulo só perto de trilho; flips, giros e grabs desligados. Rampas ainda lançam. | x0,6 |
| `CHUVA` | O skate escorrega, o pouso exige mais precisão e o grind fica mais instável. | x0,7 |
| `CAMERA_EM_MOVIMENTO` | A câmera só anda quando você sai de quadro, e flips, giros, grabs e tempo de grind só contam com ela andando. | x1 |

O sorteio oficial documentado antes (`Restricao.sorteioOficial()`, semente 2000) continua existindo para testes e para a captura de telas.

## Assets

| O quê | Onde |
|---|---|
| Sprites do jogador, dos robôs e do Charlietron | `core/assets/personagem/`, `inimigos/`, `charlietron/` (folhas 8x4, 4 direções) |
| Cinemáticas | `core/assets/cinematicas/` (abertura), `pre-fase4/` (antes do chefe), `final/legendado/` (final) |
| Músicas (MP3) | `core/assets/musicas/`: `menu`, `intro`, `fase1` a `fase4`, `resumo`, `ending` |
| Efeitos sonoros | Sintetizados por código em `audio/Audio` (placeholder; dá para trocar por arquivos reais) |
| Cenários, itens e fonte 5x7 | Desenhados por código em `render/` |

Músicas: `menu` toca no menu, `intro` na cinemática de abertura, `faseN` em cada fase, `resumo` entre fases e na derrota, `ending` no final. Arquivos de música precisam ser MP3 de verdade (confira com `file arquivo.mp3`); um MP4/AAC renomeado não toca.

## Estrutura do projeto

```
java-skate2k/
├── README.md, run.sh, run.bat
├── build.gradle, settings.gradle, gradle.properties
├── gradlew, gradlew.bat, gradle/wrapper/
├── desktop/
│   ├── build.gradle
│   └── src/main/java/com/skate2k/
│       ├── desktop/DesktopLauncher    abre a janela
│       └── desktop/CapturaTelas       ferramenta de capturas com bot
└── core/
    ├── assets/                        sprites, cinemáticas e músicas
    ├── src/test/java/com/skate2k/     testes da lógica e do render
    └── src/main/java/com/skate2k/
        ├── SkateGame.java             recursos compartilhados e troca de telas
        ├── logica/                    SEM dependência de libGDX
        │   ├── MundoJogo, Jogador, DetectorManobras, ConstantesFisica
        │   ├── Manobra, TipoManobra, Pontuacao, RepositorioRecorde
        │   ├── RoboSkatista, TipoRobo, Charlietron (chefe)
        │   ├── Cenario, ElementoCenario, TrilhoGrind, TipoElemento, TipoPiso
        │   ├── Fase, CatalogoFases, Item, TipoItem
        │   ├── Restricao              regras e sorteio
        │   └── CameraIso, Comandos, EventoJogo, BotDemo
        ├── render/                    projeção isométrica, folhas de sprites, HUD, efeitos
        ├── telas/                     Menu, Cinematica, Jogo, ResultadoFase, GameOver
        ├── audio/Audio.java           músicas e efeitos
        └── persistencia/RecordePreferences.java
```

**Desacoplamento:** o pacote `logica` não importa nada do libGDX. A tela traduz o teclado em `Comandos`, passa o delta time de `Gdx.graphics` para `MundoJogo.atualizar` e recebe de volta uma fila de `EventoJogo`, consumida por efeitos e áudio. É isso que permite aos testes jogar as fases inteiras sem janela.

## Estado do balanceamento

As metas das fases 1 a 3 foram calibradas com o `BotDemo` (5 rodadas por fase e por regra), não com jogadores humanos. O bot tem reflexos perfeitos, mas corre direto para os robôs sem desviar, então mede só por cima o risco de nocaute. A dificuldade do Charlietron ainda não foi testada com jogadores. Se ficar fácil ou difícil demais, ajuste `meta` em `CatalogoFases`, `fatorMeta` em `Restricao`, os valores de fúria em `Charlietron` e as constantes em `ConstantesFisica`.
