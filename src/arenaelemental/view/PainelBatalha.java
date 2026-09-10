package arenaelemental.view;

import arenaelemental.batalha.Batalha;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.FabricaCriaturas;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.Golpes;
import arenaelemental.mundo.Area;
import arenaelemental.persistencia.SlotDeSave;
import arenaelemental.treinador.Treinador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Tela principal: a arena em cima, o log no meio e, embaixo, o painel de acoes,
 * que muda conforme o momento do jogo:
 *
 * <ul>
 *   <li><b>exploracao</b> — explorar a area, viajar, descansar, editar a equipe e salvar;</li>
 *   <li><b>rotas</b> — os destinos que partem da area atual;</li>
 *   <li><b>batalha</b> — os golpes numa grade 2x2, com capturar, trocar e fugir
 *       empilhados ao lado;</li>
 *   <li><b>troca</b> — a equipe, para escolher quem entra em campo.</li>
 * </ul>
 *
 * <p>A troca vira <b>obrigatoria</b> quando a criatura em campo cai e ainda ha
 * reserva viva: nesse caso o botao de cancelar some, porque a batalha nao pode
 * continuar sem alguem em campo.
 */
public class PainelBatalha extends JPanel {

    private enum Modo { EXPLORANDO, ESCOLHENDO_ROTA, EM_BATALHA, ESCOLHENDO_TROCA }

    private static final int ALTURA_ACOES = 132;
    private static final int LARGURA_COLUNA = 150;

    private final JanelaPrincipal janela;
    private final PainelArena arena = new PainelArena();
    private final JTextArea logArea = new JTextArea();
    private final JLabel lblArea = new JLabel(" ");
    private final JLabel lblStatus = new JLabel(" ");

    private final CardLayout cartas = new CardLayout();
    private final JPanel painelAcoes = new JPanel(cartas);
    private final JPanel gradeGolpes = new JPanel();
    private final JPanel gradeRotas = new JPanel();
    private final JLabel lblRotas = rotulo();
    private final JPanel gradeTroca = new JPanel(new GridLayout(2, 3, 8, 8));
    private final JLabel lblTroca = rotulo();
    private final JPanel colunaTroca = coluna();

    private BotaoJogo btnExplorar, btnViajar;
    private Modo modoAtual = Modo.EXPLORANDO;
    private Batalha batalhaAtual;
    private boolean trocaObrigatoria;
    private int linhasExibidas;

    /** Sorteios que so afetam a aparencia — fora do gerador da batalha. */
    private final Random rngVisual = new Random();

