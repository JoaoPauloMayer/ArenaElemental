package arenaelemental.modelo;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** As condicoes de status no nivel da criatura, fora da batalha. */
public class CondicaoStatusTest {

    private static final Random RNG = new Random(11);

    @Test
    void criaturaComecaSemCondicao() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        assertEquals(CondicaoStatus.NENHUMA, c.getCondicao());
        assertFalse(c.temCondicao());
    }

    @Test
    void aplicarCondicaoPegaUmaVezSo() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        assertTrue(c.aplicarCondicao(CondicaoStatus.VENENO, RNG));
        assertEquals(CondicaoStatus.VENENO, c.getCondicao());

        assertFalse(c.aplicarCondicao(CondicaoStatus.PARALISIA, RNG),
                "so uma condicao nao volatil por vez, como nos jogos originais");
        assertEquals(CondicaoStatus.VENENO, c.getCondicao());
    }

    @Test
    void criaturaDeFogoNaoSeQueima() {
        Criatura braseiro = Especies.BRASEIRO.criar("B", 20);
        assertFalse(braseiro.podeReceber(CondicaoStatus.QUEIMADURA));
        assertFalse(braseiro.aplicarCondicao(CondicaoStatus.QUEIMADURA, RNG));
        assertFalse(braseiro.temCondicao());

        assertTrue(braseiro.aplicarCondicao(CondicaoStatus.PARALISIA, RNG),
                "a imunidade e' so a queimadura");
    }

    @Test
    void criaturaQueNaoEhDeFogoSeQueima() {
        Criatura marulho = Especies.MARULHO.criar("M", 20);
        assertTrue(marulho.aplicarCondicao(CondicaoStatus.QUEIMADURA, RNG));
        assertEquals(CondicaoStatus.QUEIMADURA, marulho.getCondicao());
    }

    @Test
    void criaturaDesmaiadaNaoRecebeCondicao() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.receberDano(c.getHpMaximo());
        assertFalse(c.aplicarCondicao(CondicaoStatus.VENENO, RNG));
    }

    // ------------------------------------------------------------------
    // Sono
    // ------------------------------------------------------------------

    @Test
    void oSonoDuraDeUmATresTurnos() {
        for (int i = 0; i < 200; i++) {
            Criatura c = Especies.MARULHO.criar("M", 20);
            c.aplicarCondicao(CondicaoStatus.SONO, RNG);
            int turnos = c.getTurnosDeSono();
            assertTrue(turnos >= CondicaoStatus.SONO_MINIMO && turnos <= CondicaoStatus.SONO_MAXIMO,
                    "duracao fora da faixa: " + turnos);
        }
    }

    @Test
    void descontarTurnosAcordaACriaturaNoFim() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.definirCondicao(CondicaoStatus.SONO, 3);

        assertFalse(c.descontarTurnoDeSono(), "ainda faltam 2");
        assertEquals(2, c.getTurnosDeSono());
        assertFalse(c.descontarTurnoDeSono(), "ainda falta 1");
        assertTrue(c.descontarTurnoDeSono(), "acordou agora");
        assertFalse(c.temCondicao());
        assertEquals(0, c.getTurnosDeSono());
    }

    @Test
    void descontarSonoNaoFazNadaEmQuemNaoEstaDormindo() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.aplicarCondicao(CondicaoStatus.VENENO, RNG);
        assertFalse(c.descontarTurnoDeSono());
        assertEquals(CondicaoStatus.VENENO, c.getCondicao());
    }

    // ------------------------------------------------------------------
    // Efeitos
    // ------------------------------------------------------------------

    @Test
    void aParalisiaCortaAVelocidadePelaMetade() {
        Criatura c = Especies.BRASEIRO.criar("B", 50);
        int normal = c.getVelocidade();
        assertEquals(normal, c.getVelocidadeEfetiva());

        c.aplicarCondicao(CondicaoStatus.PARALISIA, RNG);
        assertEquals(normal / 2, c.getVelocidadeEfetiva());
        assertEquals(normal, c.getVelocidade(), "o status base nao muda, so a Velocidade efetiva");
    }

    @Test
    void asOutrasCondicoesNaoMexemNaVelocidade() {
        Criatura c = Especies.MARULHO.criar("M", 50);
        c.aplicarCondicao(CondicaoStatus.VENENO, RNG);
        assertEquals(c.getVelocidade(), c.getVelocidadeEfetiva());
    }

    @Test
    void aQueimaduraCobraUmDezesseisAvosPorTurno() {
        Criatura c = Especies.MARULHO.criar("M", 50);
        c.aplicarCondicao(CondicaoStatus.QUEIMADURA, RNG);
        assertEquals(c.getHpMaximo() / 16, c.danoResidualDaCondicao());
    }

    @Test
    void oVenenoCobraODobroDaQueimadura() {
        Criatura c = Especies.MARULHO.criar("M", 50);
        c.aplicarCondicao(CondicaoStatus.VENENO, RNG);
        assertEquals(c.getHpMaximo() / 8, c.danoResidualDaCondicao());
    }

    @Test
    void oDanoResidualNuncaEhZero() {
        Criatura fraquinha = Especies.MARULHO.criar("M", 1); // HP maximo bem abaixo de 16
        fraquinha.aplicarCondicao(CondicaoStatus.QUEIMADURA, RNG);
        assertTrue(fraquinha.getHpMaximo() < 16);
        assertEquals(1, fraquinha.danoResidualDaCondicao(), "ao menos 1 de dano");
    }

    @Test
    void condicoesSemDanoResidualNaoCobramNada() {
        for (CondicaoStatus condicao : new CondicaoStatus[]{
                CondicaoStatus.NENHUMA, CondicaoStatus.PARALISIA,
                CondicaoStatus.SONO, CondicaoStatus.CONGELAMENTO}) {
            assertFalse(condicao.temDanoResidual(), condicao + " nao tira vida no fim do turno");
        }
    }

    @Test
    void sonoECongelamentoImpedemDeAgir() {
        assertTrue(CondicaoStatus.SONO.impedeDeAgir());
        assertTrue(CondicaoStatus.CONGELAMENTO.impedeDeAgir());
        assertFalse(CondicaoStatus.PARALISIA.impedeDeAgir(), "a paralisia so as vezes trava");
    }

    // ------------------------------------------------------------------
    // Cura
    // ------------------------------------------------------------------

    @Test
    void descansarCuraACondicao() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.aplicarCondicao(CondicaoStatus.SONO, RNG);
        c.restaurarTudo();
        assertFalse(c.temCondicao());
        assertEquals(0, c.getTurnosDeSono());
    }

    @Test
    void desmaiarCuraACondicao() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.aplicarCondicao(CondicaoStatus.VENENO, RNG);
        c.receberDano(c.getHpMaximo());
        assertFalse(c.temCondicao(), "uma criatura derrotada nao continua envenenada");
    }

    @Test
    void subirDeNivelNaoCuraACondicao() {
        Criatura c = Especies.MARULHO.criar("M", 5);
        c.aplicarCondicao(CondicaoStatus.PARALISIA, RNG);
        c.ganharExp(GrupoExperiencia.MEDIO_RAPIDO.expTotalParaNivel(10));
        assertTrue(c.getNivel() > 5);
        assertEquals(CondicaoStatus.PARALISIA, c.getCondicao(),
                "so o Descansar cura — subir de nivel nao");
    }

    @Test
    void definirCondicaoIgnoraCriaturaDesmaiada() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.receberDano(c.getHpMaximo());
        c.definirCondicao(CondicaoStatus.SONO, 2);
        assertFalse(c.temCondicao());
    }

    @Test
    void definirCondicaoLimitaOsTurnosDeSono() {
        Criatura c = Especies.MARULHO.criar("M", 20);
        c.definirCondicao(CondicaoStatus.SONO, 99);
        assertEquals(CondicaoStatus.SONO_MAXIMO, c.getTurnosDeSono());
        c.definirCondicao(CondicaoStatus.NENHUMA, 0);
        c.definirCondicao(CondicaoStatus.SONO, 0);
        assertEquals(1, c.getTurnosDeSono());
    }

    @Test
    void todasAsCondicoesTemSiglaECorMenosNENHUMA() {
        for (CondicaoStatus condicao : CondicaoStatus.values()) {
            assertNotNull(condicao.cor());
            if (condicao == CondicaoStatus.NENHUMA) continue;
            assertEquals(3, condicao.sigla().length(), condicao + " precisa de uma sigla de 3 letras");
            assertFalse(condicao.mensagemAoPegar(Especies.MARULHO.criar("M", 5)).isEmpty());
        }
    }
}
