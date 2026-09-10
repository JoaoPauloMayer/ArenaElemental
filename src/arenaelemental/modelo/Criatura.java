package arenaelemental.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Uma criatura individual: especie + nivel + experiencia + status calculados +
 * o estado que ela carrega entre batalhas (PP dos golpes e condicao de status).
 *
 * <p>Tudo o que e' igual em todas as criaturas da mesma especie (tipo, status
 * base, curva de experiencia, tabela de aprendizado, aparencia) vive na
 * {@link EspecieCriatura}; aqui ficam apenas os dados deste individuo.
 *
 * <p>Os seis status (HP, Ataque, Defesa, Ataque Especial, Defesa Especial e
 * Velocidade) nao sao guardados a mao: derivam dos status base da especie e do
 * nivel, pelas mesmas formulas dos jogos originais.
 *
 * <pre>
 *   HP     = floor((2 * Base + IV + floor(EV/4)) * Nivel / 100) + Nivel + 10
 *   Outros = floor((2 * Base + IV + floor(EV/4)) * Nivel / 100) + 5
 * </pre>
 *
 * <p>IVs, EVs e naturezas ainda nao sao mecanicas do jogo: os IVs ficam fixos em
 * {@link #IV_PADRAO} e os EVs em zero. Os campos existem para que essas
 * mecanicas possam entrar depois sem mexer na formula.
 */
public abstract class Criatura {

    /** Nivel maximo, como nos jogos originais. */
    public static final int NIVEL_MAXIMO = 100;

    /** IV fixo usado por todas as criaturas enquanto a mecanica nao existir. */
    public static final int IV_PADRAO = 31;

    /** Quantidade maxima de golpes que uma criatura carrega. */
    public static final int MAX_GOLPES = 4;

    private final EspecieCriatura especie;
    protected final String nome;
    private final List<Golpe> golpes = new ArrayList<>();
    private final Map<Golpe, Integer> pp = new LinkedHashMap<>();
    private final List<Golpe> aprendidosAgora = new ArrayList<>();

    private int nivel;
    private int expTotal;

    // status calculados a partir de base + nivel
    private int hpMaximo, ataque, defesa, ataqueEsp, defesaEsp, velocidade;
    private int vidaAtual;

    private CondicaoStatus condicao = CondicaoStatus.NENHUMA;
    private int turnosDeSono;

    protected Criatura(EspecieCriatura especie, String nome, int nivel) {
        this.especie = especie;
        this.nome = nome;
        this.nivel = Math.max(1, Math.min(NIVEL_MAXIMO, nivel));
        this.expTotal = especie.getGrupoExperiencia().expTotalParaNivel(this.nivel);
        recalcularStatus();
        this.vidaAtual = hpMaximo;
        aprenderGolpesAte(this.nivel, 1);
        aprendidosAgora.clear();
    }

    // ------------------------------------------------------------------
    // Especie
    // ------------------------------------------------------------------

    /** A ficha da especie a que esta criatura pertence. */
    public EspecieCriatura getEspecie() { return especie; }

    public String getNome() { return nome; }
    /** O tipo principal. Para a lista completa, use {@link #getTipos()}. */
    public TipoElemental getTipo() { return especie.getTipo(); }

    /** O segundo tipo, ou {@code null} se a especie tiver um tipo so. */
    public TipoElemental getTipoSecundario() { return especie.getTipoSecundario(); }

    /** Os tipos da criatura, o principal primeiro: um ou dois. */
    public List<TipoElemental> getTipos() { return especie.getTipos(); }

    /** Se este e' um dos tipos da criatura. */
    public boolean temTipo(TipoElemental t) { return especie.temTipo(t); }

    public EstatisticasBase getEstatisticasBase() { return especie.getEstatisticasBase(); }
    public GrupoExperiencia getGrupoExperiencia() { return especie.getGrupoExperiencia(); }
    public Aparencia getAparencia() { return especie.getAparencia(); }

    /** Descricao da habilidade da especie. */
    public String descricaoHabilidade() { return especie.getDescricaoHabilidade(); }

    // ------------------------------------------------------------------
    // Status
    // ------------------------------------------------------------------

    /** Recalcula os seis status a partir dos status base e do nivel atual. */
    public final void recalcularStatus() {
        EstatisticasBase base = especie.getEstatisticasBase();
        hpMaximo   = calcularHp(base.getHp());
        ataque     = calcularOutro(base.getAtaque());
        defesa     = calcularOutro(base.getDefesa());
        ataqueEsp  = calcularOutro(base.getAtaqueEsp());
        defesaEsp  = calcularOutro(base.getDefesaEsp());
        velocidade = calcularOutro(base.getVelocidade());
    }

    private int calcularHp(int statBase) {
        return (int) Math.floor((2.0 * statBase + IV_PADRAO) * nivel / 100.0) + nivel + 10;
    }

    private int calcularOutro(int statBase) {
        return (int) Math.floor((2.0 * statBase + IV_PADRAO) * nivel / 100.0) + 5;
    }

    public int getNivel() { return nivel; }
    public int getHpMaximo() { return hpMaximo; }
    public int getAtaque() { return ataque; }
    public int getDefesa() { return defesa; }
    public int getAtaqueEsp() { return ataqueEsp; }
    public int getDefesaEsp() { return defesaEsp; }
    public int getVelocidade() { return velocidade; }
    public int getVidaAtual() { return vidaAtual; }

    /**
     * A Velocidade que vale na hora de decidir quem ataca primeiro: a paralisia
     * corta pela metade, como nos jogos originais.
     */
    public int getVelocidadeEfetiva() {
        return condicao == CondicaoStatus.PARALISIA ? velocidade / 2 : velocidade;
    }

    public boolean estaViva() { return vidaAtual > 0; }
    public double percentualVida() { return hpMaximo == 0 ? 0 : (double) vidaAtual / hpMaximo; }

    /** Tira vida. Uma criatura que chega a zero perde a condicao de status. */
    public void receberDano(int dano) {
        vidaAtual = Math.max(0, vidaAtual - dano);
        if (vidaAtual == 0) curarCondicao();
    }

    public void curar(int quantidade) { vidaAtual = Math.min(hpMaximo, vidaAtual + quantidade); }

    /** Restaura vida, PP e condicao de status — o que o botao Descansar faz. */
    public void restaurarTudo() {
        vidaAtual = hpMaximo;
        restaurarPp();
        curarCondicao();
    }

    /** Ataque efetivo a usar contra um golpe desta categoria. */
    public int ataqueEfetivo(CategoriaGolpe categoria) {
        return categoria == CategoriaGolpe.ESPECIAL ? ataqueEsp : ataque;
    }

    /** Defesa efetiva a usar contra um golpe desta categoria. */
    public int defesaEfetiva(CategoriaGolpe categoria) {
        return categoria == CategoriaGolpe.ESPECIAL ? defesaEsp : defesa;
    }

    // ------------------------------------------------------------------
    // Condicao de status
    // ------------------------------------------------------------------

    public CondicaoStatus getCondicao() { return condicao; }

    public boolean temCondicao() { return condicao != CondicaoStatus.NENHUMA; }

    /** Quantos turnos ainda faltam para acordar (0 quando nao esta dormindo). */
    public int getTurnosDeSono() { return turnosDeSono; }

    /**
     * Se a criatura pode receber esta condicao agora.
     *
     * <p>Regras dos jogos originais: so uma condicao nao volatil por vez, uma
     * criatura do tipo Fogo nao se queima e uma do tipo Gelo nao congela —
     * valendo para qualquer um dos tipos de quem tem dois.
     */
    public boolean podeReceber(CondicaoStatus nova) {
        if (nova == null || nova == CondicaoStatus.NENHUMA) return false;
        if (!estaViva() || temCondicao()) return false;
        if (nova == CondicaoStatus.QUEIMADURA && temTipo(TipoElemental.FOGO)) return false;
        return !(nova == CondicaoStatus.CONGELAMENTO && temTipo(TipoElemental.GELO));
    }

    /**
     * Aplica a condicao, se ela puder pegar. O sono ja sorteia aqui quantos
     * turnos vai durar.
     *
     * @return {@code true} se a condicao pegou
     */
    public boolean aplicarCondicao(CondicaoStatus nova, Random rng) {
        if (!podeReceber(nova)) return false;
        condicao = nova;
        turnosDeSono = nova == CondicaoStatus.SONO
                ? CondicaoStatus.SONO_MINIMO + rng.nextInt(CondicaoStatus.SONO_MAXIMO - CondicaoStatus.SONO_MINIMO + 1)
                : 0;
        return true;
    }

    /** Tira a condicao de status, seja qual for. */
    public void curarCondicao() {
        condicao = CondicaoStatus.NENHUMA;
        turnosDeSono = 0;
    }

    /**
     * Desconta um turno de sono.
     *
     * @return {@code true} se a criatura acordou agora
     */
    public boolean descontarTurnoDeSono() {
        if (condicao != CondicaoStatus.SONO) return false;
        turnosDeSono--;
        if (turnosDeSono > 0) return false;
        curarCondicao();
        return true;
    }

    /** Dano que a condicao cobra no fim do turno (0 quando nao ha). */
    public int danoResidualDaCondicao() { return condicao.danoResidual(this); }

    // ------------------------------------------------------------------
    // Golpes e PP
    // ------------------------------------------------------------------

    /** Ensina um golpe, respeitando o limite de {@link #MAX_GOLPES}. */
    public boolean aprenderGolpe(Golpe g) {
        if (golpes.size() >= MAX_GOLPES || golpes.contains(g)) return false;
        golpes.add(g);
        pp.put(g, g.getPpMaximo());
        return true;
    }

    /**
     * Aprende os golpes da tabela cujo nivel esta na faixa informada. Com o
     * repertorio cheio, o golpe novo entra no lugar do mais antigo — e' o
     * comportamento dos jogos, sem a tela de "esquecer qual golpe?".
     */
    private void aprenderGolpesAte(int nivelAte, int nivelDe) {
        for (AprendizadoGolpe entrada : especie.getAprendizado()) {
            if (entrada.getNivel() < nivelDe || entrada.getNivel() > nivelAte) continue;
            Golpe g = entrada.getGolpe();
            if (golpes.contains(g)) continue;
            if (golpes.size() >= MAX_GOLPES) pp.remove(golpes.remove(0));
            golpes.add(g);
            pp.put(g, g.getPpMaximo());
            aprendidosAgora.add(g);
        }
    }

    public List<Golpe> getGolpes() { return Collections.unmodifiableList(golpes); }

    /** Golpes aprendidos na ultima chamada de {@link #ganharExp(int)}. */
    public List<Golpe> getGolpesAprendidosAgora() {
        return Collections.unmodifiableList(aprendidosAgora);
    }

    /** Tabela de aprendizado da especie: em que nivel cada golpe entra. */
    public List<AprendizadoGolpe> getTabelaDeAprendizado() { return especie.getAprendizado(); }

    /** PP que ainda restam deste golpe. */
    public int getPp(Golpe g) {
        if (g == null) return 0;
        if (g.isSemLimiteDePp()) return g.getPpMaximo();
        return pp.getOrDefault(g, 0);
    }

    public int getPpMaximo(Golpe g) { return g == null ? 0 : g.getPpMaximo(); }

    /** Se o golpe ainda pode ser usado. */
    public boolean temPp(Golpe g) { return g != null && (g.isSemLimiteDePp() || getPp(g) > 0); }

    /**
     * Gasta um PP do golpe.
     *
     * @return {@code false} se nao havia PP para gastar
     */
    public boolean gastarPp(Golpe g) {
        if (g == null) return false;
        if (g.isSemLimiteDePp()) return true;
        int restante = getPp(g);
        if (restante <= 0) return false;
        pp.put(g, restante - 1);
        return true;
    }

    /** Devolve todos os PP de todos os golpes. */
    public void restaurarPp() {
        for (Golpe g : golpes) pp.put(g, g.getPpMaximo());
    }

    /** Define os PP de um golpe — usado pela persistencia. */
    public void definirPp(Golpe g, int valor) {
        if (g == null || !golpes.contains(g)) return;
        pp.put(g, Math.max(0, Math.min(g.getPpMaximo(), valor)));
    }

    /** Os golpes que ainda tem PP para usar. */
    public List<Golpe> getGolpesUtilizaveis() {
        List<Golpe> disponiveis = new ArrayList<>();
        for (Golpe g : golpes) if (temPp(g)) disponiveis.add(g);
        return Collections.unmodifiableList(disponiveis);
    }

    /** Se sobrou PP em algum golpe. */
    public boolean temAlgumGolpeUtilizavel() { return !getGolpesUtilizaveis().isEmpty(); }

    /**
     * Os golpes que a criatura pode usar neste turno: os que ainda tem PP ou,
     * se nao sobrou nenhum, apenas o {@link Golpes#ESFORCO}.
     */
    public List<Golpe> getGolpesDisponiveis() {
        List<Golpe> disponiveis = getGolpesUtilizaveis();
        return disponiveis.isEmpty() ? Collections.singletonList(Golpes.ESFORCO) : disponiveis;
    }

    // ------------------------------------------------------------------
    // Experiencia
    // ------------------------------------------------------------------

    public int getExpTotal() { return expTotal; }

    /** Experiencia acumulada exigida para o nivel atual. */
    public int getExpInicioDoNivel() { return especie.getGrupoExperiencia().expTotalParaNivel(nivel); }

    /** Experiencia acumulada exigida para o proximo nivel. */
    public int getExpProximoNivel() {
        return nivel >= NIVEL_MAXIMO
                ? getExpInicioDoNivel()
                : especie.getGrupoExperiencia().expTotalParaNivel(nivel + 1);
    }

    /** Quanto ainda falta, em pontos, para subir de nivel (0 no nivel 100). */
    public int getExpFaltando() {
        return nivel >= NIVEL_MAXIMO ? 0 : Math.max(0, getExpProximoNivel() - expTotal);
    }

    /** Progresso dentro do nivel atual, de 0.0 a 1.0 — usado pela barra de EXP. */
    public double percentualExp() {
        if (nivel >= NIVEL_MAXIMO) return 1.0;
        int inicio = getExpInicioDoNivel();
        int fim = getExpProximoNivel();
        if (fim <= inicio) return 1.0;
        return Math.max(0.0, Math.min(1.0, (double) (expTotal - inicio) / (fim - inicio)));
    }

    /**
     * Adiciona experiencia e sobe de nivel quantas vezes for necessario.
     *
     * <p>Ao subir de nivel o HP maximo cresce e a vida atual sobe junto, na
     * mesma quantidade, como nos jogos originais. A condicao de status <b>nao</b>
     * e' curada: para isso existe o Descansar.
     *
     * @return quantos niveis foram ganhos
     */
    public int ganharExp(int pontos) {
        aprendidosAgora.clear();
        if (pontos <= 0 || nivel >= NIVEL_MAXIMO) return 0;
        expTotal += pontos;

        GrupoExperiencia curva = especie.getGrupoExperiencia();
        int niveisGanhos = 0;
        while (nivel < NIVEL_MAXIMO && expTotal >= curva.expTotalParaNivel(nivel + 1)) {
            int hpAntes = hpMaximo;
            nivel++;
            recalcularStatus();
            vidaAtual = Math.min(hpMaximo, vidaAtual + (hpMaximo - hpAntes));
            aprenderGolpesAte(nivel, nivel);
            niveisGanhos++;
        }
        // no nivel maximo a experiencia para de acumular
        if (nivel >= NIVEL_MAXIMO) expTotal = curva.expTotalParaNivel(NIVEL_MAXIMO);
        return niveisGanhos;
    }

    // ------------------------------------------------------------------
    // Persistencia
    // ------------------------------------------------------------------

    /**
     * Reaplica um estado vindo de um jogo salvo, por cima do estado que o
     * construtor montou.
     *
     * <p>Usado apenas por {@code arenaelemental.persistencia}. Os valores sao
     * saneados aqui — um arquivo editado a mao nao consegue produzir uma
     * criatura invalida, so uma criatura diferente da que ele pedia. Os PP
     * voltam cheios; quem tiver os PP salvos aplica-os depois com
     * {@link #definirPp(Golpe, int)}.
     *
     * @param golpesSalvos repertorio gravado; se vier vazio, o repertorio
     *                     derivado do nivel e' mantido
     */
    public void restaurarEstado(int nivel, int expTotal, int vidaAtual, List<Golpe> golpesSalvos) {
        this.nivel = Math.max(1, Math.min(NIVEL_MAXIMO, nivel));
        recalcularStatus();

        GrupoExperiencia curva = especie.getGrupoExperiencia();
        int minimo = curva.expTotalParaNivel(this.nivel);
        int maximo = this.nivel >= NIVEL_MAXIMO ? minimo : curva.expTotalParaNivel(this.nivel + 1) - 1;
        this.expTotal = Math.max(minimo, Math.min(maximo, expTotal));

        this.vidaAtual = Math.max(0, Math.min(hpMaximo, vidaAtual));

        if (golpesSalvos != null && !golpesSalvos.isEmpty()) {
            golpes.clear();
            pp.clear();
            for (Golpe g : golpesSalvos) {
                if (g != null && !golpes.contains(g) && golpes.size() < MAX_GOLPES) {
                    golpes.add(g);
                    pp.put(g, g.getPpMaximo());
                }
            }
            if (golpes.isEmpty()) aprenderGolpesAte(this.nivel, 1);
        }
        aprendidosAgora.clear();
    }

    /** Reaplica a condicao de status vinda do jogo salvo. */
    public void definirCondicao(CondicaoStatus condicao, int turnosDeSono) {
        if (condicao == null || condicao == CondicaoStatus.NENHUMA || !estaViva()) {
            curarCondicao();
            return;
        }
        this.condicao = condicao;
        this.turnosDeSono = condicao == CondicaoStatus.SONO
                ? Math.max(1, Math.min(CondicaoStatus.SONO_MAXIMO, turnosDeSono))
                : 0;
    }

    // ------------------------------------------------------------------
    // Habilidade da especie (entra como o multiplicador "Other" da formula)
    // ------------------------------------------------------------------

    /**
     * Multiplicador da habilidade da especie, aplicado no lugar do termo
     * {@code Other} da formula de dano. Retorna 1.0 quando nao se aplica.
     */
    public double multiplicadorHabilidade(Criatura alvo, Golpe golpe) { return 1.0; }

    /** Efeito disparado depois que o golpe causa dano (dreno, recuo, etc). */
    public void efeitoPosAtaque(Criatura alvo, int danoAplicado) { /* no-op por padrao */ }

    @Override
    public String toString() {
        return nome + " Nv." + nivel + " (" + especie.nomeDosTipos() + ") "
                + vidaAtual + "/" + hpMaximo + " HP"
                + (temCondicao() ? " [" + condicao.sigla() + "]" : "");
    }
}
