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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Os slots de save: arquivos independentes, resumo para a tela e o mais recente. */
public class SavesTest {

    @TempDir
    Path pasta;

    private Saves saves() { return new Saves(pasta); }

    private static Treinador treinador(String nomeDoLider, Area area) {
        Treinador t = new Treinador("Treinador");
        t.adicionarNaEquipe(Especies.BRASEIRO.criar(nomeDoLider, 12));
        t.adicionarNaEquipe(Especies.MARULHO.criar("Gotinho", 8));
        t.restaurarArea(area);
        return t;
    }

    /** Reescreve a data gravada no arquivo, para testar a ordem sem esperar o relogio. */
    private void mudarData(SlotDeSave slot, String salvoEm) throws IOException {
        Path arquivo = pasta.resolve(slot.nomeDoArquivo());
        String texto = Files.readString(arquivo, StandardCharsets.UTF_8)
                .replaceAll("salvoEm=.*", "salvoEm=" + salvoEm);
        Files.writeString(arquivo, texto, StandardCharsets.UTF_8);
    }

    @Test
    void cadaSlotTemOSeuArquivo() {
        assertEquals("jogo.save", SlotDeSave.numero(1).nomeDoArquivo(),
                "o slot 1 e' o arquivo de quando so havia um save");
        assertEquals("jogo-2.save", SlotDeSave.numero(2).nomeDoArquivo());
        assertEquals("rapido.save", SlotDeSave.RAPIDO.nomeDoArquivo());
        assertEquals(SlotDeSave.QUANTIDADE + 1, SlotDeSave.todos().size());
        assertTrue(SlotDeSave.todos().get(0).ehRapido(), "o rapido vem primeiro na lista");
    }

    @Test
    void slotInexistenteEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> SlotDeSave.numero(0));
        assertThrows(IllegalArgumentException.class, () -> SlotDeSave.numero(SlotDeSave.QUANTIDADE + 1));
    }

    @Test
    void osSlotsSaoIndependentes() {
        Saves s = saves();
        s.salvar(SlotDeSave.numero(1), treinador("Ignivo", Area.FLORESTA), new Bestiario());
        s.salvar(SlotDeSave.numero(3), treinador("Chamotim", Area.VULCAO), new Bestiario());

        assertEquals("Ignivo", s.carregar(SlotDeSave.numero(1)).getTreinador().getEquipe().get(0).getNome());
        assertEquals(Area.VULCAO, s.carregar(SlotDeSave.numero(3)).getTreinador().getArea());
        assertTrue(s.resumo(SlotDeSave.numero(2)).isVazio());
    }

    @Test
    void oResumoMostraOndeEQuemLidera() {
        Saves s = saves();
        Bestiario b = new Bestiario();
        b.marcarCapturada(Especies.BRASEIRO.criar(5));
        s.salvar(SlotDeSave.RAPIDO, treinador("Ignivo", Area.MAR_PROFUNDO), b);

        ResumoDoSave r = s.resumo(SlotDeSave.RAPIDO);
        assertTrue(r.isLegivel());
        assertEquals(Area.MAR_PROFUNDO, r.getArea());
        assertEquals("Ignivo", r.getNomeDoLider());
        assertEquals(12, r.getNivelDoLider());
        assertSame(Especies.BRASEIRO, r.getEspecieDoLider());
        assertEquals(2, r.getTamanhoDaEquipe());
        assertEquals("Mar Profundo · Ignivo Nv.12 e mais 1 · 1 capturada", r.descricao());
        assertNotNull(r.getSalvoEm());
    }

    @Test
    void umSlotVazioDizQueEstaVazio() {
        ResumoDoSave r = saves().resumo(SlotDeSave.numero(4));
        assertTrue(r.isVazio());
        assertFalse(r.isLegivel());
        assertEquals("Vazio", r.descricao());
    }

    @Test
    void umArquivoRuimViraResumoIlegivelSemDerrubarALista() throws IOException {
        Files.writeString(pasta.resolve(SlotDeSave.numero(2).nomeDoArquivo()), "versao=99\n", StandardCharsets.UTF_8);

        ResumoDoSave r = saves().resumo(SlotDeSave.numero(2));
        assertFalse(r.isVazio());
        assertFalse(r.isLegivel());
        assertTrue(r.descricao().startsWith("Arquivo ilegível"));
        assertEquals(SlotDeSave.todos().size(), saves().resumos().size(), "os outros slots continuam na lista");
    }

    @Test
    void oMaisRecenteEhOGravadoPorUltimo() throws IOException {
        Saves s = saves();
        s.salvar(SlotDeSave.numero(1), treinador("Antigo", Area.FLORESTA), new Bestiario());
        s.salvar(SlotDeSave.numero(4), treinador("Novo", Area.FLORESTA), new Bestiario());
        s.salvar(SlotDeSave.RAPIDO, treinador("Meio", Area.FLORESTA), new Bestiario());
        mudarData(SlotDeSave.numero(1), "2026-09-01T10:00:00");
        mudarData(SlotDeSave.numero(4), "2026-09-09T22:00:00");
        mudarData(SlotDeSave.RAPIDO, "2026-09-05T12:00:00");

        assertEquals(SlotDeSave.numero(4), s.maisRecente().getSlot());
    }

    @Test
    void oMaisRecenteIgnoraArquivosIlegiveis() throws IOException {
        Saves s = saves();
        s.salvar(SlotDeSave.numero(1), treinador("Bom", Area.FLORESTA), new Bestiario());
        Files.writeString(pasta.resolve(SlotDeSave.numero(2).nomeDoArquivo()),
                "versao=99\nsalvoEm=2099-01-01T00:00:00\n", StandardCharsets.UTF_8);

        assertEquals(SlotDeSave.numero(1), s.maisRecente().getSlot());
    }

    @Test
    void semNenhumSaveNaoHaMaisRecente() {
        assertFalse(saves().existeAlgum());
        assertNull(saves().maisRecente());
    }

    @Test
    void oSaveAntigoDeUmArquivoSoApareceNoSlot1() {
        // quem jogou antes dos slots tem um jogo.save na pasta padrao
        new RepositorioJogo(pasta.resolve("jogo.save")).salvar(treinador("Veterano", Area.FLORESTA), new Bestiario());

        List<ResumoDoSave> resumos = saves().resumos();
        ResumoDoSave slot1 = resumos.get(1);
        assertEquals(SlotDeSave.numero(1), slot1.getSlot());
        assertEquals("Veterano", slot1.getNomeDoLider());
        assertTrue(saves().existeAlgum());
    }
}
