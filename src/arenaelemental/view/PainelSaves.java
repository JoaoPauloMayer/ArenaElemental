package arenaelemental.view;

import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.persistencia.ErroDePersistencia;
import arenaelemental.persistencia.ResumoDoSave;
import arenaelemental.persistencia.SlotDeSave;

import javax.swing.*;
import java.awt.*;

/**
 * A lista de slots de save.
 *
 * <p>Tem dois modos. Para <b>salvar</b>, aberto pela exploracao: cada slot tem
 * "Salvar aqui" (que pergunta antes de gravar por cima de um save), o
 * salvamento rapido grava sem perguntar, e ha o botao de sair do jogo. Para
 * <b>carregar</b>, aberto pelo menu: cada slot com save legivel tem
 * "Carregar".
 */
public class PainelSaves extends JPanel {

    enum Modo { SALVAR, CARREGAR }

    private final JanelaPrincipal janela;
    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JLabel lblStatus = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel lista = new JPanel(new GridLayout(0, 1, 0, 6));
    private final BotaoJogo btnSair;
    private Modo modo = Modo.SALVAR;

    PainelSaves(JanelaPrincipal janela) {
        this.janela = janela;
        setLayout(new BorderLayout(0, 12));
        setBackground(Constantes.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 26));
        lblTitulo.setForeground(Constantes.TEXTO);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Constantes.TEXTO_SUAVE);
        cabecalho.add(lblTitulo);
        cabecalho.add(Box.createVerticalStrut(2));
        cabecalho.add(lblSubtitulo);
        add(cabecalho, BorderLayout.NORTH);

        lista.setOpaque(false);
        add(lista, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);
        BotaoJogo btnVoltar = new BotaoJogo("Voltar", Constantes.ARDOSIA, 14);
        btnVoltar.setPreferredSize(new Dimension(170, 42));
        btnVoltar.addActionListener(e -> voltar());
        rodape.add(btnVoltar, BorderLayout.WEST);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setForeground(Constantes.VERDE);
        rodape.add(lblStatus, BorderLayout.CENTER);
        btnSair = new BotaoJogo("Sair do jogo", new Color(186, 58, 72), 14);
        btnSair.setPreferredSize(new Dimension(170, 42));
        btnSair.setToolTipText("Fechar o Arena Elemental — pergunta antes se quer salvar");
        btnSair.addActionListener(e -> janela.sairDoJogo());
        rodape.add(btnSair, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
    }

    void abrir(Modo modo) {
        this.modo = modo;
        lblStatus.setText(" ");
        boolean salvando = modo == Modo.SALVAR;
        lblTitulo.setText(salvando ? "Salvar jornada" : "Carregar jornada");
        lblSubtitulo.setText(salvando
                ? "Escolha um slot. O salvamento rápido grava na hora, sem perguntar — também pelo F5 na exploração."
                : "Escolha de onde continuar.");
        btnSair.setVisible(salvando);
        atualizarLista();
    }

    private void atualizarLista() {
        lista.removeAll();
        for (ResumoDoSave r : janela.resumosDosSaves()) lista.add(criarLinha(r));
        lista.revalidate();
        lista.repaint();
    }

    private void voltar() {
        janela.mostrar(modo == Modo.SALVAR ? "BATALHA" : "MENU");
    }

    // ------------------------------------------------------------------
    // Linhas
    // ------------------------------------------------------------------

    private JPanel criarLinha(ResumoDoSave r) {
        boolean rapido = r.getSlot().ehRapido();

        JPanel linha = new JPanel(new BorderLayout(12, 0));
        linha.setBackground(r.isVazio() ? new Color(17, 21, 30) : Constantes.SUPERFICIE);
        linha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(rapido ? Constantes.comAlfa(Constantes.AMBER, 160) : Constantes.BORDA, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 12)));

        linha.add(miniatura(r.getEspecieDoLider()), BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Box.createVerticalGlue());
        String quando = r.getSalvoEm() == null ? "" : "   ·   " + r.getSalvoEm().format(JanelaPrincipal.HORA_LEGIVEL);
        JLabel nome = new JLabel(r.getSlot().getNome() + quando);
        nome.setFont(new Font("SansSerif", Font.BOLD, 14));
        nome.setForeground(rapido ? Constantes.AMBER : Constantes.TEXTO);
        JLabel detalhe = new JLabel(r.descricao());
        detalhe.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detalhe.setForeground(r.isVazio() || r.isLegivel() ? Constantes.TEXTO_SUAVE : Constantes.VERMELHO);
        if (!r.isVazio() && !r.isLegivel()) detalhe.setToolTipText(r.getErro());
        info.add(nome);
        info.add(Box.createVerticalStrut(3));
        info.add(detalhe);
        info.add(Box.createVerticalGlue());
        linha.add(info, BorderLayout.CENTER);

        JPanel lado = new JPanel(new GridBagLayout());
        lado.setOpaque(false);
        lado.add(botaoDaLinha(r));
        linha.add(lado, BorderLayout.EAST);
        return linha;
    }

    private BotaoJogo botaoDaLinha(ResumoDoSave r) {
        BotaoJogo b;
        if (modo == Modo.CARREGAR) {
            b = new BotaoJogo("Carregar", Constantes.VERDE, 13);
            b.setEnabled(r.isLegivel());
            b.addActionListener(e -> janela.carregarJogo(r.getSlot()));
        } else if (r.getSlot().ehRapido()) {
            b = new BotaoJogo("Salvar rápido", Constantes.AMBER, 13);
            b.setToolTipText("Grava na hora, por cima do salvamento rápido anterior");
            b.addActionListener(e -> salvar(r));
        } else {
            b = new BotaoJogo("Salvar aqui", Constantes.CYAN, 13);
            b.addActionListener(e -> salvar(r));
        }
        b.setPreferredSize(new Dimension(150, 38));
        return b;
    }

    /** Grava no slot. Por cima de um save numerado, pergunta antes; o rapido nunca pergunta. */
    private void salvar(ResumoDoSave r) {
        SlotDeSave slot = r.getSlot();
        if (!slot.ehRapido() && !r.isVazio()) {
            String quando = r.getSalvoEm() == null ? "" : " (salvo em " + r.getSalvoEm().format(JanelaPrincipal.HORA_LEGIVEL) + ")";
            int escolha = JOptionPane.showConfirmDialog(this,
                    "Substituir o jogo do " + slot.getNome() + quando + "?",
                    "Salvar por cima", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (escolha != JOptionPane.YES_OPTION) return;
        }
        try {
            janela.salvar(slot);
            lblStatus.setForeground(Constantes.VERDE);
            lblStatus.setText("Jogo salvo: " + slot.getNome() + ".");
        } catch (ErroDePersistencia e) {
            lblStatus.setForeground(Constantes.VERMELHO);
            lblStatus.setText(e.getMessage());
        }
        atualizarLista();
    }

    /** O lider da equipe salva, pequeno; um circulo apagado num slot vazio. */
    private static JComponent miniatura(EspecieCriatura especie) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                if (especie != null) {
                    CriaturaSprite.desenhar(g2, especie, getWidth() / 2, getHeight() / 2 + 6, 38);
                } else {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Constantes.BORDA);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1f, new float[] {4f, 4f}, 0f));
                    g2.drawOval(getWidth() / 2 - 17, getHeight() / 2 - 13, 34, 34);
                }
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(56, 50));
        return p;
    }
}
