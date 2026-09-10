package arenaelemental.batalha;

import arenaelemental.modelo.Brasalto;
import arenaelemental.modelo.Breumar;
import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpes;
import arenaelemental.modelo.Marulho;
import arenaelemental.modelo.TipoElemental;
import arenaelemental.modelo.Tocumbra;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Especies de dois tipos: efetividade combinada, STAB e imunidades. */
public class DoisTiposTest {

    private final CalculadoraDano calc = new CalculadoraDano(new Random(1));

    private double efetividade(Criatura atacante, Criatura alvo, arenaelemental.modelo.Golpe golpe) {
        return calc.calcular(atacante, alvo, golpe, false, 100).getEfetividade();
    }

    @Test
    void osMultiplicadoresDosDoisTiposSeMultiplicam() {
        Criatura atacante = new Marulho("Atacante", 30);
        Criatura brasalto = new Brasalto("Brasalto", 30);   // Fogo / Pedra

        assertEquals(4.0, efetividade(atacante, brasalto, Golpes.JATO_DAGUA), "Agua e' 2x em Fogo e 2x em Pedra");
        assertEquals(0.25, efetividade(atacante, brasalto, Golpes.GRANIZO), "Gelo e' 0,5x em Fogo e 0,5x em Pedra");
        assertEquals(1.0, efetividade(atacante, brasalto, Golpes.CHICOTE_VINHA), "0,5x em Fogo e 2x em Pedra se anulam");
    }

    @Test
    void oTipoSecundarioTambemPesaNaDefesa() {
        Criatura atacante = new Marulho("Atacante", 30);
        Criatura breumar = new Breumar("Breumar", 30);       // Sombrio / Agua

        assertEquals(2.0, efetividade(atacante, breumar, Golpes.LAMPEJO), "Sagrado bate forte no Sombrio");
        assertEquals(2.0, efetividade(atacante, breumar, Golpes.CHICOTE_VINHA), "Planta bate forte na Agua");
        assertEquals(0.5, efetividade(atacante, breumar, Golpes.BRASA), "Fogo bate fraco na Agua");
    }

    @Test
    void aFraquezaQuadruplaApareceNoDano() {
        // mesmo poder (40): Jato d'Agua leva STAB 1,5x e tipo 4x; Investida, nenhum dos dois
        Criatura atacante = new Marulho("Atacante", 40);
        int jato = calc.calcular(atacante, new Brasalto("B", 40), Golpes.JATO_DAGUA, false, 100).getDano();
        int investida = calc.calcular(atacante, new Brasalto("B", 40), Golpes.INVESTIDA, false, 100).getDano();
        assertTrue(jato >= investida * 5, jato + " vs " + investida);
    }

    @Test
    void oStabValeParaOsDoisTipos() {
        Criatura breumar = new Breumar("Breumar", 30);
        Criatura alvo = new Marulho("Alvo", 30);
        assertTrue(calc.calcular(breumar, alvo, Golpes.GARRA_SOMBRIA, false, 100).teveStab(), "tipo principal");
        assertTrue(calc.calcular(breumar, alvo, Golpes.JATO_DAGUA, false, 100).teveStab(), "tipo secundario");
        assertFalse(calc.calcular(breumar, alvo, Golpes.INVESTIDA, false, 100).teveStab());
    }

    @Test
    void asImunidadesValemParaQualquerUmDosTipos() {
        assertFalse(new Brasalto("Brasalto", 20).podeReceber(CondicaoStatus.QUEIMADURA),
                "Fogo como tipo principal ja basta para nao se queimar");
        assertTrue(new Tocumbra("Tocumbra", 20).podeReceber(CondicaoStatus.QUEIMADURA));
    }

    @Test
    void aFichaGuardaOsDoisTiposNaOrdem() {
        EspecieCriatura e = Especies.BREUMAR;
        assertEquals(TipoElemental.SOMBRIO, e.getTipo());
        assertEquals(TipoElemental.AGUA, e.getTipoSecundario());
        assertEquals(List.of(TipoElemental.SOMBRIO, TipoElemental.AGUA), e.getTipos());
        assertEquals("Sombrio / Água", e.nomeDosTipos());
        assertTrue(e.temTipo(TipoElemental.AGUA));
        assertFalse(e.temTipo(TipoElemental.PLANTA));
    }

    @Test
    void especieDeUmTipoSoNaoTemSecundario() {
        assertNull(Especies.MARULHO.getTipoSecundario());
        assertEquals(List.of(TipoElemental.AGUA), Especies.MARULHO.getTipos());
        assertEquals("Água", Especies.MARULHO.nomeDosTipos());
    }

    @Test
    void repetirOTipoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> EspecieCriatura.novo("repetida", "Repetida")
                .tipos(TipoElemental.FOGO, TipoElemental.FOGO).construir());
    }
}
