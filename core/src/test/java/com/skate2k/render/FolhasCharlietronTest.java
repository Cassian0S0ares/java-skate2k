package com.skate2k.render;

import com.badlogic.gdx.utils.JsonReader;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class FolhasCharlietronTest {
    @Test void recortesPreservamAs32SilhuetasDentroDoAtlas() throws Exception {
        for (String nome : new String[]{"movimento", "acoes", "combate", "kickflip", "impossible", "grinds"}) {
            verificarFolha(nome);
        }
    }

    private void verificarFolha(String nome) throws Exception {
        var dados = new JsonReader().parse(Files.readString(new File("assets/charlietron/" + nome + ".json").toPath()));
        var imagem = ImageIO.read(new File("assets/charlietron/" + dados.getString("image")));
        assertTrue(imagem.getColorModel().hasAlpha(), nome + ": transparência");
        var frames = dados.get("frames");
        assertEquals(32, frames.size);
        for (int i = 0; i < frames.size; i++) {
            var frame = frames.get(i);
            int[] r = frame.get("rect").asIntArray();
            assertTrue(r[2] <= 252 && r[3] <= 268, "Cabe no atlas: " + i);
            int[] pixels = new int[r[2] * r[3]];
            for (int y = 0; y < r[3]; y++) for (int x = 0; x < r[2]; x++)
                pixels[y * r[2] + x] = Integer.rotateLeft(imagem.getRGB(r[0] + x, r[1] + y), 8);
            var origens = frame.get("seeds");
            int[] sementes = new int[origens.size];
            for (int s = 0; s < sementes.length; s++) {
                int[] xy = origens.get(s).asIntArray();
                sementes[s] = xy[1] * r[2] + xy[0];
            }
            boolean[] mascara = RecorteSprite.mascara(pixels, r[2], r[3], sementes);
            int opacos = 0;
            for (int p = 0; p < pixels.length; p++)
                if (mascara[p] && (pixels[p] & 255) > 128) opacos++;
            assertTrue(opacos > 1000, "Pose vazia: " + i);
            assertEquals(frame.getInt("opaquePixels"), opacos, "Silhueta completa: " + i);
        }
    }
}
