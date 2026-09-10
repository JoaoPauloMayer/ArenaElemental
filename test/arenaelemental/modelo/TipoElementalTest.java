package arenaelemental.modelo;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static arenaelemental.modelo.TipoElemental.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A tabela de tipos. As vantagens estao travadas uma a uma, e as propriedades
 * de equilibrio tambem: mudar a tabela sem querer quebra os testes.
 */
public class TipoElementalTest {

    private static final Set<TipoElemental> NATUREZA = EnumSet.of(FOGO, AGUA, PLANTA, PEDRA, GELO);

    @Test
    void oTrianguloClassicoContinuaValendo() {
        assertEquals(2.0, FOGO.multiplicadorContra(PLANTA));
        assertEquals(2.0, PLANTA.multiplicadorContra(AGUA));
        assertEquals(2.0, AGUA.multiplicadorContra(FOGO));
        assertEquals(0.5, PLANTA.multiplicadorContra(FOGO));
        assertEquals(0.5, AGUA.multiplicadorContra(PLANTA));
        assertEquals(0.5, FOGO.multiplicadorContra(AGUA));
    }

    @Test
    void asVantagensDosTiposNovos() {
        assertEquals(2.0, PEDRA.multiplicadorContra(FOGO));
        assertEquals(2.0, PEDRA.multiplicadorContra(GELO));
        assertEquals(0.5, PEDRA.multiplicadorContra(AGUA));
        assertEquals(0.5, PEDRA.multiplicadorContra(PLANTA));

        assertEquals(2.0, GELO.multiplicadorContra(PLANTA));
        assertEquals(2.0, GELO.multiplicadorContra(AGUA));
        assertEquals(0.5, GELO.multiplicadorContra(FOGO));
        assertEquals(0.5, GELO.multiplicadorContra(PEDRA));

        assertEquals(2.0, FOGO.multiplicadorContra(GELO));
        assertEquals(0.5, FOGO.multiplicadorContra(PEDRA));
        assertEquals(2.0, AGUA.multiplicadorContra(PEDRA));
        assertEquals(0.5, AGUA.multiplicadorContra(GELO));
        assertEquals(2.0, PLANTA.multiplicadorContra(PEDRA));
        assertEquals(0.5, PLANTA.multiplicadorContra(GELO));
    }

    @Test
    void sombrioESagradoSaoFortesUmContraOOutro() {
        assertEquals(2.0, SOMBRIO.multiplicadorContra(SAGRADO));
        assertEquals(2.0, SAGRADO.multiplicadorContra(SOMBRIO));
        for (TipoElemental t : NATUREZA) {
            assertEquals(1.0, SOMBRIO.multiplicadorContra(t));
            assertEquals(1.0, SAGRADO.multiplicadorContra(t));
            assertEquals(1.0, t.multiplicadorContra(SOMBRIO));
            assertEquals(1.0, t.multiplicadorContra(SAGRADO));
        }
    }

    @Test
    void cadaTipoDaNaturezaVenceDoisEPerdeParaDois() {
        for (TipoElemental t : NATUREZA) {
            int forte = 0, fraco = 0;
            for (TipoElemental d : NATUREZA) {
                if (t.multiplicadorContra(d) == 2.0) forte++;
                if (t.multiplicadorContra(d) == 0.5) fraco++;
            }
            assertEquals(2, forte, t + " deveria ser supereficaz contra dois");
            assertEquals(2, fraco, t + " deveria ser pouco eficaz contra dois");
        }
    }

    @Test
    void todoParDaNaturezaTemUmVencedorEAVantagemEhEspelhada() {
        for (TipoElemental a : NATUREZA) {
            for (TipoElemental b : NATUREZA) {
                if (a == b) continue;
                double ida = a.multiplicadorContra(b), volta = b.multiplicadorContra(a);
                assertEquals(1.0, ida * volta, a + " x " + b + ": quem e' 2x de um lado e' 0,5x do outro");
                assertNotEquals(1.0, ida, a + " x " + b + " nao tem vencedor");
            }
        }
    }

    @Test
    void umTipoContraEleMesmoEhSempreNeutro() {
        for (TipoElemental t : TipoElemental.values()) assertEquals(1.0, t.multiplicadorContra(t), t.name());
    }

    @Test
    void normalEhNeutroContraTodosENaoHaImunidades() {
        for (TipoElemental d : TipoElemental.values()) {
            assertEquals(1.0, NORMAL.multiplicadorContra(d));
            for (TipoElemental a : TipoElemental.values()) {
                assertNotEquals(0.0, a.multiplicadorContra(d), a + " x " + d);
            }
        }
    }

    @Test
    void todoTipoTemNomeECorProprios() {
        Set<String> nomes = new java.util.HashSet<>();
        for (TipoElemental t : TipoElemental.values()) {
            assertTrue(nomes.add(t.nomeExibicao()), "nome repetido: " + t.nomeExibicao());
            assertNotNull(t.corPrincipal());
        }
    }
}
