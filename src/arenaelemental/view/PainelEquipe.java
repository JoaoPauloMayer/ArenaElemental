package arenaelemental.view;

import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpe;
import arenaelemental.mundo.Area;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A equipe do treinador, editavel, e o bestiario.
 *
 * <p>Na equipe, cada cartao tem setas para mudar a criatura de posicao e um
 * botao para torna-la lider — a primeira da equipe, que e' quem entra em campo
 * ao explorar. A ordem vai para o jogo salvo.
 *
 * <p>O bestiario mostra <b>todas</b> as especies registradas, cada uma com a
 * situacao dela: capturada, apenas vista, ou ainda desconhecida. As nao vistas
 * aparecem como silhueta, sem nome nem habilidade.
 */
public class PainelEquipe extends JPanel {

    private static final Color SILHUETA = new Color(58, 66, 80);
    private static final Color FUNDO_DESCONHECIDO = new Color(17, 21, 30);

    private final JanelaPrincipal janela;
    // grades de colunas fixas: um FlowLayout dentro do scroll nao quebraria linha
    private final JPanel listaEquipe = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel listaBestiario = new JPanel(new GridLayout(0, 5, 10, 10));
    private final JLabel lblContagem = new JLabel();

    PainelEquipe(JanelaPrincipal janela) {
        this.janela = janela;
        setLayout(new BorderLayout(12, 12));
        setBackground(Constantes.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Sua equipe");
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(Constantes.TEXTO);
        JLabel dica = new JLabel("As setas mudam a ordem. A líder, a primeira da equipe, é quem entra em campo.");
        dica.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dica.setForeground(Constantes.TEXTO_SUAVE);
        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(2));
        cabecalho.add(dica);
        add(cabecalho, BorderLayout.NORTH);

        listaEquipe.setOpaque(false);
        listaBestiario.setOpaque(false);

        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.add(alinhar(embrulhar(listaEquipe)));
        conteudo.add(alinhar(subtitulo("Bestiário")));
        conteudo.add(alinhar(embrulhar(listaBestiario)));
        conteudo.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(conteudo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        BarraRolagemEscura.aplicar(scroll);
        add(scroll, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        lblContagem.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblContagem.setForeground(Constantes.TEXTO_SUAVE);
        rodape.add(lblContagem, BorderLayout.WEST);
        BotaoJogo btnVoltar = new BotaoJogo("Voltar para a exploração", Constantes.CYAN, 14);
        btnVoltar.setPreferredSize(new Dimension(250, 42));
        btnVoltar.addActionListener(e -> janela.mostrar("BATALHA"));
        rodape.add(btnVoltar, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
    }

    /**
     * Segura a grade no tamanho preferido dela: sem isso o BoxLayout esticaria
     * os cartoes para ocupar a sobra da tela.
     */
    private static JPanel embrulhar(JPanel grade) {
        JPanel embrulho = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        embrulho.setOpaque(false);
        embrulho.add(grade);
        return embrulho;
    }

    private static JComponent alinhar(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Serif", Font.BOLD, 22));
        l.setForeground(Constantes.TEXTO);
        l.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 0));
        return l;
    }

    void atualizar() {
        listaEquipe.removeAll();
        Treinador t = janela.getTreinador();
        if (t != null) {
            List<Criatura> equipe = t.getEquipe();
            for (int i = 0; i < equipe.size(); i++) listaEquipe.add(criarCartaoDaEquipe(t, i));
        }

        Bestiario bestiario = janela.getBestiario();
        listaBestiario.removeAll();
        for (EspecieCriatura especie : Especies.todas()) {
            listaBestiario.add(criarCartaoBestiario(especie, bestiario.estado(especie)));
        }
        lblContagem.setText("Bestiário: " + bestiario.totalCapturadas() + " capturada(s) · "
                + bestiario.totalVistas() + " vista(s) de " + bestiario.totalDeEspecies() + " espécie(s)");

        listaEquipe.revalidate();
        listaBestiario.revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // Equipe
    // ------------------------------------------------------------------

    private JPanel criarCartaoDaEquipe(Treinador t, int indice) {
        Criatura c = t.getEquipe().get(indice);
        boolean lider = indice == 0;
        boolean ultima = indice == t.getEquipe().size() - 1;

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Constantes.SUPERFICIE);
        p.setBorder(BorderFactory.createLineBorder(lider ? Constantes.AMBER : Constantes.BORDA, 1, true));
        p.setPreferredSize(new Dimension(390, 178));

        JPanel spriteP = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                CriaturaSprite.desenhar((Graphics2D) g, c, getWidth() / 2, getHeight() / 2 + 4, 76);
            }
        };
        spriteP.setOpaque(false);
        spriteP.setPreferredSize(new Dimension(104, 100));
        p.add(spriteP, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(10, 2, 4, 10));

