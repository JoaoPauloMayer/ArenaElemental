package arenaelemental.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static arenaelemental.modelo.CategoriaGolpe.ESPECIAL;
import static arenaelemental.modelo.CategoriaGolpe.FISICO;
import static arenaelemental.modelo.CategoriaGolpe.STATUS;
import static arenaelemental.modelo.TipoElemental.AGUA;
import static arenaelemental.modelo.TipoElemental.FOGO;
import static arenaelemental.modelo.TipoElemental.GELO;
import static arenaelemental.modelo.TipoElemental.NORMAL;
import static arenaelemental.modelo.TipoElemental.PEDRA;
import static arenaelemental.modelo.TipoElemental.PLANTA;
import static arenaelemental.modelo.TipoElemental.SAGRADO;
import static arenaelemental.modelo.TipoElemental.SOMBRIO;

/**
 * Catalogo dos golpes existentes no jogo, indexado pelo id de cada um.
 *
 * <p>Golpes NORMAL sao neutros contra todos os tipos e nunca recebem STAB,
 * porque nenhuma criatura e' do tipo Normal.
 *
 * <p>Os golpes mais fortes pagam por isso em precisao e em PP: e' o que faz a
 * escolha do golpe importar em vez de sempre valer a pena usar o mais forte.
 *
 * <p>Para acrescentar um golpe novo basta declarar mais uma constante com
 * {@link #registrar}: ele entra no catalogo e passa a ser reconhecido pela
 * persistencia automaticamente.
 */
public final class Golpes {

    private static final Map<String, Golpe> CATALOGO = new LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Neutros — qualquer criatura pode aprender
    // ------------------------------------------------------------------

    public static final Golpe INVESTIDA = registrar(
            Golpe.novo("investida", "Investida")
                    .tipo(NORMAL).categoria(FISICO).poder(40).precisao(100).pp(35));

    public static final Golpe TRANCADA = registrar(
            Golpe.novo("trancada", "Trancada")
                    .tipo(NORMAL).categoria(FISICO).poder(80).precisao(100).pp(15)
                    .causa(CondicaoStatus.PARALISIA, 30));

    public static final Golpe FUMACA_TOXICA = registrar(
            Golpe.novo("fumaca-toxica", "Fumaça Tóxica")
                    .tipo(NORMAL).categoria(STATUS).precisao(90).pp(10)
                    .causa(CondicaoStatus.VENENO, 100));

    /**
     * O golpe de ultimo recurso: aparece sozinho quando todos os outros ficam
     * sem PP, nunca erra, nao gasta PP e cobra um quarto do HP maximo de quem o
     * usa. Sem ele, uma criatura sem PP travaria a batalha.
     */
    public static final Golpe ESFORCO = registrar(
            Golpe.novo("esforco", "Esforço")
                    .tipo(NORMAL).categoria(FISICO).poder(50).precisao(100).pp(0)
                    .semLimiteDePp().recuo(25));

    // ------------------------------------------------------------------
    // Fogo
    // ------------------------------------------------------------------

    public static final Golpe BRASA = registrar(
            Golpe.novo("brasa", "Brasa")
                    .tipo(FOGO).categoria(ESPECIAL).poder(40).precisao(100).pp(25)
                    .causa(CondicaoStatus.QUEIMADURA, 10));

    public static final Golpe PRESA_IGNEA = registrar(
            Golpe.novo("presa-ignea", "Presa Ígnea")
                    .tipo(FOGO).categoria(FISICO).poder(65).precisao(95).pp(15)
                    .causa(CondicaoStatus.QUEIMADURA, 10));

    public static final Golpe LABAREDA = registrar(
            Golpe.novo("labareda", "Labareda")
                    .tipo(FOGO).categoria(ESPECIAL).poder(90).precisao(95).pp(15)
                    .causa(CondicaoStatus.QUEIMADURA, 10));

    // ------------------------------------------------------------------
    // Agua
    // ------------------------------------------------------------------

    public static final Golpe JATO_DAGUA = registrar(
            Golpe.novo("jato-dagua", "Jato d'Água")
                    .tipo(AGUA).categoria(ESPECIAL).poder(40).precisao(100).pp(25));

    public static final Golpe AQUA_GARRA = registrar(
            Golpe.novo("aqua-garra", "Aqua Garra")
                    .tipo(AGUA).categoria(FISICO).poder(60).precisao(100).pp(20));

    public static final Golpe SOPRO_GELADO = registrar(
            Golpe.novo("sopro-gelado", "Sopro Gelado")
                    .tipo(AGUA).categoria(ESPECIAL).poder(55).precisao(95).pp(15)
                    .causa(CondicaoStatus.CONGELAMENTO, 10));

    public static final Golpe MARE_CHEIA = registrar(
            Golpe.novo("mare-cheia", "Maré Cheia")
                    .tipo(AGUA).categoria(ESPECIAL).poder(90).precisao(95).pp(15));

    // ------------------------------------------------------------------
    // Planta
    // ------------------------------------------------------------------

    public static final Golpe CHICOTE_VINHA = registrar(
            Golpe.novo("chicote-vinha", "Chicote de Vinha")
                    .tipo(PLANTA).categoria(FISICO).poder(45).precisao(100).pp(25));

    public static final Golpe FOLHA_NAVALHA = registrar(
            Golpe.novo("folha-navalha", "Folha Navalha")
                    .tipo(PLANTA).categoria(ESPECIAL).poder(55).precisao(95).pp(25));

