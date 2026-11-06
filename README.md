# Rolê x Robôs

Jogo de skate 2D em Java com libGDX, em câmera isométrica: um skatista contra robôs skatistas em quatro cenários urbanos, com visual cartunesco e cores vibrantes.

## Mecânica central

Suas manobras enchem uma bateria, a **Carga**, que não vale nada até você pisar em um robô: aí ela descarrega inteira, multiplicada pelo seu combo.
A graça é o risco de segurar: quanto mais você manobra sem pisar, maior a explosão de pontos, mas um tombo zera a Carga e o multiplicador.
Rampas, escadas, corrimãos, bancos e muretas potencializam cada manobra e sobem seu estilo, que faz o pisão bater mais forte.

## Restrição sorteada

**Resultado do sorteio: `SEM_PULO`.** Sem ollie livre: só dá para pular perto de um trilho (corrimão, banco ou mureta), e no ar só valem as manobras feitas nos trilhos.

O sorteio está em `Restricao.sorteioOficial()`, que usa `new Random(2000)` entre três regras. A semente é fixa, então o resultado é sempre esse, e um teste automatizado imprime o valor.

| Regra | Efeito | Meta de pontos |
|---|---|---|
| `SEM_PULO` (sorteada) | Pulo só perto de trilho; flips, giros e grabs desligados. Rampas ainda lançam. | x0,6 |
| `CHUVA` | O skate escorrega, o pouso exige mais precisão e o grind fica mais instável. | x0,7 |
| `CAMERA_EM_MOVIMENTO` | A câmera só anda quando você sai de quadro, e flips, giros, grabs e tempo de grind só contam com ela andando. | x1 |

No menu, a tecla **R** sorteia outra regra para variar a partida. O jogo sempre abre com a regra oficial.

## Como rodar

Requer JDK 17 ou superior. O Gradle vem junto no wrapper, e a primeira execução baixa as dependências, então precisa de internet.

```
gradlew.bat desktop:run        (Windows)
./gradlew desktop:run          (Linux/macOS)
```

Outros comandos:

```
gradlew core:test              testes da lógica, sem abrir janela
gradlew desktop:dist           gera desktop/build/libs/skate2k.jar
gradlew desktop:run --args="--captura PASTA"    tour com bot que salva PNGs das telas em PASTA (caminho absoluto)
```

Para rodar o JAR em JDK recente: `java --enable-native-access=ALL-UNNAMED -jar skate2k.jar`.

## Controles

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

## Regras

- **Pontos:** robô derrotado rende `(valor do robô + Carga) x multiplicador`. O que sobra na Carga ao fim de uma fase vencida vale metade, e só no total.
- **Multiplicador:** `1 + manobras consecutivas / 2 + elementos diferentes usados no combo`, até x16. **Cair zera.**
- **Estilo:** manobras difíceis dão mais experiência. São 5 níveis, de Zé Ninguém a Lenda do Bairro; cada um aumenta o dano do pisão e a velocidade. Cair tira um nível.
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

## Fases

Metas sem o fator da restrição; com `SEM_PULO`, multiplique por 0,6.

| # | Fase | Identidade | Itens | Inimigos | Tempo | Meta |
|---|---|---|---|---|---|---|
| 1 | Rua da Padaria | Rua com carros, faixa de pedestres, escadaria e banca | Café, Pastel, Band-Aid | 3 Rolimãs | 80 s | 10.000 |
| 2 | Parque do Pombo | Alamedas, fonte, pombos, árvores; a grama freia o skate | Tênis de Mola, Relógio, 2 Band-Aids | 3 Rolimãs, 2 Faíscas | 85 s | 18.000 |
| 3 | Bowl do Zé | Skatepark com funboxes, transferência e escada grande | Adesivo, Capacete, Tênis de Mola, 2 Band-Aids | 2 Rolimãs, 3 Faíscas, 1 Tanque | 90 s | 30.000 |
| 4 | Vila do Síndico | Subúrbio com casas, varandas, cercas e anões de jardim | Bolo da Vó, Capacete, Relógio, 2 Band-Aids | 3 Faíscas, 2 Tanques, Síndico-9000 | 95 s | 40.000 |

