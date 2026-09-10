package arenaelemental.treinador;

import arenaelemental.modelo.Braseiro;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Marulho;
import arenaelemental.treinador.Bestiario.Registro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BestiarioTest {

    @Test
    void naoRegistraAMesmaEspecieDuasVezes() {
        Bestiario b = new Bestiario();
        assertTrue(b.marcarVista(new Braseiro("Um", 5)));
        assertFalse(b.marcarVista(new Braseiro("Outro", 40)),
                "Duas criaturas da mesma especie (mesmo com nomes e niveis diferentes) "
                        + "nao devem ser registradas duas vezes");
        assertEquals(1, b.totalVistas());
    }

    @Test
    void registraEspeciesDiferentesSeparadamente() {
        Bestiario b = new Bestiario();
        b.marcarVista(new Braseiro("Fogo", 5));
        b.marcarVista(new Marulho("Agua", 5));
        assertEquals(2, b.totalVistas());
    }

    @Test
    void especieComecaNaoVista() {
        Bestiario b = new Bestiario();
        assertEquals(Registro.NAO_VISTA, b.estado(Especies.BRASEIRO));
        assertFalse(b.foiVista(Especies.BRASEIRO));
        assertFalse(b.foiCapturada(Especies.BRASEIRO));
    }

    @Test
    void vistaECapturadaSaoEstadosDiferentes() {
        Bestiario b = new Bestiario();
        b.marcarVista(Especies.BRASEIRO);
        b.marcarCapturada(Especies.MARULHO);

        assertEquals(Registro.VISTA, b.estado(Especies.BRASEIRO));
        assertEquals(Registro.CAPTURADA, b.estado(Especies.MARULHO));
        assertTrue(b.foiVista(Especies.BRASEIRO));
        assertFalse(b.foiCapturada(Especies.BRASEIRO),
                "Ver uma especie nao pode conta-la como capturada");

        assertEquals(2, b.totalVistas(), "capturadas tambem contam como vistas");
        assertEquals(1, b.totalCapturadas());
    }

    @Test
    void capturarPromoveDeVistaParaCapturada() {
        Bestiario b = new Bestiario();
        b.marcarVista(Especies.FOLHARAL);
        assertTrue(b.marcarCapturada(Especies.FOLHARAL));
        assertEquals(Registro.CAPTURADA, b.estado(Especies.FOLHARAL));
        assertEquals(1, b.totalVistas(), "a especie nao pode ser contada duas vezes");
    }

    @Test
    void capturadaNuncaRegrideParaVista() {
        Bestiario b = new Bestiario();
        b.marcarCapturada(Especies.BRASEIRO);
        b.marcarVista(Especies.BRASEIRO);
        assertEquals(Registro.CAPTURADA, b.estado(Especies.BRASEIRO),
                "Reencontrar uma especie ja capturada nao pode rebaixa-la");
        assertFalse(b.marcarCapturada(Especies.BRASEIRO), "capturar de novo nao e' novidade");
    }

    @Test
    void listasRefletemOsEstados() {
        Bestiario b = new Bestiario();
        b.marcarVista(Especies.BRASEIRO);
        b.marcarCapturada(Especies.MARULHO);

        assertEquals(2, b.vistas().size());
        assertEquals(1, b.capturadas().size());
        assertTrue(b.capturadas().contains(Especies.MARULHO));
        assertFalse(b.capturadas().contains(Especies.BRASEIRO));
    }

    @Test
    void totalDeEspeciesAcompanhaORegistro() {
        assertEquals(Especies.total(), new Bestiario().totalDeEspecies());
    }

    @Test
    void restaurarIgnoraEspecieQueNaoExisteMais() {
        Bestiario b = new Bestiario();
        b.restaurar("especie-que-nao-existe", Registro.CAPTURADA);
        assertEquals(0, b.totalVistas());
    }

    @Test
    void registrosSaoGuardadosPeloIdDaEspecie() {
        Bestiario b = new Bestiario();
        b.marcarCapturada(Especies.BRASEIRO);
        assertEquals(Registro.CAPTURADA, b.getRegistros().get("braseiro"),
                "A chave do bestiario e' o id da especie, nao o nome da classe Java");
    }
}
