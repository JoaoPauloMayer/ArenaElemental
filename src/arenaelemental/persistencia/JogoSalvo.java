package arenaelemental.persistencia;

import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uma partida guardada: o treinador com a equipe dele e o bestiario, mais a
 * hora em que o jogo foi salvo.
 *
 * <p>Os {@link #getAvisos() avisos} listam o que o arquivo pedia mas o jogo nao
 * soube reconstruir — uma especie ou um golpe que nao existem mais, por
 * exemplo. O jogo carrega assim mesmo, sem a parte que faltou, em vez de se
 * recusar a abrir.
 */
public class JogoSalvo {

    private final Treinador treinador;
    private final Bestiario bestiario;
    private final LocalDateTime salvoEm;
    private final List<String> avisos;

    public JogoSalvo(Treinador treinador, Bestiario bestiario, LocalDateTime salvoEm) {
        this(treinador, bestiario, salvoEm, Collections.emptyList());
    }

    public JogoSalvo(Treinador treinador, Bestiario bestiario, LocalDateTime salvoEm, List<String> avisos) {
        this.treinador = treinador;
        this.bestiario = bestiario;
        this.salvoEm = salvoEm;
        this.avisos = Collections.unmodifiableList(new ArrayList<>(avisos));
    }

    public Treinador getTreinador() { return treinador; }
    public Bestiario getBestiario() { return bestiario; }
    public LocalDateTime getSalvoEm() { return salvoEm; }

    /** O que o arquivo pedia e nao pode ser reconstruido. Vazio no caso normal. */
    public List<String> getAvisos() { return avisos; }
}
