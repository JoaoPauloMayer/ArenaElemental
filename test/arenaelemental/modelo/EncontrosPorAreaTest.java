package arenaelemental.modelo;

import arenaelemental.mundo.Area;
import arenaelemental.mundo.Habitats;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static arenaelemental.mundo.Area.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Quem aparece em cada area. O habitat sai do tipo (ver {@link Habitats}) e
 * esta travado aqui tipo a tipo, porque e' desenho de jogo.
 */
public class EncontrosPorAreaTest {

    @Test
    void cadaTipoViveOndeFoiPedido() {
        assertEquals(EnumSet.of(PALACIO_SAGRADO), Habitats.areasDoTipo(TipoElemental.SAGRADO));
        assertEquals(EnumSet.of(FLORESTA_PROFUNDA, MAR_PROFUNDO, MONTANHA_NEGRA), Habitats.areasDoTipo(TipoElemental.SOMBRIO));
        assertEquals(EnumSet.of(RAVINA_CONGELADA), Habitats.areasDoTipo(TipoElemental.GELO));
        assertEquals(EnumSet.of(MONTANHA, RAVINA, VULCAO), Habitats.areasDoTipo(TipoElemental.PEDRA));
        assertEquals(EnumSet.of(FLORESTA, FLORESTA_PROFUNDA), Habitats.areasDoTipo(TipoElemental.PLANTA));
        assertEquals(EnumSet.of(VULCAO, FLORESTA), Habitats.areasDoTipo(TipoElemental.FOGO));
        assertEquals(EnumSet.of(PRAIA, MAR, MAR_PROFUNDO), Habitats.areasDoTipo(TipoElemental.AGUA));
        assertTrue(Habitats.areasDoTipo(TipoElemental.NORMAL).isEmpty());
    }

    @Test
    void fogoNaFlorestaEhRaro() {
        assertEquals(Habitats.Presenca.RARA, Habitats.presenca(TipoElemental.FOGO, FLORESTA));
        assertEquals(Habitats.Presenca.COMUM, Habitats.presenca(TipoElemental.FOGO, VULCAO));
        assertEquals(Habitats.Presenca.AUSENTE, Habitats.presenca(TipoElemental.FOGO, MAR));
    }

    @Test
    void oHabitatDeCadaEspecieSaiDosTipos() {
        assertEquals(EnumSet.of(PALACIO_SAGRADO), Especies.CANDEIO.getHabitat());
        assertEquals(EnumSet.of(FLORESTA_PROFUNDA, MAR_PROFUNDO, MONTANHA_NEGRA), Especies.PENUMBRA.getHabitat());
        assertEquals(EnumSet.of(RAVINA_CONGELADA), Especies.NEVISCO.getHabitat());
        assertEquals(EnumSet.of(MONTANHA, RAVINA, VULCAO), Especies.CALHAU.getHabitat());
        assertEquals(EnumSet.of(FLORESTA, FLORESTA_PROFUNDA), Especies.FOLHARAL.getHabitat());
        assertEquals(EnumSet.of(VULCAO, FLORESTA), Especies.BRASEIRO.getHabitat());
        assertEquals(EnumSet.of(PRAIA, MAR, MAR_PROFUNDO), Especies.MARULHO.getHabitat());
    }

    @Test
    void asDeDoisTiposVivemOndeOsDoisTiposSeEncontram() {
        assertEquals(EnumSet.of(MAR_PROFUNDO), Especies.BREUMAR.getHabitat(), "Sombrio e Agua");
        assertEquals(EnumSet.of(FLORESTA_PROFUNDA), Especies.TOCUMBRA.getHabitat(), "Sombrio e Planta");
        assertEquals(EnumSet.of(VULCAO), Especies.BRASALTO.getHabitat(), "Fogo e Pedra");
    }

    @Test
    void todaAreaTemAlguemETodaEspecieViveEmAlgumLugar() {
        for (Area a : Area.values()) assertFalse(Especies.daArea(a).isEmpty(), "ninguem vive em " + a.getNome());
        for (EspecieCriatura e : Especies.todas()) assertFalse(e.getHabitat().isEmpty(), e.getNome() + " nao vive em lugar nenhum");
    }

    @Test
    void oRaroPesaMenosQueOComum() {
        assertEquals(Especies.PESO_RARO, Especies.pesoEm(Especies.BRASEIRO, FLORESTA));
        assertEquals(Especies.PESO_COMUM, Especies.pesoEm(Especies.BRASEIRO, VULCAO));
        assertEquals(Especies.PESO_COMUM, Especies.pesoEm(Especies.FOLHARAL, FLORESTA));
        assertEquals(0, Especies.pesoEm(Especies.BRASEIRO, MAR), "fora do habitat nao aparece");
        assertTrue(Especies.PESO_RARO < Especies.PESO_COMUM);
    }

    @Test
    void naFlorestaOBraseiroApareceEmMenorNumero() {
        Map<EspecieCriatura, Integer> contagem = new HashMap<>();
        Random rng = new Random(11);
        int sorteios = 8000;
        for (int i = 0; i < sorteios; i++) contagem.merge(Especies.sortear(FLORESTA, rng), 1, Integer::sum);

        // Folharal pesa 3 e Braseiro 1: 75% e 25%
        assertEquals(2, contagem.size(), "na Floresta so vivem Folharal e Braseiro");
        assertEquals(0.25, contagem.get(Especies.BRASEIRO) / (double) sorteios, 0.03);
        assertEquals(0.75, contagem.get(Especies.FOLHARAL) / (double) sorteios, 0.03);
    }

    @Test
    void oSorteioSoDevolveQuemViveNaArea() {
        Random rng = new Random(3);
        for (Area area : Area.values()) {
            for (int i = 0; i < 200; i++) {
                EspecieCriatura e = Especies.sortear(area, rng);
                assertTrue(e.viveEm(area), e.getNome() + " apareceu em " + area.getNome());
            }
        }
    }

    @Test
    void oHabitatDoRegistroSoRestringe() {
        EspecieCriatura soNoVulcao = EspecieCriatura.novo("so-no-vulcao", "Teste")
                .tipo(TipoElemental.FOGO).habitat(VULCAO).construir();
        assertEquals(EnumSet.of(VULCAO), soNoVulcao.getHabitat());

        EspecieCriatura foraDoTipo = EspecieCriatura.novo("fora-do-tipo", "Teste")
                .tipo(TipoElemental.FOGO).habitat(MAR).construir();
        assertTrue(foraDoTipo.getHabitat().isEmpty(), "o registro nao pode por Fogo no Mar");
    }

    @Test
    void aFabricaUsaAArea() {
        FabricaCriaturas fabrica = new FabricaCriaturas(new Random(8));
        boolean apareceu = false;
        for (int i = 0; i < 200; i++) {
            Criatura c = fabrica.selvagem(FLORESTA_PROFUNDA, 10);
            assertTrue(c.getEspecie().viveEm(FLORESTA_PROFUNDA));
            apareceu |= c.getEspecie() == Especies.TOCUMBRA;
        }
        assertTrue(apareceu, "a Tocumbra tem de aparecer na Floresta Profunda");
    }

    @Test
    void aMesmaSementeRepeteOsEncontrosDaArea() {
        FabricaCriaturas a = new FabricaCriaturas(new Random(42)), b = new FabricaCriaturas(new Random(42));
        for (int i = 0; i < 30; i++) {
            assertEquals(a.selvagem(MAR_PROFUNDO, 15).getEspecie(), b.selvagem(MAR_PROFUNDO, 15).getEspecie());
        }
    }
}
