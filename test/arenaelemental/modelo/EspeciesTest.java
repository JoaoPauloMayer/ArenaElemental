package arenaelemental.modelo;

import arenaelemental.modelo.Aparencia.Adorno;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** O registro de especies e a ficha de cada uma. */
public class EspeciesTest {

    @Test
    void asDezEspeciesEstaoRegistradas() {
        assertEquals(10, Especies.total());
        assertSame(Especies.BREUMAR, Especies.porId("breumar"));
        assertSame(Especies.TOCUMBRA, Especies.porId("tocumbra"));
        assertSame(Especies.BRASALTO, Especies.porId("brasalto"));
        assertSame(Especies.BRASEIRO, Especies.porId("braseiro"));
        assertSame(Especies.MARULHO, Especies.porId("marulho"));
        assertSame(Especies.FOLHARAL, Especies.porId("folharal"));
        assertSame(Especies.CALHAU, Especies.porId("calhau"));
        assertSame(Especies.NEVISCO, Especies.porId("nevisco"));
        assertSame(Especies.PENUMBRA, Especies.porId("penumbra"));
        assertSame(Especies.CANDEIO, Especies.porId("candeio"));
    }

    @Test
    void cadaTipoDeCriaturaTemPeloMenosUmaEspecie() {
        for (TipoElemental tipo : TipoElemental.values()) {
            if (tipo == TipoElemental.NORMAL) continue;
            assertFalse(Especies.doTipo(tipo).isEmpty(), "nenhuma especie do tipo " + tipo.nomeExibicao());
        }
    }

    @Test
    void idDesconhecidoDevolveNulo() {
        assertNull(Especies.porId("charizard"));
    }

    @Test
    void soAsTresPrimeirasSaoIniciais() {
        assertEquals(List.of(Especies.BRASEIRO, Especies.MARULHO, Especies.FOLHARAL), Especies.iniciais());
    }

    @Test
    void osIdsSaoUnicos() {
        Set<String> ids = new HashSet<>();
        for (EspecieCriatura e : Especies.todas()) {
            assertTrue(ids.add(e.getId()), "id repetido: " + e.getId());
        }
    }

    @Test
    void todasTemOMesmoTotalDeStatusBase() {
        for (EspecieCriatura e : Especies.todas()) {
            assertEquals(320, e.getEstatisticasBase().total(),
                    e.getNome() + " deveria ter BST 320, para nenhuma sair na frente");
        }
    }

    @Test
    void aFichaAlimentaACriatura() {
        Criatura c = Especies.BRASEIRO.criar("Teste", 50);
        assertSame(Especies.BRASEIRO, c.getEspecie());
        assertEquals(TipoElemental.FOGO, c.getTipo());
        assertSame(Especies.BRASEIRO.getEstatisticasBase(), c.getEstatisticasBase());
        assertEquals(Especies.BRASEIRO.getDescricaoHabilidade(), c.descricaoHabilidade());
    }

    @Test
    void aFichaDecideAClasseComAHabilidade() {
        assertInstanceOf(Braseiro.class, Especies.BRASEIRO.criar(5));
        assertInstanceOf(Marulho.class, Especies.MARULHO.criar(5));
        assertInstanceOf(Folharal.class, Especies.FOLHARAL.criar(5));
        assertInstanceOf(Calhau.class, Especies.CALHAU.criar(5));
        assertInstanceOf(Nevisco.class, Especies.NEVISCO.criar(5));
        assertInstanceOf(Penumbra.class, Especies.PENUMBRA.criar(5));
        assertInstanceOf(Candeio.class, Especies.CANDEIO.criar(5));
        assertInstanceOf(Breumar.class, Especies.BREUMAR.criar(5));
        assertInstanceOf(Tocumbra.class, Especies.TOCUMBRA.criar(5));
        assertInstanceOf(Brasalto.class, Especies.BRASALTO.criar(5));
    }

    @Test
    void oGolpeDoNivelCincoEhDoTipoDaEspecie() {
        // a criatura sai do nivel inicial ja com um golpe de STAB
        for (EspecieCriatura e : Especies.todas()) {
            Criatura c = e.criar(FabricaCriaturas.NIVEL_INICIAL);
            assertTrue(c.getGolpes().stream().anyMatch(g -> g.getTipo() == e.getTipo()),
                    e.getNome() + " nao tem golpe do proprio tipo no nivel inicial");
        }
    }

