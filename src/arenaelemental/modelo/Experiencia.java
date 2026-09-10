package arenaelemental.modelo;

/** Calculo da experiencia ganha ao derrotar uma criatura. */
public final class Experiencia {

    /**
     * Formula escalada por nivel usada da 5a geracao em diante:
     *
     * <pre>
     *   Exp = floor( (b * L / 5) * (1/s) * ((2L + 10)^2.5 / (L + Lp + 10)^2.5) ) + 1
     * </pre>
     *
     * onde <b>b</b> e' o rendimento de experiencia da especie derrotada,
     * <b>L</b> o nivel dela, <b>Lp</b> o nivel de quem venceu e <b>s</b> o
     * numero de participantes na batalha (sempre 1 por enquanto).
     *
     * <p>Vencer alguem de nivel bem mais alto que o seu rende muito mais que
     * vencer alguem abaixo do seu nivel.
     */
    public static int calcularGanho(Criatura derrotado, Criatura vencedor) {
        return calcularGanho(derrotado, vencedor, 1);
    }

    public static int calcularGanho(Criatura derrotado, Criatura vencedor, int participantes) {
        int s = Math.max(1, participantes);
        double b = derrotado.getEstatisticasBase().getRendimentoExp();
        double l = derrotado.getNivel();
        double lp = vencedor.getNivel();

        double escala = Math.pow(2 * l + 10, 2.5) / Math.pow(l + lp + 10, 2.5);
        return (int) Math.floor((b * l / 5.0) * (1.0 / s) * escala) + 1;
    }

    private Experiencia() { }
}
