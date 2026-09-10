package arenaelemental.batalha;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpes;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O termo <i>Burn</i> da formula de dano: um atacante queimado bate metade com
 * golpes fisicos, e nada muda nos especiais.
 */
public class TermoBurnTest {

    private final CalculadoraDano calculadora = new CalculadoraDano(new Random(1));

    @Test
    void aQueimaduraCortaPelaMetadeOGolpeFisico() {
        Criatura saudavel = Especies.MARULHO.criar("Saudável", 50);
        Criatura queimado = Especies.MARULHO.criar("Queimado", 50);
        Criatura alvo = Especies.MARULHO.criar("Alvo", 50);
        queimado.definirCondicao(CondicaoStatus.QUEIMADURA, 0);

        int normal = calculadora.calcular(saudavel, alvo, Golpes.AQUA_GARRA, false, 100).getDano();
        int comQueimadura = calculadora.calcular(queimado, alvo, Golpes.AQUA_GARRA, false, 100).getDano();

        assertEquals(normal / 2, comQueimadura,
                "Aqua Garra e' fisico: a queimadura corta o dano pela metade");
    }

    @Test
    void aQueimaduraNaoMexeNoGolpeEspecial() {
        Criatura saudavel = Especies.MARULHO.criar("Saudável", 50);
        Criatura queimado = Especies.MARULHO.criar("Queimado", 50);
        Criatura alvo = Especies.MARULHO.criar("Alvo", 50);
        queimado.definirCondicao(CondicaoStatus.QUEIMADURA, 0);

        int normal = calculadora.calcular(saudavel, alvo, Golpes.JATO_DAGUA, false, 100).getDano();
        int comQueimadura = calculadora.calcular(queimado, alvo, Golpes.JATO_DAGUA, false, 100).getDano();

        assertEquals(normal, comQueimadura,
                "Jato d'Água e' especial: a queimadura nao entra na conta");
    }

    @Test
    void asOutrasCondicoesNaoMexemNoDano() {
        Criatura saudavel = Especies.MARULHO.criar("Saudável", 50);
        Criatura alvo = Especies.MARULHO.criar("Alvo", 50);
        int normal = calculadora.calcular(saudavel, alvo, Golpes.AQUA_GARRA, false, 100).getDano();

        for (CondicaoStatus condicao : new CondicaoStatus[]{
                CondicaoStatus.PARALISIA, CondicaoStatus.VENENO, CondicaoStatus.SONO}) {
            Criatura atacante = Especies.MARULHO.criar("Atacante", 50);
            atacante.definirCondicao(condicao, 1);
            assertEquals(normal,
                    calculadora.calcular(atacante, alvo, Golpes.AQUA_GARRA, false, 100).getDano(),
                    condicao + " nao deve mexer no dano");
        }
    }

    @Test
    void aQueimaduraEntraDepoisDoTipoEAntesDaHabilidade() {
        // Folharal queimado atacando Marulho: Chicote de Vinha e' fisico, tipo 2x
        Criatura folharal = Especies.FOLHARAL.criar("Folharal", 50);
        Criatura marulho = Especies.MARULHO.criar("Marulho", 50);

        int semQueimadura = calculadora.calcular(folharal, marulho, Golpes.CHICOTE_VINHA, false, 100).getDano();
        folharal.definirCondicao(CondicaoStatus.QUEIMADURA, 0);
        int comQueimadura = calculadora.calcular(folharal, marulho, Golpes.CHICOTE_VINHA, false, 100).getDano();

        assertEquals(semQueimadura / 2, comQueimadura,
                "o corte de 50% vem depois do STAB e do multiplicador de tipo");
    }

    @Test
    void oDanoMinimoDeUmContinuaValendoComQueimadura() {
        Criatura fraco = Especies.MARULHO.criar("Fraco", 1);
        fraco.definirCondicao(CondicaoStatus.QUEIMADURA, 0);
        Criatura tanque = Especies.MARULHO.criar("Tanque", 100);

        assertTrue(calculadora.calcular(fraco, tanque, Golpes.INVESTIDA, false, 85).getDano() >= 1,
                "um golpe que acerta sempre tira ao menos 1, queimado ou nao");
    }
}
