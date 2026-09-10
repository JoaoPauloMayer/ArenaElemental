package arenaelemental.view;

import arenaelemental.modelo.Criatura;
import arenaelemental.persistencia.ErroDePersistencia;
import arenaelemental.persistencia.JogoSalvo;
import arenaelemental.persistencia.ResumoDoSave;
import arenaelemental.persistencia.Saves;
import arenaelemental.persistencia.SlotDeSave;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A janela do jogo e o estado da partida: o treinador, o bestiario e os slots
 * de save onde os dois sao gravados.
 */
public class JanelaPrincipal extends JFrame {

    static final DateTimeFormatter HORA_LEGIVEL = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cartas = new JPanel(cardLayout);
    private final Saves saves;
    private final PainelBatalha painelBatalha;
    private final PainelEquipe painelEquipe;
    private final PainelMenu painelMenu;
    private final PainelSaves painelSaves;

    private Treinador treinador;
    private Bestiario bestiario = new Bestiario();

    /**
     * O slot de onde a jornada veio ou onde foi salva por ultimo — e' nele que
     * a pergunta "salvar antes de sair?" grava. {@code null} numa jornada nova
     * que ainda nao foi salva; o salvamento rapido nao muda este slot.
     */
    private SlotDeSave slotAtual;

    public JanelaPrincipal() { this(new Saves()); }

    /** Janela com pasta de saves propria — usado para testar sem tocar nos saves reais. */
    public JanelaPrincipal(Saves saves) {
        this.saves = saves;

        setTitle("Arena Elemental");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        aplicarTemaAosTooltips();

        painelBatalha = new PainelBatalha(this);
        painelEquipe = new PainelEquipe(this);
        painelMenu = new PainelMenu(this);
        painelSaves = new PainelSaves(this);

        cartas.add(painelMenu, "MENU");
        cartas.add(new PainelEscolha(this), "ESCOLHA");
        cartas.add(painelBatalha, "BATALHA");
        cartas.add(painelEquipe, "EQUIPE");
        cartas.add(painelSaves, "SAVES");

        cartas.setBackground(Constantes.FUNDO);
        add(cartas);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { sairDoJogo(); }
        });
        mostrar("MENU");
    }

    /** Tooltips escuros, como o resto da interface. */
    private static void aplicarTemaAosTooltips() {
        UIManager.put("ToolTip.background", Constantes.SUPERFICIE_ALTA);
        UIManager.put("ToolTip.foreground", Constantes.TEXTO);
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constantes.BORDA),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        UIManager.put("ToolTip.font", new Font("SansSerif", Font.PLAIN, 12));
    }

    void mostrar(String nome) {
        if (nome.equals("EQUIPE")) painelEquipe.atualizar();
        if (nome.equals("MENU")) painelMenu.atualizar();
        if (nome.equals("BATALHA")) painelBatalha.aoMostrar();
        cardLayout.show(cartas, nome);
    }

    void iniciarJornada(Criatura inicial) {
        treinador = new Treinador("Treinador");
        treinador.adicionarNaEquipe(inicial);
        bestiario = new Bestiario();
        bestiario.marcarCapturada(inicial);
        slotAtual = null;
        painelBatalha.iniciar();
        mostrar("BATALHA");
    }

    Treinador getTreinador() { return treinador; }
    Bestiario getBestiario() { return bestiario; }

    // ------------------------------------------------------------------
    // Saves
    // ------------------------------------------------------------------

    /** A tela de slots, para salvar (durante a jornada) ou carregar (no menu). */
    void mostrarSaves(PainelSaves.Modo modo) {
        painelSaves.abrir(modo);
        cardLayout.show(cartas, "SAVES");
    }

    List<ResumoDoSave> resumosDosSaves() { return saves.resumos(); }

    boolean temJogoSalvo() { return saves.existeAlgum(); }

    /** O save que o "Continuar Jornada" carrega, ou {@code null}. */
    ResumoDoSave saveMaisRecente() { return saves.maisRecente(); }

    /** Descricao do save mais recente para o menu, ou {@code null} se nao houver. */
    String descricaoDoSave() {
        ResumoDoSave recente = saves.maisRecente();
        if (recente == null) return null;
        LocalDateTime quando = recente.getSalvoEm();
        return recente.getSlot().getNome() + (quando == null ? "" : " · salvo em " + quando.format(HORA_LEGIVEL));
    }

    /**
     * Grava a partida no slot.
     *
     * @throws ErroDePersistencia sem jornada em andamento ou se o disco falhar
     */
    void salvar(SlotDeSave slot) {
        saves.salvar(slot, treinador, bestiario);
        if (!slot.ehRapido()) slotAtual = slot;
        painelMenu.atualizar();
    }

    /**
     * Grava a partida no slot, sem lancar excecao.
     *
     * @return a linha a mostrar — sucesso ou o motivo da falha
     */
    String salvarJogo(SlotDeSave slot) {
        try {
            salvar(slot);
            return "Jogo salvo: " + slot.getNome() + ".";
        } catch (ErroDePersistencia e) {
            return e.getMessage();
        }
    }

    /** Carrega a partida do slot e vai para a tela de exploracao. */
    void carregarJogo(SlotDeSave slot) {
        try {
            JogoSalvo salvo = saves.carregar(slot);
            treinador = salvo.getTreinador();
            bestiario = salvo.getBestiario();
            slotAtual = slot;

            StringBuilder msg = new StringBuilder("Jornada retomada do ").append(slot.getNome());
            if (salvo.getSalvoEm() != null) msg.append(" (salva em ").append(salvo.getSalvoEm().format(HORA_LEGIVEL)).append(')');
            msg.append(".\n");
            for (String aviso : salvo.getAvisos()) msg.append("Aviso: ").append(aviso).append('\n');

            painelBatalha.retomar(msg.toString());
            mostrar("BATALHA");
            avisarSobreProblemas(salvo.getAvisos());
        } catch (ErroDePersistencia e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Não foi possível carregar", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Carrega o save gravado por ultimo. */
    void continuarJornada() {
        ResumoDoSave recente = saves.maisRecente();
        if (recente != null) carregarJogo(recente.getSlot());
    }

    private void avisarSobreProblemas(List<String> avisos) {
        if (avisos.isEmpty()) return;
        JOptionPane.showMessageDialog(this,
                "O jogo foi carregado, mas parte dele não pôde ser reconstruída:\n\n"
                        + String.join("\n", avisos),
                "Jogo carregado com avisos", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Sair
    // ------------------------------------------------------------------

    /**
     * Sai do jogo — pelo botao Sair ou pelo X da janela. Com uma jornada em
     * andamento, oferece salvar antes, no slot atual ou, numa jornada que
     * nunca foi salva, no salvamento rapido.
     */
    void sairDoJogo() {
        if (treinador == null) { sair(); return; }

        SlotDeSave destino = slotAtual != null ? slotAtual : SlotDeSave.RAPIDO;
        int escolha = JOptionPane.showConfirmDialog(this,
                "Salvar a jornada em " + destino.getNome() + " antes de sair?", "Sair do Arena Elemental",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) return;
        if (escolha == JOptionPane.YES_OPTION) {
            try {
                saves.salvar(destino, treinador, bestiario);
            } catch (ErroDePersistencia e) {
                int mesmoAssim = JOptionPane.showConfirmDialog(this,
                        e.getMessage() + "\n\nSair mesmo assim?", "Falha ao salvar",
                        JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (mesmoAssim != JOptionPane.YES_OPTION) return;
            }
        }
        sair();
    }

    private void sair() {
        dispose();
        System.exit(0);
    }
}
