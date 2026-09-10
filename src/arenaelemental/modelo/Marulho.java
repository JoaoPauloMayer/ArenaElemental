package arenaelemental.modelo;

/**
 * Tipo Agua — resistente, com boa defesa fisica, mas lento.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#MARULHO}. Esta classe
 * existe so pela habilidade.
 */
public class Marulho extends Criatura {

    public Marulho(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Marulho(String nome, int nivel) {
        super(Especies.MARULHO, nome, nivel);
    }

    /** Mare Cheia: +15% de dano enquanto a propria vida estiver acima de 50%. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return percentualVida() > 0.5 ? 1.15 : 1.0;
    }
}
