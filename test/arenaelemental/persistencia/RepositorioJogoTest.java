package arenaelemental.persistencia;

import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpes;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Bestiario.Registro;
import arenaelemental.treinador.Treinador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Gravar e ler a partida. */
public class RepositorioJogoTest {

    @TempDir
    Path pasta;

    private RepositorioJogo repositorio() { return new RepositorioJogo(pasta.resolve("jogo.save")); }

    private static Treinador equipeDeExemplo() {
        Treinador t = new Treinador("João");
        Criatura braseiro = Especies.BRASEIRO.criar("Ignivo", 20);
        braseiro.receberDano(7);
        braseiro.ganharExp(300);
        t.adicionarNaEquipe(braseiro);
        t.adicionarNaEquipe(Especies.MARULHO.criar("Gotinho", 8));
        return t;
    }

    @Test
    void semSaveNaoHaSave() {
        RepositorioJogo r = repositorio();
        assertFalse(r.existeSave());
        assertNull(r.dataDoSave());
        assertThrows(ErroDePersistencia.class, r::carregar);
    }

    @Test
    void idaEVoltaPreservaAEquipe() {
        RepositorioJogo r = repositorio();
        Treinador original = equipeDeExemplo();
        r.salvar(original, new Bestiario());

        assertTrue(r.existeSave());
        Treinador lido = r.carregar().getTreinador();

        assertEquals("João", lido.getNome(), "acentos precisam sobreviver ao UTF-8");
        assertEquals(2, lido.getEquipe().size());
        for (int i = 0; i < 2; i++) {
            Criatura antes = original.getEquipe().get(i);
            Criatura depois = lido.getEquipe().get(i);
            assertSame(antes.getEspecie(), depois.getEspecie());
            assertEquals(antes.getNome(), depois.getNome());
            assertEquals(antes.getNivel(), depois.getNivel());
            assertEquals(antes.getExpTotal(), depois.getExpTotal());
            assertEquals(antes.getVidaAtual(), depois.getVidaAtual());
            assertEquals(antes.getHpMaximo(), depois.getHpMaximo());
            assertEquals(antes.getGolpes(), depois.getGolpes());
        }
    }

    @Test
    void idaEVoltaPreservaOBestiario() {
        RepositorioJogo r = repositorio();
        Bestiario original = new Bestiario();
        original.marcarCapturada(Especies.BRASEIRO);
        original.marcarVista(Especies.MARULHO);

        r.salvar(equipeDeExemplo(), original);
        Bestiario lido = r.carregar().getBestiario();

        assertEquals(Registro.CAPTURADA, lido.estado(Especies.BRASEIRO));
        assertEquals(Registro.VISTA, lido.estado(Especies.MARULHO));
        assertEquals(Registro.NAO_VISTA, lido.estado(Especies.FOLHARAL));
    }

    @Test
    void oCriticoDoSaveEhOIdDaEspecieEDoGolpe() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8);

        assertTrue(texto.contains("criatura.0.especie=braseiro"),
                "a especie tem de ser gravada pelo id estavel, nao pelo nome da classe");
        assertTrue(texto.contains("investida"), "os golpes tambem vao pelo id");
        assertTrue(texto.contains("versao=" + RepositorioJogo.VERSAO));
    }

    @Test
    void salvarDuasVezesSobrescreveSemDuplicar() {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());

        Treinador menor = new Treinador("Outro");
        menor.adicionarNaEquipe(Especies.FOLHARAL.criar("Broto", 3));
        r.salvar(menor, new Bestiario());

        Treinador lido = r.carregar().getTreinador();
        assertEquals("Outro", lido.getNome());
        assertEquals(1, lido.getEquipe().size());
    }

    @Test
    void versaoDiferenteEhRecusadaComMensagemClara() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("versao=" + RepositorioJogo.VERSAO, "versao=99");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        ErroDePersistencia erro = assertThrows(ErroDePersistencia.class, r::carregar);
        assertTrue(erro.getMessage().contains("99"));
    }

    @Test
    void especieQueNaoExisteMaisViraAvisoEmVezDeQuebrarOSave() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("criatura.1.especie=marulho", "criatura.1.especie=especie-extinta");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertEquals(1, salvo.getTreinador().getEquipe().size(),
                "a criatura desconhecida sai, o resto do jogo carrega");
        assertFalse(salvo.getAvisos().isEmpty());
    }

    @Test
    void golpeQueNaoExisteMaisViraAviso() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("investida", "golpe-extinto");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertFalse(salvo.getAvisos().isEmpty());
        assertFalse(salvo.getTreinador().getEquipe().isEmpty());
    }

    @Test
    void valoresForaDaFaixaSaoSaneadosAoCarregar() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("criatura.0.nivel=20", "criatura.0.nivel=9999")
                .replace("criatura.0.vida=", "criatura.0.vida=-500\n#");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        Criatura c = r.carregar().getTreinador().getEquipe().get(0);
        assertEquals(Criatura.NIVEL_MAXIMO, c.getNivel(), "o nivel tem de ser limitado a 100");
        assertTrue(c.getVidaAtual() >= 0 && c.getVidaAtual() <= c.getHpMaximo());
    }

    @Test
    void arquivoIlegivelDaMensagemEmVezDeStackTrace() throws IOException {
        RepositorioJogo r = repositorio();
        Files.writeString(r.getArquivo(), "isto nao e um save\n", StandardCharsets.UTF_8);
        ErroDePersistencia erro = assertThrows(ErroDePersistencia.class, r::carregar);
        assertNotNull(erro.getMessage());
    }

    @Test
    void naoDeixaArquivoTemporarioParaTras() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        try (var arquivos = Files.list(pasta)) {
            List<String> nomes = arquivos.map(p -> p.getFileName().toString()).toList();
            assertEquals(List.of("jogo.save"), nomes,
                    "a gravacao atomica nao pode deixar .tmp para tras");
        }
    }

    @Test
    void apagarRemoveOSave() {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        r.apagar();
        assertFalse(r.existeSave());
        r.apagar(); // apagar de novo nao pode explodir
    }

    @Test
    void dataDoSaveEhLidaDoArquivo() {
        RepositorioJogo r = repositorio();
        r.salvar(equipeDeExemplo(), new Bestiario());
        assertNotNull(r.dataDoSave());
        assertNotNull(r.carregar().getSalvoEm());
    }

    @Test
    void salvarSemJornadaEhRecusado() {
        assertThrows(ErroDePersistencia.class, () -> repositorio().salvar(null, new Bestiario()));
    }

    @Test
    void oRepertorioSalvoVenceODerivadoDoNivel() {
        RepositorioJogo r = repositorio();
        Treinador t = new Treinador("Treinador");
        Criatura c = Especies.BRASEIRO.criar("Ignivo", 40);
        c.restaurarEstado(40, c.getExpTotal(), c.getHpMaximo(), List.of(Golpes.INVESTIDA));
        t.adicionarNaEquipe(c);
        r.salvar(t, new Bestiario());

        Criatura lido = r.carregar().getTreinador().getEquipe().get(0);
        assertEquals(List.of(Golpes.INVESTIDA), lido.getGolpes(),
                "o repertorio gravado tem de vencer o que o nivel daria");
    }

    @Test
    void caminhoPadraoFicaNaPastaDoUsuario() {
        Path padrao = RepositorioJogo.caminhoPadrao();
        assertTrue(padrao.startsWith(System.getProperty("user.home")));
        assertEquals("jogo.save", padrao.getFileName().toString());
    }
}
