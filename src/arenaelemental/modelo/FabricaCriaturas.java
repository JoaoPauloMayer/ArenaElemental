package arenaelemental.modelo;

import arenaelemental.mundo.Area;

import java.util.Random;

/**
 * Cria as criaturas iniciais e os encontros selvagens, a partir do registro de
 * {@link Especies}.
 *
 * <p>Nao ha nenhum {@code switch} de tipo aqui: a fabrica sorteia entre as
 * especies registradas, sejam elas tres ou trinta. Uma especie nova entra no
 * jogo so por ser registrada.
 *
 * <p>O sorteio usa um {@link Random} recebido no construtor. Os atalhos
 * estaticos usam uma instancia compartilhada, para quem nao se importa com a
 * semente; os testes constroem a fabrica com um {@code Random} de semente fixa
 * e obtem encontros reproduziveis.
 */
public class FabricaCriaturas {

    /** Nivel com que a criatura inicial comeca a jornada. */
    public static final int NIVEL_INICIAL = 5;

    /** Instancia usada pelos atalhos estaticos. */
    private static final FabricaCriaturas PADRAO = new FabricaCriaturas();

    private final Random rng;

    public FabricaCriaturas() { this(new Random()); }

    /** Fabrica com gerador proprio — use nos testes para encontros reproduziveis. */
    public FabricaCriaturas(Random rng) { this.rng = rng; }

    /** A criatura inicial da especie escolhida, no {@link #NIVEL_INICIAL}. */
    public Criatura inicial(EspecieCriatura especie) {
        return especie.criar(NIVEL_INICIAL);
    }

    /**
     * Encontro selvagem nesta area, em nivel proximo ao do jogador (de um
     * abaixo a dois acima), para que a experiencia ganha continue fazendo
     * sentido conforme ele evolui. A especie sai de
     * {@link Especies#sortear(Area, Random)}: so as que vivem na area, e as
     * raras la em menor numero.
     */
    public Criatura selvagem(Area area, int nivelDoJogador) {
        return selvagem(Especies.sortear(area, rng), nivelDoJogador);
    }

    /** Encontro sem area: qualquer especie registrada, com a mesma chance. */
    public Criatura selvagem(int nivelDoJogador) {
        return selvagem(Especies.sortear(rng), nivelDoJogador);
    }

    /** Encontro selvagem de uma especie especifica, no nivel do encontro. */
    public Criatura selvagem(EspecieCriatura especie, int nivelDoJogador) {
        return especie.criarComNomeSorteado(nivelDeEncontro(nivelDoJogador), rng);
    }

    /** Nivel do proximo encontro: de um abaixo a dois acima do nivel do jogador. */
    public int nivelDeEncontro(int nivelDoJogador) {
        return Math.max(2, Math.min(Criatura.NIVEL_MAXIMO, nivelDoJogador - 1 + rng.nextInt(4)));
    }

    // ------------------------------------------------------------------
    // Atalhos estaticos
    // ------------------------------------------------------------------

    public static Criatura novaInicial(EspecieCriatura especie) { return PADRAO.inicial(especie); }

    public static Criatura selvagemAleatorio(Area area, int nivelDoJogador) {
        return PADRAO.selvagem(area, nivelDoJogador);
    }
}
