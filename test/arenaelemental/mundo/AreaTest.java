package arenaelemental.mundo;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static arenaelemental.mundo.Area.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * O mapa. As rotas estao travadas uma a uma: sao desenho de jogo, e mudar uma
 * delas tem de ser uma decisao, nao um acidente.
 */
public class AreaTest {

    @Test
    void aJornadaComecaNaFloresta() {
        assertEquals(FLORESTA, Area.INICIAL);
    }

    @Test
    void asRotasSaoAsDoMapa() {
        assertEquals(List.of(FLORESTA_PROFUNDA, MONTANHA, RAVINA), FLORESTA.getDestinos());
        assertEquals(List.of(FLORESTA), FLORESTA_PROFUNDA.getDestinos());
        assertEquals(List.of(FLORESTA, MONTANHA_NEGRA, VULCAO), MONTANHA.getDestinos());
        assertEquals(List.of(MONTANHA), VULCAO.getDestinos());
        assertEquals(List.of(MONTANHA, PALACIO_SAGRADO), MONTANHA_NEGRA.getDestinos());
        assertEquals(List.of(FLORESTA, RAVINA_CONGELADA, MAR), RAVINA.getDestinos());
        assertEquals(List.of(RAVINA, MAR), RAVINA_CONGELADA.getDestinos());
        assertEquals(List.of(MAR, RAVINA), PRAIA.getDestinos());
        assertEquals(List.of(MAR_PROFUNDO, RAVINA_CONGELADA, PRAIA), MAR.getDestinos());
        assertEquals(List.of(MAR), MAR_PROFUNDO.getDestinos());
    }

    @Test
    void doPalacioSagradoSeVaiParaQualquerOutraArea() {
        Set<Area> esperadas = EnumSet.allOf(Area.class);
        esperadas.remove(PALACIO_SAGRADO);
        assertEquals(esperadas, new HashSet<>(PALACIO_SAGRADO.getDestinos()));
    }

    @Test
    void nenhumaRotaLevaParaAPropriaArea() {
        // explorar a area e' sempre possivel e nao e' uma rota
        for (Area a : Area.values()) assertFalse(a.temRotaPara(a), a.getNome());
    }

    @Test
    void todaAreaTemPeloMenosUmaSaida() {
        for (Area a : Area.values()) assertFalse(a.getDestinos().isEmpty(), a.getNome());
    }

    @Test
    void todaAreaEhAlcancavelAPartirDaFloresta() {
        assertEquals(EnumSet.allOf(Area.class), alcancaveisAPartirDe(Area.INICIAL));
    }

    @Test
    void deTodaAreaDaParaVoltarAFloresta() {
        // como o jogo salvo guarda a area, uma regiao sem saida prenderia o
        // jogador nela para sempre
        for (Area a : Area.values()) {
            assertTrue(alcancaveisAPartirDe(a).contains(Area.INICIAL), a.getNome() + " nao tem caminho de volta");
        }
    }

    private static Set<Area> alcancaveisAPartirDe(Area origem) {
        Set<Area> alcancadas = EnumSet.of(origem);
        boolean cresceu = true;
        while (cresceu) {
            cresceu = false;
            for (Area a : EnumSet.copyOf(alcancadas)) cresceu |= alcancadas.addAll(a.getDestinos());
        }
        return alcancadas;
    }

    @Test
    void asRotasSaoDeMaoUnica() {
        assertTrue(RAVINA.temRotaPara(MAR));
        assertFalse(MAR.temRotaPara(RAVINA), "do Mar nao se volta direto para a Ravina");
        assertTrue(PALACIO_SAGRADO.temRotaPara(FLORESTA));
        assertFalse(FLORESTA.temRotaPara(PALACIO_SAGRADO));
        assertFalse(FLORESTA.temRotaPara(null));
    }

    @Test
    void asListasDeDestinoNaoPodemSerAlteradas() {
        assertThrows(UnsupportedOperationException.class, () -> FLORESTA.getDestinos().add(VULCAO));
        assertThrows(UnsupportedOperationException.class, () -> PALACIO_SAGRADO.getDestinos().clear());
    }

    // ------------------------------------------------------------------
    // Ids e nomes
    // ------------------------------------------------------------------

    @Test
    void osIdsSaoUnicosEVoltamPeloPorId() {
        Set<String> ids = new HashSet<>();
        for (Area a : Area.values()) {
            assertTrue(ids.add(a.getId()), "id repetido: " + a.getId());
            assertSame(a, Area.porId(a.getId()));
        }
        assertNull(Area.porId("atlantida"));
        assertNull(Area.porId(null));
    }

    @Test
    void porNomeIgnoraAcentoMaiusculaESeparador() {
        assertEquals(PALACIO_SAGRADO, Area.porNome("Palácio Sagrado"));
        assertEquals(PALACIO_SAGRADO, Area.porNome("palacio_sagrado"));
        assertEquals(PALACIO_SAGRADO, Area.porNome("PALACIO-SAGRADO"));
        assertEquals(VULCAO, Area.porNome("vulcao"));
        assertEquals(VULCAO, Area.porNome("Vulcão"));
        assertEquals(MAR_PROFUNDO, Area.porNome("  mar   profundo "));
    }

    @Test
    void porNomeNaoConfundeAreasDeNomeParecido() {
        assertEquals(MAR, Area.porNome("Mar"));
        assertEquals(RAVINA, Area.porNome("Ravina"));
        assertEquals(RAVINA_CONGELADA, Area.porNome("Ravina Congelada"));
        assertEquals(FLORESTA, Area.porNome("floresta"));
        assertNull(Area.porNome("floresta densa"));
        assertNull(Area.porNome(null));
    }
}
