package arenaelemental.persistencia;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.Golpes;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** PP e condicao de status no jogo salvo, e a compatibilidade com a versao 1. */
public class SavePpECondicaoTest {

    @TempDir
    Path pasta;

    private RepositorioJogo repositorio() { return new RepositorioJogo(pasta.resolve("jogo.save")); }

    private static Treinador comUmaCriatura(Criatura c) {
        Treinador t = new Treinador("Treinador");
        t.adicionarNaEquipe(c);
        return t;
    }

    @Test
    void osPpGastosSobrevivemAoIdaEVolta() {
        RepositorioJogo r = repositorio();
        Criatura original = Especies.MARULHO.criar("Gotinho", 30);
        Golpe golpe = original.getGolpes().get(0);
        for (int i = 0; i < 4; i++) original.gastarPp(golpe);
        int esperado = original.getPp(golpe);

        r.salvar(comUmaCriatura(original), new Bestiario());
        Criatura lida = r.carregar().getTreinador().getEquipe().get(0);

        assertEquals(esperado, lida.getPp(golpe));
        assertNotEquals(golpe.getPpMaximo(), lida.getPp(golpe), "o PP gasto tem de continuar gasto");
    }

    @Test
    void osPpDeTodosOsGolpesSaoGravados() throws IOException {
        RepositorioJogo r = repositorio();
        Criatura c = Especies.BRASEIRO.criar("Ignivo", 30);
        for (Golpe g : c.getGolpes()) c.definirPp(g, 1);

        r.salvar(comUmaCriatura(c), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8);
        assertTrue(texto.contains("criatura.0.pp="), "os PP precisam estar no arquivo");

        Criatura lida = r.carregar().getTreinador().getEquipe().get(0);
        for (Golpe g : lida.getGolpes()) assertEquals(1, lida.getPp(g));
    }

    @Test
    void aCondicaoDeStatusSobreviveAoIdaEVolta() {
        RepositorioJogo r = repositorio();
        Criatura c = Especies.MARULHO.criar("Gotinho", 30);
        c.definirCondicao(CondicaoStatus.QUEIMADURA, 0);

        r.salvar(comUmaCriatura(c), new Bestiario());
        Criatura lida = r.carregar().getTreinador().getEquipe().get(0);

        assertEquals(CondicaoStatus.QUEIMADURA, lida.getCondicao());
    }

    @Test
    void osTurnosDeSonoSobrevivemAoIdaEVolta() {
        RepositorioJogo r = repositorio();
        Criatura c = Especies.MARULHO.criar("Gotinho", 30);
        c.definirCondicao(CondicaoStatus.SONO, 2);

        r.salvar(comUmaCriatura(c), new Bestiario());
        Criatura lida = r.carregar().getTreinador().getEquipe().get(0);

        assertEquals(CondicaoStatus.SONO, lida.getCondicao());
        assertEquals(2, lida.getTurnosDeSono());
    }

    @Test
    void criaturaSemCondicaoNaoGravaAChave() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(comUmaCriatura(Especies.MARULHO.criar("Gotinho", 30)), new Bestiario());

        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8);
        assertFalse(texto.contains("condicao="), "sem condicao, sem chave");
        assertFalse(r.carregar().getTreinador().getEquipe().get(0).temCondicao());
    }

    @Test
    void condicaoDesconhecidaViraAvisoENaoQuebraOSave() throws IOException {
        RepositorioJogo r = repositorio();
        Criatura c = Especies.MARULHO.criar("Gotinho", 30);
        c.definirCondicao(CondicaoStatus.VENENO, 0);
        r.salvar(comUmaCriatura(c), new Bestiario());

        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("condicao=VENENO", "condicao=MALDICAO_ANTIGA");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertFalse(salvo.getAvisos().isEmpty());
        assertEquals(1, salvo.getTreinador().getEquipe().size(), "o resto do jogo carrega");
        assertFalse(salvo.getTreinador().getEquipe().get(0).temCondicao());
    }

    // ------------------------------------------------------------------
    // Compatibilidade com a versao 1
    // ------------------------------------------------------------------

    @Test
    void umSaveDaVersao1CarregaComOsPpCheiosESemCondicao() throws IOException {
        RepositorioJogo r = repositorio();
        Files.writeString(r.getArquivo(), String.join("\n",
                "versao=1",
                "salvoEm=2026-09-01T11:30:00",
                "treinador=Antigo",
                "criaturas=1",
                "criatura.0.especie=braseiro",
                "criatura.0.nome=Ignivo",
                "criatura.0.nivel=12",
                "criatura.0.exp=1750",
                "criatura.0.vida=30",
                "criatura.0.golpes=investida,brasa",
                "bestiario.braseiro=CAPTURADA",
                ""), StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        Criatura c = salvo.getTreinador().getEquipe().get(0);

        assertEquals("Antigo", salvo.getTreinador().getNome());
        assertEquals(12, c.getNivel());
        assertEquals(30, c.getVidaAtual());
        assertFalse(c.temCondicao(), "a versao 1 nao tinha condicoes de status");
        assertEquals(Golpes.INVESTIDA.getPpMaximo(), c.getPp(Golpes.INVESTIDA),
                "sem a chave de PP, os golpes voltam cheios");
        assertEquals(Golpes.BRASA.getPpMaximo(), c.getPp(Golpes.BRASA));
        assertTrue(salvo.getAvisos().isEmpty(), "um save da versao 1 e' valido, nao um erro");
    }

    @Test
    void umSaveDeVersaoFuturaEhRecusado() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(comUmaCriatura(Especies.MARULHO.criar("M", 10)), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("versao=" + RepositorioJogo.VERSAO, "versao=99");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        ErroDePersistencia erro = assertThrows(ErroDePersistencia.class, r::carregar);
        assertTrue(erro.getMessage().contains("99"));
    }

    @Test
    void osSavesNovosSaoGravadosNaVersaoAtual() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(comUmaCriatura(Especies.MARULHO.criar("M", 10)), new Bestiario());
        assertTrue(Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .contains("versao=" + RepositorioJogo.VERSAO));
        assertTrue(RepositorioJogo.VERSAO_MINIMA_SUPORTADA < RepositorioJogo.VERSAO,
                "a versao 1 continua sendo aceita");
    }

    @Test
    void ppDeUmGolpeQueNaoExisteMaisEhIgnorado() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(comUmaCriatura(Especies.MARULHO.criar("M", 10)), new Bestiario());

        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8);
        texto = texto.replace("criatura.0.pp=", "criatura.0.pp=golpe-extinto:3,");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertEquals(1, salvo.getTreinador().getEquipe().size());
    }
}
