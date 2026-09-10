package arenaelemental.modelo;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** As habilidades das especies de Pedra, Gelo, Sombrio e Sagrado, e a imunidade do Gelo. */
public class HabilidadesNovasTest {

    @Test
    void calhauBateMaisForteEmQuemEhMaisRapido() {
        Criatura calhau = new Calhau("Calhau", 30);
        Criatura rapido = new Nevisco("Nevisco", 30);
        Criatura lento = new Calhau("Outro", 30);

        assertTrue(rapido.getVelocidadeEfetiva() > calhau.getVelocidadeEfetiva());
        assertEquals(1.25, calhau.multiplicadorHabilidade(rapido, Golpes.PEDRADA));
        assertEquals(1.0, calhau.multiplicadorHabilidade(lento, Golpes.PEDRADA),
                "com a mesma Velocidade, nao ha bonus");
    }

    @Test
    void neviscoBateMaisForteEmQuemTemCondicao() {
        Criatura nevisco = new Nevisco("Nevisco", 30);
        Criatura alvo = new Marulho("Marulho", 30);

        assertEquals(1.0, nevisco.multiplicadorHabilidade(alvo, Golpes.GRANIZO));
        alvo.definirCondicao(CondicaoStatus.PARALISIA, 0);
        assertEquals(1.25, nevisco.multiplicadorHabilidade(alvo, Golpes.GRANIZO));
    }

    @Test
    void penumbraBateMaisForteEmQuemEstaComAVidaCheia() {
        Criatura penumbra = new Penumbra("Penumbra", 30);
        Criatura alvo = new Folharal("Folharal", 30);

        assertEquals(1.3, penumbra.multiplicadorHabilidade(alvo, Golpes.GARRA_SOMBRIA));
        alvo.receberDano(1);
        assertEquals(1.0, penumbra.multiplicadorHabilidade(alvo, Golpes.GARRA_SOMBRIA));
    }

    @Test
    void candeioSeCuraUmDezesseisAvosAoCausarDano() {
        Criatura candeio = new Candeio("Candeio", 40);
        candeio.receberDano(30);
        int antes = candeio.getVidaAtual();

        candeio.efeitoPosAtaque(new Marulho("Alvo", 40), 12);
        assertEquals(antes + candeio.getHpMaximo() / 16, candeio.getVidaAtual());

        int depois = candeio.getVidaAtual();
        candeio.efeitoPosAtaque(new Marulho("Alvo", 40), 0);
        assertEquals(depois, candeio.getVidaAtual(), "sem dano causado, nao ha cura");
    }

    @Test
    void aCuraDoCandeioNaoPassaDoMaximo() {
        Criatura candeio = new Candeio("Candeio", 40);
        candeio.efeitoPosAtaque(new Marulho("Alvo", 40), 20);
        assertEquals(candeio.getHpMaximo(), candeio.getVidaAtual());
    }

    @Test
    void umaCriaturaDeGeloNaoCongela() {
        Criatura nevisco = new Nevisco("Nevisco", 20);
        assertFalse(nevisco.podeReceber(CondicaoStatus.CONGELAMENTO));
        assertFalse(nevisco.aplicarCondicao(CondicaoStatus.CONGELAMENTO, new Random(1)));
        assertTrue(nevisco.podeReceber(CondicaoStatus.QUEIMADURA), "as outras condicoes pegam normalmente");
    }
}
