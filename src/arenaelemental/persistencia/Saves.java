package arenaelemental.persistencia;

import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Os saves do jogo: os {@link SlotDeSave#QUANTIDADE} slots numerados e o do
 * salvamento rapido, todos na mesma pasta.
 *
 * <p>Cada slot e' um arquivo independente, lido e gravado por um
 * {@link RepositorioJogo} — o formato, a gravacao atomica e os avisos de
 * carregamento sao os dele. Esta classe so decide qual arquivo e' de qual slot
 * e resume o que ha em cada um para a tela.
 */
public class Saves {

    private final Path pasta;

    /** Os saves no local padrao: {@code <pasta do usuario>/.arenaelemental/}. */
    public Saves() { this(RepositorioJogo.caminhoPadrao().getParent()); }

    /** Os saves numa pasta especifica — usado pelos testes. */
    public Saves(Path pasta) { this.pasta = pasta; }

    public Path getPasta() { return pasta; }

    /** O repositorio que le e grava o arquivo deste slot. */
    public RepositorioJogo repositorio(SlotDeSave slot) {
        return new RepositorioJogo(pasta.resolve(slot.nomeDoArquivo()));
    }

    public void salvar(SlotDeSave slot, Treinador treinador, Bestiario bestiario) {
        repositorio(slot).salvar(treinador, bestiario);
    }

    public JogoSalvo carregar(SlotDeSave slot) {
        return repositorio(slot).carregar();
    }

    /** O que ha no slot, sem nunca lancar excecao: um arquivo ruim vira resumo ilegivel. */
    public ResumoDoSave resumo(SlotDeSave slot) {
        RepositorioJogo r = repositorio(slot);
        if (!r.existeSave()) return ResumoDoSave.vazio(slot);
        try {
            return ResumoDoSave.de(slot, r.carregar());
        } catch (ErroDePersistencia e) {
            return ResumoDoSave.ilegivel(slot, e.getMessage());
        }
    }

    /** O resumo de todos os slots: o salvamento rapido primeiro, depois os numerados. */
    public List<ResumoDoSave> resumos() {
        List<ResumoDoSave> lista = new ArrayList<>();
        for (SlotDeSave slot : SlotDeSave.todos()) lista.add(resumo(slot));
        return Collections.unmodifiableList(lista);
    }

    /** Se ha pelo menos um slot com arquivo. */
    public boolean existeAlgum() {
        for (SlotDeSave slot : SlotDeSave.todos()) if (repositorio(slot).existeSave()) return true;
        return false;
    }

    /**
     * O save legivel gravado por ultimo, ou {@code null} se nao houver nenhum.
     * E' o que o "Continuar Jornada" do menu carrega.
     */
    public ResumoDoSave maisRecente() {
        ResumoDoSave melhor = null;
        for (ResumoDoSave r : resumos()) {
            if (!r.isLegivel()) continue;
            if (melhor == null || depois(r.getSalvoEm(), melhor.getSalvoEm())) melhor = r;
        }
        return melhor;
    }

    /** Um save sem data conta como o mais antigo de todos. */
    private static boolean depois(LocalDateTime a, LocalDateTime b) {
        if (a == null) return false;
        return b == null || a.isAfter(b);
    }
}
