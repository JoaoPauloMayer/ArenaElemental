package arenaelemental.modelo;

/**
 * Curvas de experiencia dos jogos originais: quanta experiencia <em>acumulada</em>
 * e' necessaria para estar em um dado nivel.
 *
 * <p>As tres criaturas atuais usam {@link #MEDIO_RAPIDO} (n^3), a curva mais
 * comum. As outras ficam prontas para especies futuras.
 */
public enum GrupoExperiencia {
    /** n^3 — curva padrao. */
    MEDIO_RAPIDO,
    /** 4n^3/5 — sobe mais rapido. */
    RAPIDO,
    /** 5n^3/4 — sobe mais devagar. */
    LENTO,
    /** 6n^3/5 - 15n^2 + 100n - 140 — lenta no comeco, rapida depois. */
    MEDIO_LENTO;

    /** Experiencia total acumulada necessaria para estar no nivel informado. */
    public int expTotalParaNivel(int nivel) {
        if (nivel <= 1) return 0;
        long n = nivel;
        switch (this) {
            case RAPIDO:      return (int) (4 * n * n * n / 5);
            case LENTO:       return (int) (5 * n * n * n / 4);
            case MEDIO_LENTO: return (int) Math.max(0, (6 * n * n * n / 5) - 15 * n * n + 100 * n - 140);
            case MEDIO_RAPIDO:
            default:          return (int) (n * n * n);
        }
    }
}
