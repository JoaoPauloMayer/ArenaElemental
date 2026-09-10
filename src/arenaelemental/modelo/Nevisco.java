package arenaelemental.modelo;

/**
 * Tipo Gelo — rapido, com bom ataque especial e defesas baixas.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#NEVISCO}. Esta classe
 * existe so pela habilidade.
 */
public class Nevisco extends Criatura {

    public Nevisco(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Nevisco(String nome, int nivel) {
        super(Especies.NEVISCO, nome, nivel);
    }

    /** Frio Cortante: +25% de dano contra alvos com alguma condicao de status. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return alvo.temCondicao() ? 1.25 : 1.0;
    }
}