        JPanel linhaNome = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaNome.setOpaque(false);
        JLabel nome = new JLabel(c.getNome() + "  Nv." + c.getNivel());
        nome.setFont(new Font("SansSerif", Font.BOLD, 15));
        nome.setForeground(Constantes.TEXTO);
        linhaNome.add(nome);
        if (lider) {
            linhaNome.add(Box.createHorizontalStrut(8));
            linhaNome.add(selo("LÍDER", Constantes.AMBER));
        }
        // sem teto, o BoxLayout daria a sobra de altura do cartao a esta linha
        linhaNome.setMaximumSize(linhaNome.getPreferredSize());

        JLabel tipo = new JLabel("<html>" + c.getEspecie().getNome() + " · "
                + Constantes.tiposEmHtml(c.getEspecie()) + "</html>");
        tipo.setFont(new Font("SansSerif", Font.BOLD, 12));
        tipo.setForeground(Constantes.TEXTO_SUAVE);

        String situacao = !c.estaViva() ? "  ·  Desmaiada"
                : (c.temCondicao() ? "  ·  " + c.getCondicao().nomeExibicao() : "");
        JLabel vida = new JLabel(c.getVidaAtual() + "/" + c.getHpMaximo() + " HP" + situacao);
        vida.setFont(new Font("SansSerif", Font.PLAIN, 12));
        vida.setForeground(!c.estaViva() ? Constantes.VERMELHO
                : (c.temCondicao() ? c.getCondicao().cor() : Constantes.TEXTO_SUAVE));

        JLabel status = new JLabel("Atk " + c.getAtaque() + " · Def " + c.getDefesa()
                + " · SpA " + c.getAtaqueEsp() + " · SpD " + c.getDefesaEsp() + " · Spe " + c.getVelocidade());
        status.setFont(new Font("SansSerif", Font.PLAIN, 11));
        status.setForeground(Constantes.TEXTO_SUAVE);

        JLabel golpes = new JLabel("<html>" + repertorioComPp(c) + "</html>");
        golpes.setFont(new Font("SansSerif", Font.PLAIN, 11));
        golpes.setForeground(Constantes.TEXTO_SUAVE);

