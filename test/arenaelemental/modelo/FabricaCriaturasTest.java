package arenaelemental.modelo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Encontros selvagens: sorteio reproduzivel e nivel dentro da faixa. */
public class FabricaCriaturasTest {

    @Test
    void aMesmaSementeProduzOsMesmosEncontros() {
        List<String> primeira = encontros(new Random(42), 30);
        List<String> segunda = encontros(new Random(42), 30);
        assertEquals(primeira, segunda,
                "Com a mesma semente a fabrica tem de repetir a sequencia de encontros");
    }

    @Test
    void sementesDiferentesProduzemEncontrosDiferentes() {
        assertNotEquals(encontros(new Random(1), 30), encontros(new Random(2), 30));
    }

    private static List<String> encontros(Random rng, int quantos) {
        FabricaCriaturas fabrica = new FabricaCriaturas(rng);
        List<String> saida = new ArrayList<>();
        for (int i = 0; i < quantos; i++) {
            Criatura c = fabrica.selvagem(20);
            saida.add(c.getEspecie().getId() + '/' + c.getNome() + "/Nv." + c.getNivel());
        }
        return saida;
    }

    @Test
    void oNivelDoEncontroVaiDeUmAbaixoADoisAcima() {
        FabricaCriaturas fabrica = new FabricaCriaturas(new Random(9));
        for (int i = 0; i < 500; i++) {
            int nivel = fabrica.selvagem(30).getNivel();
            assertTrue(nivel >= 29 && nivel <= 32, "nivel fora da faixa: " + nivel);
        }
    }

    @Test
    void oNivelNuncaFicaAbaixoDeDoisNemAcimaDoMaximo() {
        FabricaCriaturas fabrica = new FabricaCriaturas(new Random(11));
        for (int i = 0; i < 200; i++) {
            assertTrue(fabrica.selvagem(1).getNivel() >= 2);
            assertTrue(fabrica.selvagem(Criatura.NIVEL_MAXIMO).getNivel() <= Criatura.NIVEL_MAXIMO);
        }
    }

    @Test
    void aFabricaSoUsaEspeciesRegistradas() {
        FabricaCriaturas fabrica = new FabricaCriaturas(new Random(5));
        for (int i = 0; i < 300; i++) {
            EspecieCriatura especie = fabrica.selvagem(10).getEspecie();
            assertSame(especie, Especies.porId(especie.getId()),
                    "A fabrica nao pode inventar especies fora do registro");
        }
    }

    @Test
    void aInicialNasceNoNivelCombinado() {
        Criatura inicial = new FabricaCriaturas(new Random(0)).inicial(Especies.FOLHARAL);
        assertEquals(FabricaCriaturas.NIVEL_INICIAL, inicial.getNivel());
        assertSame(Especies.FOLHARAL, inicial.getEspecie());
        assertEquals("Folharal", inicial.getNome(), "a inicial usa o nome canonico da especie");
    }

    @Test
    void encontroDeEspecieEscolhidaRespeitaAEspecie() {
        FabricaCriaturas fabrica = new FabricaCriaturas(new Random(4));
        for (int i = 0; i < 50; i++) {
            Criatura c = fabrica.selvagem(Especies.MARULHO, 12);
            assertSame(Especies.MARULHO, c.getEspecie());
            assertTrue(Especies.MARULHO.getNomesSugeridos().contains(c.getNome()));
        }
    }
}
