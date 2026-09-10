package arenaelemental.view;

import arenaelemental.modelo.Criatura;
import arenaelemental.mundo.Area;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * A cena: a criatura do jogador embaixo a esquerda e a selvagem em cima a
 * direita, sobre o cenario da area, com os cartoes de vida por cima.
 *
 * <p>A arena guarda as proprias criaturas, e nao copias do tipo e do estado
 * delas: assim o desenho nunca fica defasado do que aconteceu na batalha.
 *
 * <p>Sem cenario — a area nao tem imagem, ou o arquivo nao pode ser lido — o
 * fundo e' um gradiente pintado na cor da area. O jogo nunca depende de uma
 * imagem existir.
 */
public class PainelArena extends JPanel {

    /** Escurecimento no pe da cena, para as criaturas nao flutuarem na foto. */
    private static final Color SOMBRA_DO_CHAO = new Color(0, 0, 0, 120);
    private static final int ARCO = 18;
    private static final int MARGEM = 14;

    private final BarraVida barraJogador = new BarraVida(null, true);   // com barra de EXP
    private final BarraVida barraSelvagem = new BarraVida(null, false);

    private Criatura esquerda, direita;
    private Area area;
    private Cenario cenario;

    // a imagem ja redimensionada para o tamanho atual do painel
    private BufferedImage fundoPronto;
    private Cenario cenarioDoFundoPronto;
    private int larguraDoFundoPronto, alturaDoFundoPronto;

    PainelArena() {
        setOpaque(false);
        setLayout(null);            // os cartoes de vida sao posicionados em doLayout
        setPreferredSize(new Dimension(760, 320));
        add(barraSelvagem);
        add(barraJogador);
    }

    /** Coloca as duas criaturas em cena, com os cartoes de vida. Qualquer uma pode ser {@code null}. */
    void configurar(Criatura esquerda, Criatura direita) {
        this.esquerda = esquerda;
        this.direita = direita;
        barraJogador.setCriatura(esquerda);
        barraSelvagem.setCriatura(direita);
        repaint();
    }

    /** Troca a area e o cenario. Cenario {@code null} pinta o gradiente da area. */
    void mostrar(Area area, Cenario cenario) {
        this.area = area;
        this.cenario = cenario;
        this.fundoPronto = null;
        repaint();
    }

    Cenario getCenario() { return cenario; }

