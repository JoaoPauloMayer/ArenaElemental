package arenaelemental.modelo;

/**
 * Natureza do golpe, que decide qual par de status entra na formula de dano.
 *
 * <ul>
 *   <li>{@code FISICO}   &rarr; Ataque do atacante contra Defesa do alvo</li>
 *   <li>{@code ESPECIAL} &rarr; Ataque Especial do atacante contra Defesa Especial do alvo</li>
 *   <li>{@code STATUS}   &rarr; nao causa dano; existe pelo efeito que aplica</li>
 * </ul>
 */
public enum CategoriaGolpe {
    FISICO("Físico"), ESPECIAL("Especial"), STATUS("Status");

    private final String exibicao;

    CategoriaGolpe(String exibicao) { this.exibicao = exibicao; }

    public String nomeExibicao() { return exibicao; }

    /** Se o golpe causa dano — golpes de status nunca causam. */
    public boolean causaDano() { return this != STATUS; }
}
