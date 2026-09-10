package arenaelemental.modelo;

import java.awt.Color;

/**
 * As condicoes de status nao volateis: as que ficam na criatura depois que a
 * batalha acaba, e das quais so uma pode estar ativa por vez.
 *
 * <p>Cada uma age em um momento diferente do turno:
 *
 * <table border="1">
 *   <caption>Efeito de cada condicao</caption>
 *   <tr><th>Condicao</th><th>Antes de agir</th><th>No dano</th><th>Fim do turno</th></tr>
 *   <tr><td>Queimadura</td><td>—</td><td>golpes fisicos a 0,5x (o termo <i>Burn</i>)</td><td>perde 1/16 do HP maximo</td></tr>
 *   <tr><td>Paralisia</td><td>25% de perder o turno</td><td>Velocidade pela metade</td><td>—</td></tr>
 *   <tr><td>Veneno</td><td>—</td><td>—</td><td>perde 1/8 do HP maximo</td></tr>
 *   <tr><td>Sono</td><td>nao age por 1 a 3 turnos</td><td>—</td><td>—</td></tr>
 *   <tr><td>Congelamento</td><td>nao age; 20% de descongelar por turno</td><td>—</td><td>—</td></tr>
 * </table>
 *
 * <p>Descansar cura a condicao junto com a vida, e subir de nivel nao a cura —
 * como nos jogos originais.
 */
public enum CondicaoStatus {

    NENHUMA("Nenhuma", "", new Color(150, 155, 160), 0),
    QUEIMADURA("Queimadura", "QUE", new Color(230, 92, 46), 16),
    PARALISIA("Paralisia", "PAR", new Color(226, 178, 42), 0),
    VENENO("Veneno", "VEN", new Color(150, 84, 168), 8),
    SONO("Sono", "SON", new Color(110, 120, 134), 0),
    CONGELAMENTO("Congelamento", "CON", new Color(96, 178, 214), 0);

    /** Chance de a paralisia tirar o turno da criatura, em porcentagem. */
    public static final int CHANCE_PARALISIA_TRAVAR = 25;

    /** Chance de descongelar no comeco do turno, em porcentagem. */
    public static final int CHANCE_DESCONGELAR = 20;

    /** Menor e maior duracao do sono, em turnos. */
    public static final int SONO_MINIMO = 1, SONO_MAXIMO = 3;

    private final String exibicao;
    private final String sigla;
    private final Color cor;
    private final int divisorDoDanoResidual;

    CondicaoStatus(String exibicao, String sigla, Color cor, int divisorDoDanoResidual) {
        this.exibicao = exibicao;
        this.sigla = sigla;
        this.cor = cor;
        this.divisorDoDanoResidual = divisorDoDanoResidual;
    }

    public String nomeExibicao() { return exibicao; }

    /** Tres letras para a etiqueta ao lado da barra de vida. */
    public String sigla() { return sigla; }

    public Color cor() { return cor; }

    /** Se a condicao tira vida no fim de cada turno. */
    public boolean temDanoResidual() { return divisorDoDanoResidual > 0; }

    /** Quanto a condicao tira da criatura no fim do turno (ao menos 1). */
    public int danoResidual(Criatura criatura) {
        if (!temDanoResidual()) return 0;
        return Math.max(1, criatura.getHpMaximo() / divisorDoDanoResidual);
    }

    /** Se a condicao impede a criatura de agir antes mesmo de sortear. */
    public boolean impedeDeAgir() { return this == SONO || this == CONGELAMENTO; }

    /** Mensagem de combate quando a condicao pega. */
    public String mensagemAoPegar(Criatura alvo) {
        switch (this) {
            case QUEIMADURA:    return alvo.getNome() + " se queimou!";
            case PARALISIA:     return alvo.getNome() + " ficou paralisado!";
            case VENENO:        return alvo.getNome() + " foi envenenado!";
            case SONO:          return alvo.getNome() + " adormeceu!";
            case CONGELAMENTO:  return alvo.getNome() + " foi congelado!";
            default:            return "";
        }
    }

    /** Mensagem do dano de fim de turno. */
    public String mensagemDoDanoResidual(Criatura alvo, int dano) {
        String origem = this == QUEIMADURA ? "pela queimadura" : "pelo veneno";
        return alvo.getNome() + " perdeu " + dano + " de vida " + origem + ".";
    }
}
