package arenaelemental.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Uma entrada da tabela de aprendizado: em que nivel a especie aprende o golpe. */
public class AprendizadoGolpe {
    private final int nivel;
    private final Golpe golpe;

    public AprendizadoGolpe(int nivel, Golpe golpe) {
        this.nivel = nivel;
        this.golpe = golpe;
    }

    public int getNivel() { return nivel; }
    public Golpe getGolpe() { return golpe; }

    /** Monta uma tabela ja ordenada por nivel. */
    public static List<AprendizadoGolpe> tabela(AprendizadoGolpe... entradas) {
        List<AprendizadoGolpe> lista = new ArrayList<>(Arrays.asList(entradas));
        lista.sort(Comparator.comparingInt(AprendizadoGolpe::getNivel));
        return lista;
    }

    /** Atalho de leitura para montar a tabela. */
    public static AprendizadoGolpe em(int nivel, Golpe golpe) {
        return new AprendizadoGolpe(nivel, golpe);
    }
}
