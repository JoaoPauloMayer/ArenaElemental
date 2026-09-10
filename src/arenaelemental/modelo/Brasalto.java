package arenaelemental.modelo;

/**
 * Tipo Fogo e Pedra — vive so no Vulcao; lento, forte e resistente.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#BRASALTO}. Esta
 * classe existe so pela habilidade.
 */
public class Brasalto extends Criatura {

    public Brasalto(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Brasalto(String nome, int nivel) {
        super(Especies.BRASALTO, nome, nivel);
    }

    /** Erupcao: +30% de dano enquanto a propria vida estiver abaixo da metade. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return percentualVida() < 0.5 ? 1.3 : 1.0;
    }
}