    PainelBatalha(JanelaPrincipal janela) {
        this.janela = janela;
        setLayout(new BorderLayout(0, 10));
        setBackground(Constantes.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        add(construirTopo(), BorderLayout.NORTH);
        add(arena, BorderLayout.CENTER);

        JPanel sul = new JPanel(new BorderLayout(0, 10));
        sul.setOpaque(false);
        sul.add(construirLog(), BorderLayout.NORTH);

        painelAcoes.setOpaque(false);
        painelAcoes.setPreferredSize(new Dimension(10, ALTURA_ACOES));
        painelAcoes.add(construirExploracao(), Modo.EXPLORANDO.name());
        painelAcoes.add(construirRotas(), Modo.ESCOLHENDO_ROTA.name());
        painelAcoes.add(construirBatalha(), Modo.EM_BATALHA.name());
        painelAcoes.add(construirTroca(), Modo.ESCOLHENDO_TROCA.name());
        sul.add(painelAcoes, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "salvamentoRapido");
        getActionMap().put("salvamentoRapido", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isShowing()) salvamentoRapido();
            }
        });

        aplicarModo(Modo.EXPLORANDO);
    }

    // ------------------------------------------------------------------
    // Montagem da tela
    // ------------------------------------------------------------------

    private JPanel construirTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        lblArea.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblArea.setForeground(Constantes.TEXTO);
        lblArea.setIconTextGap(8);
        topo.add(lblArea, BorderLayout.WEST);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblStatus.setForeground(Constantes.TEXTO_SUAVE);
        topo.add(lblStatus, BorderLayout.EAST);
        return topo;
    }

    private JScrollPane construirLog() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logArea.setBackground(Constantes.SUPERFICIE);
        logArea.setForeground(Constantes.TEXTO);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JScrollPane scroll = new JScrollPane(logArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(10, 76));
        scroll.setBorder(BorderFactory.createLineBorder(Constantes.BORDA));
        scroll.getViewport().setBackground(Constantes.SUPERFICIE);
        BarraRolagemEscura.aplicar(scroll);
        return scroll;
    }

    /** Explorar em destaque a esquerda; viajar, descansar, equipe e salvar numa grade ao lado. */
    private JPanel construirExploracao() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        btnExplorar = new BotaoJogo("Explorar", Area.INICIAL.getCor(), 20);
        btnExplorar.setPreferredSize(new Dimension(300, 10));
        btnExplorar.setToolTipText("Procurar uma criatura selvagem nesta área");
        btnExplorar.addActionListener(e -> explorar());
        p.add(btnExplorar, BorderLayout.WEST);

        JPanel grade = new JPanel(new GridLayout(2, 2, 8, 8));
        grade.setOpaque(false);
        btnViajar = botao("Viajar", new Color(0, 164, 206), " ", e -> abrirRotas());
        grade.add(btnViajar);
        BotaoJogo descansar = botao("Descansar", Constantes.VERDE, "Vida, PP e condições", e -> descansar());
        descansar.setToolTipText("Restaura vida, PP e condições de status de toda a equipe");
        grade.add(descansar);
        grade.add(botao("Equipe", Constantes.VIOLETA, "Ordem, líder e bestiário", e -> janela.mostrar("EQUIPE")));
        BotaoJogo salvar = botao("Salvar / Sair", Constantes.ARDOSIA, "Slots, salvamento rápido e sair",
                e -> janela.mostrarSaves(PainelSaves.Modo.SALVAR));
        salvar.setToolTipText("F5 faz o salvamento rápido direto, sem abrir a lista");
        grade.add(salvar);
        p.add(grade, BorderLayout.CENTER);
        return p;
    }

    /** Os destinos numa grade, com o botao de voltar na coluna da direita. */
    private JPanel construirRotas() {
        JPanel p = new JPanel(new BorderLayout(8, 6));
        p.setOpaque(false);
        p.add(lblRotas, BorderLayout.NORTH);
        gradeRotas.setOpaque(false);
        p.add(gradeRotas, BorderLayout.CENTER);

        JPanel col = coluna();
        col.add(botaoCompacto("Voltar", Constantes.ARDOSIA, e -> aplicarModo(Modo.EXPLORANDO)));
        p.add(col, BorderLayout.EAST);
        return p;
    }

    /** Os golpes numa grade 2x2; capturar, trocar e fugir pequenos, um acima do outro. */
    private JPanel construirBatalha() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        gradeGolpes.setOpaque(false);
        p.add(gradeGolpes, BorderLayout.CENTER);

        JPanel col = coluna();
        BotaoJogo capturar = botaoCompacto("Capturar", Constantes.AMBER, e -> capturar());
        capturar.setToolTipText("Tentar capturar a criatura selvagem — se falhar, ela ataca");
        BotaoJogo trocar = botaoCompacto("Trocar", new Color(80, 108, 156), e -> abrirEscolhaDeTroca(false));
        trocar.setToolTipText("Colocar outra criatura em campo — gasta o turno");
        BotaoJogo fugir = botaoCompacto("Fugir", new Color(186, 58, 72), e -> fugir());
        fugir.setToolTipText("Sair da batalha");
        col.add(capturar);
        col.add(trocar);
        col.add(fugir);
        p.add(col, BorderLayout.EAST);
        return p;
    }

    private JPanel construirTroca() {
        JPanel p = new JPanel(new BorderLayout(8, 6));
        p.setOpaque(false);
        p.add(lblTroca, BorderLayout.NORTH);
        gradeTroca.setOpaque(false);
        p.add(gradeTroca, BorderLayout.CENTER);
        p.add(colunaTroca, BorderLayout.EAST);
        return p;
    }

    private static BotaoJogo botao(String titulo, Color cor, String detalhe, ActionListener acao) {
        BotaoJogo b = new BotaoJogo(titulo, cor);
        b.setDetalhe(detalhe);
        b.addActionListener(acao);
        return b;
    }

    private static BotaoJogo botaoCompacto(String titulo, Color cor, ActionListener acao) {
        BotaoJogo b = new BotaoJogo(titulo, cor, 13);
        b.addActionListener(acao);
        return b;
    }

    /** A coluna estreita da direita, com ate tres botoes pequenos empilhados. */
    private static JPanel coluna() {
        JPanel col = new JPanel(new GridLayout(3, 1, 0, 6));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(LARGURA_COLUNA, 10));
        return col;
    }

    private static JLabel rotulo() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(Constantes.TEXTO_SUAVE);
        return l;
    }

    /** Uma casa vazia da grade, para ela manter a forma com menos de quatro golpes. */
    private static JComponent casaVazia() {
        JPanel vazia = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constantes.SUPERFICIE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Constantes.BORDA);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        vazia.setOpaque(false);
        return vazia;
    }

    /** A bolinha na cor da area, ao lado do nome dela no topo. */
    private static Icon bolinha(Color cor) {
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constantes.comAlfa(cor, 80));
                g2.fillOval(x, y, 16, 16);
                g2.setColor(cor);
                g2.fillOval(x + 4, y + 4, 8, 8);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    // ------------------------------------------------------------------
    // Botoes de golpe
    // ------------------------------------------------------------------

    /**
     * Recria os botoes de golpe. Todos os golpes aparecem; os que estao sem PP
     * ficam desabilitados, para o jogador ver o que acabou. Com o repertorio
     * inteiro zerado, entra o botao do Esforço na primeira casa.
     */
    private void montarBotoesDeGolpe(Criatura ativa) {
        List<JComponent> botoes = new ArrayList<>();
        if (ativa != null) {
            if (!ativa.temAlgumGolpeUtilizavel()) botoes.add(criarBotaoDeGolpe(ativa, Golpes.ESFORCO, true));
            for (Golpe g : ativa.getGolpes()) botoes.add(criarBotaoDeGolpe(ativa, g, ativa.temPp(g)));
        }

        int colunas = Math.max(2, (botoes.size() + 1) / 2);
        gradeGolpes.removeAll();
        gradeGolpes.setLayout(new GridLayout(2, colunas, 8, 8));
        for (JComponent b : botoes) gradeGolpes.add(b);
        for (int i = botoes.size(); i < 2 * colunas; i++) gradeGolpes.add(casaVazia());
        gradeGolpes.revalidate();
        gradeGolpes.repaint();
    }

    private BotaoJogo criarBotaoDeGolpe(Criatura ativa, Golpe g, boolean utilizavel) {
        BotaoJogo b = new BotaoJogo(g.getNome(), Constantes.corVivida(g.getTipo()), 16);
        b.setAlinharAEsquerda(true);
        b.setEtiqueta(g.getTipo().nomeExibicao().toUpperCase(Locale.ROOT));
        String poder = g.causaDano() ? " · Poder " + g.getPoder() : "";
        b.setDetalhe(g.getCategoria().nomeExibicao() + poder + " · " + g.getPrecisao() + "%");
        b.setCanto(g.isSemLimiteDePp() ? "PP ∞" : "PP " + ativa.getPp(g) + "/" + ativa.getPpMaximo(g));
        b.setEnabled(utilizavel);
        b.setToolTipText(descricaoDoGolpe(g, utilizavel));
        if (utilizavel) b.addActionListener(e -> usarGolpe(g));
        return b;
    }

    private static String descricaoDoGolpe(Golpe g, boolean utilizavel) {
        if (!utilizavel) return "Sem PP — descanse para recuperar";
        if (g.temEfeitoStatus()) {
            return g.getChanceStatus() + "% de chance de causar "
                    + g.getEfeitoStatus().nomeExibicao().toLowerCase();
        }
        if (g.getRecuoPercentualDoHpMaximo() > 0) {
            return "Custa " + g.getRecuoPercentualDoHpMaximo() + "% do próprio HP máximo";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Rotas
    // ------------------------------------------------------------------

    private void abrirRotas() {
        if (janela.getTreinador() == null) return;
        montarRotas();
        aplicarModo(Modo.ESCOLHENDO_ROTA);
    }

    /**
     * Um botao por destino, na cor da area. O detalhe avisa quando o destino
     * nao tem rota direta de volta — as rotas sao de mao unica.
     */
    private void montarRotas() {
        Area aqui = janela.getTreinador().getArea();
        List<Area> destinos = aqui.getDestinos();
        lblRotas.setText("Rotas a partir de " + aqui.getNome());

        gradeRotas.removeAll();
        gradeRotas.setLayout(new GridLayout(0, destinos.size() <= 3 ? 3 : 4, 8, 8));
        for (Area destino : destinos) {
            BotaoJogo b = new BotaoJogo(destino.getNome(), destino.getCor(), 14);
            b.setDetalhe(destino.temRotaPara(aqui) ? "Ida e volta" : "Sem volta direta para cá");
            b.setToolTipText(destino.getDescricao());
            b.addActionListener(e -> viajarPara(destino));
            gradeRotas.add(b);
        }
        gradeRotas.revalidate();
        gradeRotas.repaint();
    }

    private void viajarPara(Area destino) {
        Treinador t = janela.getTreinador();
        if (t == null) return;
        if (!t.viajarPara(destino)) {
            escrever("Não há rota daqui para " + destino.getNome() + ".");
            aplicarModo(Modo.EXPLORANDO);
            return;
        }
        // a selvagem derrotada da ultima batalha fica para tras
        batalhaAtual = null;
        arena.configurar(t.getAtiva(), null);
        mostrarAreaAtual();
        apresentarArea(destino);
        aplicarModo(Modo.EXPLORANDO);
    }

    /** Poe na tela a area onde o treinador esta: nome, cor, cenario e os botoes que dependem dela. */
    private void mostrarAreaAtual() {
        Area area = janela.getTreinador().getArea();
        lblArea.setText(area.getNome());
        lblArea.setIcon(bolinha(area.getCor()));
        arena.mostrar(area, Cenarios.sortear(area, rngVisual));
        btnExplorar.setCor(area.getCor());
        btnExplorar.setDetalhe("Procurar criaturas: " + area.getNome());
        int rotas = area.getDestinos().size();
        btnViajar.setDetalhe(rotas == 1 ? "1 rota a partir daqui" : rotas + " rotas a partir daqui");
    }

    private void apresentarArea(Area area) {
        escrever("— " + area.getNome() + " — " + area.getDescricao());
    }

    // ------------------------------------------------------------------
    // Troca de criatura
    // ------------------------------------------------------------------

    /** Abre a lista da equipe. Obrigatoria quando a criatura em campo caiu. */
    private void abrirEscolhaDeTroca(boolean obrigatoria) {
        Treinador t = janela.getTreinador();
        if (t == null) return;
        if (!obrigatoria && !t.temReservaPara(emCampo())) {
            escrever("Não há outra criatura de pé para entrar no lugar.");
            return;
        }
        trocaObrigatoria = obrigatoria;
        montarBotoesDeTroca();
        aplicarModo(Modo.ESCOLHENDO_TROCA);
    }

    /**
     * Quem esta em campo: a criatura da batalha, e nao a ativa do treinador —
     * depois de uma queda as duas divergem (ver {@link Treinador#reservasPara}).
     */
    private Criatura emCampo() {
        return batalhaAtual != null ? batalhaAtual.getDoJogador() : janela.getTreinador().getAtiva();
    }

    private void montarBotoesDeTroca() {
        Treinador t = janela.getTreinador();
        Criatura emCampo = emCampo();

        gradeTroca.removeAll();
        List<Criatura> equipe = t.getEquipe();
        for (int i = 0; i < equipe.size(); i++) {
            Criatura c = equipe.get(i);
            boolean podeEntrar = c.estaViva() && c != emCampo;
            gradeTroca.add(criarBotaoDeTroca(i, c, podeEntrar, c == emCampo));
        }
        for (int i = equipe.size(); i < Treinador.getTamanhoMaxEquipe(); i++) gradeTroca.add(casaVazia());

        colunaTroca.removeAll();
        if (!trocaObrigatoria) {
            colunaTroca.add(botaoCompacto("Cancelar", Constantes.ARDOSIA, e -> fecharEscolhaDeTroca()));
        }
        lblTroca.setText(trocaObrigatoria
                ? "Sua criatura caiu. Escolha quem entra em campo."
                : "Quem entra em campo? Trocar gasta o turno.");

        gradeTroca.revalidate();
        gradeTroca.repaint();
        colunaTroca.revalidate();
        colunaTroca.repaint();
    }

    private BotaoJogo criarBotaoDeTroca(int indice, Criatura c, boolean podeEntrar, boolean emCampo) {
        String condicao = c.temCondicao() ? " · " + c.getCondicao().sigla() : "";
        String situacao = emCampo ? " · em campo" : (!c.estaViva() ? " · desmaiada" : "");

        BotaoJogo b = new BotaoJogo(c.getNome() + "  Nv." + c.getNivel(), Constantes.corVivida(c.getTipo()), 14);
        b.setAlinharAEsquerda(true);
        b.setDetalhe(c.getVidaAtual() + "/" + c.getHpMaximo() + " HP" + condicao + situacao);
        b.setEnabled(podeEntrar);
        if (podeEntrar) b.addActionListener(e -> trocarPara(indice));
        return b;
    }

    private void trocarPara(int indice) {
        // a troca obrigatoria acontece justamente com a batalha "terminada" pela
        // queda da criatura do jogador; so a troca voluntaria exige batalha em curso
        boolean podeTrocar = batalhaAtual != null
                && (trocaObrigatoria ? batalhaAtual.aCriaturaDoJogadorCaiu() : !batalhaAtual.terminou());
        if (!podeTrocar) {
            fecharEscolhaDeTroca();
            return;
        }
        Treinador t = janela.getTreinador();
        Criatura nova = t.trocarPara(indice);
        if (nova == null) {
            // nao deveria acontecer: o botao so fica habilitado para quem pode
            // entrar. Fechar o painel mesmo assim evita prender o jogador aqui.
            escrever("Essa criatura não pode entrar em campo agora.");
            fecharEscolhaDeTroca();
            return;
        }

        if (trocaObrigatoria) {
            // a queda ja custou o turno: a substituicao e' de graca
            batalhaAtual.substituirCaida(nova);
            trocaObrigatoria = false;
            atualizarLog();
            atualizarCena();
            montarBotoesDeGolpe(batalhaAtual.getDoJogador());
            aplicarModo(Modo.EM_BATALHA);
            return;
        }

        // trocar por vontade propria custa o turno: a selvagem ataca de graca
        batalhaAtual.trocarCriatura(nova);
        atualizarLog();
        atualizarCena();
        depoisDoTurno();
    }

    private void fecharEscolhaDeTroca() {
        trocaObrigatoria = false;
        boolean emBatalha = batalhaAtual != null && !batalhaAtual.terminou();
        if (emBatalha) montarBotoesDeGolpe(batalhaAtual.getDoJogador());
        aplicarModo(emBatalha ? Modo.EM_BATALHA : Modo.EXPLORANDO);
    }

    // ------------------------------------------------------------------
    // Ciclo de jogo
    // ------------------------------------------------------------------

    /** Comeco de jornada nova. */
    void iniciar() {
        prepararExploracao("Sua jornada começa! Explore a área para encontrar criaturas "
                + "ou use Viajar para seguir uma rota.");
    }

    /** Retomada de um jogo salvo. */
    void retomar(String mensagem) {
        prepararExploracao(mensagem);
    }

    /**
     * Chamado sempre que esta tela volta a aparecer. Fora de batalha, a equipe
     * pode ter mudado de ordem na tela de Equipe — e com ela quem esta a frente.
     */
    void aoMostrar() {
        Treinador t = janela.getTreinador();
        if (t == null || (batalhaAtual != null && !batalhaAtual.terminou())) return;
        batalhaAtual = null;
        arena.configurar(t.getAtiva(), null);
        atualizarStatus();
    }

    private void prepararExploracao(String mensagemInicial) {
        Treinador t = janela.getTreinador();
        batalhaAtual = null;
        trocaObrigatoria = false;
        linhasExibidas = 0;
        logArea.setText("");
        escrever(mensagemInicial.trim());
        if (t != null) {
            arena.configurar(t.getAtiva(), null);
            mostrarAreaAtual();
            apresentarArea(t.getArea());
        }
        atualizarStatus();
        aplicarModo(Modo.EXPLORANDO);
    }

    /** Mantem o cabecalho sincronizado com o estado real da equipe. */
    private void atualizarStatus() {
        Treinador t = janela.getTreinador();
        if (t == null) return;
        lblStatus.setText(t.getNome()
                + "   ·   Equipe " + t.getEquipe().size() + "/" + Treinador.getTamanhoMaxEquipe()
                + "   ·   De pé " + t.quantasVivas());
    }

    private void explorar() {
        Treinador t = janela.getTreinador();
        Criatura ativa = t.getAtiva();
        if (ativa == null) {
            escrever("Sua equipe não tem nenhuma criatura em condições de batalhar! Use Descansar.");
            return;
        }
        Area area = t.getArea();
        Criatura selvagem = FabricaCriaturas.selvagemAleatorio(area, ativa.getNivel());
        batalhaAtual = new Batalha(ativa, selvagem);
        // a especie conta como "vista" assim que aparece, mesmo que voce fuja
        janela.getBestiario().marcarVista(selvagem);
        // cada batalha sorteia de novo entre os cenarios da area
        arena.mostrar(area, Cenarios.sortear(area, rngVisual));
        arena.configurar(ativa, selvagem);
        montarBotoesDeGolpe(ativa);
        logArea.setText("");
        linhasExibidas = 0;
        atualizarLog();
        aplicarModo(Modo.EM_BATALHA);
    }

    /** Restaura a equipe inteira — o Centro de Cura, em forma de botao. */
    private void descansar() {
        Treinador t = janela.getTreinador();
        if (t == null) return;
        if (!t.precisaDescansar()) {
            escrever("Sua equipe já está descansada.");
            return;
        }
        t.restaurarEquipe();
        batalhaAtual = null;
        arena.configurar(t.getAtiva(), null);
        escrever("Sua equipe foi totalmente restaurada — vida, PP e condições de status.");
        atualizarStatus();
    }

    /** Grava a partida em disco. */
    /** O F5: salvamento rapido direto, so fora de batalha. */
    private void salvamentoRapido() {
        if (janela.getTreinador() == null) return;
        if (modoAtual != Modo.EXPLORANDO && modoAtual != Modo.ESCOLHENDO_ROTA) {
            escrever("Não dá para salvar no meio de uma batalha.");
            return;
        }
        escrever(janela.salvarJogo(SlotDeSave.RAPIDO));
    }

    private void usarGolpe(Golpe golpe) {
        if (batalhaAtual == null || batalhaAtual.terminou()) return;
        batalhaAtual.executarTurno(golpe);
        atualizarLog();
        atualizarCena();
        depoisDoTurno();
    }

    private void capturar() {
        if (batalhaAtual == null || batalhaAtual.terminou()) return;
        Criatura selvagem = batalhaAtual.getSelvagem();

        if (batalhaAtual.tentarCapturar()) {
            atualizarLog();
            janela.getBestiario().marcarCapturada(selvagem);
            if (!janela.getTreinador().adicionarNaEquipe(selvagem)) {
                escrever("(Equipe cheia — a criatura foi registrada no Bestiário, mas não coube no time)");
            }
            atualizarStatus();
            encerrarBatalha();
            return;
        }

        // a tentativa falha custa o turno: a selvagem ataca de graca
        batalhaAtual.turnoApenasDoSelvagem();
        atualizarLog();
        atualizarCena();
        depoisDoTurno();
    }

    private void fugir() {
        escrever("Você fugiu da batalha.");
        encerrarBatalha();
    }

    /**
     * Decide o que acontece depois de um turno: seguir batalhando, comemorar a
     * vitoria, pedir a proxima criatura ou encerrar por derrota.
     */
    private void depoisDoTurno() {
        if (batalhaAtual == null) return;

        if (!batalhaAtual.terminou()) {
            montarBotoesDeGolpe(batalhaAtual.getDoJogador());
            aplicarModo(Modo.EM_BATALHA);
            return;
        }

        if (!batalhaAtual.getSelvagem().estaViva()) {
            escrever("Você venceu a batalha!");
            atualizarStatus();
            aplicarModo(Modo.EXPLORANDO);
            return;
        }

        if (batalhaAtual.aCriaturaDoJogadorCaiu()) {
            Treinador t = janela.getTreinador();
            if (t.temReservaPara(batalhaAtual.getDoJogador())) {
                abrirEscolhaDeTroca(true);
            } else {
                escrever("Toda a sua equipe foi derrotada! Use Descansar para se recuperar.");
                encerrarBatalha();
            }
            return;
        }
        aplicarModo(Modo.EXPLORANDO);
    }

    /** Redesenha a arena e o cabecalho a partir do estado real das criaturas. */
    private void atualizarCena() {
        if (batalhaAtual == null) return;
        arena.configurar(batalhaAtual.getDoJogador(), batalhaAtual.getSelvagem());
        atualizarStatus();   // "De pé" muda quando alguem cai
    }

    /** Sai da batalha (captura, fuga ou derrota) e volta ao estado de exploracao. */
    private void encerrarBatalha() {
        batalhaAtual = null;
        trocaObrigatoria = false;
        arena.configurar(janela.getTreinador().getAtiva(), null);
        atualizarStatus();
        aplicarModo(Modo.EXPLORANDO);
    }

    /** Imprime todas as linhas do log da batalha que ainda nao foram exibidas. */
    private void atualizarLog() {
        if (batalhaAtual == null) return;
        List<String> log = batalhaAtual.getLog();
        while (linhasExibidas < log.size()) {
            logArea.append(log.get(linhasExibidas) + "\n");
            linhasExibidas++;
        }
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /** Escreve uma linha da interface (fora do log da batalha) e rola para o fim. */
    private void escrever(String linha) {
        logArea.append(linha + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void aplicarModo(Modo novo) {
        modoAtual = novo;
        cartas.show(painelAcoes, novo.name());
    }
}
