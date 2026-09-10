package arenaelemental.batalha;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Experiencia;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.Golpes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Uma batalha de turnos entre a criatura do jogador e uma selvagem.
 *
 * <p>O turno acontece nesta ordem:
 *
 * <ol>
 *   <li>a ordem e' decidida pela Velocidade <em>efetiva</em> — a paralisia corta
 *       pela metade —, com empate desfeito por sorteio;</li>
 *   <li>cada um, na sua vez, checa se consegue agir (sono, congelamento,
 *       paralisia), gasta o PP, sorteia a precisao e so entao causa dano;</li>
 *   <li>no fim do turno, queimadura e veneno cobram a parte deles.</li>
 * </ol>
 *
 * <p>Se o primeiro golpe derrubar o alvo, o segundo nao acontece.
 *
 * <p>A criatura do jogador nao e' final: {@link #trocarCriatura(Criatura)} a
 * substitui gastando o turno, e {@link #substituirCaida(Criatura)} coloca a
 * proxima em campo de graca depois de uma queda.
 */
public class Batalha {

    private Criatura doJogador;
    private final Criatura selvagem;
    private final List<String> log = new ArrayList<>();
    private final CalculadoraDano calculadora;
    private final Captura captura;
    private final Random rng;

    private int expGanhaNaBatalha;
    private int niveisGanhosNaBatalha;
    private boolean capturada;

    public Batalha(Criatura doJogador, Criatura selvagem) {
        this(doJogador, selvagem, new Random());
    }

    /**
     * Batalha com gerador proprio. Todo sorteio da batalha — dano, ordem do
     * turno, precisao, efeitos de status, golpe da selvagem e captura — sai
     * deste mesmo {@link Random}, de modo que uma semente fixa reproduz a
     * batalha inteira.
     */
    public Batalha(Criatura doJogador, Criatura selvagem, Random rng) {
        this.doJogador = doJogador;
        this.selvagem = selvagem;
        this.rng = rng;
        this.calculadora = new CalculadoraDano(rng);
        this.captura = new Captura(rng);
        log.add("Um " + selvagem.getNome() + " selvagem (Nv." + selvagem.getNivel() + ") apareceu!");
    }

    public Criatura getDoJogador() { return doJogador; }
    public Criatura getSelvagem() { return selvagem; }
    public List<String> getLog() { return log; }

    /** Experiencia que o jogador ganhou nesta batalha (0 enquanto nao vencer). */
    public int getExpGanhaNaBatalha() { return expGanhaNaBatalha; }

    /** Quantos niveis a criatura do jogador subiu nesta batalha. */
    public int getNiveisGanhosNaBatalha() { return niveisGanhosNaBatalha; }

    /** Se a selvagem foi capturada nesta batalha. */
    public boolean foiCapturada() { return capturada; }

    public boolean terminou() { return capturada || !doJogador.estaViva() || !selvagem.estaViva(); }

    /** Se a batalha parou porque a criatura do jogador caiu, e nao por vitoria. */
    public boolean aCriaturaDoJogadorCaiu() { return !capturada && !doJogador.estaViva(); }

    /** Os golpes que a criatura do jogador pode usar agora (PP e Esforço considerados). */
    public List<Golpe> golpesDoJogador() { return doJogador.getGolpesDisponiveis(); }

    /** Golpe aleatorio entre os que a selvagem ainda pode usar. */
    public Golpe golpeDoSelvagem() {
        List<Golpe> disponiveis = selvagem.getGolpesDisponiveis();
        return disponiveis.get(rng.nextInt(disponiveis.size()));
    }

    // ------------------------------------------------------------------
    // Turno
    // ------------------------------------------------------------------

    /**
     * Executa um turno completo: o jogador usa o golpe escolhido e a selvagem
     * responde com um golpe aleatorio, na ordem ditada pela Velocidade efetiva.
     */
    public void executarTurno(Golpe golpeDoJogador) {
        if (terminou()) return;
        Golpe golpeSelvagem = golpeDoSelvagem();

        if (jogadorVaiPrimeiro()) {
            atacar(doJogador, selvagem, golpeDoJogador);
            if (selvagem.estaViva() && doJogador.estaViva()) atacar(selvagem, doJogador, golpeSelvagem);
        } else {
            atacar(selvagem, doJogador, golpeSelvagem);
            if (doJogador.estaViva() && selvagem.estaViva()) atacar(doJogador, selvagem, golpeDoJogador);
        }
        fecharTurno();
    }

    /**
     * Turno em que o jogador nao ataca — usado quando ele tenta capturar e
     * falha, ou quando troca de criatura: a selvagem ganha um golpe de graca.
     */
    public void turnoApenasDoSelvagem() {
        if (terminou()) return;
        atacar(selvagem, doJogador, golpeDoSelvagem());
        fecharTurno();
    }

    private boolean jogadorVaiPrimeiro() {
        int velJogador = doJogador.getVelocidadeEfetiva();
        int velSelvagem = selvagem.getVelocidadeEfetiva();
        if (velJogador != velSelvagem) return velJogador > velSelvagem;
        return rng.nextBoolean(); // empate de Velocidade e' decidido no sorteio
    }

    private void atacar(Criatura atacante, Criatura alvo, Golpe golpe) {
        if (!atacante.estaViva() || !alvo.estaViva()) return;
        if (!podeAgir(atacante)) return;

        // sem PP no golpe escolhido, entra o Esforço
        if (!atacante.temPp(golpe)) golpe = Golpes.ESFORCO;
        atacante.gastarPp(golpe);

        log.add(atacante.getNome() + " usou " + golpe.getNome() + "!");

        if (!golpe.acerta(rng)) {
            log.add(atacante.getNome() + " errou o golpe!");
            return;
        }

        int danoAplicado = 0;
        if (golpe.causaDano()) {
            ResultadoDano r = calculadora.calcular(atacante, alvo, golpe);
            if (r.semEfeito()) {
                log.add("Não afeta " + alvo.getNome() + "...");
                return;
            }

            danoAplicado = r.getDano();
            alvo.receberDano(danoAplicado);
            if (r.foiCritico()) log.add("Foi um acerto crítico!");
            String efetividade = r.textoEfetividade();
            if (efetividade != null) log.add(efetividade);
            log.add(alvo.getNome() + " levou " + danoAplicado + " de dano.");

            int vidaAntes = atacante.getVidaAtual();
            atacante.efeitoPosAtaque(alvo, danoAplicado);
            int curado = atacante.getVidaAtual() - vidaAntes;
            if (curado > 0) log.add(atacante.getNome() + " recuperou " + curado + " de vida.");

            aplicarRecuo(atacante, golpe);
        }

        aplicarEfeitoStatus(atacante, alvo, golpe, danoAplicado);

        if (!alvo.estaViva()) log.add(alvo.getNome() + " foi derrotado!");
        if (!atacante.estaViva()) log.add(atacante.getNome() + " foi derrotado!");
    }

    /**
     * Sono, congelamento e paralisia agem antes do golpe.
     *
     * @return {@code false} se a criatura perdeu o turno
     */
    private boolean podeAgir(Criatura c) {
        switch (c.getCondicao()) {
            case SONO:
                if (c.descontarTurnoDeSono()) {
                    log.add(c.getNome() + " acordou!");
                    return true;
                }
                log.add(c.getNome() + " está dormindo...");
                return false;

            case CONGELAMENTO:
                if (rng.nextInt(100) < CondicaoStatus.CHANCE_DESCONGELAR) {
                    c.curarCondicao();
                    log.add(c.getNome() + " descongelou!");
                    return true;
                }
                log.add(c.getNome() + " está congelado e não consegue se mover.");
                return false;

            case PARALISIA:
                if (rng.nextInt(100) < CondicaoStatus.CHANCE_PARALISIA_TRAVAR) {
                    log.add(c.getNome() + " está paralisado e não conseguiu se mover!");
                    return false;
                }
                return true;

            default:
                return true;
        }
    }

    /** O recuo do Esforço: o atacante paga uma fatia do proprio HP maximo. */
    private void aplicarRecuo(Criatura atacante, Golpe golpe) {
        int percentual = golpe.getRecuoPercentualDoHpMaximo();
        if (percentual <= 0) return;
        int recuo = Math.max(1, atacante.getHpMaximo() * percentual / 100);
        atacante.receberDano(recuo);
        log.add(atacante.getNome() + " se machucou com o esforço e perdeu " + recuo + " de vida.");
    }

    /** Sorteia e aplica a condicao de status que o golpe pode deixar no alvo. */
    private void aplicarEfeitoStatus(Criatura atacante, Criatura alvo, Golpe golpe, int danoAplicado) {
        if (!golpe.temEfeitoStatus() || !alvo.estaViva()) return;
        // um golpe de dano so passa o efeito se tiver efetivamente acertado
        if (golpe.causaDano() && danoAplicado <= 0) return;
        if (!golpe.efeitoPega(rng)) return;

        CondicaoStatus condicao = golpe.getEfeitoStatus();
        if (alvo.aplicarCondicao(condicao, rng)) {
            log.add(condicao.mensagemAoPegar(alvo));
        } else if (alvo.temCondicao()) {
            log.add(alvo.getNome() + " já está " + alvo.getCondicao().nomeExibicao().toLowerCase() + ".");
        }
    }

    /** Fim do turno: queimadura e veneno cobram a parte deles. */
    private void fecharTurno() {
        cobrarDanoResidual(doJogador);
        cobrarDanoResidual(selvagem);
        registrarDesfecho();
    }

    private void cobrarDanoResidual(Criatura c) {
        if (!c.estaViva() || !c.getCondicao().temDanoResidual()) return;
        int dano = c.danoResidualDaCondicao();
        CondicaoStatus condicao = c.getCondicao();
        c.receberDano(dano);
        log.add(condicao.mensagemDoDanoResidual(c, dano));
        if (!c.estaViva()) log.add(c.getNome() + " foi derrotado!");
    }

    // ------------------------------------------------------------------
    // Troca de criatura
    // ------------------------------------------------------------------

    /**
     * Troca a criatura em campo por escolha do jogador. Como nos jogos
     * originais, a troca <b>gasta o turno</b>: a selvagem ataca de graca.
     *
     * @return {@code true} se a troca aconteceu
     */
    public boolean trocarCriatura(Criatura nova) {
        if (terminou() || !podeEntrar(nova)) return false;
        anunciarTroca(nova);
        doJogador = nova;
        turnoApenasDoSelvagem();
        return true;
    }

    /**
     * Coloca a proxima criatura em campo depois que a anterior caiu. Nao gasta
     * turno: a queda ja custou o dela.
     *
     * @return {@code true} se a substituicao aconteceu
     */
    public boolean substituirCaida(Criatura nova) {
        if (capturada || !selvagem.estaViva()) return false;
        if (doJogador.estaViva() || !podeEntrar(nova)) return false;
        anunciarTroca(nova);
        doJogador = nova;
        return true;
    }

    private boolean podeEntrar(Criatura nova) {
        return nova != null && nova != doJogador && nova.estaViva();
    }

    private void anunciarTroca(Criatura nova) {
        if (doJogador.estaViva()) log.add("Volte, " + doJogador.getNome() + "!");
        log.add("Vai, " + nova.getNome() + "!");
    }

    // ------------------------------------------------------------------
    // Captura
    // ------------------------------------------------------------------

    /**
     * Tenta capturar a criatura selvagem, sorteando com o gerador da batalha.
     *
     * <p>Falhar custa o turno: quem chama deve dar o golpe de graca a selvagem
     * com {@link #turnoApenasDoSelvagem()}.
     *
     * @return {@code true} se a captura deu certo, encerrando a batalha
     */
    public boolean tentarCapturar() {
        if (terminou()) return false;
        if (captura.tentar(selvagem)) {
            capturada = true;
            log.add("Você capturou " + selvagem.getNome() + " (Nv." + selvagem.getNivel() + ")!");
            return true;
        }
        log.add(selvagem.getNome() + " escapou da captura!");
        return false;
    }

    /** A chance de a proxima tentativa de captura dar certo, de 0.0 a 1.0. */
    public double chanceDeCaptura() { return Captura.calcularChance(selvagem); }

    // ------------------------------------------------------------------
    // Desfecho
    // ------------------------------------------------------------------

    /** Fecha a batalha: distribui experiencia se a selvagem caiu. */
    private void registrarDesfecho() {
        if (selvagem.estaViva() || expGanhaNaBatalha > 0) return;

        if (doJogador.estaViva()) {
            expGanhaNaBatalha = Experiencia.calcularGanho(selvagem, doJogador);
            int nivelAntes = doJogador.getNivel();
            niveisGanhosNaBatalha = doJogador.ganharExp(expGanhaNaBatalha);
            log.add(doJogador.getNome() + " ganhou " + expGanhaNaBatalha + " de experiência!");
            if (niveisGanhosNaBatalha > 0) {
                log.add(doJogador.getNome() + " subiu para o nível " + doJogador.getNivel() + "!");
                for (Golpe novo : doJogador.getGolpesAprendidosAgora()) {
                    log.add(doJogador.getNome() + " aprendeu " + novo.getNome() + "!");
                }
                if (doJogador.getNivel() >= Criatura.NIVEL_MAXIMO && nivelAntes < Criatura.NIVEL_MAXIMO) {
                    log.add(doJogador.getNome() + " atingiu o nível máximo!");
                }
            }
        }
    }
}
