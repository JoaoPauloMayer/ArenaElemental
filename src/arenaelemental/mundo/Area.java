package arenaelemental.mundo;

import arenaelemental.modelo.TipoElemental;

import java.awt.Color;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * As areas do mapa e as rotas entre elas.
 *
 * <p>As rotas sao <b>de mao unica</b>: {@code FLORESTA} leva a {@code MONTANHA},
 * mas nada garante o caminho inverso. Cada linha do bloco {@code static} abaixo
 * diz para onde se pode ir <em>a partir</em> de uma area; explorar a propria
 * area e' sempre possivel e por isso nao aparece nas rotas.
 *
 * <p>O {@link #getId() id} vai para o jogo salvo e, como o id das especies,
 * nunca deve mudar. O nome de exibicao pode ser reescrito a vontade.
 */
public enum Area {

    FLORESTA("floresta", "Floresta", TipoElemental.PLANTA, new Color(46, 184, 92),
            "Trilhas de terra batida entre árvores altas. Um bom lugar para começar."),
    FLORESTA_PROFUNDA("floresta-profunda", "Floresta Profunda", TipoElemental.PLANTA, new Color(22, 140, 96),
            "A copa fecha o céu, e só a luz que escapa pelas folhas mostra o caminho."),
    VULCAO("vulcao", "Vulcão", TipoElemental.FOGO, new Color(240, 72, 32),
            "O chão estala de calor e o ar cheira a enxofre."),
    MONTANHA("montanha", "Montanha", TipoElemental.PEDRA, new Color(172, 138, 100),
            "Encostas de pedra e um vento frio a cada passo."),
    MONTANHA_NEGRA("montanha-negra", "Montanha Negra", TipoElemental.SOMBRIO, new Color(124, 100, 176),
            "Rochas escuras como carvão, envoltas numa névoa que não se dissipa."),
    PALACIO_SAGRADO("palacio-sagrado", "Palácio Sagrado", TipoElemental.SAGRADO, new Color(236, 190, 64),
            "Colunas antigas no topo do mundo. Daqui partem caminhos para todas as terras."),
    RAVINA("ravina", "Ravina", TipoElemental.PEDRA, new Color(210, 112, 60),
            "Paredões de pedra avermelhada, com um rio correndo lá embaixo."),
    RAVINA_CONGELADA("ravina-congelada", "Ravina Congelada", TipoElemental.GELO, new Color(112, 196, 240),
            "O rio da ravina virou gelo, e o vento corta como lâmina."),
    PRAIA("praia", "Praia", TipoElemental.AGUA, new Color(238, 196, 112),
            "Areia morna, conchas e o barulho constante das ondas."),
    MAR("mar", "Mar", TipoElemental.AGUA, new Color(30, 136, 229),
            "Mar aberto até onde a vista alcança."),
    MAR_PROFUNDO("mar-profundo", "Mar Profundo", TipoElemental.AGUA, new Color(52, 72, 200),
            "Longe da costa, a água escurece e o fundo desaparece.");

    /** Onde toda jornada nova comeca. */
    public static final Area INICIAL = FLORESTA;

    private static final Map<Area, List<Area>> ROTAS = new EnumMap<>(Area.class);

    static {
        rotas(FLORESTA,          FLORESTA_PROFUNDA, MONTANHA, RAVINA);
        rotas(FLORESTA_PROFUNDA, FLORESTA);
        rotas(MONTANHA,          FLORESTA, MONTANHA_NEGRA, VULCAO);
        rotas(VULCAO,            MONTANHA);
        rotas(MONTANHA_NEGRA,    MONTANHA, PALACIO_SAGRADO);
        rotas(RAVINA,            FLORESTA, RAVINA_CONGELADA, MAR);
        rotas(RAVINA_CONGELADA,  RAVINA, MAR);
        rotas(PRAIA,             MAR, RAVINA);
        rotas(MAR,               MAR_PROFUNDO, RAVINA_CONGELADA, PRAIA);
        rotas(MAR_PROFUNDO,      MAR);

        // o Palacio Sagrado sai para qualquer area
        List<Area> todasAsOutras = new ArrayList<>();
        for (Area a : values()) if (a != PALACIO_SAGRADO) todasAsOutras.add(a);
        ROTAS.put(PALACIO_SAGRADO, Collections.unmodifiableList(todasAsOutras));
    }

    private static void rotas(Area origem, Area... destinos) {
        ROTAS.put(origem, List.of(destinos));
    }

    private final String id;
    private final String nome;
    private final TipoElemental tipoPredominante;
    private final Color cor;
    private final String descricao;

    Area(String id, String nome, TipoElemental tipoPredominante, Color cor, String descricao) {
        this.id = id;
        this.nome = nome;
        this.tipoPredominante = tipoPredominante;
        this.cor = cor;
        this.descricao = descricao;
    }

    /** Identificador estavel, gravado no jogo salvo. */
    public String getId() { return id; }

    public String getNome() { return nome; }

    /**
     * O tipo elementar que domina a area, ou {@code null} se ela for neutra
     * (hoje nenhuma e'). Decide so os cenarios emprestados quando a area nao
     * tem imagem propria; quem aparece nos encontros vem de {@link Habitats}.
     */
    public TipoElemental getTipoPredominante() { return tipoPredominante; }

    /** Cor da area nos botoes de rota e no fundo pintado da arena. */
    public Color getCor() { return cor; }

    /** Uma frase sobre a area, mostrada ao chegar nela. */
    public String getDescricao() { return descricao; }

    /** Para onde se pode ir a partir daqui, na ordem em que as rotas foram declaradas. */
    public List<Area> getDestinos() { return ROTAS.get(this); }

    /** Se existe rota direta daqui para o destino. */
    public boolean temRotaPara(Area destino) {
        return destino != null && getDestinos().contains(destino);
    }

    /** A area com este id exato, ou {@code null}. */
    public static Area porId(String id) {
        for (Area a : values()) if (a.id.equals(id)) return a;
        return null;
    }

    /**
     * A area com este id ou nome de exibicao, sem ligar para maiusculas,
     * acentos ou separadores: {@code "Palácio Sagrado"}, {@code "palacio_sagrado"}
     * e {@code "palacio-sagrado"} dao todos no {@link #PALACIO_SAGRADO}.
     *
     * @return a area, ou {@code null} se o nome nao for de nenhuma
     */
    public static Area porNome(String nome) {
        if (nome == null) return null;
        String procurado = normalizar(nome);
        for (Area a : values()) {
            if (normalizar(a.id).equals(procurado) || normalizar(a.nome).equals(procurado)) return a;
        }
        return null;
    }

    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.trim().replaceAll("[\\s_-]+", "-");
    }
}
