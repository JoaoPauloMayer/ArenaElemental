package arenaelemental.batalha;

import arenaelemental.modelo.CategoriaGolpe;
import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.TipoElemental;
import java.util.Random;

/**
 * A formula de dano da 9a geracao.
 *
 * <pre>
 *   Dano = [ (((2 * Nivel / 5 + 2) * Poder * Ataque / Defesa) / 50) + 2 ]
 *          x Targets x Weather x Critical x Random x STAB x Type x Burn x Item x Screens x Other
 * </pre>
 *
 * <p>Cada etapa trunca os decimais para baixo antes do multiplicador seguinte.
 *
 * <p>Termos ativos: <b>Critical</b>, <b>Random</b>, <b>STAB</b>, <b>Type</b>,
 * <b>Burn</b> (um atacante queimado bate metade com golpes fisicos) e
 * <b>Other</b>, que carrega a habilidade da especie.
 *
 * <p>Termos que este projeto ainda nao tem, e que por isso valem sempre 1:
 * <b>Targets</b> (nao ha batalha em dupla), <b>Weather</b> (nao ha clima),
 * <b>Item</b> (nao ha itens equipados) e <b>Screens</b> (nao ha Reflect/Light
 * Screen). Terastalizacao nunca sera implementada, entao o STAB e' sempre 1.5x.
 */
public class CalculadoraDano {

    /** Chance de acerto critico: 1 em 24, como no estagio 0 dos jogos atuais. */
    public static final int CHANCE_CRITICO = 24;

    /** Multiplicador de um acerto critico. */
    public static final double MULT_CRITICO = 1.5;

    /** Bonus de mesmo tipo (Same-Type Attack Bonus). */
    public static final double MULT_STAB = 1.5;

    /** O termo <i>Burn</i>: um atacante queimado bate metade com golpes fisicos. */
    public static final double MULT_QUEIMADURA = 0.5;

    /** Se o termo <i>Burn</i> se aplica: atacante queimado usando golpe fisico. */
    private static boolean queimado(Criatura atacante, Golpe golpe) {
        return atacante.getCondicao() == CondicaoStatus.QUEIMADURA
                && golpe.getCategoria() == CategoriaGolpe.FISICO;
    }

    private final Random rng;

    public CalculadoraDano() { this(new Random()); }

    /** Construtor com gerador proprio, para testes deterministicos. */
    public CalculadoraDano(Random rng) { this.rng = rng; }

    public ResultadoDano calcular(Criatura atacante, Criatura alvo, Golpe golpe) {
        boolean critico = rng.nextInt(CHANCE_CRITICO) == 0;
        int rolagem = 85 + rng.nextInt(16); // 85..100 inclusive
        return calcular(atacante, alvo, golpe, critico, rolagem);
    }

    /**
     * Versao com critico e rolagem informados — usada pelos testes e pelo
     * calculo dos extremos de dano.
     *
     * @param rolagem valor do damage roll, de 85 a 100
     */
    public ResultadoDano calcular(Criatura atacante, Criatura alvo, Golpe golpe,
                                  boolean critico, int rolagem) {
        // contra dois tipos os multiplicadores se multiplicam: 2x e 2x dao 4x,
        // 2x e 0,5x se anulam
        double efetividade = 1.0;
        for (TipoElemental tipoDoAlvo : alvo.getTipos()) {
            efetividade *= golpe.getTipo().multiplicadorContra(tipoDoAlvo);
        }
        boolean stab = atacante.temTipo(golpe.getTipo());

        if (golpe.getPoder() <= 0 || efetividade == 0.0) {
            return new ResultadoDano(golpe, 0, false, efetividade, stab, rolagem);
        }

        int ataque = atacante.ataqueEfetivo(golpe.getCategoria());
        int defesa = Math.max(1, alvo.defesaEfetiva(golpe.getCategoria()));

        // --- base: tudo em inteiros, que ja truncam para baixo ---
        int fatorNivel = (2 * atacante.getNivel()) / 5 + 2;
        long parcial = (long) fatorNivel * golpe.getPoder() * ataque;
        int dano = (int) (parcial / defesa / 50) + 2;

        // --- cadeia de multiplicadores, truncando a cada passo ---
        // Targets e Weather ficam de fora (valem 1)
        if (critico)   dano = truncar(dano, MULT_CRITICO);
        dano = truncar(dano, rolagem / 100.0);
        if (stab)      dano = truncar(dano, MULT_STAB);
        dano = truncar(dano, efetividade);
        if (queimado(atacante, golpe)) dano = truncar(dano, MULT_QUEIMADURA); // termo "Burn"
        // Item e Screens ficam de fora (valem 1)
        dano = truncar(dano, atacante.multiplicadorHabilidade(alvo, golpe)); // termo "Other"

        // um golpe que acerta sempre tira ao menos 1 de vida
        return new ResultadoDano(golpe, Math.max(1, dano), critico, efetividade, stab, rolagem);
    }

    private static int truncar(int valor, double multiplicador) {
        return (int) Math.floor(valor * multiplicador);
    }

    /** Menor dano possivel do golpe (rolagem 85, sem critico). */
    public int danoMinimo(Criatura atacante, Criatura alvo, Golpe golpe) {
        return calcular(atacante, alvo, golpe, false, 85).getDano();
    }

    /** Maior dano possivel do golpe (rolagem 100, sem critico). */
    public int danoMaximo(Criatura atacante, Criatura alvo, Golpe golpe) {
        return calcular(atacante, alvo, golpe, false, 100).getDano();
    }
}
