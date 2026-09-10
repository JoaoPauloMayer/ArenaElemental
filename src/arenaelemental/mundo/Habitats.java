package arenaelemental.mundo;

import arenaelemental.modelo.TipoElemental;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static arenaelemental.modelo.TipoElemental.*;
import static arenaelemental.mundo.Area.*;

/**
 * Onde cada tipo de criatura vive.
 *
 * <p>E' a unica tabela de habitat do jogo: uma especie vive nas areas em que
 * <b>todos</b> os seus tipos vivem. Por isso uma especie Sombrio / Agua so
 * aparece no Mar Profundo — a unica area que tem os dois —, e uma especie nova
 * entra no mapa certo sozinha, so por ter o tipo que tem.
 *
 * <p>Um tipo pode ser <b>comum</b> ou <b>raro</b> numa area. Raro quer dizer
 * que aparece, mas em menor numero (ver {@code Especies.pesoEm}).
 */
public final class Habitats {

    /** Quanto um tipo aparece numa area. A ordem importa: da menor para a maior. */
    public enum Presenca { AUSENTE, RARA, COMUM }

    private static final Map<TipoElemental, Map<Area, Presenca>> TABELA = new EnumMap<>(TipoElemental.class);

    static {
        comum(PLANTA,  FLORESTA, FLORESTA_PROFUNDA);
        comum(FOGO,    VULCAO);
        rara(FOGO,     FLORESTA);
        comum(AGUA,    PRAIA, MAR, MAR_PROFUNDO);
        comum(PEDRA,   MONTANHA, RAVINA, VULCAO);
        comum(GELO,    RAVINA_CONGELADA);
        comum(SOMBRIO, FLORESTA_PROFUNDA, MAR_PROFUNDO, MONTANHA_NEGRA);
        comum(SAGRADO, PALACIO_SAGRADO);
    }

    private static void comum(TipoElemental tipo, Area... areas) {
        for (Area a : areas) linha(tipo).put(a, Presenca.COMUM);
    }

    private static void rara(TipoElemental tipo, Area... areas) {
        for (Area a : areas) linha(tipo).put(a, Presenca.RARA);
    }

    private static Map<Area, Presenca> linha(TipoElemental tipo) {
        return TABELA.computeIfAbsent(tipo, t -> new EnumMap<>(Area.class));
    }

    private Habitats() { }

    /** Quanto este tipo aparece nesta area. */
    public static Presenca presenca(TipoElemental tipo, Area area) {
        Map<Area, Presenca> linha = TABELA.get(tipo);
        if (linha == null) return Presenca.AUSENTE;
        return linha.getOrDefault(area, Presenca.AUSENTE);
    }

    /** As areas onde o tipo aparece, comum ou raro. Vazio para o Normal, que nenhuma criatura tem. */
    public static Set<Area> areasDoTipo(TipoElemental tipo) {
        Map<Area, Presenca> linha = TABELA.get(tipo);
        return linha == null ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(linha.keySet()));
    }
}
