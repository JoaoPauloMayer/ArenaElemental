package arenaelemental.batalha;

import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Marulho;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CapturaTest {

    /** Gerador que devolve exatamente os numeros combinados, na ordem. */
    private static Random rngComValores(double... valores) {
        return new Random() {
            private int i;
            @Override public double nextDouble() { return valores[i++]; }
        };
    }

    @Test
    void chanceEhMaiorQuandoAlvoEstaComPoucaVida() {
        Criatura vidaCheia = new Marulho("A", 50);
        Criatura vidaBaixa = new Marulho("B", 50);
        vidaBaixa.receberDano((int) (vidaBaixa.getHpMaximo() * 0.9));

        assertTrue(Captura.calcularChance(vidaBaixa) > Captura.calcularChance(vidaCheia),
                "A chance de captura deve aumentar conforme a vida do alvo diminui");
    }

    @Test
    void chanceNuncaFicaAbaixoDoMinimo() {
        Criatura vidaCheia = new Marulho("Cheia", 50);
        assertTrue(Captura.calcularChance(vidaCheia) >= Captura.CHANCE_MINIMA,
                "A chance minima de captura deve ser 15%, mesmo com o alvo em vida cheia");
    }

    @Test
    void chanceNuncaUltrapassaOMaximo() {
        Criatura quaseMorta = new Marulho("QuaseMorta", 50);
        quaseMorta.receberDano(quaseMorta.getHpMaximo() - 1);
        assertTrue(Captura.calcularChance(quaseMorta) <= Captura.CHANCE_MAXIMA,
                "A chance maxima de captura deve ser 95%, mesmo com o alvo quase derrotado");
    }

    @Test
    void sorteioAbaixoDaChanceCaptura() {
        Criatura alvo = new Marulho("Alvo", 50);          // vida cheia -> chance 0.25
        double chance = Captura.calcularChance(alvo);
        Captura captura = new Captura(rngComValores(chance - 0.01));
        assertTrue(captura.tentar(alvo));
    }

    @Test
    void sorteioAcimaDaChanceFalha() {
        Criatura alvo = new Marulho("Alvo", 50);
        double chance = Captura.calcularChance(alvo);
        Captura captura = new Captura(rngComValores(chance + 0.01));
        assertFalse(captura.tentar(alvo));
    }

    @Test
    void mesmaSementeProduzOMesmoResultado() {
        Criatura alvo = new Marulho("Alvo", 50);
        boolean[] primeira = new boolean[20];
        boolean[] segunda = new boolean[20];

        Captura a = new Captura(new Random(7));
        Captura b = new Captura(new Random(7));
        for (int i = 0; i < primeira.length; i++) {
            primeira[i] = a.tentar(alvo);
            segunda[i] = b.tentar(alvo);
        }
        assertArrayEquals(primeira, segunda,
                "Com a mesma semente a sequencia de capturas tem de se repetir — "
                        + "era o que Math.random() impedia");
    }
}
