package arenaelemental;

import java.util.Random;

/**
 * Um {@link Random} de teste que fixa apenas os sorteios de uma faixa
 * especifica, deixando os outros seguirem normalmente.
 *
 * <p>Quase tudo na batalha sorteia com {@code nextInt(100)}: precisao, chance de
 * efeito de status, paralisia e descongelamento. Fixando essa faixa da para
 * decidir o desfecho sem depender de procurar uma semente que sirva.
 */
public class RandomControlado extends Random {

    private final int faixa;
    private final int valor;

    private RandomControlado(long semente, int faixa, int valor) {
        super(semente);
        this.faixa = faixa;
        this.valor = valor;
    }

    /** Todo {@code nextInt(100)} devolve 0 — o sorteio mais favoravel possivel. */
    public static Random sempreSorteiaBaixo() { return new RandomControlado(1, 100, 0); }

    /** Todo {@code nextInt(100)} devolve 99 — o sorteio mais desfavoravel possivel. */
    public static Random sempreSorteiaAlto() { return new RandomControlado(1, 100, 99); }

    /** Todo {@code nextInt(100)} devolve o valor informado. */
    public static Random comNextInt100(int valor) { return new RandomControlado(1, 100, valor); }

    @Override
    public int nextInt(int bound) {
        return bound == faixa ? valor : super.nextInt(bound);
    }
}
