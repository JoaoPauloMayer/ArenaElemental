package arenaelemental.view;

import arenaelemental.modelo.TipoElemental;
import arenaelemental.mundo.Area;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Um cenario: a imagem de fundo desenhada atras das criaturas.
 *
 * <p>Um cenario pode ser <b>de uma area</b> — a imagem esta na subpasta com o
 * nome da area, como {@code "Floresta Profunda/"} — ou <b>solto</b>, direto na
 * pasta de cenarios. O solto tem o tipo elementar tirado do <b>nome do
 * arquivo</b>, nao de uma lista escrita a mao: um {@code "ocean four.jpg"}
 * entra como cenario de Agua sozinho, e as areas de Agua sem imagem propria
 * passam a usa-lo. Nomes que nao caem em nenhuma palavra-chave viram cenarios
 * <b>neutros</b>, que nenhuma area pega emprestado: para usar um deles, basta
 * move-lo para a subpasta de uma area.
 *
 * <p>A {@link #getAncoraVertical() ancora vertical} decide que faixa da imagem
 * sobrevive ao corte: a arena e' uma tira larga e baixa, entao quase toda imagem
 * perde altura. 0.0 mantem o topo, 1.0 mantem o pe, 0.5 corta pelo meio.
 */
public class Cenario {

    /** Extensoes que o {@code ImageIO} le sem plugin extra. */
    static final String[] EXTENSOES = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    private final Path arquivo;
    private final String nome;
    private final TipoElemental tipo;
    private final double ancoraVertical;
    private final Area area;

    /** Cenario solto, sem area. */
    Cenario(Path arquivo, String nome, TipoElemental tipo, double ancoraVertical) {
        this(arquivo, nome, tipo, ancoraVertical, null);
    }

    Cenario(Path arquivo, String nome, TipoElemental tipo, double ancoraVertical, Area area) {
        this.arquivo = arquivo;
        this.nome = nome;
        this.tipo = tipo;
        this.ancoraVertical = Math.max(0.0, Math.min(1.0, ancoraVertical));
        this.area = area;
    }

    public Path getArquivo() { return arquivo; }

    /** Nome de exibicao, tirado do nome do arquivo. */
    public String getNome() { return nome; }

    /** O tipo a que o cenario pertence, ou {@code null} se ele for neutro. */
    public TipoElemental getTipo() { return tipo; }

    /** A area dona desta imagem, ou {@code null} se o cenario for solto. */
    public Area getArea() { return area; }

    /** Se o cenario serve para qualquer batalha. */
    public boolean ehNeutro() { return tipo == null; }

    /** Que faixa da imagem manter ao cortar: 0.0 topo, 0.5 meio, 1.0 pe. */
    public double getAncoraVertical() { return ancoraVertical; }

    /** Se este cenario pode aparecer numa batalha contra uma criatura deste tipo. */
    public boolean serveParaTipo(TipoElemental tipoDaSelvagem) {
        return ehNeutro() || tipo == tipoDaSelvagem;
    }

    @Override
    public String toString() {
        String classe = area != null ? area.getNome() : (ehNeutro() ? "neutro" : tipo.nomeExibicao());
        return nome + " [" + classe + "]";
    }

    // ------------------------------------------------------------------
    // Leitura do nome do arquivo
    // ------------------------------------------------------------------

    /** Se o nome do arquivo tem uma extensao de imagem que sabemos abrir. */
    static boolean ehImagem(String nomeDoArquivo) {
        String minusculo = nomeDoArquivo.toLowerCase(Locale.ROOT);
        for (String ext : EXTENSOES) if (minusculo.endsWith(ext)) return true;
        return false;
    }

    /**
     * O tipo elementar sugerido pelo nome do arquivo, ou {@code null} para
     * neutro. Aceita os termos em ingles e em portugues.
     *
     * <p>A ordem das perguntas decide os nomes que caem em dois tipos: Gelo vem
     * antes de Agua ({@code "frozen lake"} e' Gelo) e Planta antes de Sombrio e
     * Sagrado ({@code "dark forest"} e {@code "twilight forest"} sao Planta).
     */
    static TipoElemental tipoPeloNome(String nomeDoArquivo) {
        String n = semAcentos(nomeDoArquivo.toLowerCase(Locale.ROOT));
        if (contemAlguma(n, "volcano", "vulcao", "lava", "magma", "fire", "fogo", "ember", "inferno")) {
            return TipoElemental.FOGO;
        }
        if (contemAlguma(n, "ice", "gelo", "gelad", "snow", "neve", "frozen", "congelad", "glacier", "geleira",
                "frost", "geada", "winter", "inverno")) {
            return TipoElemental.GELO;
        }
        if (contemAlguma(n, "ocean", "oceano", "sea", "mar", "water", "agua", "river", "rio", "lake", "lago", "beach", "praia")) {
            return TipoElemental.AGUA;
        }
        if (contemAlguma(n, "forest", "floresta", "bosque", "jungle", "selva", "wood", "mata", "grove", "garden", "jardim")) {
            return TipoElemental.PLANTA;
        }
        if (contemAlguma(n, "rock", "stone", "pedra", "rocha", "mountain", "montanha", "canyon", "cliff",
                "penhasco", "ravina", "cave", "caverna")) {
            return TipoElemental.PEDRA;
        }
        if (contemAlguma(n, "dark", "shadow", "sombra", "sombri", "night", "noite", "abyss", "abismo", "trevas")) {
            return TipoElemental.SOMBRIO;
        }
        if (contemAlguma(n, "holy", "sacred", "sagrad", "temple", "templo", "cathedral", "catedral", "palace",
                "palacio", "shrine", "light", "luz")) {
            // "santuario" ficou de fora de proposito: o "rio" dentro dele o
            // mandaria para Agua antes de chegar aqui
            return TipoElemental.SAGRADO;
        }
        return null;
    }

    /**
     * Ancora padrao para o corte, conforme o tipo.
     *
     * <p>Cenarios de Fogo costumam ter a lava embaixo e o ceu vazio em cima, e
     * os de floresta perdem pouco cortando um pouco acima do meio para manter
     * copa e chao na mesma faixa.
     */
    static double ancoraPeloTipo(TipoElemental tipo) {
        if (tipo == TipoElemental.FOGO) return 0.58;
        if (tipo == TipoElemental.PLANTA) return 0.45;
        return 0.5;
    }

    /** "twilight forest.jpg" -> "Twilight Forest". */
    static String nomeDeExibicao(String nomeDoArquivo) {
        String semExtensao = nomeDoArquivo;
        int ponto = semExtensao.lastIndexOf('.');
        if (ponto > 0) semExtensao = semExtensao.substring(0, ponto);
        semExtensao = semExtensao.replace('_', ' ').replace('-', ' ').trim();
        if (semExtensao.isEmpty()) return nomeDoArquivo;

        StringBuilder sb = new StringBuilder();
        for (String palavra : semExtensao.split("\\s+")) {
            if (palavra.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(palavra.charAt(0))).append(palavra.substring(1));
        }
        return sb.toString();
    }

    private static boolean contemAlguma(String texto, String... termos) {
        for (String t : termos) if (texto.contains(t)) return true;
        return false;
    }

    /** Deixa "vulcão" virar "vulcao", para o nome do arquivo poder ter acento. */
    private static String semAcentos(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
