package arenaelemental.modelo;

/**
 * Criatura sem habilidade especial: usa a ficha da especie e nada mais.
 *
 * <p>E' a implementacao padrao de {@link EspecieCriatura} — uma especie nova so
 * precisa de subclasse propria se tiver habilidade, efeito pos-ataque ou algum
 * comportamento particular.
 */
public class CriaturaComum extends Criatura {
    public CriaturaComum(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }
}
