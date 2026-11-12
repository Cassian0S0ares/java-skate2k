package com.skate2k.render;

import com.badlogic.gdx.utils.JsonReader;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class RecorteSpriteTest {
    @Test
    void isolaAs32PosesCompletasSemPixelsOpacosDosVizinhos() throws Exception {
        var dados = new JsonReader().parse(Files.readString(new File("assets/personagem/movimento.json").toPath()));
        var imagem = ImageIO.read(new File("assets/" + dados.getString("source")));
        // Áreas das silhuetas completas, medidas na folha original antes dos recortes.
        int[] areas = {7196,7405,7666,7271,7563,8171,7157,7297,
                7581,7705,7710,7472,7843,8602,7894,8086,
                7650,7641,8044,7604,8572,8962,7938,8501,
                7094,7782,8139,7264,7838,8994,7373,8310};
        int recortesComVizinho = 0;
        for (int i = 0; i < areas.length; i++) {
            int[] r = dados.get("rects").get(i).asIntArray();
            int[] pixels = new int[r[2] * r[3]];
            for (int y = 0; y < r[3]; y++) for (int x = 0; x < r[2]; x++)
                pixels[y * r[2] + x] = Integer.rotateLeft(imagem.getRGB(r[0] + x, r[1] + y), 8);
            boolean[] mascara = RecorteSprite.mascara(pixels, r[2], r[3]);
            int selecionados = 0, descartados = 0;
            for (int p = 0; p < pixels.length; p++) {
                if ((pixels[p] & 255) > 128) {
                    if (mascara[p]) selecionados++; else descartados++;
                }
            }
            assertEquals(areas[i], selecionados, "Pose " + i + " cortada ou misturada");
            if (descartados > 0) recortesComVizinho++;
        }
        assertTrue(recortesComVizinho > 0, "A folha contém poses com retângulos sobrepostos");
    }
}
