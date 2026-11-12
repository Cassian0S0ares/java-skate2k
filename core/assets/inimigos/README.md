# Sprites dos inimigos

`robos.png`: PNG RGBA com transparência real, 1254 × 1254 px, grade 4 × 4.

Linhas, de cima para baixo: Rolimã (laranja), Faísca (ciano), Tanque (roxo), Síndico (cinza, gravata e prancheta).
Colunas, da esquerda para a direita: SE, SW, NW, NE, em direções de tela.

São 16 poses direcionais. O renderizador adiciona balanço durante movimento, tremor de preparação,
compressão, cores de ataque/dano e estrelas de atordoamento. Não há quadros desenhados de animação.
A sombra é desenhada pelo jogo. A textura é compartilhada pelo `Desenho` e liberada no encerramento.

Gerado com a ferramenta integrada image_gen. Prompt completo: `prompt.txt`.
