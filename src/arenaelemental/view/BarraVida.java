package arenaelemental.view;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.TipoElemental;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Cartao de status de uma criatura em batalha: nome, nivel, tipo(s), a etiqueta da
 * condicao de status e a barra de vida. Para a criatura do jogador mostra
 * tambem a barra de experiencia.
 *
 * <p>O cartao fica por cima da arena, entao tem fundo proprio — escuro e
 * translucido — para ser legivel sobre qualquer cenario. Sem criatura, nao
 * desenha nada.
 */
public class BarraVida extends JPanel {

    private static final Color FUNDO_CARTAO = new Color(12, 15, 22, 215);
    private static final Color TRILHO = new Color(52, 60, 76);
    private static final int MARGEM = 10;

    private Criatura criatura;
    private final boolean mostrarExp;

    public BarraVida(Criatura criatura) { this(criatura, false); }

    public BarraVida(Criatura criatura, boolean mostrarExp) {
        this.criatura = criatura;
        this.mostrarExp = mostrarExp;
        setOpaque(false);
        setPreferredSize(new Dimension(250, mostrarExp ? 88 : 66));
    }

    public void setCriatura(Criatura c) { this.criatura = c; repaint(); }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (criatura == null) return;
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // o cartao, com um filete na cor do tipo a esquerda
        g.setColor(FUNDO_CARTAO);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g.setColor(new Color(255, 255, 255, 34));
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        // de dois tipos, o filete e' dividido: o principal em cima
        int alturaFilete = getHeight() - 20;
        List<TipoElemental> tipos = criatura.getTipos();
        int pedaco = alturaFilete / tipos.size();
        for (int i = 0; i < tipos.size(); i++) {
            g.setColor(Constantes.corVivida(tipos.get(i)));
            int altura = i == tipos.size() - 1 ? alturaFilete - pedaco * i : pedaco;
            g.fillRoundRect(0, 10 + pedaco * i, 4, altura, 4, 4);
        }

        int x = MARGEM + 2;
        int larguraBarra = getWidth() - x - MARGEM;
        int direita = x + larguraBarra;

        // nome + nivel
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(Constantes.TEXTO);
        g.drawString(criatura.getNome(), x, MARGEM + 14);

        String nivel = "Nv." + criatura.getNivel();
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString(nivel, direita - g.getFontMetrics().stringWidth(nivel), MARGEM + 14);

        // tipos, cada um na sua cor, e a etiqueta da condicao de status logo depois
        int yTipo = MARGEM + 29;
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int fimDoTipo = x;
        for (int i = 0; i < tipos.size(); i++) {
            if (i > 0) {
                g.setColor(Constantes.TEXTO_SUAVE);
                g.drawString(" / ", fimDoTipo, yTipo);
                fimDoTipo += fm.stringWidth(" / ");
            }
            TipoElemental t = tipos.get(i);
            g.setColor(Constantes.clarear(t.corPrincipal(), 0.25));
            g.drawString(t.nomeExibicao(), fimDoTipo, yTipo);
            fimDoTipo += fm.stringWidth(t.nomeExibicao());
        }
        if (criatura.temCondicao()) desenharEtiquetaDeCondicao(g, fimDoTipo + 8, yTipo - 11);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(Constantes.TEXTO_SUAVE);
        String hp = criatura.getVidaAtual() + " / " + criatura.getHpMaximo() + " HP";
        g.drawString(hp, direita - g.getFontMetrics().stringWidth(hp), yTipo);

        // barra de vida
        int yVida = yTipo + 7, alturaVida = 10;
        double pctVida = Math.max(0, criatura.percentualVida());
        Color corVida = pctVida > 0.5 ? Constantes.VERDE
                : (pctVida > 0.2 ? Constantes.AMBER : Constantes.VERMELHO);
        desenharBarra(g, x, yVida, larguraBarra, alturaVida, pctVida, corVida);

        if (!mostrarExp) return;

        // barra de experiencia
        int yExp = yVida + alturaVida + 6, alturaExp = 5;
        desenharBarra(g, x, yExp, larguraBarra, alturaExp, criatura.percentualExp(), Constantes.CYAN);

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(Constantes.TEXTO_SUAVE);
        String exp = criatura.getNivel() >= Criatura.NIVEL_MAXIMO
                ? "EXP máx."
                : "EXP: faltam " + criatura.getExpFaltando() + " para o Nv." + (criatura.getNivel() + 1);
        g.drawString(exp, x, yExp + alturaExp + 13);
    }

    /** A plaquinha colorida com a sigla da condicao (QUE, PAR, VEN, SON, CON). */
    private void desenharEtiquetaDeCondicao(Graphics2D g, int x, int y) {
        CondicaoStatus condicao = criatura.getCondicao();
        String sigla = condicao.sigla();

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        int largura = g.getFontMetrics().stringWidth(sigla) + 12;
        int altura = 14;

        g.setColor(condicao.cor());
        g.fillRoundRect(x, y, largura, altura, 6, 6);
        g.setColor(Color.WHITE);
        g.drawString(sigla, x + 6, y + altura - 4);
    }

    private void desenharBarra(Graphics2D g, int x, int y, int largura, int altura,
                               double percentual, Color cor) {
        int arco = altura;
        g.setColor(TRILHO);
        g.fillRoundRect(x, y, largura, altura, arco, arco);

        int preenchido = (int) (largura * Math.max(0, Math.min(1, percentual)));
        if (preenchido > 0) {
            g.setPaint(new GradientPaint(0, y, Constantes.clarear(cor, 0.2), 0, y + altura, cor));
            g.fillRoundRect(x, y, Math.max(preenchido, arco), altura, arco, arco);
        }
    }
}
