package arenaelemental.view;

import arenaelemental.modelo.TipoElemental;
import arenaelemental.mundo.Area;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A descoberta de cenarios de batalha.
 *
 * <p>Os testes montam a propria pasta de imagens num diretorio temporario: eles
 * nao podem depender das imagens que estao na pasta do projeto, que sao arte de
 * terceiros e podem nem existir em outra maquina.
 */
public class CenariosTest {

    @TempDir
    Path pasta;

    @BeforeEach
    void limparCache() { Cenarios.recarregar(); }

    // ------------------------------------------------------------------
    // Leitura do nome do arquivo
    // ------------------------------------------------------------------

    @Test
    void reconheceAsExtensoesDeImagem() {
        assertTrue(Cenario.ehImagem("fundo.jpg"));
        assertTrue(Cenario.ehImagem("fundo.JPEG"));
        assertTrue(Cenario.ehImagem("fundo.png"));
        assertTrue(Cenario.ehImagem("fundo.Gif"));
        assertTrue(Cenario.ehImagem("fundo.bmp"));

        assertFalse(Cenario.ehImagem("leiame.txt"));
        assertFalse(Cenario.ehImagem("fundo.psd"));
        assertFalse(Cenario.ehImagem("fundo"));
    }

    @Test
    void oTipoSaiDoNomeDoArquivo() {
        assertEquals(TipoElemental.FOGO, Cenario.tipoPeloNome("volcano one.jpg"));
        assertEquals(TipoElemental.FOGO, Cenario.tipoPeloNome("lava-field.png"));
        assertEquals(TipoElemental.AGUA, Cenario.tipoPeloNome("ocean three.jpg"));
        assertEquals(TipoElemental.AGUA, Cenario.tipoPeloNome("praia ao amanhecer.jpg"));
        assertEquals(TipoElemental.PLANTA, Cenario.tipoPeloNome("twilight forest.jpg"));
        assertEquals(TipoElemental.PLANTA, Cenario.tipoPeloNome("floresta densa.png"));
    }

    @Test
    void oTipoAceitaNomesComAcento() {
        assertEquals(TipoElemental.FOGO, Cenario.tipoPeloNome("vulcão adormecido.jpg"));
        assertEquals(TipoElemental.AGUA, Cenario.tipoPeloNome("ÁGUA parada.png"));
    }

    @Test
    void osTiposNovosTambemSaemDoNome() {
        assertEquals(TipoElemental.PEDRA, Cenario.tipoPeloNome("rocky mountain.jpg"));
        assertEquals(TipoElemental.GELO, Cenario.tipoPeloNome("snow field.png"));
        assertEquals(TipoElemental.SOMBRIO, Cenario.tipoPeloNome("shadow realm.png"));
        assertEquals(TipoElemental.SAGRADO, Cenario.tipoPeloNome("light.jpg"));
        assertEquals(TipoElemental.SAGRADO, Cenario.tipoPeloNome("templo antigo.jpg"));
    }

    @Test
    void aOrdemDecideOsNomesQueCaemEmDoisTipos() {
        assertEquals(TipoElemental.GELO, Cenario.tipoPeloNome("frozen lake.jpg"), "Gelo vem antes de Agua");
        assertEquals(TipoElemental.PLANTA, Cenario.tipoPeloNome("dark forest.jpg"), "Planta vem antes de Sombrio");
        assertEquals(TipoElemental.PLANTA, Cenario.tipoPeloNome("twilight forest.jpg"));
    }

    @Test
    void nomeSemPalavraChaveViraCenarioNeutro() {
        assertNull(Cenario.tipoPeloNome("storm one.jpg"));
        assertNull(Cenario.tipoPeloNome("ruinas.png"));
    }

    @Test
    void oNomeDeExibicaoLimpaOArquivo() {
        assertEquals("Twilight Forest", Cenario.nomeDeExibicao("twilight forest.jpg"));
        assertEquals("Dark Forest", Cenario.nomeDeExibicao("dark_forest.PNG"));
        assertEquals("Volcano One", Cenario.nomeDeExibicao("volcano-one.jpeg"));
        assertEquals("Light", Cenario.nomeDeExibicao("light.jpg"));
    }

    @Test
    void aAncoraAcompanhaOTipo() {
        // a arena e' uma tira larga: o corte precisa saber que faixa preservar
        assertTrue(Cenario.ancoraPeloTipo(TipoElemental.FOGO) > 0.5,
                "cenarios de Fogo tem a lava embaixo");
        assertTrue(Cenario.ancoraPeloTipo(TipoElemental.PLANTA) < 0.5,
                "cenarios de floresta ganham cortando um pouco acima do meio");
        assertEquals(0.5, Cenario.ancoraPeloTipo(null));
    }

