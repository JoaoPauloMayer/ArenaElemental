package arenaelemental.modelo;

/**
 * Tipo Pedra — lento e resistente, forte no ataque fisico.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#CALHAU}. Esta classe
 * existe so pela habilidade.
 */
public class Calhau extends Criatura {

    public Calhau(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Calhau(String nome, int nivel) {
        super(Especies.CALHAU, nome, nivel);
    }

    /** Rolo Compressor: +25% de dano contra alvos mais rapidos que ele. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return alvo.getVelocidadeEfetiva() > getVelocidadeEfetiva() ? 1.25 : 1.0;
    }
}
