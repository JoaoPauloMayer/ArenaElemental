package arenaelemental.modelo;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code restaurarEstado} — o caminho de volta do jogo salvo.
 *
 * <p>O arquivo e' texto e pode ser editado a mao, entao o metodo tem de sanear
 * o que recebe: nenhum arquivo deve conseguir produzir uma criatura invalida.
 */
public class CriaturaRestauracaoTest {

    @Test
    void restauraNivelExpVidaEGolpes() {
        Criatura c = Especies.BRASEIRO.criar("Ignivo", 5);
        c.restaurarEstado(30, 27100, 12, List.of(Golpes.LABAREDA, Golpes.BRASA));

        assertEquals(30, c.getNivel());
        assertEquals(27100, c.getExpTotal());
        assertEquals(12, c.getVidaAtual());
        assertEquals(List.of(Golpes.LABAREDA, Golpes.BRASA), c.getGolpes());
    }

    @Test
    void osStatusSaoRecalculadosNoNivelRestaurado() {
        Criatura restaurada = Especies.BRASEIRO.criar("A", 5);
        restaurada.restaurarEstado(50, 125000, 1, List.of(Golpes.BRASA));
        Criatura nativa = Especies.BRASEIRO.criar("B", 50);

        assertEquals(nativa.getHpMaximo(), restaurada.getHpMaximo());
        assertEquals(nativa.getAtaqueEsp(), restaurada.getAtaqueEsp());
        assertEquals(nativa.getVelocidade(), restaurada.getVelocidade());
    }

    @Test
    void nivelForaDaFaixaEhLimitado() {
        Criatura c = Especies.MARULHO.criar("M", 5);
        c.restaurarEstado(5000, 0, 1, List.of());
        assertEquals(Criatura.NIVEL_MAXIMO, c.getNivel());

        c.restaurarEstado(-3, 0, 1, List.of());
        assertEquals(1, c.getNivel());
    }

    @Test
    void expEhLimitadaAFaixaDoNivel() {
        Criatura c = Especies.FOLHARAL.criar("F", 5);

        c.restaurarEstado(20, 0, 5, List.of());
        assertEquals(c.getExpInicioDoNivel(), c.getExpTotal(),
                "exp abaixo do minimo do nivel sobe para o minimo");

        c.restaurarEstado(20, 999_999_999, 5, List.of());
        assertTrue(c.getExpTotal() < c.getExpProximoNivel(),
                "exp acima do nivel nao pode deixar a criatura pronta para subir sozinha");
        assertEquals(20, c.getNivel());
    }

    @Test
    void vidaEhLimitadaEntreZeroEOMaximo() {
        Criatura c = Especies.BRASEIRO.criar("B", 20);

        c.restaurarEstado(20, c.getExpTotal(), -400, List.of());
        assertEquals(0, c.getVidaAtual());
        assertFalse(c.estaViva());

        c.restaurarEstado(20, c.getExpTotal(), 99999, List.of());
        assertEquals(c.getHpMaximo(), c.getVidaAtual());
    }

    @Test
    void listaDeGolpesVaziaMantemORepertorioDoNivel() {
        Criatura c = Especies.BRASEIRO.criar("B", 35);
        List<Golpe> antes = List.copyOf(c.getGolpes());
        c.restaurarEstado(35, c.getExpTotal(), c.getHpMaximo(), Collections.emptyList());
        assertEquals(antes, c.getGolpes());
    }

    @Test
    void golpesNulosENAoRepetidosSaoIgnorados() {
        Criatura c = Especies.BRASEIRO.criar("B", 35);
        c.restaurarEstado(35, c.getExpTotal(), c.getHpMaximo(),
                java.util.Arrays.asList(Golpes.BRASA, null, Golpes.BRASA, Golpes.INVESTIDA));
        assertEquals(List.of(Golpes.BRASA, Golpes.INVESTIDA), c.getGolpes());
    }

    @Test
    void naoPassaDoLimiteDeQuatroGolpes() {
        Criatura c = Especies.BRASEIRO.criar("B", 35);
        c.restaurarEstado(35, c.getExpTotal(), c.getHpMaximo(),
                List.of(Golpes.BRASA, Golpes.INVESTIDA, Golpes.TRANCADA,
                        Golpes.LABAREDA, Golpes.PRESA_IGNEA));
        assertEquals(Criatura.MAX_GOLPES, c.getGolpes().size());
    }

    @Test
    void restaurarNaoDeixaGolpeAprendidoAgoraPendente() {
        Criatura c = Especies.BRASEIRO.criar("B", 5);
        c.restaurarEstado(40, 64000, 10, List.of(Golpes.LABAREDA));
        assertTrue(c.getGolpesAprendidosAgora().isEmpty(),
                "carregar um jogo nao pode anunciar golpes 'recem-aprendidos'");
    }
}
