package arenaelemental.view;

import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.FabricaCriaturas;
import javax.swing.*;
import java.awt.*;

/**
 * Tela de escolha da criatura inicial.
 *
 * <p>Os cartoes saem de {@link Especies#iniciais()}: marcar uma especie nova
 * com {@code .inicial()} no registro basta para ela aparecer aqui.
 */
public class PainelEscolha extends JPanel {

    PainelEscolha(JanelaPrincipal janela) {
        setLayout(new BorderLayout());
        setBackground(Constantes.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titulo = new JLabel("Escolha sua primeira criatura", SwingConstants.CENTER);
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(Constantes.TEXTO);
        add(titulo, BorderLayout.NORTH);

        JPanel cartas = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 20));
        cartas.setOpaque(false);
        for (EspecieCriatura especie : Especies.iniciais()) {
            cartas.add(new CartaoCriatura(especie,
                    () -> janela.iniciarJornada(FabricaCriaturas.novaInicial(especie))));
        }

        JScrollPane scroll = new JScrollPane(cartas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        BarraRolagemEscura.aplicar(scroll);
        add(scroll, BorderLayout.CENTER);
    }
}
