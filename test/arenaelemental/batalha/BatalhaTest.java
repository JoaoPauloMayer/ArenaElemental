package arenaelemental.batalha;

import arenaelemental.modelo.Braseiro;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Folharal;
import arenaelemental.modelo.Golpes;
import arenaelemental.modelo.Marulho;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class BatalhaTest {

    /** Primeira linha do log depois da mensagem de encontro. */
    private String primeiraAcao(List<String> log) {
        return log.get(1);
    }

    @Test
    void quemTemMaisVelocidadeAtacaPrimeiro() {
        // Braseiro Nv.5 tem Velocidade 13; Marulho Nv.5 tem 10
        Criatura braseiro = new Braseiro("Braseiro", 5);
        Criatura marulho = new Marulho("Marulho", 5);
        assertTrue(braseiro.getVelocidade() > marulho.getVelocidade());

        Batalha comJogadorRapido = new Batalha(braseiro, marulho, new Random(1));
        comJogadorRapido.executarTurno(Golpes.BRASA);
        assertTrue(primeiraAcao(comJogadorRapido.getLog()).startsWith("Braseiro usou"),
                "o mais rapido e do jogador, entao ele abre o turno");
    }

    @Test
    void oSelvagemAtacaPrimeiroQuandoEhMaisRapido() {
        Criatura marulho = new Marulho("Marulho", 5);   // lento, do jogador
        Criatura braseiro = new Braseiro("Braseiro", 5); // rapido, selvagem
        Batalha b = new Batalha(marulho, braseiro, new Random(1));
        b.executarTurno(Golpes.JATO_DAGUA);
        assertTrue(primeiraAcao(b.getLog()).startsWith("Braseiro usou"),
                "o selvagem e mais rapido, entao ele abre o turno");
    }

    @Test
    void turnoCausaDanoNosDoisLados() {
        Criatura braseiro = new Braseiro("Braseiro", 5);
        Criatura marulho = new Marulho("Marulho", 5);
        int vidaJogador = braseiro.getVidaAtual();
        int vidaSelvagem = marulho.getVidaAtual();

        new Batalha(braseiro, marulho, new Random(3)).executarTurno(Golpes.BRASA);

        assertTrue(marulho.getVidaAtual() < vidaSelvagem, "o selvagem levou dano");
        assertTrue(braseiro.getVidaAtual() < vidaJogador, "o jogador levou o revide");
    }

    @Test
    void turnoApenasDoSelvagemNaoDeixaOJogadorAtacar() {
        Criatura braseiro = new Braseiro("Braseiro", 5);
        Criatura marulho = new Marulho("Marulho", 5);
        int vidaSelvagem = marulho.getVidaAtual();

        Batalha b = new Batalha(braseiro, marulho, new Random(3));
        b.turnoApenasDoSelvagem();

        assertEquals(vidaSelvagem, marulho.getVidaAtual(),
                "numa captura falha o selvagem nao pode levar dano");
        assertTrue(braseiro.getVidaAtual() < braseiro.getHpMaximo(), "mas o jogador leva");
    }

    @Test
    void vencerRendeExperienciaAoJogador() {
        Criatura braseiro = new Braseiro("Braseiro", 50); // muito acima do nivel
        Criatura marulho = new Marulho("Marulho", 5);
        int expAntes = braseiro.getExpTotal();

        Batalha b = new Batalha(braseiro, marulho, new Random(5));
        while (!b.terminou()) b.executarTurno(Golpes.INVESTIDA);

        assertFalse(marulho.estaViva());
        assertTrue(b.getExpGanhaNaBatalha() > 0, "derrotar rende experiencia");
        assertEquals(expAntes + b.getExpGanhaNaBatalha(), braseiro.getExpTotal());
        assertTrue(b.getLog().stream().anyMatch(l -> l.contains("ganhou")),
                "o ganho de experiencia aparece no log");
    }

    @Test
    void experienciaNaoEhDistribuidaDuasVezes() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura marulho = new Marulho("Marulho", 5);
        Batalha b = new Batalha(braseiro, marulho, new Random(5));
        while (!b.terminou()) b.executarTurno(Golpes.INVESTIDA);

        int exp = braseiro.getExpTotal();
        b.executarTurno(Golpes.INVESTIDA); // turno depois de acabar: nao faz nada
        assertEquals(exp, braseiro.getExpTotal());
    }

    @Test
    void batalhaTerminaQuandoUmDosDoisCai() {
        Criatura forte = new Braseiro("Forte", 60);
        Criatura fraco = new Folharal("Folharal", 2); // cai em um golpe so
        Batalha b = new Batalha(forte, fraco, new Random(9));
        assertFalse(b.terminou());
        // Trancada tem 100% de precisao: o teste e' sobre o fim da batalha,
        // nao sobre o sorteio da precisao
        b.executarTurno(Golpes.TRANCADA);
        assertTrue(b.terminou());
    }

    // ------------------------------------------------------------------
    // Captura
    // ------------------------------------------------------------------

    @Test
    void capturaBemSucedidaEncerraABatalha() {
        // vida no minimo -> chance maxima; a semente 3 acerta a primeira tentativa
        Criatura jogador = new Braseiro("Meu", 20);
        Criatura selvagem = new Marulho("Selvagem", 20);
        selvagem.receberDano(selvagem.getHpMaximo() - 1);

        Batalha b = new Batalha(jogador, selvagem, new Random(3));
        assertTrue(b.tentarCapturar(), "com a vida no minimo a chance e' de 95%");
        assertTrue(b.foiCapturada());
        assertTrue(b.terminou(), "capturar encerra a batalha");
        assertTrue(b.getLog().get(b.getLog().size() - 1).contains("capturou"));
    }

    @Test
    void capturaFalhaNaoEncerraABatalha() {
        // vida cheia -> chance de 25%; a semente 1 erra a primeira tentativa
        Criatura jogador = new Braseiro("Meu", 20);
        Criatura selvagem = new Marulho("Selvagem", 20);

        Batalha b = new Batalha(jogador, selvagem, new Random(1));
        assertFalse(b.tentarCapturar());
        assertFalse(b.foiCapturada());
        assertFalse(b.terminou(), "errar a captura deixa a batalha em andamento");
        assertTrue(b.getLog().get(b.getLog().size() - 1).contains("escapou"));
    }

    @Test
    void naoDaParaCapturarDepoisDeCapturar() {
        Criatura jogador = new Braseiro("Meu", 20);
        Criatura selvagem = new Marulho("Selvagem", 20);
        selvagem.receberDano(selvagem.getHpMaximo() - 1);

        Batalha b = new Batalha(jogador, selvagem, new Random(3));
        assertTrue(b.tentarCapturar());
        assertFalse(b.tentarCapturar(), "a batalha ja terminou");
    }

    @Test
    void aChanceDeCapturaSobeConformeASelvagemEnfraquece() {
        Criatura jogador = new Braseiro("Meu", 20);
        Criatura selvagem = new Marulho("Selvagem", 20);
        Batalha b = new Batalha(jogador, selvagem, new Random(0));

        double comVidaCheia = b.chanceDeCaptura();
        selvagem.receberDano(selvagem.getHpMaximo() - 1);
        assertTrue(b.chanceDeCaptura() > comVidaCheia);
    }

    @Test
    void aMesmaSementeReproduzABatalhaInteiraInclusiveACaptura() {
        assertEquals(rodar(new Random(2024)), rodar(new Random(2024)),
                "dano, ordem do turno, golpe da selvagem e captura saem todos do mesmo gerador");
    }

    private List<String> rodar(Random rng) {
        Criatura jogador = new Braseiro("Meu", 25);
        Criatura selvagem = new Marulho("Selvagem", 25);
        Batalha b = new Batalha(jogador, selvagem, rng);
        for (int i = 0; i < 4 && !b.terminou(); i++) {
            b.executarTurno(Golpes.BRASA);
            if (!b.terminou()) b.tentarCapturar();
        }
        return b.getLog();
    }
}
