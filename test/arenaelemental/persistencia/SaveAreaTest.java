package arenaelemental.persistencia;

import arenaelemental.modelo.Especies;
import arenaelemental.mundo.Area;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** A area do mapa no jogo salvo, e os saves de antes do mapa existir. */
public class SaveAreaTest {

    @TempDir
    Path pasta;

    private RepositorioJogo repositorio() { return new RepositorioJogo(pasta.resolve("jogo.save")); }

    private static Treinador treinadorEm(Area area) {
        Treinador t = new Treinador("Treinador");
        t.adicionarNaEquipe(Especies.MARULHO.criar("Gotinho", 10));
        t.restaurarArea(area);
        return t;
    }

    @Test
    void aAreaSobreviveAoIdaEVolta() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(treinadorEm(Area.RAVINA_CONGELADA), new Bestiario());

        assertTrue(Files.readString(r.getArquivo(), StandardCharsets.UTF_8).contains("area=ravina-congelada"),
                "o arquivo grava o id, nao o nome de exibicao");
        assertEquals(Area.RAVINA_CONGELADA, r.carregar().getTreinador().getArea());
    }

    @Test
    void umSaveDeAntesDoMapaRecomecaNaFloresta() throws IOException {
        RepositorioJogo r = repositorio();
        Files.writeString(r.getArquivo(), String.join("\n",
                "versao=2",
                "salvoEm=2026-09-05T18:00:00",
                "treinador=Antigo",
                "criaturas=1",
                "criatura.0.especie=marulho",
                "criatura.0.nome=Gotinho",
                "criatura.0.nivel=10",
                "criatura.0.exp=1000",
                "criatura.0.vida=20",
                "criatura.0.golpes=investida,jato-dagua",
                ""), StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertEquals(Area.FLORESTA, salvo.getTreinador().getArea());
        assertTrue(salvo.getAvisos().isEmpty(), "a falta da area nao e' um erro do arquivo");
    }

    @Test
    void umaAreaQueNaoExisteMaisViraAvisoEVoltaParaAFloresta() throws IOException {
        RepositorioJogo r = repositorio();
        r.salvar(treinadorEm(Area.VULCAO), new Bestiario());
        String texto = Files.readString(r.getArquivo(), StandardCharsets.UTF_8)
                .replace("area=vulcao", "area=atlantida");
        Files.writeString(r.getArquivo(), texto, StandardCharsets.UTF_8);

        JogoSalvo salvo = r.carregar();
        assertEquals(Area.FLORESTA, salvo.getTreinador().getArea());
        assertEquals(1, salvo.getAvisos().size());
        assertTrue(salvo.getAvisos().get(0).contains("atlantida"));
        assertEquals(1, salvo.getTreinador().getEquipe().size(), "o resto do jogo carrega");
    }

    @Test
    void aOrdemDaEquipeEhGravada() {
        RepositorioJogo r = repositorio();
        Treinador t = treinadorEm(Area.FLORESTA);
        t.adicionarNaEquipe(Especies.BRASEIRO.criar("Ignivo", 12));
        t.tornarLider(1);

        r.salvar(t, new Bestiario());
        Treinador lido = r.carregar().getTreinador();
        assertEquals("Ignivo", lido.getEquipe().get(0).getNome());
        assertEquals("Ignivo", lido.getAtiva().getNome(), "a lider volta a frente");
    }
}