    @Override
    public void doLayout() {
        Dimension js = barraJogador.getPreferredSize();
        Dimension ss = barraSelvagem.getPreferredSize();
        barraSelvagem.setBounds(MARGEM, MARGEM, ss.width, ss.height);
        barraJogador.setBounds(getWidth() - MARGEM - js.width, getHeight() - MARGEM - js.height,
                js.width, js.height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int largura = getWidth(), altura = getHeight();
        if (largura <= 0 || altura <= 0) { g.dispose(); return; }

        // tudo o que e' desenhado fica dentro dos cantos arredondados
        Shape moldura = new RoundRectangle2D.Float(0, 0, largura, altura, ARCO, ARCO);
        g.setClip(moldura);

        BufferedImage fundo = fundoPara(largura, altura);
        if (fundo != null) {
            g.drawImage(fundo, 0, 0, null);
        } else {
            desenharFundoPintado(g, largura, altura);
        }
        desenharSombraDoChao(g, largura, altura);

        int tamanhoJogador = Math.min(140, altura * 2 / 5);
        int tamanhoSelvagem = Math.min(116, altura / 3);
        CriaturaSprite.desenhar(g, esquerda, 175, altura - 40 - tamanhoJogador / 2, tamanhoJogador);
        CriaturaSprite.desenhar(g, direita, largura - 200, 50 + tamanhoSelvagem / 2 + 14, tamanhoSelvagem);

        if (fundo != null && cenario != null) desenharNomeDoCenario(g, largura);

        g.setClip(null);
        g.setColor(new Color(255, 255, 255, 30));
        g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, largura - 1, altura - 1, ARCO, ARCO));
        g.dispose();
    }

    // ------------------------------------------------------------------
    // Fundo
    // ------------------------------------------------------------------

    /**
     * A imagem do cenario ja cortada e redimensionada para o painel, ou
     * {@code null} se nao houver cenario legivel.
     *
     * <p>O resultado fica guardado ate o cenario ou o tamanho do painel mudarem:
     * redimensionar um JPEG de 1600px a cada repaint sairia caro.
     */
    private BufferedImage fundoPara(int largura, int altura) {
        if (cenario == null) return null;
        if (fundoPronto != null
                && cenario == cenarioDoFundoPronto
                && largura == larguraDoFundoPronto
                && altura == alturaDoFundoPronto) {
            return fundoPronto;
        }

        BufferedImage original = Cenarios.imagemDe(cenario);
        if (original == null) return null;

        fundoPronto = recortarParaPreencher(original, largura, altura, cenario.getAncoraVertical());
        cenarioDoFundoPronto = cenario;
        larguraDoFundoPronto = largura;
        alturaDoFundoPronto = altura;
        return fundoPronto;
    }

    /**
     * Redimensiona a imagem para cobrir o painel inteiro sem distorcer, cortando
     * o que sobra.
     *
     * <p>A arena e' uma faixa larga (perto de 2,7:1), entao praticamente toda
     * imagem perde altura no corte. A ancora decide que faixa fica: 0.0 mantem
     * o topo, 1.0 o pe, 0.5 corta pelo meio.
     */
    private static BufferedImage recortarParaPreencher(BufferedImage original,
                                                       int largura, int altura,
                                                       double ancoraVertical) {
        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double escala = Math.max((double) largura / original.getWidth(),
                                 (double) altura / original.getHeight());
        int larguraEscalada = (int) Math.ceil(original.getWidth() * escala);
        int alturaEscalada = (int) Math.ceil(original.getHeight() * escala);

        int x = (largura - larguraEscalada) / 2;                        // centraliza na horizontal
        int y = (int) Math.round(-(alturaEscalada - altura) * ancoraVertical);

        g.drawImage(original, x, y, larguraEscalada, alturaEscalada, null);
        g.dispose();
        return destino;
    }

    /** Escurece o pe da cena, para dar chao as criaturas. */
    private static void desenharSombraDoChao(Graphics2D g, int largura, int altura) {
        int faixa = Math.max(40, altura / 3);
        g.setPaint(new GradientPaint(0, altura - faixa, new Color(0, 0, 0, 0),
                                     0, altura, SOMBRA_DO_CHAO));
        g.fillRect(0, altura - faixa, largura, faixa);
    }

    /**
     * O fundo usado quando nao ha imagem: um ceu na cor da area que escurece
     * para baixo, um brilho no horizonte e um chao.
     */
    private void desenharFundoPintado(Graphics2D g, int largura, int altura) {
        Color cor = area != null ? area.getCor() : Constantes.ARDOSIA;
        Color ceu = Constantes.misturar(cor, Constantes.FUNDO, 0.45);
        Color baixo = Constantes.misturar(cor, Constantes.FUNDO, 0.82);
        g.setPaint(new GradientPaint(0, 0, ceu, 0, altura, baixo));
        g.fillRect(0, 0, largura, altura);

        // brilho no horizonte
        int horizonte = altura * 3 / 5;
        g.setPaint(new RadialGradientPaint(largura / 2f, horizonte, largura * 0.55f,
                new float[] {0f, 1f},
                new Color[] {Constantes.comAlfa(Constantes.clarear(cor, 0.2), 70), new Color(0, 0, 0, 0)}));
        g.fillRect(0, 0, largura, altura);

        // chao
        g.setColor(Constantes.comAlfa(Constantes.misturar(cor, Color.BLACK, 0.7), 150));
        g.fillOval(-largura / 4, horizonte + 20, largura * 3 / 2, altura);
    }

    /**
     * O nome do cenario, discreto, no canto de cima a direita.
     *
     * <p>Sobre uma plaquinha escura, e nao solto na imagem: texto branco sozinho
     * desaparece nos cenarios claros e texto escuro some nos escuros.
     */
    private void desenharNomeDoCenario(Graphics2D g, int larguraPainel) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String nome = cenario.getNome();
        FontMetrics fm = g.getFontMetrics();
        int largura = fm.stringWidth(nome);
        int x = larguraPainel - MARGEM - largura - 6, y = MARGEM;

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x - 6, y, largura + 12, fm.getHeight() + 2, 8, 8);
        g.setColor(new Color(255, 255, 255, 210));
        g.drawString(nome, x, y + 1 + fm.getAscent());
    }
}
