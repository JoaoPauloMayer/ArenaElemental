package arenaelemental.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * O botao do jogo: um bloco de cor vivida com gradiente, pintado a mao.
 *
 * <p>O {@code JButton} do Swing pinta o proprio fundo de um jeito que muda com
 * o look and feel e ignora cor em parte deles; aqui o botao desenha tudo —
 * fundo, brilho, contorno e texto — para ficar igual em qualquer maquina.
 *
 * <p>Alem do titulo, o botao pode ter:
 * <ul>
 *   <li>um {@link #setDetalhe(String) detalhe}, a linha menor embaixo do titulo;</li>
 *   <li>uma {@link #setEtiqueta(String) etiqueta}, a plaquinha no canto de cima
 *       (o tipo do golpe, por exemplo);</li>
 *   <li>um {@link #setCanto(String) canto}, o texto a direita do detalhe (os PP).</li>
 * </ul>
 * Quando o botao fica baixo demais para duas linhas, so o titulo aparece.
 *
 * <p>Sobre cores claras (areia, dourado) o texto fica escuro; sobre as demais,
 * branco — a escolha sai da luminancia da cor.
 */
class BotaoJogo extends JButton {

    private static final int ALTURA_MINIMA_PARA_DETALHE = 44;

    private Color cor;
    private String detalhe, etiqueta, canto;
    private boolean alinharAEsquerda;
    private final Font fonteTitulo;

    BotaoJogo(String titulo, Color cor) { this(titulo, cor, 15); }

    BotaoJogo(String titulo, Color cor, int tamanhoTitulo) {
        super(titulo);
        this.cor = cor;
        this.fonteTitulo = new Font("SansSerif", Font.BOLD, tamanhoTitulo);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(fonteTitulo);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void setCor(Color cor) { this.cor = cor; repaint(); }

    void setDetalhe(String detalhe) { this.detalhe = detalhe; repaint(); }

    void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; repaint(); }

    void setCanto(String canto) { this.canto = canto; repaint(); }

    /** Titulo e detalhe encostados a esquerda, em vez de centralizados. */
    void setAlinharAEsquerda(boolean esquerda) { this.alinharAEsquerda = esquerda; repaint(); }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        float arco = Math.min(16, h * 0.5f);
        boolean ativo = isEnabled();

        Color base = ativo ? cor : Constantes.DESABILITADO;
        ButtonModel m = getModel();
        if (ativo && m.isPressed()) base = Constantes.escurecer(base, 0.18);
        else if (ativo && m.isRollover()) base = Constantes.clarear(base, 0.14);

        RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, w, h, arco, arco);
        if (ativo) {
            g.setPaint(new GradientPaint(0, 0, Constantes.clarear(base, 0.08),
                                         0, h, Constantes.escurecer(base, 0.30)));
        } else {
            g.setColor(base);
        }
        g.fill(forma);

        if (ativo) {
            // brilho na metade de cima: e' o que faz a cor parecer viva
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 60),
                                         0, h * 0.55f, new Color(255, 255, 255, 0)));
            g.fill(new RoundRectangle2D.Float(1, 1, w - 2, h * 0.55f, arco, arco));
        }

        g.setStroke(new BasicStroke(1f));
        g.setColor(ativo ? Constantes.comAlfa(Constantes.clarear(base, 0.35), 170)
                         : Constantes.BORDA);
        g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arco, arco));

        if (isFocusOwner() && ativo) {
            g.setColor(new Color(255, 255, 255, 190));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, w - 5, h - 5, arco - 2, arco - 2));
        }

        desenharTextos(g, w, h, ativo);
        g.dispose();
    }

    private void desenharTextos(Graphics2D g, int w, int h, boolean ativo) {
        // decidido pela cor de repouso: se fosse pela cor com hover, o texto
        // trocaria de branco para escuro so de passar o mouse
        boolean fundoClaro = ativo && Constantes.luminancia(cor) > 0.62;
        Color corTexto = !ativo ? Constantes.TEXTO_SUAVE
                : (fundoClaro ? new Color(24, 26, 34) : Color.WHITE);
        Color corSombra = fundoClaro ? new Color(255, 255, 255, 70) : new Color(0, 0, 0, 90);
        Color corDetalhe = !ativo ? Constantes.TEXTO_SUAVE
                : Constantes.comAlfa(corTexto, fundoClaro ? 200 : 215);

        int margem = 14;
        boolean duasLinhas = detalhe != null && h >= ALTURA_MINIMA_PARA_DETALHE;

        // etiqueta no canto de cima, quando cabe
        int larguraEtiqueta = 0;
        if (etiqueta != null && duasLinhas) {
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fe = g.getFontMetrics();
            larguraEtiqueta = fe.stringWidth(etiqueta) + 12;
            int xe = w - margem + 4 - larguraEtiqueta, ye = 8, he = 16;
            g.setColor(fundoClaro ? new Color(0, 0, 0, 55) : new Color(0, 0, 0, 70));
            g.fillRoundRect(xe, ye, larguraEtiqueta, he, 8, 8);
            g.setColor(corTexto);
            g.drawString(etiqueta, xe + 6, ye + he - 4);
            larguraEtiqueta += 6;
        }

        g.setFont(fonteTitulo);
        FontMetrics ft = g.getFontMetrics();
        int espacoTitulo = w - 2 * margem - (alinharAEsquerda ? larguraEtiqueta : 0);
        String titulo = caber(getText(), ft, espacoTitulo);

        int yTitulo;
        if (duasLinhas) {
            int alturaBloco = ft.getAscent() + 4 + 13;
            yTitulo = (h - alturaBloco) / 2 + ft.getAscent();
        } else {
            yTitulo = (h - ft.getHeight()) / 2 + ft.getAscent();
        }
        int xTitulo = alinharAEsquerda ? margem : (w - ft.stringWidth(titulo)) / 2;

        if (ativo) {
            g.setColor(corSombra);
            g.drawString(titulo, xTitulo, yTitulo + 1);
        }
        g.setColor(corTexto);
        g.drawString(titulo, xTitulo, yTitulo);

        if (!duasLinhas) return;

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fd = g.getFontMetrics();
        int yDetalhe = yTitulo + 4 + fd.getAscent();

        int larguraCanto = 0;
        if (canto != null) {
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            larguraCanto = g.getFontMetrics().stringWidth(canto);
            g.setColor(corTexto);
            g.drawString(canto, w - margem - larguraCanto, yDetalhe);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            larguraCanto += 10;
        }

        String textoDetalhe = caber(detalhe, fd, w - 2 * margem - larguraCanto);
        int xDetalhe = alinharAEsquerda ? margem : (w - fd.stringWidth(textoDetalhe)) / 2;
        g.setColor(corDetalhe);
        g.drawString(textoDetalhe, xDetalhe, yDetalhe);
    }

    /** Corta o texto com reticencias para caber na largura. */
    private static String caber(String texto, FontMetrics fm, int largura) {
        if (texto == null) return "";
        if (fm.stringWidth(texto) <= largura) return texto;
        String reticencias = "…";
        int fim = texto.length();
        while (fim > 0 && fm.stringWidth(texto.substring(0, fim) + reticencias) > largura) fim--;
        return texto.substring(0, fim) + reticencias;
    }
}
