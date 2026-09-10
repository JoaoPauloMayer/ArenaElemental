package arenaelemental.modelo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import arenaelemental.modelo.Aparencia.Adorno;
import arenaelemental.mundo.Area;
import arenaelemental.mundo.Habitats;

/**
 * O registro de todas as especies do jogo.
 *
 * <p>E' a unica lista de especies que existe: a fabrica de encontros, a tela de
 * escolha inicial e o bestiario leem daqui, em vez de cada um manter o seu
 * proprio {@code switch}. Acrescentar uma especie e' acrescentar um bloco
 * {@link #registrar(EspecieCriatura)} — nada mais precisa ser tocado.
 *
 * <p>O {@code id} de cada especie e' gravado no jogo salvo e no bestiario, e
 * por isso <b>nunca deve mudar</b>. O nome de exibicao pode.
 */
public final class Especies {

    /** Declarado antes das constantes: elas se registram durante a inicializacao. */
    private static final Map<String, EspecieCriatura> REGISTRO = new LinkedHashMap<>();

    public static final EspecieCriatura BRASEIRO = registrar(
            EspecieCriatura.novo("braseiro", "Braseiro")
                    .tipo(TipoElemental.FOGO)
                    .base(44, 58, 42, 62, 48, 66).rendimentoExp(62)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(230, 92, 46), Adorno.CHAMA)
                    .habilidade("Labareda: +30% de dano contra alvos com vida abaixo de 30%")
                    .nomes("Braseiro", "Ignivo", "Chamotim")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.BRASA)
                    .aprende(15, Golpes.PRESA_IGNEA)
                    .aprende(20, Golpes.FUMACA_TOXICA)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.LABAREDA)
                    .inicial()
                    .construtor(Braseiro::new)
                    .construir());

    public static final EspecieCriatura MARULHO = registrar(
            EspecieCriatura.novo("marulho", "Marulho")
                    .tipo(TipoElemental.AGUA)
                    .base(52, 50, 62, 54, 58, 44).rendimentoExp(63)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(58, 130, 214), Adorno.ONDAS)
                    .habilidade("Maré Cheia: +15% de dano enquanto a própria vida estiver acima de 50%")
                    .nomes("Marulho", "Aquário", "Gotinho")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.JATO_DAGUA)
                    .aprende(15, Golpes.AQUA_GARRA)
                    .aprende(20, Golpes.SOPRO_GELADO)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.MARE_CHEIA)
                    .inicial()
                    .construtor(Marulho::new)
                    .construir());

    public static final EspecieCriatura FOLHARAL = registrar(
            EspecieCriatura.novo("folharal", "Folharal")
                    .tipo(TipoElemental.PLANTA)
                    .base(48, 46, 52, 64, 56, 54).rendimentoExp(64)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(87, 168, 74), Adorno.FOLHAS)
                    .habilidade("Sugar Seiva: recupera 20% do dano causado como vida")
                    .nomes("Folharal", "Brotante", "Ramalho")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.CHICOTE_VINHA)
                    .aprende(15, Golpes.FOLHA_NAVALHA)
                    .aprende(20, Golpes.ESPORO_SONIFERO)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.BOMBA_SEMENTE)
                    .inicial()
                    .construtor(Folharal::new)
                    .construir());

    public static final EspecieCriatura CALHAU = registrar(
            EspecieCriatura.novo("calhau", "Calhau")
                    .tipo(TipoElemental.PEDRA)
                    .base(56, 62, 72, 36, 50, 44).rendimentoExp(64)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(150, 128, 104), Adorno.ESPINHOS)
                    .habilidade("Rolo Compressor: +25% de dano contra alvos mais rápidos que ele")
                    .nomes("Calhau", "Pedrusco", "Seixo")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.PEDRADA)
                    .aprende(15, Golpes.ROCHA_LANCADA)
                    .aprende(20, Golpes.TREMOR)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.DESMORONAMENTO)
                    .construtor(Calhau::new)
                    .construir());

    public static final EspecieCriatura NEVISCO = registrar(
            EspecieCriatura.novo("nevisco", "Nevisco")
                    .tipo(TipoElemental.GELO)
                    .base(46, 44, 48, 64, 52, 66).rendimentoExp(63)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(150, 208, 236), Adorno.CRISTAL)
                    .habilidade("Frio Cortante: +25% de dano contra alvos com condição de status")
                    .nomes("Nevisco", "Friúme", "Gelito")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.GRANIZO)
                    .aprende(15, Golpes.RAJADA_GELIDA)
                    .aprende(20, Golpes.GEADA)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.NEVASCA)
                    .construtor(Nevisco::new)
                    .construir());

    public static final EspecieCriatura PENUMBRA = registrar(
            EspecieCriatura.novo("penumbra", "Penumbra")
                    .tipo(TipoElemental.SOMBRIO)
                    .base(48, 64, 44, 56, 44, 64).rendimentoExp(63)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(92, 74, 138), Adorno.CHIFRES)
                    .habilidade("Emboscada: +30% de dano contra alvos com a vida cheia")
                    .nomes("Penumbra", "Breuzinho", "Negrume")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.GARRA_SOMBRIA)
                    .aprende(15, Golpes.GOLPE_TRAICOEIRO)
                    .aprende(20, Golpes.PESADELO)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.ECLIPSE)
                    .construtor(Penumbra::new)
                    .construir());

    public static final EspecieCriatura CANDEIO = registrar(
            EspecieCriatura.novo("candeio", "Candeio")
                    .tipo(TipoElemental.SAGRADO)
                    .base(58, 40, 54, 60, 68, 40).rendimentoExp(65)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(240, 214, 140), Adorno.AUREOLA)
                    .habilidade("Bênção: recupera 1/16 do HP máximo a cada golpe que causa dano")
                    .nomes("Candeio", "Lumiar", "Círio")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.LAMPEJO)
                    .aprende(15, Golpes.LANCA_DE_LUZ)
                    .aprende(20, Golpes.CLARAO_OFUSCANTE)
                    .aprende(25, Golpes.TRANCADA)
                    .aprende(35, Golpes.LUZ_DIVINA)
                    .construtor(Candeio::new)
                    .construir());

    // ------------------------------------------------------------------
    // Dois tipos: vivem so onde os dois tipos se encontram (ver Habitats)
    // ------------------------------------------------------------------

    public static final EspecieCriatura BREUMAR = registrar(
            EspecieCriatura.novo("breumar", "Breumar")
                    .tipos(TipoElemental.SOMBRIO, TipoElemental.AGUA)       // Mar Profundo
                    .base(54, 58, 50, 62, 52, 44).rendimentoExp(66)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(34, 50, 96), Adorno.ANTENA)
                    .habilidade("Isca Luminosa: +25% de dano com golpes especiais")
                    .nomes("Breumar", "Lanterneiro", "Fundão")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.GARRA_SOMBRIA)
                    .aprende(10, Golpes.JATO_DAGUA)
                    .aprende(20, Golpes.PESADELO)
                    .aprende(30, Golpes.MARE_CHEIA)
                    .aprende(38, Golpes.ECLIPSE)
                    .construtor(Breumar::new)
                    .construir());

    public static final EspecieCriatura TOCUMBRA = registrar(
            EspecieCriatura.novo("tocumbra", "Tocumbra")
                    .tipos(TipoElemental.SOMBRIO, TipoElemental.PLANTA)     // Floresta Profunda
                    .base(60, 62, 60, 44, 54, 40).rendimentoExp(66)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(62, 84, 60), Adorno.GALHOS)
                    .habilidade("Emaranhado: +25% de dano com golpes físicos")
                    .nomes("Tocumbra", "Raizeira", "Galhudo")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.GARRA_SOMBRIA)
                    .aprende(10, Golpes.CHICOTE_VINHA)
                    .aprende(20, Golpes.ESPORO_SONIFERO)
                    .aprende(30, Golpes.GOLPE_TRAICOEIRO)
                    .aprende(38, Golpes.BOMBA_SEMENTE)
                    .construtor(Tocumbra::new)
                    .construir());

    public static final EspecieCriatura BRASALTO = registrar(
            EspecieCriatura.novo("brasalto", "Brasalto")
                    .tipos(TipoElemental.FOGO, TipoElemental.PEDRA)         // Vulcao
                    .base(58, 66, 68, 50, 44, 34).rendimentoExp(66)
                    .grupoExp(GrupoExperiencia.MEDIO_RAPIDO)
                    .aparencia(new Color(118, 74, 58), Adorno.MAGMA)
                    .habilidade("Erupção: +30% de dano enquanto a própria vida estiver abaixo da metade")
                    .nomes("Brasalto", "Lavrocha", "Tição")
                    .aprende(1,  Golpes.INVESTIDA)
                    .aprende(5,  Golpes.BRASA)
                    .aprende(10, Golpes.PEDRADA)
                    .aprende(20, Golpes.TREMOR)
                    .aprende(30, Golpes.PRESA_IGNEA)
                    .aprende(38, Golpes.DESMORONAMENTO)
                    .construtor(Brasalto::new)
                    .construir());

    /**
     * Coloca a especie no registro. Recusa ids repetidos, que fariam duas
     * especies disputarem a mesma entrada do bestiario e do jogo salvo.
     */
    public static EspecieCriatura registrar(EspecieCriatura especie) {
        if (REGISTRO.putIfAbsent(especie.getId(), especie) != null) {
            throw new IllegalArgumentException("Ja existe uma especie com o id '" + especie.getId() + "'");
        }
        return especie;
    }

    /** Todas as especies, na ordem em que foram registradas. */
    public static List<EspecieCriatura> todas() {
        return Collections.unmodifiableList(new ArrayList<>(REGISTRO.values()));
    }

    /** A especie com este id, ou {@code null} se ela nao existir mais. */
    public static EspecieCriatura porId(String id) { return REGISTRO.get(id); }

    /** As especies oferecidas na tela de escolha da criatura inicial. */
    public static List<EspecieCriatura> iniciais() {
        List<EspecieCriatura> lista = new ArrayList<>();
        for (EspecieCriatura e : REGISTRO.values()) if (e.ehInicial()) lista.add(e);
        return Collections.unmodifiableList(lista);
    }

    /** As especies que tem este tipo, como principal ou como secundario. */
    public static List<EspecieCriatura> doTipo(TipoElemental tipo) {
        List<EspecieCriatura> lista = new ArrayList<>();
        for (EspecieCriatura e : REGISTRO.values()) if (e.temTipo(tipo)) lista.add(e);
        return Collections.unmodifiableList(lista);
    }

    /** Uma especie qualquer, sorteada entre todas as registradas, sem olhar a area. */
    public static EspecieCriatura sortear(Random rng) {
        List<EspecieCriatura> lista = todas();
        return lista.get(rng.nextInt(lista.size()));
    }

    // ------------------------------------------------------------------
    // Encontros por area
    // ------------------------------------------------------------------

    /** Peso de uma especie que e' comum na area. */
    public static final int PESO_COMUM = 3;

    /** Peso de uma especie que aparece na area, mas em menor numero. */
    public static final int PESO_RARO = 1;

    /**
     * Quanto a especie pesa no sorteio de encontros desta area: 0 se ela nao
     * vive la, {@link #PESO_RARO} se algum dos tipos dela e' raro la (Fogo na
     * Floresta, por exemplo), {@link #PESO_COMUM} nos outros casos. O habitat
     * sai de {@link Habitats}.
     */
    public static int pesoEm(EspecieCriatura especie, Area area) {
        if (!especie.viveEm(area)) return 0;
        for (TipoElemental t : especie.getTipos()) {
            if (Habitats.presenca(t, area) == Habitats.Presenca.RARA) return PESO_RARO;
        }
        return PESO_COMUM;
    }

    /** As especies que podem aparecer nesta area, na ordem do registro. */
    public static List<EspecieCriatura> daArea(Area area) {
        List<EspecieCriatura> lista = new ArrayList<>();
        for (EspecieCriatura e : REGISTRO.values()) if (pesoEm(e, area) > 0) lista.add(e);
        return Collections.unmodifiableList(lista);
    }

    /** Sorteia a especie de um encontro nesta area, respeitando os pesos. */
    public static EspecieCriatura sortear(Area area, Random rng) {
        List<EspecieCriatura> candidatas = daArea(area);
        if (candidatas.isEmpty()) {
            throw new IllegalStateException("Nenhuma espécie vive em " + area.getNome() + " (ver Habitats)");
        }
        int total = 0;
        for (EspecieCriatura e : candidatas) total += pesoEm(e, area);
        int sorteio = rng.nextInt(total);
        for (EspecieCriatura e : candidatas) {
            sorteio -= pesoEm(e, area);
            if (sorteio < 0) return e;
        }
        throw new IllegalStateException("sorteio fora da soma dos pesos");
    }

    /** Quantas especies existem no jogo — o total do bestiario. */
    public static int total() { return REGISTRO.size(); }

    private Especies() { }
}