## Assets e placeholders

O projeto **não tem nenhum arquivo de imagem, som ou fonte**. Tudo é gerado por código:

| O quê | Onde | Situação |
|---|---|---|
| Personagens, robôs, cenários e itens | `render/RenderizadorEntidades`, `render/RenderizadorCenario` | Arte final em formas vetoriais; pode ser trocada por sprites |
| Fonte (5x7, com acentos) | `render/FontePixel` | Arte final; pode ser trocada por um `BitmapFont` |
| **Efeitos sonoros** | `audio/Audio` | **Placeholder sintetizado, trocar por sons reais** |
| **Música** | `audio/Audio` (`gerarMusica`) | **Placeholder sintetizado, trocar por uma faixa real** |

Para usar áudio real, crie `assets/sons/` e carregue os arquivos com `Gdx.audio.newSound(...)` e `newMusic(...)` em `Audio.carregar()`.

## Estrutura do projeto

```
java-skate2k/
├── README.md
├── build.gradle, settings.gradle, gradle.properties
├── gradlew, gradlew.bat, gradle/wrapper/
├── desktop/
│   ├── build.gradle
│   └── src/main/java/com/skate2k/desktop/
│       ├── DesktopLauncher.java      abre a janela
│       └── CapturaTelas.java         ferramenta de capturas com bot
└── core/
    ├── build.gradle
    ├── src/test/java/com/skate2k/logica/
    │   └── LogicaTest.java           regras e simulação das fases
    └── src/main/java/com/skate2k/
        ├── SkateGame.java            recursos compartilhados e troca de telas
        ├── logica/                   SEM dependência de libGDX
        │   ├── ConstantesFisica      gravidade, velocidades, força do pulo
        │   ├── MundoJogo             a simulação de uma fase
        │   ├── Jogador               estados e física do skatista
        │   ├── DetectorManobras      voo, rotação do skate e pouso
        │   ├── Manobra, TipoManobra
        │   ├── RoboSkatista, TipoRobo
        │   ├── Pontuacao, RepositorioRecorde
        │   ├── Cenario, ElementoCenario, TrilhoGrind
        │   ├── TipoElemento, TipoPiso, Direcao, VisualBloco
        │   ├── Fase, CatalogoFases
        │   ├── Item, TipoItem
        │   ├── Restricao             regras e sorteio
        │   ├── CameraIso, Comandos, EventoJogo
        │   └── BotDemo               jogador automático (menu e testes)
        ├── render/
        │   ├── Projecao              fórmula isométrica
        │   ├── RenderizadorMundo     ordem de pintura
        │   ├── RenderizadorCenario, RenderizadorEntidades, RenderizadorHud
        │   └── Efeitos, Desenho, FontePixel, Paleta
        ├── telas/
        │   └── TelaBase, TelaMenu, TelaJogo, TelaResultadoFase, TelaGameOver
        ├── audio/Audio.java
        └── persistencia/RecordePreferences.java
```

**Desacoplamento:** o pacote `logica` não importa nada do libGDX. A tela traduz o teclado em `Comandos`, passa o delta time de `Gdx.graphics` para `MundoJogo.atualizar` e recebe de volta uma fila de `EventoJogo`, consumida por efeitos e áudio. É isso que permite aos testes jogar as fases inteiras sem janela.

## Estado do balanceamento

As metas foram calibradas com o `BotDemo` (5 rodadas por fase e por regra), não com jogadores humanos. O bot tem reflexos perfeitos, mas corre direto para os robôs sem desviar, então ele mede só por cima o risco de nocaute. Se ficar fácil ou difícil demais, ajuste `meta` em `CatalogoFases`, `fatorMeta` em `Restricao` e os valores em `ConstantesFisica`.
