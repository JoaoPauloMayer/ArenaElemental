package arenaelemental.view;

import arenaelemental.modelo.EspecieCriatura;
import javax.swing.*;
import java.awt.*;

/**
 * Cartao da tela de escolha inicial, montado a partir da ficha da especie.
 *
 * <p>Nome, tipo, habilidade e sprite saem todos de {@link EspecieCriatura}:
 * uma especie nova aparece aqui sem nenhuma linha a mais.
 */
public class CartaoCriatura extends JPanel {

    CartaoCriatura(EspecieCriatura especie, Runnable aoEscolher) {
        setLayout(new BorderLayout());
        setBackground(Constantes.SUPERFICIE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constantes.BORDA, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));
        setPreferredSize(new Dimension(230, 340));

        // espaco em cima para o adorno (chama, folhas) nao ser cortado
        JPanel spritePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                CriaturaSprite.desenhar((Graphics2D) g, especie, getWidth() / 2, getHeight() / 2 + 16, 110);
            }
        };
        spritePanel.setPreferredSize(new Dimension(230, 168));
        spritePanel.setOpaque(false);
        add(spritePanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 8, 16));

        JLabel lblNome = new JLabel(especie.getNome());
        lblNome.setFont(new Font("Serif", Font.BOLD, 20));
        lblNome.setForeground(Constantes.TEXTO);
        lblNome.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTipo = new JLabel("<html>Tipo " + Constantes.tiposEmHtml(especie) + "</html>");
        lblTipo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTipo.setForeground(Constantes.TEXTO_SUAVE);
        lblTipo.setAlignmentX(CENTER_ALIGNMENT);

        JTextArea lblHabilidade = new JTextArea(especie.getDescricaoHabilidade());
        lblHabilidade.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblHabilidade.setForeground(Constantes.TEXTO_SUAVE);
        lblHabilidade.setLineWrap(true);
        lblHabilidade.setWrapStyleWord(true);
        lblHabilidade.setOpaque(false);
        lblHabilidade.setEditable(false);
        lblHabilidade.setFocusable(false);
        lblHabilidade.setAlignmentX(CENTER_ALIGNMENT);
        lblHabilidade.setMaximumSize(new Dimension(190, 60));

        infoPanel.add(lblNome);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblTipo);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblHabilidade);
        add(infoPanel, BorderLayout.CENTER);

        BotaoJogo btn = new BotaoJogo("Escolher", Constantes.corVivida(especie.getTipo()), 14);
        btn.setPreferredSize(new Dimension(10, 40));
        btn.addActionListener(e -> aoEscolher.run());
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        rodape.add(btn);
        add(rodape, BorderLayout.SOUTH);
    }
}
