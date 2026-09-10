package arenaelemental.view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Barra de rolagem fina e escura, sem as setinhas: a do look and feel padrao e'
 * clara e grossa, e destoa do tema escuro.
 */
class BarraRolagemEscura extends BasicScrollBarUI {

    /** Troca as duas barras do painel de rolagem por barras escuras. */
    static void aplicar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new BarraRolagemEscura());
        scroll.getHorizontalScrollBar().setUI(new BarraRolagemEscura());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setOpaque(false);
    }

    @Override
    protected void configureScrollBarColors() {
        thumbColor = Constantes.BORDA;
        trackColor = Constantes.SUPERFICIE;
    }

    @Override protected JButton createDecreaseButton(int orientacao) { return semBotao(); }

    @Override protected JButton createIncreaseButton(int orientacao) { return semBotao(); }

    private static JButton semBotao() {
        JButton b = new JButton();
        Dimension zero = new Dimension(0, 0);
        b.setPreferredSize(zero);
        b.setMinimumSize(zero);
        b.setMaximumSize(zero);
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        // trilho invisivel: so o polegar aparece
    }

    @Override
    protected void paintThumb(Graphics graphics, JComponent c, Rectangle r) {
        if (r.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isThumbRollover() ? Constantes.TEXTO_SUAVE : thumbColor);
        g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
        g.dispose();
    }
}
