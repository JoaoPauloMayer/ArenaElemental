package arenaelemental.modelo;

/**
 * Tipo Sombrio e Agua — vive so no Mar Profundo; atacante especial.
 *
 * <p>Os numeros da especie ficam na ficha {@link Especies#BREUMAR}. Esta classe
 * existe so pela habilidade.
 */
public class Breumar extends Criatura {

    public Breumar(EspecieCriatura especie, String nome, int nivel) {
        super(especie, nome, nivel);
    }

    public Breumar(String nome, int nivel) {
        super(Especies.BREUMAR, nome, nivel);
    }

    /** Isca Luminosa: +25% de dano com golpes especiais. */
    @Override
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) {
        return golpe.getCategoria() == CategoriaGolpe.ESPECIAL ? 1.25 : 1.0;
    }
}
