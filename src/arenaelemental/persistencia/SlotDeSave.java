package arenaelemental.persistencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Um lugar para guardar uma partida: um dos slots numerados ou o do
 * salvamento rapido.
 *
 * <p>O slot 1 grava em {@code jogo.save}, o mesmo arquivo de quando o jogo so
 * tinha um save: quem ja jogava encontra a partida antiga no Slot 1, sem
 * migracao nenhuma.
 */
public final class SlotDeSave {

    /** Quantos slots numerados existem, alem do salvamento rapido. */
    public static final int QUANTIDADE = 5;

    /** O salvamento rapido: grava sem perguntar, por cima do anterior. */
    public static final SlotDeSave RAPIDO = new SlotDeSave(0);

    private static final List<SlotDeSave> NUMERADOS;

    static {
        List<SlotDeSave> lista = new ArrayList<>();
        for (int i = 1; i <= QUANTIDADE; i++) lista.add(new SlotDeSave(i));
        NUMERADOS = Collections.unmodifiableList(lista);
    }

    private final int numero;

    private SlotDeSave(int numero) { this.numero = numero; }

    /** O slot numerado, de 1 a {@link #QUANTIDADE}. */
    public static SlotDeSave numero(int n) {
        if (n < 1 || n > QUANTIDADE) {
            throw new IllegalArgumentException("Slot " + n + " não existe (1 a " + QUANTIDADE + ")");
        }
        return NUMERADOS.get(n - 1);
    }

    /** Os slots numerados, em ordem. */
    public static List<SlotDeSave> numerados() { return NUMERADOS; }

    /** O salvamento rapido primeiro, depois os numerados. */
    public static List<SlotDeSave> todos() {
        List<SlotDeSave> lista = new ArrayList<>();
        lista.add(RAPIDO);
        lista.addAll(NUMERADOS);
        return Collections.unmodifiableList(lista);
    }

    public boolean ehRapido() { return numero == 0; }

    /** O numero do slot; 0 no salvamento rapido. */
    public int getNumero() { return numero; }

    /** "Slot 3" ou "Salvamento rápido". */
    public String getNome() { return ehRapido() ? "Salvamento rápido" : "Slot " + numero; }

    /** O nome do arquivo, dentro da pasta de saves. */
    public String nomeDoArquivo() {
        if (ehRapido()) return "rapido.save";
        return numero == 1 ? "jogo.save" : "jogo-" + numero + ".save";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SlotDeSave && ((SlotDeSave) o).numero == numero;
    }

    @Override
    public int hashCode() { return numero; }

    @Override
    public String toString() { return getNome(); }
}
