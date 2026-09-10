package arenaelemental.treinador;

import arenaelemental.modelo.Criatura;
import arenaelemental.mundo.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O treinador, a equipe dele e a area do mapa onde ele esta.
 *
 * <p>A criatura ativa e' uma escolha do jogador, guardada em
 * {@link #getIndiceAtiva()}. Se ela cair, {@link #getAtiva()} escorrega
 * sozinha para a primeira que ainda estiver de pe, para que o jogo nunca fique
 * sem criatura ativa tendo reserva disponivel.
 */
public class Treinador {

    private static final int TAMANHO_MAX_EQUIPE = 6;

    private final String nome;
    private final List<Criatura> equipe = new ArrayList<>();
    private int indiceAtiva;
    private Area area = Area.INICIAL;

    public Treinador(String nome) { this.nome = nome; }

    public String getNome() { return nome; }

    // ------------------------------------------------------------------
    // Mapa
    // ------------------------------------------------------------------

    /** A area onde o treinador esta. Toda jornada comeca em {@link Area#INICIAL}. */
    public Area getArea() { return area; }

    /**
     * Viaja para outra area, se houver rota direta ate ela.
     *
     * @return se a viagem aconteceu
     */
    public boolean viajarPara(Area destino) {
        if (!area.temRotaPara(destino)) return false;
        area = destino;
        return true;
    }

    /**
     * Coloca o treinador numa area sem exigir rota — so para reconstruir um
     * jogo salvo. {@code null} vira a area inicial.
     */
    public void restaurarArea(Area area) {
        this.area = area == null ? Area.INICIAL : area;
    }

    /** A equipe, so para leitura: use {@link #adicionarNaEquipe(Criatura)} para mexer nela. */
    public List<Criatura> getEquipe() { return Collections.unmodifiableList(equipe); }

    public static int getTamanhoMaxEquipe() { return TAMANHO_MAX_EQUIPE; }

    public boolean adicionarNaEquipe(Criatura c) {
        if (equipe.size() >= TAMANHO_MAX_EQUIPE) return false;
        equipe.add(c);
        return true;
    }

    // ------------------------------------------------------------------
    // Criatura ativa e troca
    // ------------------------------------------------------------------

    /** A posicao da criatura ativa na equipe. */
    public int getIndiceAtiva() { return indiceAtiva; }

    /**
     * A criatura que esta em campo: a escolhida, se ainda estiver viva, ou a
     * primeira viva da equipe. {@code null} com a equipe toda desmaiada.
     */
    public Criatura getAtiva() {
        if (indiceAtiva >= 0 && indiceAtiva < equipe.size() && equipe.get(indiceAtiva).estaViva()) {
            return equipe.get(indiceAtiva);
        }
        for (int i = 0; i < equipe.size(); i++) {
            if (equipe.get(i).estaViva()) {
                indiceAtiva = i;
                return equipe.get(i);
            }
        }
        return null;
    }

    /**
     * Se da para colocar em campo a criatura desta posicao: ela existe e esta
     * de pe.
     *
     * <p>Escolher a posicao que ja e' a ativa e' permitido e nao faz nada de
     * errado. A regra tem de ser essa porque quem esta em campo durante uma
     * batalha e' a criatura da {@code Batalha}, que pode divergir do indice
     * guardado aqui — e' o que acontece logo depois de uma queda, quando
     * {@link #getAtiva()} ja escorregou para a proxima viva. Recusar a posicao
     * ativa deixaria justamente essa criatura impossivel de escolher.
     */
    public boolean podeTrocarPara(int indice) {
        return indice >= 0 && indice < equipe.size() && equipe.get(indice).estaViva();
    }

    /**
     * Coloca em campo a criatura desta posicao.
     *
     * @return a criatura que entrou, ou {@code null} se a troca nao era possivel
     */
    public Criatura trocarPara(int indice) {
        if (!podeTrocarPara(indice)) return null;
        indiceAtiva = indice;
        return equipe.get(indice);
    }

    // ------------------------------------------------------------------
    // Ordem da equipe
    // ------------------------------------------------------------------

    /**
     * Troca duas criaturas de posicao. A escolha de quem esta ativa acompanha a
     * criatura, e nao a posicao.
     *
     * @return se as duas posicoes existiam
     */
    public boolean trocarDePosicao(int i, int j) {
        if (i < 0 || j < 0 || i >= equipe.size() || j >= equipe.size()) return false;
        Collections.swap(equipe, i, j);
        if (indiceAtiva == i) indiceAtiva = j;
        else if (indiceAtiva == j) indiceAtiva = i;
        return true;
    }

    /**
     * Leva a criatura desta posicao para a frente da equipe e a torna a ativa.
     *
     * <p>E' a primeira posicao, e nao so o indice ativo, que conta: o descanso
     * devolve a lideranca a primeira da equipe, e o jogo salvo grava a ordem.
     *
     * @return se a posicao existia
     */
    public boolean tornarLider(int indice) {
        if (indice < 0 || indice >= equipe.size()) return false;
        equipe.add(0, equipe.remove(indice));
        indiceAtiva = 0;
        return true;
    }

    /** As criaturas vivas que estao fora de campo — as candidatas a uma troca. */
    public List<Criatura> reservasDisponiveis() { return reservasPara(getAtiva()); }

    /** Se ha alguma criatura viva no banco para entrar no lugar da ativa. */
    public boolean temReservaDisponivel() { return !reservasDisponiveis().isEmpty(); }

    /**
     * As criaturas vivas que podem entrar no lugar desta.
     *
     * <p>Durante a batalha e' esta a pergunta certa, e nao
     * {@link #reservasDisponiveis()}: quando a criatura em campo cai,
     * {@link #getAtiva()} ja escorregou para a proxima viva, e perguntar pelas
     * reservas "da ativa" deixaria justamente essa de fora — com duas criaturas
     * na equipe, o jogo acharia que nao sobrou ninguem.
     */
    public List<Criatura> reservasPara(Criatura emCampo) {
        List<Criatura> reservas = new ArrayList<>();
        for (Criatura c : equipe) {
            if (c.estaViva() && c != emCampo) reservas.add(c);
        }
        return Collections.unmodifiableList(reservas);
    }

    /** Se ha alguma criatura viva para entrar no lugar desta. */
    public boolean temReservaPara(Criatura emCampo) { return !reservasPara(emCampo).isEmpty(); }

    /** Quantas criaturas da equipe ainda estao de pe. */
    public int quantasVivas() {
        int n = 0;
        for (Criatura c : equipe) if (c.estaViva()) n++;
        return n;
    }

    // ------------------------------------------------------------------
    // Descanso
    // ------------------------------------------------------------------

    /**
     * Restaura vida, PP e condicao de status de toda a equipe, inclusive das
     * criaturas desmaiadas, e devolve a lideranca a primeira da equipe.
     */
    public void restaurarEquipe() {
        for (Criatura c : equipe) c.restaurarTudo();
        indiceAtiva = 0;
    }

    /** Se ha alguma criatura machucada, sem PP ou com condicao de status. */
    public boolean precisaDescansar() {
        for (Criatura c : equipe) {
            if (c.getVidaAtual() < c.getHpMaximo()) return true;
            if (c.temCondicao()) return true;
            if (!estaComOsPpCheios(c)) return true;
        }
        return false;
    }

    private static boolean estaComOsPpCheios(Criatura c) {
        for (arenaelemental.modelo.Golpe g : c.getGolpes()) {
            if (c.getPp(g) < c.getPpMaximo(g)) return false;
        }
        return true;
    }
}