    @Test
    void aAncoraEhLimitadaEntreZeroEUm() {
        assertEquals(1.0, cenario("x.jpg", null, 9.0).getAncoraVertical());
        assertEquals(0.0, cenario("x.jpg", null, -3.0).getAncoraVertical());
    }

    private Cenario cenario(String arquivo, TipoElemental tipo, double ancora) {
        return new Cenario(pasta.resolve(arquivo), arquivo, tipo, ancora);
    }

    // ------------------------------------------------------------------
    // Leitura da pasta
    // ------------------------------------------------------------------

    @Test
    void lerPastaClassificaCadaImagem() throws IOException {
        criarImagem("volcano one.jpg", 200, 120);
        criarImagem("ocean two.png", 200, 120);
        criarImagem("dark forest.png", 200, 120);
        criarImagem("storm.png", 200, 120);

        List<Cenario> achados = Cenarios.lerPasta(pasta);
        assertEquals(4, achados.size());

        assertEquals(TipoElemental.FOGO, porNome(achados, "Volcano One").getTipo());
        assertEquals(TipoElemental.AGUA, porNome(achados, "Ocean Two").getTipo());
        assertEquals(TipoElemental.PLANTA, porNome(achados, "Dark Forest").getTipo());
        assertTrue(porNome(achados, "Storm").ehNeutro());
    }

    @Test
    void lerPastaIgnoraOQueNaoEhImagem() throws IOException {
        criarImagem("ocean.png", 100, 100);
        Files.writeString(pasta.resolve("creditos.txt"), "arte de fulano", StandardCharsets.UTF_8);
        Files.createDirectory(pasta.resolve("uma pasta"));

        List<Cenario> achados = Cenarios.lerPasta(pasta);
        assertEquals(1, achados.size());
        assertEquals("Ocean", achados.get(0).getNome());
    }

    @Test
    void lerPastaInexistenteDevolveListaVazia() {
        assertTrue(Cenarios.lerPasta(pasta.resolve("nao existe")).isEmpty());
        assertTrue(Cenarios.lerPasta(null).isEmpty());
    }

    @Test
    void lerPastaVaziaDevolveListaVazia() {
        assertTrue(Cenarios.lerPasta(pasta).isEmpty());
    }

    @Test
    void aOrdemDaLeituraEhEstavel() throws IOException {
        criarImagem("ocean two.png", 60, 60);
        criarImagem("dark forest.png", 60, 60);
        criarImagem("volcano one.png", 60, 60);

        List<String> nomes = new ArrayList<>();
        for (Cenario c : Cenarios.lerPasta(pasta)) nomes.add(c.getNome());
        assertEquals(List.of("Dark Forest", "Ocean Two", "Volcano One"), nomes,
                "ordem alfabetica do arquivo, para o sorteio ser reproduzivel");
    }

    // ------------------------------------------------------------------
    // Selecao
    // ------------------------------------------------------------------

    @Test
    void paraTipoPegaOsDoTipoEOsNeutros() {
        List<Cenario> todos = List.of(
                cenario("volcano.jpg", TipoElemental.FOGO, 0.5),
                cenario("ocean.jpg", TipoElemental.AGUA, 0.5),
                cenario("forest.jpg", TipoElemental.PLANTA, 0.5),
                cenario("ruinas.jpg", null, 0.5));

        List<Cenario> deFogo = Cenarios.paraTipo(todos, TipoElemental.FOGO);
        assertEquals(2, deFogo.size(), "o de Fogo e o neutro");
        for (Cenario c : deFogo) assertTrue(c.serveParaTipo(TipoElemental.FOGO));
    }

    @Test
    void semCenarioDoTipoNemNeutroQualquerUmServe() {
        List<Cenario> soDeAgua = List.of(cenario("ocean.jpg", TipoElemental.AGUA, 0.5));
        assertEquals(soDeAgua, Cenarios.paraTipo(soDeAgua, TipoElemental.FOGO),
                "um cenario torto e' melhor que fundo nenhum");
    }

    @Test
    void oNeutroServeParaQualquerTipo() {
        Cenario neutro = cenario("ruinas.jpg", null, 0.5);
        for (TipoElemental tipo : TipoElemental.values()) assertTrue(neutro.serveParaTipo(tipo));
    }

