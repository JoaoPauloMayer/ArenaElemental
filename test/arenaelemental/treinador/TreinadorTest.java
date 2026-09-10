package arenaelemental.treinador;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Marulho;
import arenaelemental.mundo.Area;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TreinadorTest {

    @Test
    void naoAdicionaAlemDoLimiteDeSeis() {
        Treinador t = new Treinador("Ana");
        for (int i = 0; i < 6; i++) {
            assertTrue(t.adicionarNaEquipe(new Marulho("C" + i, 5)));
        }
        assertFalse(t.adicionarNaEquipe(new Marulho("Setima", 5)),
                "Nao deve ser possivel adicionar uma setima criatura na equipe");
        assertEquals(6, t.getEquipe().size());
    }

    @Test
    void getAtivaRetornaPrimeiraCriaturaViva() {
        Treinador t = new Treinador("Ana");
        Criatura desmaiada = new Marulho("Desmaiada", 5);
        desmaiada.receberDano(desmaiada.getHpMaximo());
        Criatura viva = new Marulho("Viva", 5);
        t.adicionarNaEquipe(desmaiada);
        t.adicionarNaEquipe(viva);
        assertEquals(viva, t.getAtiva(), "getAtiva deve pular criaturas desmaiadas e retornar a primeira viva");
    }

    @Test
    void getAtivaRetornaNuloComAEquipeTodaDesmaiada() {
        Treinador t = new Treinador("Ana");
        Criatura c = new Marulho("Unica", 5);
        c.receberDano(c.getHpMaximo());
        t.adicionarNaEquipe(c);
        assertNull(t.getAtiva());
    }

    @Test
    void descansarRestauraAEquipeInteiraInclusiveOsDesmaiados() {
        Treinador t = new Treinador("Ana");
        Criatura desmaiada = new Marulho("Desmaiada", 5);
        Criatura machucada = new Marulho("Machucada", 5);
        desmaiada.receberDano(desmaiada.getHpMaximo());
        machucada.receberDano(3);
        t.adicionarNaEquipe(desmaiada);
        t.adicionarNaEquipe(machucada);

        assertEquals(machucada, t.getAtiva(), "antes de descansar, a desmaiada e pulada");
        assertTrue(t.precisaDescansar());

        t.restaurarEquipe();

        assertFalse(t.precisaDescansar());
        assertEquals(desmaiada.getHpMaximo(), desmaiada.getVidaAtual(), "a desmaiada volta com vida cheia");
        assertEquals(machucada.getHpMaximo(), machucada.getVidaAtual());
        assertEquals(desmaiada, t.getAtiva(), "com todos de pe, a ativa volta a ser a primeira da equipe");
    }

    @Test
    void naoPrecisaDescansarComAEquipeIntacta() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Nova", 5));
        assertFalse(t.precisaDescansar());
    }

    @Test
    void precisaDescansarQuandoFaltaPp() {
        Treinador t = new Treinador("Ana");
        Criatura c = new Marulho("Cansada", 5);
        t.adicionarNaEquipe(c);
        assertFalse(t.precisaDescansar());

        c.gastarPp(c.getGolpes().get(0));
        assertTrue(t.precisaDescansar(), "PP gasto tambem e' motivo para descansar");

        t.restaurarEquipe();
        assertFalse(t.precisaDescansar());
    }

    @Test
    void precisaDescansarComCondicaoDeStatus() {
        Treinador t = new Treinador("Ana");
        Criatura c = new Marulho("Envenenada", 5);
        t.adicionarNaEquipe(c);
        c.definirCondicao(CondicaoStatus.VENENO, 0);

        assertTrue(t.precisaDescansar(), "com vida cheia mas envenenada, ainda precisa descansar");
        t.restaurarEquipe();
        assertFalse(c.temCondicao());
        assertFalse(t.precisaDescansar());
    }

    // ------------------------------------------------------------------
    // Criatura ativa e troca
    // ------------------------------------------------------------------

    @Test
    void aAtivaComecaSendoAPrimeiraDaEquipe() {
        Treinador t = new Treinador("Ana");
        Criatura primeira = new Marulho("Primeira", 5);
        t.adicionarNaEquipe(primeira);
        t.adicionarNaEquipe(new Marulho("Segunda", 5));

        assertEquals(0, t.getIndiceAtiva());
        assertSame(primeira, t.getAtiva());
    }

    @Test
    void trocarParaColocaOutraCriaturaAFrente() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Primeira", 5));
        Criatura segunda = new Marulho("Segunda", 5);
        t.adicionarNaEquipe(segunda);

        assertSame(segunda, t.trocarPara(1));
        assertEquals(1, t.getIndiceAtiva());
        assertSame(segunda, t.getAtiva());
    }

    @Test
    void trocarParaAPosicaoJaAtivaEhPermitidoENaoMudaNada() {
        // precisa ser permitido: durante a batalha quem esta em campo e' a
        // criatura da Batalha, que diverge deste indice logo apos uma queda —
        // recusar a posicao ativa tornaria essa criatura inescolhivel
        Treinador t = new Treinador("Ana");
        Criatura unica = new Marulho("Primeira", 5);
        t.adicionarNaEquipe(unica);

        assertTrue(t.podeTrocarPara(0));
        assertSame(unica, t.trocarPara(0));
        assertEquals(0, t.getIndiceAtiva());
    }

    @Test
    void aCriaturaParaQueAAtivaEscorregouContinuaEscolhivel() {
        Treinador t = new Treinador("Ana");
        Criatura primeira = new Marulho("Primeira", 5);
        Criatura segunda = new Marulho("Segunda", 5);
        t.adicionarNaEquipe(primeira);
        t.adicionarNaEquipe(segunda);

        primeira.receberDano(primeira.getHpMaximo());
        assertSame(segunda, t.getAtiva(), "a ativa escorregou para a posicao 1");

        assertTrue(t.podeTrocarPara(1), "a criatura que entrou sozinha ainda pode ser escolhida");
        assertSame(segunda, t.trocarPara(1));
    }

    @Test
    void naoTrocaParaUmaPosicaoInvalidaOuDesmaiada() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Primeira", 5));
        Criatura caida = new Marulho("Caida", 5);
        caida.receberDano(caida.getHpMaximo());
        t.adicionarNaEquipe(caida);

        assertFalse(t.podeTrocarPara(1), "nao da para mandar uma desmaiada para o campo");
        assertTrue(t.podeTrocarPara(0), "a viva continua escolhivel");
        assertFalse(t.podeTrocarPara(9));
        assertFalse(t.podeTrocarPara(-1));
        assertNull(t.trocarPara(1));
    }

    @Test
    void asReservasSaoAsVivasForaDeCampo() {
        Treinador t = new Treinador("Ana");
        Criatura ativa = new Marulho("Ativa", 5);
        Criatura reserva = new Marulho("Reserva", 5);
        Criatura caida = new Marulho("Caida", 5);
        caida.receberDano(caida.getHpMaximo());
        t.adicionarNaEquipe(ativa);
        t.adicionarNaEquipe(reserva);
        t.adicionarNaEquipe(caida);

        assertEquals(List.of(reserva), t.reservasDisponiveis());
        assertTrue(t.temReservaDisponivel());
        assertEquals(2, t.quantasVivas());
    }

    @Test
    void depoisDeUmaQuedaAReservaContaAPartirDeQuemCaiu() {
        // o bug: com duas criaturas, a primeira cai, a ativa escorrega para a
        // segunda, e "as reservas da ativa" ficam vazias — o jogo declarava
        // derrota com a segunda ainda de pe
        Treinador t = new Treinador("Ana");
        Criatura caiu = new Marulho("Caiu", 5);
        Criatura dePe = new Marulho("DePe", 5);
        t.adicionarNaEquipe(caiu);
        t.adicionarNaEquipe(dePe);
        caiu.receberDano(caiu.getHpMaximo());

        assertSame(dePe, t.getAtiva(), "a ativa escorregou");
        assertFalse(t.temReservaDisponivel(), "a pergunta errada: reservas da ativa");
        assertTrue(t.temReservaPara(caiu), "a pergunta certa: quem entra no lugar de quem caiu");
        assertEquals(List.of(dePe), t.reservasPara(caiu));
    }

    @Test
    void comAEquipeTodaNoChaoNaoHaReservaParaNinguem() {
        Treinador t = new Treinador("Ana");
        Criatura a = new Marulho("A", 5), b = new Marulho("B", 5);
        t.adicionarNaEquipe(a);
        t.adicionarNaEquipe(b);
        a.receberDano(a.getHpMaximo());
        b.receberDano(b.getHpMaximo());
        assertFalse(t.temReservaPara(a));
        assertFalse(t.temReservaPara(b));
    }

    @Test
    void semReservaViavelNaoHaParaQuemTrocar() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Sozinha", 5));
        assertFalse(t.temReservaDisponivel());
        assertTrue(t.reservasDisponiveis().isEmpty());
    }

    @Test
    void aAtivaEscorregaSozinhaQuandoAEscolhidaCai() {
        Treinador t = new Treinador("Ana");
        Criatura primeira = new Marulho("Primeira", 5);
        Criatura segunda = new Marulho("Segunda", 5);
        t.adicionarNaEquipe(primeira);
        t.adicionarNaEquipe(segunda);

        assertSame(primeira, t.getAtiva());
        primeira.receberDano(primeira.getHpMaximo());

        assertSame(segunda, t.getAtiva(), "com a escolhida no chao, entra a primeira viva");
        assertEquals(1, t.getIndiceAtiva());
    }

    @Test
    void aEquipeVaziaNaoTemAtiva() {
        Treinador t = new Treinador("Ana");
        assertNull(t.getAtiva());
        assertEquals(0, t.quantasVivas());
        assertFalse(t.temReservaDisponivel());
    }

    @Test
    void aEquipeDevolvidaEhSomenteLeitura() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Unica", 5));
        assertThrows(UnsupportedOperationException.class,
                () -> t.getEquipe().add(new Marulho("Intrusa", 5)));
    }

    // ------------------------------------------------------------------
    // Ordem da equipe
    // ------------------------------------------------------------------

    @Test
    void trocarDePosicaoInverteAsDuasCriaturas() {
        Treinador t = new Treinador("Ana");
        Criatura a = new Marulho("A", 5), b = new Marulho("B", 5), c = new Marulho("C", 5);
        t.adicionarNaEquipe(a);
        t.adicionarNaEquipe(b);
        t.adicionarNaEquipe(c);

        assertTrue(t.trocarDePosicao(0, 2));
        assertEquals(List.of(c, b, a), t.getEquipe());
    }

    @Test
    void aAtivaAcompanhaACriaturaQuandoElaMudaDePosicao() {
        Treinador t = new Treinador("Ana");
        Criatura a = new Marulho("A", 5), b = new Marulho("B", 5);
        t.adicionarNaEquipe(a);
        t.adicionarNaEquipe(b);
        assertSame(a, t.getAtiva());

        t.trocarDePosicao(0, 1);
        assertSame(a, t.getAtiva(), "mudar a ordem nao muda quem esta em campo");
        assertEquals(1, t.getIndiceAtiva());
    }

    @Test
    void trocarDePosicaoRecusaPosicoesInexistentes() {
        Treinador t = new Treinador("Ana");
        Criatura unica = new Marulho("Unica", 5);
        t.adicionarNaEquipe(unica);

        assertFalse(t.trocarDePosicao(0, 1));
        assertFalse(t.trocarDePosicao(-1, 0));
        assertEquals(List.of(unica), t.getEquipe());
    }

    @Test
    void tornarLiderLevaACriaturaParaAFrenteETornaAAtiva() {
        Treinador t = new Treinador("Ana");
        Criatura a = new Marulho("A", 5), b = new Marulho("B", 5), c = new Marulho("C", 5);
        t.adicionarNaEquipe(a);
        t.adicionarNaEquipe(b);
        t.adicionarNaEquipe(c);

        assertTrue(t.tornarLider(2));
        assertEquals(List.of(c, a, b), t.getEquipe(), "as outras mantem a ordem entre si");
        assertSame(c, t.getAtiva());
    }

    @Test
    void aLiderContinuaAFrenteDepoisDeDescansar() {
        // o descanso devolve a lideranca a primeira posicao: por isso tornar
        // lider mexe na ordem, e nao so no indice ativo
        Treinador t = new Treinador("Ana");
        Criatura a = new Marulho("A", 5), b = new Marulho("B", 5);
        t.adicionarNaEquipe(a);
        t.adicionarNaEquipe(b);

        t.tornarLider(1);
        t.restaurarEquipe();
        assertSame(b, t.getAtiva());
    }

    @Test
    void tornarLiderRecusaPosicaoInexistente() {
        Treinador t = new Treinador("Ana");
        t.adicionarNaEquipe(new Marulho("Unica", 5));
        assertFalse(t.tornarLider(3));
        assertFalse(t.tornarLider(-1));
    }

    // ------------------------------------------------------------------
    // Mapa
    // ------------------------------------------------------------------

    @Test
    void oTreinadorComecaNaFloresta() {
        assertEquals(Area.FLORESTA, new Treinador("Ana").getArea());
    }

    @Test
    void viajaQuandoHaRotaDireta() {
        Treinador t = new Treinador("Ana");
        assertTrue(t.viajarPara(Area.MONTANHA));
        assertEquals(Area.MONTANHA, t.getArea());
        assertTrue(t.viajarPara(Area.VULCAO));
        assertEquals(Area.VULCAO, t.getArea());
    }

    @Test
    void naoViajaSemRotaDireta() {
        Treinador t = new Treinador("Ana");
        assertFalse(t.viajarPara(Area.MAR_PROFUNDO), "da Floresta nao ha rota para o Mar Profundo");
        assertFalse(t.viajarPara(Area.FLORESTA), "ficar na mesma area nao e' viagem");
        assertFalse(t.viajarPara(null));
        assertEquals(Area.FLORESTA, t.getArea());
    }

    @Test
    void restaurarAreaNaoExigeRotaENuloViraAInicial() {
        Treinador t = new Treinador("Ana");
        t.restaurarArea(Area.MAR_PROFUNDO);
        assertEquals(Area.MAR_PROFUNDO, t.getArea());
        t.restaurarArea(null);
        assertEquals(Area.INICIAL, t.getArea());
    }
}
