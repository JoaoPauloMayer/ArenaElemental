package arenaelemental.modelo;

/**
 * Tipo Sagrado — lento e resistente no especial, que se cura aos poucos.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#CANDEIO}. Esta classe
 * existe so pela habilidade.
 */
public class Candeio extends Criatura {

    public Candeio(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Candeio(String nome, int nivel) {
        super(Especies.CANDEIO, nome, nivel);
    }

    /** Bencao: recupera 1/16 do HP maximo a cada golpe que causa dano. */
    @Override
    public void efeitoPosAtaque(Criatura alvo, int danoAplicado) {
        if (danoAplicado > 0) curar(Math.max(1, getHpMaximo() / 16));
    }
}
