package arenaelemental.persistencia;

import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.mundo.Area;
import arenaelemental.treinador.Treinador;

import java.time.LocalDateTime;

/**
 * O que a lista de saves mostra de um slot: se esta vazio, se o arquivo e'
 * legivel e, quando e', quando foi salvo, onde o jogador estava e quem lidera a
 * equipe.
 */
public final class ResumoDoSave {

    private final SlotDeSave slot;
    private final boolean existe;
    private final String erro;
    private final LocalDateTime salvoEm;
    private final Area area;
    private final String nomeDoLider;
    private final int nivelDoLider;
    private final EspecieCriatura especieDoLider;
    private final int tamanhoDaEquipe;
    private final int capturadas;

    private ResumoDoSave(SlotDeSave slot, boolean existe, String erro, JogoSalvo jogo) {
        this.slot = slot;
        this.existe = existe;
        this.erro = erro;
        if (jogo == null) {
            salvoEm = null; area = null; nomeDoLider = null; especieDoLider = null;
            nivelDoLider = 0; tamanhoDaEquipe = 0; capturadas = 0;
            return;
        }
        Treinador t = jogo.getTreinador();
        Criatura lider = t.getEquipe().isEmpty() ? null : t.getEquipe().get(0);
        this.salvoEm = jogo.getSalvoEm();
        this.area = t.getArea();
        this.nomeDoLider = lider == null ? null : lider.getNome();
        this.nivelDoLider = lider == null ? 0 : lider.getNivel();
        this.especieDoLider = lider == null ? null : lider.getEspecie();
        this.tamanhoDaEquipe = t.getEquipe().size();
        this.capturadas = jogo.getBestiario().totalCapturadas();
    }

    static ResumoDoSave vazio(SlotDeSave slot) { return new ResumoDoSave(slot, false, null, null); }

    static ResumoDoSave ilegivel(SlotDeSave slot, String erro) { return new ResumoDoSave(slot, true, erro, null); }

    static ResumoDoSave de(SlotDeSave slot, JogoSalvo jogo) { return new ResumoDoSave(slot, true, null, jogo); }

    public SlotDeSave getSlot() { return slot; }

    /** Se nao ha nada gravado neste slot. */
    public boolean isVazio() { return !existe; }

    /** Se ha um arquivo e ele pode ser carregado. */
    public boolean isLegivel() { return existe && erro == null; }

    /** Por que o arquivo nao pode ser lido, ou {@code null}. */
    public String getErro() { return erro; }

    public LocalDateTime getSalvoEm() { return salvoEm; }
    public Area getArea() { return area; }
    public String getNomeDoLider() { return nomeDoLider; }
    public int getNivelDoLider() { return nivelDoLider; }
    public EspecieCriatura getEspecieDoLider() { return especieDoLider; }
    public int getTamanhoDaEquipe() { return tamanhoDaEquipe; }
    public int getCapturadas() { return capturadas; }

    /** "Floresta Profunda · Ignivo Nv.12 e mais 2 · 4 capturadas", ou o estado do slot. */
    public String descricao() {
        if (isVazio()) return "Vazio";
        if (!isLegivel()) return "Arquivo ilegível: " + erro;
        StringBuilder sb = new StringBuilder(area.getNome());
        if (nomeDoLider != null) {
            sb.append(" · ").append(nomeDoLider).append(" Nv.").append(nivelDoLider);
            if (tamanhoDaEquipe > 1) sb.append(" e mais ").append(tamanhoDaEquipe - 1);
        }
        sb.append(" · ").append(capturadas).append(capturadas == 1 ? " capturada" : " capturadas");
        return sb.toString();
    }
}
