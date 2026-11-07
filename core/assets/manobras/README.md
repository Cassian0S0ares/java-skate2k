# Sprites de manobras

Arte do personagem cartoon baseada em `../personagem_cartoon_skate_v2.png`, criada com a ferramenta integrada `image_gen`. Os 14 PNGs contêm 344 poses. O catálogo cobre os 16 valores de `TipoManobra`, com animações compostas para as variações. Abra `previa.html` no navegador para visualizar as sequências.

## Conteúdo

| Arquivo | Uso |
| --- | --- |
| `salto_pouso.png` | Preparação, saída, voo, absorção do impacto e recuperação |
| `kickflip_ciclo.png` | Kickflip, Double Kickflip e Triple Kickflip |
| `impossible_ciclo.png` | Impossible e Double Impossible |
| `hardflip_ciclo.png` | Combinação de flip e impossible reconhecida pelo jogo |
| `giro_8direcoes.png` | Giros de 180°, 360°, 540° e 720°, nos dois sentidos |
| `varial_se.png`, `varial_sw.png`, `varial_nw.png`, `varial_ne.png` | Flip com meio giro do corpo, nos dois sentidos |
| `grab.png` | Pega-Pão: alcançar, segurar e soltar o shape |
| `grind_5050.png` | 50-50: entrada, equilíbrio central/lateral e saída |
| `boardslide.png` | Boardslide com prancha transversal e equilíbrio lateral |
| `gap_escada.png` | Gap da Escada: preparação, salto e pouso |
| `voo_longo.png` | Voo da Galinha, com ciclo de sustentação |

## Quadros e direções

`manifest.json` contém os tamanhos reais, os retângulos de recorte e a ordem de reprodução. As coordenadas têm origem no canto superior esquerdo e os índices começam em zero. Use `rect` para recortar; `cellRect` registra a célula nominal. Alguns recortes ultrapassam ligeiramente a célula para preservar braços e mãos. `contentBounds` mede a região visível e `previewScale` normaliza apenas a prévia.

As folhas de 8 colunas × 4 linhas usam SE ↘, SW ↙, NW ↖ e NE ↗, de cima para baixo. As poses de costas mostram a mochila. Na folha de giro, os oito quadros são SE, S, SW, W, NW, N, NE e E. Essa folha tem 1254 × 1254 pixels; as outras têm 1536 × 1024.

As folhas de varial têm 6 colunas × 2 linhas: horário em cima e anti-horário embaixo. As animações do manifesto usam os três quadros intermediários de cada linha e as vistas da folha de giro para a saída e a captura final. Os quadros de ponta desenhados nessas folhas não são usados, pois algumas orientações geradas divergiram da direção pedida. Siga a sequência do manifesto em vez de reproduzir a folha inteira.

## Variações presentes no código

- Kickflip: um, dois e três ciclos de rotação, com uma única saída e um único pouso. Acima de três voltas, o detector continua pontuando como Triple Kickflip.
- Impossible: um e dois ciclos; acima de dois, o detector continua pontuando como Double Impossible.
- Giros: 180°, 360°, 540° e 720°, horário e anti-horário, a partir das quatro direções. Os quadros de giro são compartilhados; a sequência define o total. Na projeção atual, `Comandos.giro = +1` corresponde ao anti-horário da tela e `-1` ao horário.
- Fakie: muda a orientação inicial do corpo em 180° em relação ao deslocamento. O manifesto inclui aliases para essa postura. Giros de 180°/540° e o varial básico terminam na orientação oposta.
- Grab: entrada, sustentação repetível e soltura. O código reconhece a manobra a partir de 0,25 s e aumenta a dificuldade acima de 0,7 s. Soltar antes do pouso continua sendo uma regra da lógica.
- 50-50 e boardslide: centro, inclinação para os dois lados e saída. A duração altera pontos/dificuldade acima de 1,2 s e 2,4 s; não exige outro desenho. O manifesto associa as poses ao valor de equilíbrio.
- Gap da Escada e Voo da Galinha são bônus contextuais. Voo longo exige pelo menos 1,45 s e outra manobra reconhecida. Seus ciclos de voo podem ser sustentados até a descida.

## Uso no jogo

Este pacote adiciona arte e sequências; `RenderizadorEntidades` ainda desenha o personagem proceduralmente. A integração com o renderizador não foi feita nesta entrega. Os tempos do manifesto são referências de animação: a física, a altura, o contato com trilhos e a confirmação de manobras devem continuar vindo de `Jogador` e `DetectorManobras`.

As poses ilustradas discretizam os movimentos. O código permite combinar e acumular rotações continuamente; as folhas cobrem as famílias e variações nomeadas, não cada combinação possível de ângulos simultâneos. Hardflip e varial seguem as combinações reconhecidas pelo jogo, sem alterar suas regras. A restrição `SEM_PULO` pode impedir manobras aéreas durante a partida, mas seus assets também estão incluídos.

Prompts e parâmetros de geração: `../../../output/imagegen/manobras/specs-final.json`. Relatório de recortes: `../../../output/imagegen/manobras/sheet-metadata-final.json`.