    public static final Golpe ESPORO_SONIFERO = registrar(
            Golpe.novo("esporo-sonifero", "Esporo Sonífero")
                    .tipo(PLANTA).categoria(STATUS).precisao(75).pp(15)
                    .causa(CondicaoStatus.SONO, 100));

    public static final Golpe BOMBA_SEMENTE = registrar(
            Golpe.novo("bomba-semente", "Bomba Semente")
                    .tipo(PLANTA).categoria(FISICO).poder(80).precisao(100).pp(15));

    // ------------------------------------------------------------------
    // Pedra
    // ------------------------------------------------------------------

    public static final Golpe PEDRADA = registrar(
            Golpe.novo("pedrada", "Pedrada")
                    .tipo(PEDRA).categoria(FISICO).poder(40).precisao(100).pp(25));

    public static final Golpe ROCHA_LANCADA = registrar(
            Golpe.novo("rocha-lancada", "Rocha Lançada")
                    .tipo(PEDRA).categoria(FISICO).poder(65).precisao(95).pp(15));

    public static final Golpe TREMOR = registrar(
            Golpe.novo("tremor", "Tremor")
                    .tipo(PEDRA).categoria(FISICO).poder(55).precisao(100).pp(15)
                    .causa(CondicaoStatus.PARALISIA, 20));

    public static final Golpe DESMORONAMENTO = registrar(
            Golpe.novo("desmoronamento", "Desmoronamento")
                    .tipo(PEDRA).categoria(FISICO).poder(90).precisao(90).pp(10));

    // ------------------------------------------------------------------
    // Gelo
    // ------------------------------------------------------------------

    public static final Golpe GRANIZO = registrar(
            Golpe.novo("granizo", "Granizo")
                    .tipo(GELO).categoria(ESPECIAL).poder(40).precisao(100).pp(25));

    public static final Golpe RAJADA_GELIDA = registrar(
            Golpe.novo("rajada-gelida", "Rajada Gélida")
                    .tipo(GELO).categoria(ESPECIAL).poder(65).precisao(95).pp(15));

    public static final Golpe GEADA = registrar(
            Golpe.novo("geada", "Geada")
                    .tipo(GELO).categoria(ESPECIAL).poder(50).precisao(95).pp(15)
                    .causa(CondicaoStatus.CONGELAMENTO, 15));

    public static final Golpe NEVASCA = registrar(
            Golpe.novo("nevasca", "Nevasca")
                    .tipo(GELO).categoria(ESPECIAL).poder(90).precisao(90).pp(10)
                    .causa(CondicaoStatus.CONGELAMENTO, 10));

    // ------------------------------------------------------------------
    // Sombrio
    // ------------------------------------------------------------------

    public static final Golpe GARRA_SOMBRIA = registrar(
            Golpe.novo("garra-sombria", "Garra Sombria")
                    .tipo(SOMBRIO).categoria(FISICO).poder(40).precisao(100).pp(25));

    public static final Golpe GOLPE_TRAICOEIRO = registrar(
            Golpe.novo("golpe-traicoeiro", "Golpe Traiçoeiro")
                    .tipo(SOMBRIO).categoria(FISICO).poder(65).precisao(95).pp(15));

    public static final Golpe PESADELO = registrar(
            Golpe.novo("pesadelo", "Pesadelo")
                    .tipo(SOMBRIO).categoria(STATUS).precisao(75).pp(15)
                    .causa(CondicaoStatus.SONO, 100));

    public static final Golpe ECLIPSE = registrar(
            Golpe.novo("eclipse", "Eclipse")
                    .tipo(SOMBRIO).categoria(ESPECIAL).poder(90).precisao(95).pp(15));

    // ------------------------------------------------------------------
    // Sagrado
    // ------------------------------------------------------------------

    public static final Golpe LAMPEJO = registrar(
            Golpe.novo("lampejo", "Lampejo")
                    .tipo(SAGRADO).categoria(ESPECIAL).poder(40).precisao(100).pp(25));

    public static final Golpe LANCA_DE_LUZ = registrar(
            Golpe.novo("lanca-de-luz", "Lança de Luz")
                    .tipo(SAGRADO).categoria(ESPECIAL).poder(60).precisao(100).pp(20));

    public static final Golpe CLARAO_OFUSCANTE = registrar(
            Golpe.novo("clarao-ofuscante", "Clarão Ofuscante")
                    .tipo(SAGRADO).categoria(STATUS).precisao(90).pp(15)
                    .causa(CondicaoStatus.PARALISIA, 100));

    public static final Golpe LUZ_DIVINA = registrar(
            Golpe.novo("luz-divina", "Luz Divina")
                    .tipo(SAGRADO).categoria(ESPECIAL).poder(90).precisao(95).pp(15));

    // ------------------------------------------------------------------

    /** Coloca o golpe no catalogo. Recusa ids repetidos. */
    public static Golpe registrar(Golpe.Builder builder) {
        Golpe g = builder.construir();
        if (CATALOGO.putIfAbsent(g.getId(), g) != null) {
            throw new IllegalArgumentException("Ja existe um golpe com o id '" + g.getId() + "'");
        }
        return g;
    }

    /** O golpe com este id, ou {@code null} se nao existir mais no catalogo. */
    public static Golpe porId(String id) { return CATALOGO.get(id); }

    /** Todos os golpes do catalogo, na ordem em que foram declarados. */
    public static List<Golpe> todos() {
        return Collections.unmodifiableList(new ArrayList<>(CATALOGO.values()));
    }

    private Golpes() { }
}