    @Test
    void sortearSempreCaiEmUmCandidatoValido() {
        List<Cenario> todos = List.of(
                cenario("volcano.jpg", TipoElemental.FOGO, 0.5),
                cenario("ocean.jpg", TipoElemental.AGUA, 0.5),
                cenario("ruinas.jpg", null, 0.5));

        Random rng = new Random(3);
        for (int i = 0; i < 200; i++) {
            Cenario sorteado = Cenarios.sortear(todos, TipoElemental.FOGO, rng);
            assertTrue(sorteado.serveParaTipo(TipoElemental.FOGO), "sorteou " + sorteado);
        }
    }

    @Test
    void sortearComAMesmaSementeRepeteASequencia() {
        List<Cenario> todos = List.of(
                cenario("volcano one.jpg", TipoElemental.FOGO, 0.5),
                cenario("volcano two.jpg", TipoElemental.FOGO, 0.5),
                cenario("ruinas.jpg", null, 0.5));

        assertEquals(sequencia(todos, new Random(8)), sequencia(todos, new Random(8)));
    }

    private List<String> sequencia(List<Cenario> todos, Random rng) {
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < 20; i++) nomes.add(Cenarios.sortear(todos, TipoElemental.FOGO, rng).getNome());
        return nomes;
    }

    @Test
    void sortearSemNenhumCenarioDevolveNulo() {
        assertNull(Cenarios.sortear(List.of(), TipoElemental.AGUA, new Random(1)),
                "sem imagem nenhuma a arena volta ao gradiente pintado");
    }

    // ------------------------------------------------------------------
    // Cenarios por area
    // ------------------------------------------------------------------

    @Test
    void asImagensDaSubpastaDeUmaAreaSaoDelaMesmo() throws IOException {
        criarImagem("Floresta Profunda/copa escura.jpg", 60, 60);
        criarImagem("Vulcão/cratera.png", 60, 60);
        criarImagem("palacio_sagrado/colunas.png", 60, 60);

        List<Cenario> achados = Cenarios.lerPasta(pasta);
        assertEquals(3, achados.size());
        assertEquals(Area.FLORESTA_PROFUNDA, porNome(achados, "Copa Escura").getArea());
        assertEquals(Area.VULCAO, porNome(achados, "Cratera").getArea(), "o nome da pasta pode ter acento");
        assertEquals(Area.PALACIO_SAGRADO, porNome(achados, "Colunas").getArea());
    }

    @Test
    void aImagemDeAreaHerdaOTipoDaArea() throws IOException {
        // o nome do arquivo nao importa dentro da subpasta: e' a pasta que decide
        criarImagem("Vulcão/qualquer coisa.png", 60, 60);
        Cenario c = Cenarios.lerPasta(pasta).get(0);
        assertEquals(TipoElemental.FOGO, c.getTipo());
        assertEquals(Cenario.ancoraPeloTipo(TipoElemental.FOGO), c.getAncoraVertical());
    }

    @Test
    void soltasESubpastasSaoLidasJuntas() throws IOException {
        criarImagem("ocean.png", 60, 60);
        criarImagem("Mar/ondas.png", 60, 60);

        List<Cenario> achados = Cenarios.lerPasta(pasta);
        assertEquals(2, achados.size());
        assertNull(porNome(achados, "Ocean").getArea(), "a solta nao pertence a area nenhuma");
        assertEquals(Area.MAR, porNome(achados, "Ondas").getArea());
    }

    @Test
    void subpastaComNomeQueNaoEhDeAreaEhIgnorada() throws IOException {
        criarImagem("rascunhos/esboco.png", 60, 60);
        assertTrue(Cenarios.lerPasta(pasta).isEmpty());
    }

    @Test
    void aAreaUsaAsPropriasImagensAntesDeTudo() {
        List<Cenario> todos = List.of(
                cenario("forest.jpg", TipoElemental.PLANTA, 0.5),
                deArea("copa.jpg", Area.FLORESTA),
                deArea("raizes.jpg", Area.FLORESTA_PROFUNDA));

        List<Cenario> daFloresta = Cenarios.paraArea(todos, Area.FLORESTA);
        assertEquals(1, daFloresta.size());
        assertEquals(Area.FLORESTA, daFloresta.get(0).getArea());
    }

    @Test
    void semImagemPropriaAAreaPegaEmprestadasAsSoltasDoSeuTipo() {
        List<Cenario> todos = List.of(
                cenario("ocean.jpg", TipoElemental.AGUA, 0.5),
                cenario("forest.jpg", TipoElemental.PLANTA, 0.5),
                cenario("ruinas.jpg", null, 0.5),
                deArea("ondas.jpg", Area.MAR));

        List<Cenario> daPraia = Cenarios.paraArea(todos, Area.PRAIA);
        assertEquals(1, daPraia.size(), "so o oceano solto: nem o neutro, nem a imagem do Mar");
        assertEquals(TipoElemental.AGUA, daPraia.get(0).getTipo());
        assertNull(daPraia.get(0).getArea());
    }

    @Test
    void semImagemPropriaNemSoltaDoTipoAAreaFicaSemCenario() {
        List<Cenario> todos = List.of(
                cenario("ruinas.jpg", null, 0.5),
                cenario("volcano.jpg", TipoElemental.FOGO, 0.5),
                deArea("colunas.jpg", Area.PALACIO_SAGRADO));

        assertTrue(Cenarios.paraArea(todos, Area.MONTANHA).isEmpty(),
                "sem imagem de Pedra, a Montanha pinta o gradiente da cor dela");
        assertNull(Cenarios.sortear(todos, Area.MONTANHA, new Random(1)));
    }

    @Test
    void oPalacioPegaEmprestadaUmaImagemSagradaSolta() {
        List<Cenario> todos = List.of(
                cenario("light.jpg", TipoElemental.SAGRADO, 0.5),
                cenario("ruinas.jpg", null, 0.5));
        List<Cenario> doPalacio = Cenarios.paraArea(todos, Area.PALACIO_SAGRADO);
        assertEquals(1, doPalacio.size());
        assertEquals(TipoElemental.SAGRADO, doPalacio.get(0).getTipo());
    }

    @Test
    void sortearPorAreaSempreCaiNaArea() {
        List<Cenario> todos = List.of(
                deArea("um.jpg", Area.MAR_PROFUNDO),
                deArea("dois.jpg", Area.MAR_PROFUNDO),
                deArea("tres.jpg", Area.MAR));

        Random rng = new Random(5);
        for (int i = 0; i < 100; i++) {
            assertEquals(Area.MAR_PROFUNDO, Cenarios.sortear(todos, Area.MAR_PROFUNDO, rng).getArea());
        }
    }

    private Cenario deArea(String arquivo, Area area) {
        return new Cenario(pasta.resolve(arquivo), arquivo, area.getTipoPredominante(), 0.5, area);
    }

    // ------------------------------------------------------------------
    // Carregamento da imagem
    // ------------------------------------------------------------------

    @Test
    void aImagemEhLidaDoDisco() throws IOException {
        criarImagem("ocean.png", 120, 80);
        Cenario c = Cenarios.lerPasta(pasta).get(0);

        BufferedImage img = Cenarios.imagemDe(c);
        assertNotNull(img);
        assertEquals(120, img.getWidth());
        assertEquals(80, img.getHeight());
    }

    @Test
    void aImagemLidaFicaEmCache() throws IOException {
        criarImagem("ocean.png", 40, 40);
        Cenario c = Cenarios.lerPasta(pasta).get(0);
        assertSame(Cenarios.imagemDe(c), Cenarios.imagemDe(c),
                "reabrir o JPEG a cada repaint sairia caro");
    }

    @Test
    void arquivoCorrompidoNaoDerrubaOJogo() throws IOException {
        // extensao de imagem, conteudo que nao e' imagem
        Files.writeString(pasta.resolve("ocean.png"), "isto nao e um png", StandardCharsets.UTF_8);
        Cenario c = Cenarios.lerPasta(pasta).get(0);

        assertNull(Cenarios.imagemDe(c), "sem imagem legivel, a arena cai no gradiente");
        assertNull(Cenarios.imagemDe(c), "a segunda tentativa tambem, sem explodir");
    }

    @Test
    void arquivoApagadoDepoisDaLeituraNaoDerrubaOJogo() throws IOException {
        criarImagem("ocean.png", 30, 30);
        Cenario c = Cenarios.lerPasta(pasta).get(0);
        Files.delete(c.getArquivo());
        assertNull(Cenarios.imagemDe(c));
    }

    @Test
    void cenarioNuloNaoTemImagem() {
        assertNull(Cenarios.imagemDe(null));
    }

    // ------------------------------------------------------------------

    private void criarImagem(String nome, int largura, int altura) throws IOException {
        BufferedImage img = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(80, 140, 200));
        g.fillRect(0, 0, largura, altura);
        g.dispose();
        String formato = nome.toLowerCase().endsWith(".jpg") || nome.toLowerCase().endsWith(".jpeg")
                ? "jpg" : "png";
        Path destino = pasta.resolve(nome);
        Files.createDirectories(destino.getParent());
        ImageIO.write(img, formato, destino.toFile());
    }

    private static Cenario porNome(List<Cenario> lista, String nome) {
        for (Cenario c : lista) if (c.getNome().equals(nome)) return c;
        throw new AssertionError("cenario nao encontrado: " + nome);
    }
}
