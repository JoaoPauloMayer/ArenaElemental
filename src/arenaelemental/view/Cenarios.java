package arenaelemental.view;

import arenaelemental.modelo.TipoElemental;
import arenaelemental.mundo.Area;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

/**
 * A pasta de cenarios e o sorteio de qual usar em cada area.
 *
 * <p>Os cenarios sao apenas os arquivos de imagem de uma pasta: nao ha lista
 * escrita a mao em lugar nenhum. Uma imagem na subpasta {@code "Vulcão/"} vira
 * cenario do Vulcao; uma imagem solta como {@code "ocean four.jpg"} entra no
 * rodizio dos cenarios de Agua, pela classificacao do nome (ver {@link Cenario}).
 *
 * <p>Sem a pasta, ou com a pasta vazia, o jogo continua funcionando: a arena
 * pinta um gradiente na cor da area. Nenhuma imagem e' obrigatoria para o jogo
 * rodar.
 */
public final class Cenarios {

    /** Nomes de pasta procurados, na ordem, a partir de cada raiz candidata. */
    private static final String[] NOMES_DE_PASTA = {
            "imagens de referencia", "imagens de referência", "cenarios", "assets/cenarios"
    };

    private static List<Cenario> cenarios;
    private static Path pastaUsada;

    /** Cache das imagens ja lidas do disco, por caminho. */
    private static final Map<Path, BufferedImage> CACHE = new HashMap<>();

    /** Caminhos que falharam ao abrir — para nao tentar de novo a cada repaint. */
    private static final Map<Path, Boolean> FALHAS = new HashMap<>();

    private Cenarios() { }

    // ------------------------------------------------------------------
    // Descoberta
    // ------------------------------------------------------------------

    /** Todos os cenarios encontrados, carregando a pasta na primeira chamada. */
    public static synchronized List<Cenario> todos() {
        if (cenarios == null) cenarios = descobrir();
        return cenarios;
    }

    /** A pasta de onde os cenarios vieram, ou {@code null} se nenhuma foi achada. */
    public static synchronized Path getPastaUsada() {
        todos();
        return pastaUsada;
    }

    /** Relê a pasta — util depois de acrescentar imagens com o jogo aberto. */
    public static synchronized void recarregar() {
        cenarios = null;
        pastaUsada = null;
        CACHE.clear();
        FALHAS.clear();
    }