        for (JComponent comp : new JComponent[] {linhaNome, tipo, vida, status, golpes}) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            info.add(comp);
            info.add(Box.createVerticalStrut(2));
        }
        p.add(info, BorderLayout.CENTER);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controles.setOpaque(false);
        controles.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 6));
        controles.add(botaoDeControle("←", 44, !lider, "Mover para a esquerda",
                () -> t.trocarDePosicao(indice, indice - 1)));
        controles.add(botaoDeControle("→", 44, !ultima, "Mover para a direita",
                () -> t.trocarDePosicao(indice, indice + 1)));
        controles.add(botaoDeControle("Tornar líder", 120, !lider, "Levar para a frente da equipe",
                () -> t.tornarLider(indice)));
        p.add(controles, BorderLayout.SOUTH);
        return p;
    }

    private BotaoJogo botaoDeControle(String texto, int largura, boolean habilitado, String dica, Runnable acao) {
        BotaoJogo b = new BotaoJogo(texto, Constantes.ARDOSIA, 13);
        b.setPreferredSize(new Dimension(largura, 30));
        b.setEnabled(habilitado);
        b.setToolTipText(dica);
        b.addActionListener(e -> { acao.run(); atualizar(); });
        return b;
    }

    private static JLabel selo(String texto, Color cor) {
        JLabel l = new JLabel(texto);
        l.setOpaque(true);
        l.setBackground(cor);
        l.setForeground(new Color(24, 26, 34));
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return l;
    }

    /** Os golpes da criatura com os PP restantes, dois por linha. */
    private static String repertorioComPp(Criatura c) {
        StringBuilder sb = new StringBuilder();
        List<Golpe> golpes = c.getGolpes();
        for (int i = 0; i < golpes.size(); i++) {
            Golpe g = golpes.get(i);
            if (i > 0) sb.append(i % 2 == 0 ? "<br>" : " &nbsp;·&nbsp; ");
            sb.append(g.getNome()).append(' ').append(c.getPp(g)).append('/').append(c.getPpMaximo(g));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Bestiario
    // ------------------------------------------------------------------

    /** A habilidade e onde encontrar a especie. */
    private static String dicaDoBestiario(EspecieCriatura especie) {
        StringBuilder onde = new StringBuilder();
        for (Area a : especie.getHabitat()) {
            if (onde.length() > 0) onde.append(", ");
            onde.append(a.getNome());
        }
        return "<html>" + especie.getDescricaoHabilidade() + "<br>Vive em: " + onde + "</html>";
    }

    private JPanel criarCartaoBestiario(EspecieCriatura especie, Bestiario.Registro registro) {
        boolean conhecida = registro != Bestiario.Registro.NAO_VISTA;
        boolean capturada = registro == Bestiario.Registro.CAPTURADA;

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(conhecida ? Constantes.SUPERFICIE : FUNDO_DESCONHECIDO);
        p.setBorder(BorderFactory.createLineBorder(
                capturada ? Constantes.VERDE : Constantes.BORDA, capturada ? 2 : 1, true));
        p.setPreferredSize(new Dimension(150, 150));

        JPanel spriteP = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (conhecida) {
                    CriaturaSprite.desenhar((Graphics2D) g, especie, getWidth() / 2, getHeight() / 2, 74);
                } else {
                    // silhueta: mesma forma, sem cor nem adorno
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(SILHUETA);
                    g2.fillOval(getWidth() / 2 - 37, getHeight() / 2 - 37, 74, 74);
                }
            }
        };
        spriteP.setOpaque(false);
        spriteP.setPreferredSize(new Dimension(150, 92));
        p.add(spriteP, BorderLayout.CENTER);

        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new BoxLayout(rodape, BoxLayout.Y_AXIS));
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JLabel nome = new JLabel(conhecida ? especie.getNome() : "???");
        nome.setFont(new Font("SansSerif", Font.BOLD, 13));
        nome.setForeground(conhecida ? Constantes.TEXTO : Constantes.TEXTO_SUAVE);
        nome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel estado = new JLabel(conhecida ? registro.nomeExibicao() : "Não encontrada");
        estado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        estado.setForeground(capturada ? Constantes.VERDE : Constantes.TEXTO_SUAVE);
        estado.setAlignmentX(Component.CENTER_ALIGNMENT);

        rodape.add(nome);
        rodape.add(estado);
        if (conhecida) {
            JLabel tipo = new JLabel("<html>" + Constantes.tiposEmHtml(especie) + "</html>");
            tipo.setFont(new Font("SansSerif", Font.PLAIN, 11));
            tipo.setAlignmentX(Component.CENTER_ALIGNMENT);
            rodape.add(tipo);
            p.setToolTipText(dicaDoBestiario(especie));
        }
        p.add(rodape, BorderLayout.SOUTH);
        return p;
    }
}
