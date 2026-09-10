package arenaelemental.view;

import javax.swing.*;
import java.awt.*;

/**
 * Tela inicial.
 *
 * <p>"Continuar Jornada" carrega o save gravado por ultimo, e mostra embaixo
 * qual slot e quando; "Carregar Jogo" abre a lista de slots. Os dois so ficam
 * ativos quando existe algum save.
 */
public class PainelMenu extends JPanel {

    private final JanelaPrincipal janela;
    private final BotaoJogo btnContinuar, btnCarregar;
    private final JLabel lblSave = new JLabel(" ");

    PainelMenu(JanelaPrincipal janela) {
        this.janela = janela;
        setLayout(new GridBagLayout());
        setBackground(Constantes.FUNDO);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.insets = new Insets(10, 0, 10, 0);

        JLabel titulo = new JLabel("ARENA ELEMENTAL");
        titulo.setFont(new Font("Serif", Font.BOLD, 44));
        titulo.setForeground(Constantes.TEXTO);
        c.gridy = 0; add(titulo, c);

        JLabel subtitulo = new JLabel("Capture. Treine. Batalhe.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(Constantes.CYAN);
        c.gridy = 1; add(subtitulo, c);

        BotaoJogo btnJogar = criarBotao("Nova Jornada", Constantes.CYAN);
        btnJogar.addActionListener(e -> janela.mostrar("ESCOLHA"));
        c.gridy = 2; c.insets = new Insets(40, 0, 10, 0); add(btnJogar, c);

        btnContinuar = criarBotao("Continuar Jornada", Constantes.AMBER);
        btnContinuar.setToolTipText("Carrega o jogo salvo por último");
        btnContinuar.addActionListener(e -> janela.continuarJornada());
        c.gridy = 3; c.insets = new Insets(10, 0, 4, 0); add(btnContinuar, c);

        lblSave.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSave.setForeground(Constantes.TEXTO_SUAVE);
        c.gridy = 4; c.insets = new Insets(0, 0, 10, 0); add(lblSave, c);

        btnCarregar = criarBotao("Carregar Jogo", Constantes.VIOLETA);
        btnCarregar.setToolTipText("Escolher entre os slots de save");
        btnCarregar.addActionListener(e -> janela.mostrarSaves(PainelSaves.Modo.CARREGAR));
        c.gridy = 5; c.insets = new Insets(10, 0, 10, 0); add(btnCarregar, c);

        BotaoJogo btnSair = criarBotao("Sair", Constantes.ARDOSIA);
        btnSair.addActionListener(e -> janela.sairDoJogo());
        c.gridy = 6; add(btnSair, c);

        atualizar();
    }

    /** Liga ou desliga os botoes de continuar e carregar conforme existam saves. */
    void atualizar() {
        String descricao = janela.descricaoDoSave();
        btnContinuar.setEnabled(descricao != null);
        btnCarregar.setEnabled(janela.temJogoSalvo());
        lblSave.setText(descricao == null ? "Nenhum jogo salvo ainda" : descricao);
    }

    private static BotaoJogo criarBotao(String texto, Color cor) {
        BotaoJogo b = new BotaoJogo(texto, cor, 16);
        b.setPreferredSize(new Dimension(260, 50));
        return b;
    }
}
