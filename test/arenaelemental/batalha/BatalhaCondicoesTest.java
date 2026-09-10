package arenaelemental.batalha;

import arenaelemental.RandomControlado;
import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Como as condicoes de status e a precisao se comportam dentro do turno. */
public class BatalhaCondicoesTest {

    private static Criatura jogador(int nivel) { return Especies.MARULHO.criar("Meu", nivel); }
    private static Criatura selvagem(int nivel) { return Especies.FOLHARAL.criar("Selvagem", nivel); }

    private static boolean logTem(Batalha b, String trecho) {
        for (String linha : b.getLog()) if (linha.contains(trecho)) return true;
        return false;
    }

    // ------------------------------------------------------------------
    // Sono e congelamento
    // ------------------------------------------------------------------

    @Test
    void criaturaDormindoNaoAge() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.SONO, 3);

        Batalha b = new Batalha(meu, alvo, new Random(1));
        int vidaDoAlvo = alvo.getVidaAtual();
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "está dormindo"), "o log deve dizer que ela dormiu o turno");
        assertFalse(logTem(b, "Meu usou"), "quem dorme nao usa golpe");
        assertEquals(vidaDoAlvo, alvo.getVidaAtual(), "o alvo nao pode ter levado dano");
        assertEquals(2, meu.getTurnosDeSono(), "o contador de sono anda mesmo assim");
    }

    @Test
    void criaturaAcordaEAgeNoMesmoTurno() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.SONO, 1);

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "acordou!"));
        assertTrue(logTem(b, "Meu usou"), "acordar no comeco do turno ja permite agir");
        assertFalse(meu.temCondicao());
    }

    @Test
    void criaturaCongeladaNaoAgeEnquantoNaoDescongela() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.CONGELAMENTO, 0);

        // nextInt(100) fixo em 99: nunca cai abaixo dos 20% de descongelar
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "está congelado"));
        assertFalse(logTem(b, "Meu usou"));
        assertEquals(CondicaoStatus.CONGELAMENTO, meu.getCondicao());
    }

    @Test
    void criaturaCongeladaDescongelaNoSorteioBaixo() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.CONGELAMENTO, 0);

        // nextInt(100) fixo em 0: sempre abaixo dos 20% de descongelar
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "descongelou!"));
        assertTrue(logTem(b, "Meu usou"));
        assertFalse(meu.temCondicao());
    }

    // ------------------------------------------------------------------
    // Paralisia
    // ------------------------------------------------------------------

    @Test
    void aParalisiaAsVezesTiraOTurno() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.PARALISIA, 0);

        // nextInt(100) fixo em 0: sempre abaixo dos 25% que travam
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "está paralisado e não conseguiu se mover"));
        assertFalse(logTem(b, "Meu usou"));
    }

    @Test
    void aParalisiaDeixaAgirNoSorteioAlto() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.PARALISIA, 0);

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.INVESTIDA); // 100% de precisao, nao erra nem no sorteio alto

        assertFalse(logTem(b, "não conseguiu se mover"));
        assertTrue(logTem(b, "Meu usou"));
    }

    @Test
    void aParalisiaMudaAOrdemDoTurno() {
        // Braseiro Nv.30 e' mais rapido que Marulho Nv.30 — ate ficar paralisado
        Criatura braseiro = Especies.BRASEIRO.criar("Rapido", 30);
        Criatura marulho = Especies.MARULHO.criar("Lento", 30);
        assertTrue(braseiro.getVelocidade() > marulho.getVelocidade());

        // Trancada: 100% de precisao e conhecida pelas duas no nivel 30
        Batalha antes = new Batalha(braseiro, marulho, RandomControlado.sempreSorteiaAlto());
        antes.executarTurno(Golpes.TRANCADA);
        assertTrue(primeiraAcao(antes).startsWith("Rapido usou"),
                "o mais rapido abre o turno, seja qual for o golpe sorteado pela outra");

        braseiro.definirCondicao(CondicaoStatus.PARALISIA, 0);
        assertTrue(braseiro.getVelocidadeEfetiva() < marulho.getVelocidadeEfetiva(),
                "com a Velocidade pela metade ele passa a ser o mais lento");

        Batalha depois = new Batalha(braseiro, marulho, RandomControlado.sempreSorteiaAlto());
        depois.executarTurno(Golpes.TRANCADA);
        assertTrue(primeiraAcao(depois).startsWith("Lento usou"),
                "o paralisado perde a iniciativa");
    }

    private static String primeiraAcao(Batalha b) {
        for (String linha : b.getLog()) if (linha.contains(" usou ")) return linha;
        return "(ninguem atacou)";
    }

    // ------------------------------------------------------------------
    // Dano de fim de turno
    // ------------------------------------------------------------------

    @Test
    void aQueimaduraCobraNoFimDoTurno() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.definirCondicao(CondicaoStatus.QUEIMADURA, 0);
        int esperado = meu.getHpMaximo() / 16;
        int antes = meu.getVidaAtual();

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "pela queimadura"));
        int perdaTotal = antes - meu.getVidaAtual();
        assertTrue(perdaTotal >= esperado, "a queimadura entra alem do dano do golpe da selvagem");
    }

    @Test
    void oVenenoCobraNoFimDoTurno() {
        Criatura meu = jogador(30);   // no nivel 30 o Marulho ainda sabe Jato d Agua
        Criatura alvo = selvagem(30);
        alvo.definirCondicao(CondicaoStatus.VENENO, 0);
        int esperado = alvo.getHpMaximo() / 8;
        int antes = alvo.getVidaAtual();

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertTrue(logTem(b, "pelo veneno"));
        assertTrue(antes - alvo.getVidaAtual() >= esperado);
    }

    @Test
    void oDanoResidualPodeDerrubarACriatura() {
        Criatura meu = jogador(40);
        Criatura alvo = selvagem(40);
        alvo.definirCondicao(CondicaoStatus.VENENO, 0);
        alvo.receberDano(alvo.getHpMaximo() - 1); // sobra 1 de vida
        alvo.definirCondicao(CondicaoStatus.VENENO, 0);

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertFalse(alvo.estaViva());
        assertTrue(b.terminou());
        assertTrue(logTem(b, "foi derrotado!"));
    }

    @Test
    void semCondicaoNinguemPerdeVidaNoFimDoTurno() {
        Criatura meu = jogador(40);
        Criatura alvo = selvagem(40);
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA);

        assertFalse(logTem(b, "pela queimadura"));
        assertFalse(logTem(b, "pelo veneno"));
    }

    // ------------------------------------------------------------------
    // Golpes que aplicam condicao
    // ------------------------------------------------------------------

    @Test
    void golpeDeStatusAplicaACondicaoSemCausarDano() {
        Criatura meu = Especies.FOLHARAL.criar("Meu", 30);
        Criatura alvo = Especies.MARULHO.criar("Selvagem", 30);
        int vidaDoAlvo = alvo.getVidaAtual();

        // sorteio baixo: Esporo Sonífero acerta (75%) e o efeito pega (100%)
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        b.executarTurno(Golpes.ESPORO_SONIFERO);

        assertEquals(CondicaoStatus.SONO, alvo.getCondicao());
        assertTrue(logTem(b, "adormeceu!"));
        assertEquals(vidaDoAlvo, alvo.getVidaAtual(), "um golpe de status nao tira vida");
    }

    @Test
    void golpeQueErraNaoAplicaCondicao() {
        Criatura meu = Especies.FOLHARAL.criar("Meu", 30);
        Criatura alvo = Especies.MARULHO.criar("Selvagem", 30);

        // Esporo Sonífero tem 75% de precisao; com nextInt(100)=99 ele erra
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.ESPORO_SONIFERO);

        assertTrue(logTem(b, "errou o golpe!"));
        assertFalse(alvo.temCondicao(), "golpe que erra nao deixa efeito");
    }

    @Test
    void oEfeitoSecundarioDeUmGolpeDeDanoPegaComOSorteioBaixo() {
        // o Braseiro e' o mais rapido: assim ele age antes de a selvagem poder
        // paralisa-lo de volta e tirar o turno dele
        Criatura meu = Especies.BRASEIRO.criar("Meu", 30);
        Criatura alvo = Especies.MARULHO.criar("Selvagem", 30);
        assertTrue(meu.getVelocidade() > alvo.getVelocidade());

        // Trancada: 100% de precisao e 30% de paralisia; nextInt(100)=0 faz pegar
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        b.executarTurno(Golpes.TRANCADA);

        assertEquals(CondicaoStatus.PARALISIA, alvo.getCondicao());
        assertTrue(logTem(b, "ficou paralisado!"));
    }

    @Test
    void umaCriaturaDeFogoNaoPegaQueimadura() {
        Criatura meu = Especies.MARULHO.criar("Meu", 30);
        Criatura alvo = Especies.BRASEIRO.criar("Selvagem", 30);
        alvo.receberDano(0);

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaBaixo());
        for (int i = 0; i < 3 && !b.terminou(); i++) b.executarTurno(Golpes.JATO_DAGUA);

        assertNotEquals(CondicaoStatus.QUEIMADURA, alvo.getCondicao());
    }

    // ------------------------------------------------------------------
    // Precisao dentro da batalha
    // ------------------------------------------------------------------

    @Test
    void golpeQueErraNaoTiraVida() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        int vidaDoAlvo = alvo.getVidaAtual();

        // Sopro Gelado tem 95%; nextInt(100)=99 erra
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.SOPRO_GELADO);

        assertTrue(logTem(b, "Meu errou o golpe!"));
        assertEquals(vidaDoAlvo, alvo.getVidaAtual());
    }

    @Test
    void oGolpeGastaPpMesmoQuandoErra() {
        Criatura meu = jogador(30);
        Criatura alvo = selvagem(30);
        meu.aprenderGolpe(Golpes.SOPRO_GELADO);
        int antes = meu.getPp(Golpes.SOPRO_GELADO);

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.SOPRO_GELADO);

        assertTrue(logTem(b, "errou o golpe!"));
        assertEquals(antes - 1, meu.getPp(Golpes.SOPRO_GELADO),
                "errar tambem custa PP, como nos jogos originais");
    }

    // ------------------------------------------------------------------
    // Esforço
    // ------------------------------------------------------------------

    @Test
    void semPpEmNadaOGolpeViraEsforcoECobraRecuo() {
        Criatura meu = jogador(40);
        Criatura alvo = selvagem(40);
        for (var g : meu.getGolpes()) meu.definirPp(g, 0);
        int vidaAntes = meu.getVidaAtual();
        int recuoEsperado = meu.getHpMaximo() * Golpes.ESFORCO.getRecuoPercentualDoHpMaximo() / 100;

        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        b.executarTurno(Golpes.JATO_DAGUA); // sem PP: entra o Esforço no lugar

        assertTrue(logTem(b, "Meu usou Esforço!"));
        assertTrue(logTem(b, "se machucou com o esforço"));
        assertTrue(vidaAntes - meu.getVidaAtual() >= recuoEsperado);
    }

    @Test
    void aSelvagemTambemUsaEsforcoQuandoFicaSemPp() {
        Criatura meu = jogador(40);
        Criatura alvo = selvagem(40);
        for (var g : alvo.getGolpes()) alvo.definirPp(g, 0);

        assertEquals(List.of(Golpes.ESFORCO), alvo.getGolpesDisponiveis());
        Batalha b = new Batalha(meu, alvo, RandomControlado.sempreSorteiaAlto());
        assertEquals(Golpes.ESFORCO, b.golpeDoSelvagem());
    }

    // ------------------------------------------------------------------
    // Reprodutibilidade
    // ------------------------------------------------------------------

    @Test
    void aMesmaSementeReproduzOTurnoComCondicoes() {
        assertEquals(rodar(new Random(99)), rodar(new Random(99)),
                "precisao, efeito de status e dano residual saem todos do mesmo gerador");
    }

    private List<String> rodar(Random rng) {
        Criatura meu = Especies.BRASEIRO.criar("Meu", 30);
        Criatura alvo = Especies.FOLHARAL.criar("Selvagem", 30);
        Batalha b = new Batalha(meu, alvo, rng);
        for (int i = 0; i < 8 && !b.terminou(); i++) b.executarTurno(Golpes.BRASA);
        return b.getLog();
    }
}
