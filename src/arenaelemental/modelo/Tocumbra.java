package arenaelemental.modelo;

/**
 * Tipo Sombrio e Planta — vive so na Floresta Profunda; atacante fisico.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#TOCUMBRA}. Esta
 * classe existe so pela habilidade.
 */
public class Tocumbra extends Criatura {

    public Tocumbra(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Tocumbra(String nome, int nivel) {
        super(Especies.TOCUMBRA, nome, nivel);
    }

    /** Emaranhado: +25% de dano com golpes fisicos. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return golpe.getCategoria() == CategoriaGolpe.FISICO ? 1.25 : 1.0;
    }
}