    @Test
    void cadaEspecieTemAparenciaPropria() {
        Set<Adorno> adornos = new HashSet<>();
        for (EspecieCriatura e : Especies.todas()) {
            assertNotNull(e.getAparencia());
            adornos.add(e.getAparencia().getAdorno());
        }
        assertEquals(Especies.total(), adornos.size(),
                "Especies diferentes precisam de adornos diferentes para nao ficarem iguais na tela");
    }

    @Test
    void doTipoFiltraPeloTipoElementar() {
        assertEquals(List.of(Especies.MARULHO, Especies.BREUMAR), Especies.doTipo(TipoElemental.AGUA),
                "o tipo secundario tambem conta");
        assertTrue(Especies.doTipo(TipoElemental.NORMAL).isEmpty(),
                "Nenhuma criatura e' do tipo Normal");
    }

    @Test
    void sortearSoDevolveEspeciesRegistradas() {
        Random rng = new Random(1);
        for (int i = 0; i < 200; i++) {
            assertTrue(Especies.todas().contains(Especies.sortear(rng)));
        }
    }

    @Test
    void especieSemConstrutorProprioViraCriaturaComum() {
        EspecieCriatura avulsa = EspecieCriatura.novo("teste-avulso", "Avulso")
                .tipo(TipoElemental.PLANTA)
                .base(50, 50, 50, 50, 50, 50)
                .aprende(1, Golpes.INVESTIDA)
                .construir();

        Criatura c = avulsa.criar(10);
        assertInstanceOf(CriaturaComum.class, c,
                "Uma especie sem habilidade nao deve exigir uma subclasse nova");
        assertEquals(1.0, c.multiplicadorHabilidade(c, Golpes.INVESTIDA));
        assertEquals(1, c.getGolpes().size());
    }

    @Test
    void especieAvulsaNaoEntraNoRegistroSemRegistrar() {
        EspecieCriatura avulsa = EspecieCriatura.novo("nao-registrada", "Fantasma")
                .tipo(TipoElemental.FOGO).construir();
        assertNull(Especies.porId("nao-registrada"));
        assertFalse(Especies.todas().contains(avulsa));
    }

    @Test
    void registrarDuasVezesOMesmoIdEhRecusado() {
        EspecieCriatura duplicada = EspecieCriatura.novo("braseiro", "Outro Braseiro")
                .tipo(TipoElemental.FOGO).construir();
        assertThrows(IllegalArgumentException.class, () -> Especies.registrar(duplicada),
                "Dois ids iguais disputariam a mesma entrada do bestiario e do save");
    }

    @Test
    void aparenciaPadraoUsaACorDoTipo() {
        EspecieCriatura semAparencia = EspecieCriatura.novo("sem-aparencia", "Sem")
                .tipo(TipoElemental.AGUA).construir();
        Color esperada = TipoElemental.AGUA.corPrincipal();
        assertEquals(esperada, semAparencia.getAparencia().getCorCorpo());
        assertEquals(Adorno.NENHUM, semAparencia.getAparencia().getAdorno());
    }

    @Test
    void oAprendizadoSaiOrdenadoPorNivel() {
        List<AprendizadoGolpe> tabela = EspecieCriatura.novo("fora-de-ordem", "Bagunca")
                .tipo(TipoElemental.FOGO)
                .aprende(25, Golpes.TRANCADA)
                .aprende(1, Golpes.INVESTIDA)
                .aprende(15, Golpes.PRESA_IGNEA)
                .construir()
                .getAprendizado();

        assertEquals(1, tabela.get(0).getNivel());
        assertEquals(15, tabela.get(1).getNivel());
        assertEquals(25, tabela.get(2).getNivel());
    }

    @Test
    void nomesSugeridosCaemNoNomeDaEspecieQuandoNaoInformados() {
        EspecieCriatura e = EspecieCriatura.novo("anonima", "Anonima")
                .tipo(TipoElemental.PLANTA).construir();
        assertEquals(List.of("Anonima"), e.getNomesSugeridos());
        assertEquals("Anonima", e.criarComNomeSorteado(5, new Random(3)).getNome());
    }
}
