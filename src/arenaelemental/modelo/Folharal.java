package arenaelemental.modelo;

/**
 * Tipo Planta — equilibrado, com bom ataque especial e dreno de vida.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#FOLHARAL}. Esta
 * classe existe so pela habilidade.
 */
public class Folharal extends Criatura {

    public Folharal(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Folharal(String nome, int nivel) {
        super(Especies.FOLHARAL, nome, nivel);
    }

    /** Sugar Seiva: recupera 20% do dano causado. */
    @Override
    public void efeitoPosAtaque(Criatura alvo, int danoAplicado) {
        curar((int) Math.round(danoAplicado * 0.2));
    }
}
