# Cinemática de abertura

Cinco quadros estáticos em pixel art. Os quatro primeiros têm legendas;
o quadro final usa a versão sem texto.
Artes preparadas primeiro; a reprodução dentro do jogo ainda não foi implementada.
Geradas com a ferramenta integrada `image_gen`, sem CLI.

## Ordem e roteiro

1. `01-fabrica.png`: “As grandes empresas dominaram o mundo.
   E transformaram o futuro em uma fábrica.”
2. `01b-linha-de-montagem.png`: “Dentro das fábricas, um exército crescia.
   Máquinas feitas para obedecer.”
3. `02-robos.png`: “Seus robôs tomaram as ruas.
   E destruíram tudo o que ainda tinha vida.”
4. `02b-bairro-ocupado.png`: “O medo fechou portas e calou as ruas.
   Mas nem todo mundo desistiu.”
5. `03-outra-ideia-sem-texto.png`: protagonista saindo de casa com o skate,
   sem legenda ou faixa inferior. `03-outra-ideia.png` é a versão anterior.

Os quadros intermediários conectam a origem do exército à destruição e
mostram o cotidiano sob ocupação antes da apresentação do protagonista.
Os nomes dos três arquivos originais foram preservados; seguir esta lista.

O protagonista preserva os elementos visuais de `../personagem_base.png`:
cabelo claro espetado, óculos amarelos, bandana com X, jaqueta amarela e preta,
calça laranja e tênis pretos e brancos. Ele sai de casa carregando o skate.

## Direção dos prompts

- Comum: ilustração narrativa para jogo de skate distópico, quadro horizontal
  16:9, pixel art de 16 bits com pixels quadrados visíveis, paleta limitada de
  roxo carvão, azul aço e âmbar. Painel escuro inferior com texto em português
  em letras pixeladas claras e grandes. Sem marcas reais ou marca d'água.
- Fábrica: complexo industrial imenso dominando uma cidade pequena, chaminés
  escondendo o sol, esteiras, torres empresariais e devastação ambiental;
  plano geral dramático e as duas frases da cena 1.
- Robôs: máquinas industriais gigantes demolindo um bairro e arrancando as
  últimas árvores; robôs patrulhando ruas vazias, garras mecânicas,
  escavadeiras, água poluída, escombros e fábricas ao fundo; sem gore,
  com as duas frases do quadro dos robôs.
- Protagonista: personagem com a aparência descrita acima, de corpo inteiro,
  saindo pela porta aberta de sua casa humilde com um skate de rodas visíveis
  debaixo do braço; pose confiante, luz quente da casa sugerindo esperança,
  chaminés e silhuetas de robôs distantes. A versão atual não tem texto.

### Edição do quadro final

Ferramenta: `image_gen` integrada. Referência: `03-outra-ideia.png`.
Prompt: remover todo o texto e a faixa escura inferior, mantendo as dimensões
e estendendo naturalmente a ilustração com degraus, concreto e cenário.
Preservar personagem, skate, pose, casa, robôs, cidade, paleta e pixel art.
Sem novos personagens, legendas, bordas ou marca d'água.
- Linha de montagem: esteiras dentro da fábrica, braços mecânicos soldando
  robôs roxos com cabeças retangulares e olhos âmbar, fileiras saindo em
  direção à cidade poluída. Ponte entre a fábrica e a invasão das ruas.
- Bairro ocupado: comércio fechado, barreiras e patrulhas robóticas, drone
  de vigilância e fumaça distante; casa de tijolos com porta fechada e janela
  iluminada, revelando um skate e antecipando a chegada do protagonista.

Os prompts completos dos novos quadros estão em `prompts-conexoes.md`.

## Uso futuro no jogo

Carregar os PNGs na ordem do roteiro acima e usar filtro `Nearest` para preservar
a aparência pixelada. Manter a proporção da imagem inteira, sem cortar
o painel das legendas. Os arquivos são quadros completos, não folhas
de animação de personagem.
