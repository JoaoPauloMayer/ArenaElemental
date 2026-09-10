package arenaelemental.modelo;

import arenaelemental.RandomControlado;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Precisao dos golpes e PP por criatura. */
public class PrecisaoEPpTest {

    // ------------------------------------------------------------------
    // Precisao
    // ------------------------------------------------------------------

    @Test
    void golpeDePrecisao100NuncaErra() {
        assertEquals(100, Golpes.INVESTIDA.getPrecisao());
        Random pessimista = RandomControlado.sempreSorteiaAlto();
        for (int i = 0; i < 50; i++) {
            assertTrue(Golpes.INVESTIDA.acerta(pessimista),
                    "um golpe de 100% de precisao nao pode errar nem no pior sorteio");
        }
    }

    @Test
    void golpeDePrecisao95ErraNoSorteioAlto() {
        assertEquals(95, Golpes.LABAREDA.getPrecisao());
        assertFalse(Golpes.LABAREDA.acerta(RandomControlado.comNextInt100(95)));
        assertFalse(Golpes.LABAREDA.acerta(RandomControlado.comNextInt100(99)));
        assertTrue(Golpes.LABAREDA.acerta(RandomControlado.comNextInt100(94)));
        assertTrue(Golpes.LABAREDA.acerta(RandomControlado.comNextInt100(0)));
    }

    @Test
    void aPrecisaoBateComAFrequenciaDeAcertos() {
        Random rng = new Random(7);
        int acertos = 0, tentativas = 20_000;
        for (int i = 0; i < tentativas; i++) if (Golpes.ESPORO_SONIFERO.acerta(rng)) acertos++;

        double taxa = 100.0 * acertos / tentativas;
        assertEquals(Golpes.ESPORO_SONIFERO.getPrecisao(), taxa, 1.5,
                "Esporo Sonífero tem 75% de precisao; a frequencia deve ficar perto disso");
    }

    @Test
    void precisaoEhLimitadaAFaixaDeZeroACem() {
        assertEquals(100, Golpe.novo("x", "X").precisao(500).construir().getPrecisao());
        assertEquals(0, Golpe.novo("y", "Y").precisao(-20).construir().getPrecisao());
    }

    @Test
    void golpeDePrecisaoZeroSempreErra() {
        Golpe inutil = Golpe.novo("inutil", "Inútil").precisao(0).construir();
        Random otimista = RandomControlado.sempreSorteiaBaixo();
        for (int i = 0; i < 20; i++) assertFalse(inutil.acerta(otimista));
    }

    // ------------------------------------------------------------------
    // PP
    // ------------------------------------------------------------------

    @Test
    void osGolpesComecamComOsPpCheios() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        for (Golpe g : c.getGolpes()) {
            assertEquals(g.getPpMaximo(), c.getPp(g), g.getNome() + " deveria comecar cheio");
            assertTrue(c.temPp(g));
        }
    }

    @Test
    void usarOGolpeGastaUmPp() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        int antes = c.getPp(Golpes.BRASA);
        assertTrue(c.gastarPp(Golpes.BRASA));
        assertEquals(antes - 1, c.getPp(Golpes.BRASA));
    }

    @Test
    void osPpSaoContadosPorCriaturaENaoPorGolpe() {
        Criatura um = Especies.BRASEIRO.criar("Um", 5);
        Criatura outro = Especies.BRASEIRO.criar("Outro", 5);
        for (int i = 0; i < 5; i++) um.gastarPp(Golpes.BRASA);

        assertEquals(Golpes.BRASA.getPpMaximo() - 5, um.getPp(Golpes.BRASA));
        assertEquals(Golpes.BRASA.getPpMaximo(), outro.getPp(Golpes.BRASA),
                "gastar os PP de um Braseiro nao pode mexer nos do outro");
    }

    @Test
    void golpeSemPpNaoPodeSerUsado() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        zerar(c, Golpes.BRASA);

        assertEquals(0, c.getPp(Golpes.BRASA));
        assertFalse(c.temPp(Golpes.BRASA));
        assertFalse(c.gastarPp(Golpes.BRASA), "nao da para gastar o que nao existe");
        assertFalse(c.getGolpesUtilizaveis().contains(Golpes.BRASA));
    }

    @Test
    void comTodosOsPpZeradosSobraApenasOEsforco() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        for (Golpe g : c.getGolpes()) zerar(c, g);

        assertFalse(c.temAlgumGolpeUtilizavel());
        assertEquals(List.of(Golpes.ESFORCO), c.getGolpesDisponiveis(),
                "sem PP em nada, o Esforço e' a unica saida — senao a batalha travaria");
    }

    @Test
    void oEsforcoNaoGastaPp() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        assertTrue(Golpes.ESFORCO.isSemLimiteDePp());
        for (int i = 0; i < 30; i++) {
            assertTrue(c.temPp(Golpes.ESFORCO));
            assertTrue(c.gastarPp(Golpes.ESFORCO));
        }
        assertTrue(c.temPp(Golpes.ESFORCO));
    }

    @Test
    void comAlgumPpSobrandoOEsforcoNaoAparece() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        for (Golpe g : c.getGolpes()) if (g != Golpes.INVESTIDA) zerar(c, g);

        assertEquals(List.of(Golpes.INVESTIDA), c.getGolpesDisponiveis());
        assertFalse(c.getGolpesDisponiveis().contains(Golpes.ESFORCO));
    }

    @Test
    void descansarDevolveOsPp() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        for (Golpe g : c.getGolpes()) zerar(c, g);
        c.restaurarTudo();

        for (Golpe g : c.getGolpes()) assertEquals(g.getPpMaximo(), c.getPp(g));
        assertTrue(c.temAlgumGolpeUtilizavel());
    }

    @Test
    void oGolpeAprendidoAoSubirDeNivelChegaComOsPpCheios() {
        Criatura c = Especies.BRASEIRO.criar("B", 34);
        c.ganharExp(GrupoExperiencia.MEDIO_RAPIDO.expTotalParaNivel(35) - c.getExpTotal());

        assertTrue(c.getGolpes().contains(Golpes.LABAREDA));
        assertEquals(Golpes.LABAREDA.getPpMaximo(), c.getPp(Golpes.LABAREDA));
    }

    @Test
    void definirPpEhLimitadoAoMaximoDoGolpe() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        c.definirPp(Golpes.BRASA, 9999);
        assertEquals(Golpes.BRASA.getPpMaximo(), c.getPp(Golpes.BRASA));
        c.definirPp(Golpes.BRASA, -50);
        assertEquals(0, c.getPp(Golpes.BRASA));
    }

    @Test
    void definirPpIgnoraGolpeQueACriaturaNaoSabe() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        c.definirPp(Golpes.MARE_CHEIA, 3);
        assertEquals(0, c.getPp(Golpes.MARE_CHEIA), "ela nao sabe Maré Cheia");
    }

    @Test
    void golpesFortesCustamMaisCaroEmPpEPrecisao() {
        assertTrue(Golpes.INVESTIDA.getPpMaximo() > Golpes.LABAREDA.getPpMaximo(),
                "o golpe fraco tem de durar mais que o forte");
        assertTrue(Golpes.LABAREDA.getPoder() > Golpes.INVESTIDA.getPoder());
    }

    private static void zerar(Criatura c, Golpe g) { c.definirPp(g, 0); }
}
