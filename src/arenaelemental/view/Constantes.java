package arenaelemental.view;

import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.TipoElemental;

import java.awt.Color;

/**
 * A paleta do tema escuro, compartilhada por todas as telas.
 *
 * <p>Os tons de fundo sobem em degraus — {@link #FUNDO}, {@link #SUPERFICIE},
 * {@link #SUPERFICIE_ALTA} — para que cartoes e caixas se destaquem do fundo
 * sem precisar de borda grossa. As cores vividas ficam para o que se clica.
 */
public class Constantes {

    static final Color FUNDO = new Color(12, 15, 22);
    static final Color SUPERFICIE = new Color(21, 26, 36);
    static final Color SUPERFICIE_ALTA = new Color(30, 37, 50);
    static final Color BORDA = new Color(46, 56, 74);

    static final Color TEXTO = new Color(232, 237, 244);
    static final Color TEXTO_SUAVE = new Color(148, 160, 178);

    static final Color CYAN = new Color(0, 196, 232);
    static final Color AMBER = new Color(255, 170, 64);
    static final Color VERDE = new Color(60, 200, 110);
    static final Color VERMELHO = new Color(236, 72, 72);
    static final Color VIOLETA = new Color(144, 92, 240);
    static final Color ARDOSIA = new Color(76, 92, 120);

    /** Botao que nao pode ser usado agora (golpe sem PP, criatura desmaiada). */
    static final Color DESABILITADO = new Color(44, 50, 62);

    /**
     * A cor vivida de cada tipo, para os botoes de golpe. Mais saturada que a
     * {@link TipoElemental#corPrincipal()}, que e' usada em texto.
     */
    static Color corVivida(TipoElemental tipo) {
        switch (tipo) {
            case FOGO:    return new Color(255, 84, 30);
            case AGUA:    return new Color(24, 142, 255);
            case PLANTA:  return new Color(40, 196, 72);
            case PEDRA:   return new Color(196, 142, 70);
            case GELO:    return new Color(56, 214, 240);
            case SOMBRIO: return new Color(120, 62, 190);
            case SAGRADO: return new Color(255, 200, 50);
            default:      return new Color(150, 142, 176);
        }
    }

    /**
     * Os tipos da especie em HTML de rotulo Swing, cada um na sua cor:
     * {@code Sombrio / Água}. Sem as tags {@code <html>}, para caber no meio de
     * outro texto.
     */
    static String tiposEmHtml(EspecieCriatura especie) {
        StringBuilder sb = new StringBuilder();
        for (TipoElemental t : especie.getTipos()) {
            if (sb.length() > 0) sb.append(" <font color='").append(hex(TEXTO_SUAVE)).append("'>/</font> ");
            sb.append("<font color='").append(hex(clarear(t.corPrincipal(), 0.2))).append("'>")
              .append(t.nomeExibicao()).append("</font>");
        }
        return sb.toString();
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ------------------------------------------------------------------
    // Contas de cor
    // ------------------------------------------------------------------

    /** Mistura {@code a} com {@code b}: 0.0 devolve {@code a}, 1.0 devolve {@code b}. */
    static Color misturar(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    static Color clarear(Color c, double t) { return misturar(c, Color.WHITE, t); }

    static Color escurecer(Color c, double t) { return misturar(c, Color.BLACK, t); }

    static Color comAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    /** Luminancia percebida, de 0 (preto) a 1 (branco). */
    static double luminancia(Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
    }
}
