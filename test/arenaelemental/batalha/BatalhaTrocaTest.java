package arenaelemental.batalha;

import arenaelemental.RandomControlado;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.Golpes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Troca de criatura no meio da batalha. */
public class BatalhaTrocaTest {

    private static boolean logTem(Batalha b, String trecho) {
        for (String linha : b.getLog()) if (linha.contains(trecho)) return true;
        return false;
    }

    private static int quantasVezes(Batalha b, String trecho) {
        int n = 0;
        for (String linha : b.getLog()) if (linha.contains(trecho)) n++;
        return n;
    }

    /** Zera os PP de todos os golpes menos um, para o sorteio da selvagem ser previsivel. */
    private static void deixarApenas(Criatura c, Golpe unico) {
        for (Golpe g : c.getGolpes()) if (g != unico) c.definirPp(g, 0);
    }

    @Test
    void trocarPorEscolhaColocaAOutraEmCampo() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 5); // fraca, nao derruba ninguem

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        assertTrue(b.trocarCriatura(reserva));

        assertSame(reserva, b.getDoJogador());
        assertTrue(logTem(b, "Volte, Primeiro!"));
        assertTrue(logTem(b, "Vai, Segundo!"));
    }

    @Test
    void trocarPorEscolhaCustaOTurno() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);
        // deixa a selvagem com um golpe so, de dano e 100% de precisao, para o
        // teste ser sobre a troca e nao sobre o sorteio do golpe dela
        deixarApenas(alvo, Golpes.CHICOTE_VINHA);

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        int vidaDaReserva = reserva.getVidaAtual();
        b.trocarCriatura(reserva);

        assertTrue(logTem(b, "Selvagem usou"), "a selvagem ataca de graça quando você troca");
        assertEquals(0, quantasVezes(b, "Primeiro usou"), "quem saiu nao chega a atacar");
        assertTrue(reserva.getVidaAtual() < vidaDaReserva,
                "quem entra e' quem leva o golpe do turno");
    }

    @Test
    void naoTrocaParaAMesmaCriaturaQueJaEstaEmCampo() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);
        Batalha b = new Batalha(emCampo, alvo, new Random(2));

        assertFalse(b.trocarCriatura(emCampo));
        assertSame(emCampo, b.getDoJogador());
    }

    @Test
    void naoTrocaParaUmaCriaturaDesmaiada() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura caida = Especies.MARULHO.criar("Caida", 30);
        caida.receberDano(caida.getHpMaximo());
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);

        Batalha b = new Batalha(emCampo, alvo, new Random(2));
        assertFalse(b.trocarCriatura(caida));
        assertSame(emCampo, b.getDoJogador());
    }

    @Test
    void naoTrocaComABatalhaJaTerminada() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 60);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 2);

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.TRANCADA);
        assertTrue(b.terminou());

        assertFalse(b.trocarCriatura(reserva));
        assertSame(emCampo, b.getDoJogador());
    }

    // ------------------------------------------------------------------
    // Substituicao depois da queda
    // ------------------------------------------------------------------

    @Test
    void substituirCaidaColocaAProximaSemGastarTurno() {
        Criatura caiu = Especies.MARULHO.criar("Caiu", 5);
        Criatura reserva = Especies.MARULHO.criar("Reserva", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);
        caiu.receberDano(caiu.getHpMaximo());

        Batalha b = new Batalha(caiu, alvo, RandomControlado.sempreSorteiaAlto());
        assertTrue(b.aCriaturaDoJogadorCaiu());
        int vidaDaReserva = reserva.getVidaAtual();

        assertTrue(b.substituirCaida(reserva));
        assertSame(reserva, b.getDoJogador());
        assertFalse(b.terminou(), "com alguem em campo a batalha volta a correr");
        assertEquals(vidaDaReserva, reserva.getVidaAtual(),
                "a queda ja custou o turno: quem entra nao leva golpe de graça");
        assertFalse(logTem(b, "Volte, Caiu!"), "nao se chama de volta quem ja caiu");
        assertTrue(logTem(b, "Vai, Reserva!"));
    }

    @Test
    void substituirCaidaNaoFuncionaComACriaturaAindaDePe() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);

        Batalha b = new Batalha(emCampo, alvo, new Random(2));
        assertFalse(b.substituirCaida(reserva), "so vale depois de uma queda");
        assertSame(emCampo, b.getDoJogador());
    }

    @Test
    void substituirCaidaNaoFuncionaSeASelvagemJaCaiu() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 60);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 2);

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.TRANCADA);
        assertFalse(alvo.estaViva());
        assertFalse(b.substituirCaida(reserva), "a batalha ja foi vencida");
    }

    @Test
    void aExperienciaVaiParaQuemEstaEmCampoNoFim() {
        Criatura caiu = Especies.MARULHO.criar("Caiu", 5);
        Criatura reserva = Especies.BRASEIRO.criar("Reserva", 40);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 3);
        caiu.receberDano(caiu.getHpMaximo());

        Batalha b = new Batalha(caiu, alvo, RandomControlado.sempreSorteiaAlto());
        b.substituirCaida(reserva);
        int expAntes = reserva.getExpTotal();

        b.executarTurno(Golpes.TRANCADA);

        assertTrue(b.getExpGanhaNaBatalha() > 0);
        assertTrue(reserva.getExpTotal() > expAntes, "quem terminou a batalha leva a experiencia");
        assertEquals(0, caiu.getExpTotal() - caiu.getExpInicioDoNivel());
    }

    @Test
    void depoisDeTrocarOTurnoSegueComANovaCriatura() {
        Criatura emCampo = Especies.BRASEIRO.criar("Primeiro", 30);
        Criatura reserva = Especies.MARULHO.criar("Segundo", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 5);

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        b.trocarCriatura(reserva);
        b.executarTurno(Golpes.JATO_DAGUA); // golpe do Marulho, nao do Braseiro

        assertTrue(logTem(b, "Segundo usou Jato d'Água!"));
    }

    @Test
    void aCriaturaDoJogadorCaiuSoValeQuandoNaoHouveVitoriaNemCaptura() {
        Criatura emCampo = Especies.MARULHO.criar("Meu", 5);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 60);

        Batalha b = new Batalha(emCampo, alvo, RandomControlado.sempreSorteiaAlto());
        for (int i = 0; i < 6 && !b.terminou(); i++) b.executarTurno(Golpes.INVESTIDA);

        assertTrue(b.terminou());
        assertFalse(emCampo.estaViva());
        assertTrue(b.aCriaturaDoJogadorCaiu());
        assertFalse(b.foiCapturada());
    }
}
