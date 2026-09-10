package arenaelemental.batalha;

import arenaelemental.modelo.*;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraDanoTest {

    private final CalculadoraDano calc = new CalculadoraDano(new Random(42));

    /**
     * Caso de referencia conferido a mao.
     *
     * <p>Braseiro Nv.50 (SpA 82) usa Labareda (Fogo, Especial, poder 90) contra
     * Folharal Nv.50 (SpD 76):
     * <pre>
     *   fatorNivel = (2*50)/5 + 2               = 22
     *   base       = 22 * 90 * 82 / 76 / 50 + 2 = 44
     *   roll 100   = 44 -> STAB 1.5 = 66 -> tipo 2x = 132
     * </pre>
     */
    @Test
    void formulaBateComOCalculoManual() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura folharal = new Folharal("Folharal", 50);
        assertEquals(82, braseiro.getAtaqueEsp());
        assertEquals(76, folharal.getDefesaEsp());

        assertEquals(132, calc.calcular(braseiro, folharal, Golpes.LABAREDA, false, 100).getDano());
        assertEquals(110, calc.calcular(braseiro, folharal, Golpes.LABAREDA, false, 85).getDano());
    }

    @Test
    void criticoMultiplicaPorUmEMeio() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura folharal = new Folharal("Folharal", 50);
        int normal = calc.calcular(braseiro, folharal, Golpes.LABAREDA, false, 100).getDano();
        int critico = calc.calcular(braseiro, folharal, Golpes.LABAREDA, true, 100).getDano();
        assertEquals(132, normal);
        assertEquals(198, critico, "o critico entra antes do STAB e do tipo: 44*1.5=66 -> 99 -> 198");
        assertTrue(calc.calcular(braseiro, folharal, Golpes.LABAREDA, true, 100).foiCritico());
    }

    @Test
    void golpeEspecialUsaSpaContraSpdEOFisicoUsaAtkContraDef() {
        Criatura folharal = new Folharal("Folharal", 50); // Atk 66, SpA 84
        Criatura marulho = new Marulho("Marulho", 50);    // Def 82, SpD 78
        Golpe fisico   = new Golpe("teste-fisico", "Teste Físico",   TipoElemental.PLANTA, CategoriaGolpe.FISICO,   90);
        Golpe especial = new Golpe("teste-especial", "Teste Especial", TipoElemental.PLANTA, CategoriaGolpe.ESPECIAL, 90);

        int danoFisico = calc.calcular(folharal, marulho, fisico, false, 100).getDano();
        int danoEspecial = calc.calcular(folharal, marulho, especial, false, 100).getDano();

        assertEquals(98, danoFisico);
        assertEquals(132, danoEspecial);
        assertTrue(danoEspecial > danoFisico,
                "Folharal tem Ataque Especial maior que o Ataque, entao o golpe especial doi mais");
    }

    @Test
    void stabSoValeQuandoOTipoDoGolpeEhODaCriatura() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura marulho = new Marulho("Marulho", 50);
        assertTrue(calc.calcular(braseiro, marulho, Golpes.BRASA, false, 100).teveStab(),
                "Braseiro e do tipo Fogo usando um golpe de Fogo");
        assertFalse(calc.calcular(braseiro, marulho, Golpes.INVESTIDA, false, 100).teveStab(),
                "Investida e do tipo Normal, nunca recebe STAB");
    }

    @Test
    void efetividadeSegueOTrianguloClassico() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura folharal = new Folharal("Folharal", 50);
        Criatura marulho = new Marulho("Marulho", 50);

        assertEquals(2.0, calc.calcular(braseiro, folharal, Golpes.BRASA, false, 100).getEfetividade());
        assertEquals(0.5, calc.calcular(braseiro, marulho, Golpes.BRASA, false, 100).getEfetividade());
        assertEquals(1.0, calc.calcular(braseiro, folharal, Golpes.INVESTIDA, false, 100).getEfetividade());
    }

    @Test
    void rolagemFicaSempreEntre85E100() {
        Criatura a = new Braseiro("A", 50);
        Criatura b = new Marulho("B", 50);
        CalculadoraDano aleatoria = new CalculadoraDano(new Random(7));
        for (int i = 0; i < 500; i++) {
            int r = aleatoria.calcular(a, b, Golpes.BRASA).getRolagem();
            assertTrue(r >= 85 && r <= 100, "rolagem fora da faixa: " + r);
        }
    }

    @Test
    void rolagemProduzUmIntervaloDeDanoDeCercaDe15PorCento() {
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura folharal = new Folharal("Folharal", 50);
        int minimo = calc.danoMinimo(braseiro, folharal, Golpes.LABAREDA);
        int maximo = calc.danoMaximo(braseiro, folharal, Golpes.LABAREDA);
        assertEquals(110, minimo);
        assertEquals(132, maximo);
        assertTrue(minimo >= maximo * 0.8, "o piso do damage roll fica proximo de 85% do teto");
    }

    @Test
    void danoNuncaFicaAbaixoDeUm() {
        Criatura fraco = new Folharal("Fraco", 1);
        Criatura forte = new Marulho("Forte", 100);
        int dano = calc.calcular(fraco, forte, Golpes.INVESTIDA, false, 85).getDano();
        assertTrue(dano >= 1, "um golpe que acerta sempre tira ao menos 1 de vida, deu " + dano);
    }

    @Test
    void habilidadeEntraComoOTermoOther() {
        // Labareda do Braseiro: +30% contra alvo abaixo de 30% da vida
        Criatura braseiro = new Braseiro("Braseiro", 50);
        Criatura alvoCheio = new Folharal("Cheio", 50);
        Criatura alvoFraco = new Folharal("Fraco", 50);
        alvoFraco.receberDano((int) (alvoFraco.getHpMaximo() * 0.85));

        int contraCheio = calc.calcular(braseiro, alvoCheio, Golpes.INVESTIDA, false, 100).getDano();
        int contraFraco = calc.calcular(braseiro, alvoFraco, Golpes.INVESTIDA, false, 100).getDano();
        assertTrue(contraFraco > contraCheio,
                "a habilidade multiplica o dano no fim da cadeia: " + contraFraco + " vs " + contraCheio);
    }
}
