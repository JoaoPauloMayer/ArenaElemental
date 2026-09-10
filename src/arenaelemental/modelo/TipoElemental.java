package arenaelemental.modelo;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

/**
 * Tipos elementares e a tabela de efetividade.
 *
 * <p>Os cinco tipos da natureza — Fogo, Agua, Planta, Pedra e Gelo — formam um
 * ciclo equilibrado: cada um e' supereficaz (2x) contra dois e pouco eficaz
 * (0,5x) contra outros dois, e todo par tem um vencedor. O triangulo classico
 * Fogo &gt; Planta &gt; Agua &gt; Fogo continua dentro dele.
 *
 * <p>Sombrio e Sagrado ficam fora do ciclo: sao supereficazes um contra o
 * outro e neutros contra os elementos, que tambem sao neutros contra eles.
 *
 * <p>Um tipo contra ele mesmo e' sempre 1x, e nao ha imunidades.
 *
 * <p>NORMAL existe apenas como tipo de <em>golpe</em> neutro (dano 1x contra
 * todos): nenhuma criatura e' do tipo Normal.
 */
public enum TipoElemental {
    NORMAL, FOGO, AGUA, PLANTA, PEDRA, GELO, SOMBRIO, SAGRADO;

    private static final Map<TipoElemental, Map<TipoElemental, Double>> TABELA =
            new EnumMap<>(TipoElemental.class);

    static {
        forteContra(FOGO,    PLANTA, GELO);    fracoContra(FOGO,   AGUA, PEDRA);
        forteContra(AGUA,    FOGO, PEDRA);     fracoContra(AGUA,   PLANTA, GELO);
        forteContra(PLANTA,  AGUA, PEDRA);     fracoContra(PLANTA, FOGO, GELO);
        forteContra(PEDRA,   FOGO, GELO);      fracoContra(PEDRA,  AGUA, PLANTA);
        forteContra(GELO,    PLANTA, AGUA);    fracoContra(GELO,   FOGO, PEDRA);

        forteContra(SOMBRIO, SAGRADO);
        forteContra(SAGRADO, SOMBRIO);
    }

    private static void forteContra(TipoElemental atacante, TipoElemental... defensores) {
        for (TipoElemental d : defensores) linha(atacante).put(d, 2.0);
    }

    private static void fracoContra(TipoElemental atacante, TipoElemental... defensores) {
        for (TipoElemental d : defensores) linha(atacante).put(d, 0.5);
    }

    private static Map<TipoElemental, Double> linha(TipoElemental atacante) {
        return TABELA.computeIfAbsent(atacante, t -> new EnumMap<>(TipoElemental.class));
    }

    /** Multiplicador de efetividade deste tipo (o do golpe) contra o tipo do defensor. */
    public double multiplicadorContra(TipoElemental defensor) {
        Map<TipoElemental, Double> linha = TABELA.get(this);
        if (linha == null || defensor == null) return 1.0;
        return linha.getOrDefault(defensor, 1.0);
    }

    /** Mensagem de combate para um multiplicador de efetividade, ou null se for neutro. */
    public static String textoEfetividade(double multiplicador) {
        if (multiplicador > 1.0) return "É supereficaz!";
        if (multiplicador < 1.0) return "Não é muito eficaz...";
        return null;
    }

    public Color corPrincipal() {
        switch (this) {
            case FOGO:    return new Color(230, 92, 46);
            case AGUA:    return new Color(58, 130, 214);
            case PLANTA:  return new Color(87, 168, 74);
            case PEDRA:   return new Color(176, 140, 92);
            case GELO:    return new Color(92, 196, 226);
            case SOMBRIO: return new Color(126, 96, 178);
            case SAGRADO: return new Color(222, 184, 64);
            default:      return new Color(150, 155, 160);
        }
    }

    public String nomeExibicao() {
        switch (this) {
            case FOGO:    return "Fogo";
            case AGUA:    return "Água";
            case PLANTA:  return "Planta";
            case PEDRA:   return "Pedra";
            case GELO:    return "Gelo";
            case SOMBRIO: return "Sombrio";
            case SAGRADO: return "Sagrado";
            default:      return "Normal";
        }
    }
}
