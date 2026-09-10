package arenaelemental.modelo;

/**
 * Tipo Sombrio — rapido e agressivo, com defesas baixas.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#PENUMBRA}. Esta
 * classe existe so pela habilidade.
 */
public class Penumbra extends Criatura {

    public Penumbra(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Penumbra(String nome, int nivel) {
        super(Especies.PENUMBRA, nome, nivel);
    }

    /** Emboscada: +30% de dano contra alvos com a vida cheia. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return alvo.getVidaAtual() >= alvo.getHpMaximo() ? 1.3 : 1.0;
    }
}
