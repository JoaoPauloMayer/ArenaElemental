package arenaelemental.modelo;

/**
 * Tipo Fogo — rapido e forte no ataque especial, mas fragil.
 *
 * <p>Os numeros da especie (status base, aprendizado, aparencia, apelidos)
 * ficam na ficha {@link Especies#BRASEIRO}. Esta classe existe so pela
 * habilidade.
 */
public class Braseiro extends Criatura {

    public Braseiro(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Braseiro(String nome, int nivel) {
        super(Especies.BRASEIRO, nome, nivel);
    }

    /** Labareda: +30% de dano contra alvos com menos de 30% da vida. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return alvo.percentualVida() < 0.3 ? 1.3 : 1.0;
    }
}
