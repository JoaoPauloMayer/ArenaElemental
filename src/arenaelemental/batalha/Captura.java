package arenaelemental.batalha;

import arenaelemental.modelo.Criatura;

import java.util.Random;

/**
 * A chance de capturar uma criatura selvagem.
 *
 * <p>Quanto menor a vida do alvo, maior a chance — de {@link #CHANCE_MINIMA}
 * com a vida cheia ate {@link #CHANCE_MAXIMA} com o alvo quase derrotado.
 *
 * <p>{@link #calcularChance(Criatura)} e' uma funcao pura e continua estatica.
 * O sorteio, esse, usa um {@link Random} recebido no construtor, como o resto
 * da batalha: assim um teste consegue fixar a semente e decidir se a captura
 * vai dar certo, em vez de depender de {@code Math.random()}.
 */
public class Captura {

    public static final double CHANCE_MINIMA = 0.15;
    public static final double CHANCE_MAXIMA = 0.95;

    private final Random rng;

    public Captura() { this(new Random()); }

    /** Construtor com gerador proprio, para testes deterministicos. */
    public Captura(Random rng) { this.rng = rng; }

    /** A chance de captura do alvo, de {@value #CHANCE_MINIMA} a {@value #CHANCE_MAXIMA}. */
    public static double calcularChance(Criatura alvo) {
        double chance = 1.0 - alvo.percentualVida() * 0.75;
        return Math.max(CHANCE_MINIMA, Math.min(CHANCE_MAXIMA, chance));
    }

    /** Sorteia se a captura deu certo. */
    public boolean tentar(Criatura alvo) {
        return rng.nextDouble() < calcularChance(alvo);
    }
}
