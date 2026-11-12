# Protagonista caído

`caido.png`: folha transparente com quatro poses de queda, em grade 2 × 2, na ordem SE, SW, NW, NE.
Criada com image_gen integrado usando `personagem_cartoon_skate_v2.png` como referência de identidade.
Prompt completo em `caido.prompt.txt`. O skate e as estrelas são desenhados separadamente pelo jogo.

`FolhaPersonagem` recorta as margens transparentes e centraliza o corpo caído no ponto de contato.
As poses de movimento vêm da folha original; saltos, grabs, flips e grinds usam os recortes do
manifesto em `manobras/manifest.json`. Os ciclos de flip acompanham os ângulos da física em oito poses.

`movimento.json` registra os 32 recortes medidos individualmente na folha original, cujas poses
não formam uma grade regular. `AtlasMovimento` isola a maior silhueta de cada recorte, preserva
sua borda suavizada e monta uma grade transparente em memória, com apoio alinhado pela base do skate.
Isso evita braços cortados e fragmentos de poses vizinhas durante a animação.
Para inspecionar os quadros usados pelo jogo: `--captura output/personagem sprites-movimento`.