    private static List<Cenario> descobrir() {
        for (Path raiz : raizesCandidatas()) {
            for (String nome : NOMES_DE_PASTA) {
                Path candidata = raiz.resolve(nome);
                if (!Files.isDirectory(candidata)) continue;
                List<Cenario> achados = lerPasta(candidata);
                if (!achados.isEmpty()) {
                    pastaUsada = candidata;
                    return achados;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Onde procurar a pasta: o diretorio de trabalho e os dois niveis acima
     * dele. Cobre rodar da raiz do projeto e de dentro de {@code build/}.
     */
    private static List<Path> raizesCandidatas() {
        List<Path> raizes = new ArrayList<>();
        Path atual = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 3 && atual != null; i++) {
            raizes.add(atual);
            atual = atual.getParent();
        }
        return raizes;
    }

    /**
     * Le uma pasta especifica: as imagens soltas nela e as das subpastas com
     * nome de area. Subpastas com qualquer outro nome sao ignoradas. Publico
     * para os testes apontarem para onde quiserem.
     */
    public static List<Cenario> lerPasta(Path pasta) {
        if (pasta == null || !Files.isDirectory(pasta)) return Collections.emptyList();

        List<Cenario> achados = new ArrayList<>();
        try {
            for (Path arquivo : imagensDe(pasta)) {
                String nomeDoArquivo = arquivo.getFileName().toString();
                TipoElemental tipo = Cenario.tipoPeloNome(nomeDoArquivo);
                achados.add(new Cenario(arquivo,
                        Cenario.nomeDeExibicao(nomeDoArquivo),
                        tipo,
                        Cenario.ancoraPeloTipo(tipo)));
            }
            for (Path subpasta : subpastasDe(pasta)) {
                Area area = Area.porNome(subpasta.getFileName().toString());
                if (area == null) continue;
                TipoElemental tipo = area.getTipoPredominante();
                for (Path arquivo : imagensDe(subpasta)) {
                    achados.add(new Cenario(arquivo,
                            Cenario.nomeDeExibicao(arquivo.getFileName().toString()),
                            tipo,
                            Cenario.ancoraPeloTipo(tipo),
                            area));
                }
            }
        } catch (IOException | UncheckedIOException e) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(achados);
    }

    /** As imagens de uma pasta, em ordem alfabetica — para o sorteio ser reproduzivel. */
    private static List<Path> imagensDe(Path pasta) throws IOException {
        try (Stream<Path> arquivos = Files.list(pasta)) {
            List<Path> ordenados = new ArrayList<>();
            arquivos.filter(Files::isRegularFile)
                    .filter(p -> Cenario.ehImagem(p.getFileName().toString()))
                    .forEach(ordenados::add);
            ordenados.sort(Comparator.comparing(p -> p.getFileName().toString()));
            return ordenados;
        }
    }

    private static List<Path> subpastasDe(Path pasta) throws IOException {
        try (Stream<Path> itens = Files.list(pasta)) {
            List<Path> ordenadas = new ArrayList<>();
            itens.filter(Files::isDirectory).forEach(ordenadas::add);
            ordenadas.sort(Comparator.comparing(p -> p.getFileName().toString()));
            return ordenadas;
        }
    }

    // ------------------------------------------------------------------
    // Sorteio
    // ------------------------------------------------------------------

    /**
     * Os cenarios de uma area.
     *
     * <p>Primeiro as imagens da propria area. Sem nenhuma, uma area com tipo
     * predominante pega emprestados os cenarios soltos <em>desse tipo</em> — a
     * Floresta usa as florestas soltas, o Mar os oceanos. Os neutros soltos e
     * as imagens de outra area nunca entram: uma catedral na Montanha combina
     * menos com ela do que o gradiente pintado na cor da area, que e' o que uma
     * lista vazia produz.
     */
    public static List<Cenario> paraArea(List<Cenario> disponiveis, Area area) {
        TipoElemental tipo = area.getTipoPredominante();
        List<Cenario> daArea = new ArrayList<>();
        List<Cenario> emprestados = new ArrayList<>();
        for (Cenario c : disponiveis) {
            if (c.getArea() == area) daArea.add(c);
            else if (c.getArea() == null && tipo != null && c.getTipo() == tipo) emprestados.add(c);
        }
        return Collections.unmodifiableList(daArea.isEmpty() ? emprestados : daArea);
    }

    /**
     * Sorteia um cenario para a area.
     *
     * @return o cenario sorteado, ou {@code null} se nao houver imagem para ela
     */
    public static Cenario sortear(Area area, Random rng) {
        return sortear(todos(), area, rng);
    }

    /** Versao pura, para os testes: sorteia dentro da lista informada. */
    public static Cenario sortear(List<Cenario> disponiveis, Area area, Random rng) {
        List<Cenario> candidatos = paraArea(disponiveis, area);
        if (candidatos.isEmpty()) return null;
        return candidatos.get(rng.nextInt(candidatos.size()));
    }

    /**
     * Os cenarios que servem para uma criatura deste tipo, entre os informados.
     * Sem nenhum do tipo nem neutro, devolve todos.
     */
    public static List<Cenario> paraTipo(List<Cenario> disponiveis, TipoElemental tipo) {
        List<Cenario> combinam = new ArrayList<>();
        for (Cenario c : disponiveis) if (c.serveParaTipo(tipo)) combinam.add(c);
        // sem nenhum do tipo nem neutro, qualquer cenario e' melhor que nenhum
        return Collections.unmodifiableList(combinam.isEmpty() ? disponiveis : combinam);
    }

    /**
     * Sorteia, entre os informados, um cenario para uma criatura deste tipo.
     *
     * @return o cenario sorteado, ou {@code null} se a lista estiver vazia
     */
    public static Cenario sortear(List<Cenario> disponiveis, TipoElemental tipo, Random rng) {
        List<Cenario> candidatos = paraTipo(disponiveis, tipo);
        if (candidatos.isEmpty()) return null;
        return candidatos.get(rng.nextInt(candidatos.size()));
    }

    // ------------------------------------------------------------------
    // Imagens
    // ------------------------------------------------------------------

    /**
     * A imagem do cenario, lida do disco na primeira vez e guardada em cache.
     *
     * @return a imagem, ou {@code null} se o arquivo nao puder ser lido — nesse
     *         caso a arena cai no gradiente pintado
     */
    public static synchronized BufferedImage imagemDe(Cenario cenario) {
        if (cenario == null) return null;
        Path arquivo = cenario.getArquivo();
        if (FALHAS.containsKey(arquivo)) return null;

        BufferedImage guardada = CACHE.get(arquivo);
        if (guardada != null) return guardada;

        try {
            BufferedImage lida = ImageIO.read(arquivo.toFile());
            if (lida == null) {          // formato que o ImageIO nao reconheceu
                FALHAS.put(arquivo, true);
                return null;
            }
            CACHE.put(arquivo, lida);
            return lida;
        } catch (IOException e) {
            FALHAS.put(arquivo, true);
            return null;
        }
    }
}
