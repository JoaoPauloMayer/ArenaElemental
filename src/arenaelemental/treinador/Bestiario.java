package arenaelemental.treinador;

import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.Especies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O registro de especies do jogador: quais ele ja encontrou e quais ja capturou.
 *
 * <p>Cada especie tem um dos tres estados de {@link Registro}. Encontrar uma
 * criatura selvagem marca a especie como {@code VISTA}; captura-la sobe para
 * {@code CAPTURADA}, e esse estado nunca regride — encontrar de novo uma
 * especie ja capturada nao a rebaixa.
 *
 * <p>As entradas sao guardadas pelo id da {@link EspecieCriatura}, e nao pelo
 * nome da classe Java: renomear ou trocar a classe de uma especie nao apaga o
 * progresso de quem ja jogou, e especies sem subclasse propria contam
 * normalmente.
 */
public class Bestiario {

    /** Situacao de uma especie no bestiario. */
    public enum Registro {
        NAO_VISTA("Não vista"), VISTA("Vista"), CAPTURADA("Capturada");

        private final String exibicao;
        Registro(String exibicao) { this.exibicao = exibicao; }
        public String nomeExibicao() { return exibicao; }
    }

    private final Map<String, Registro> registros = new LinkedHashMap<>();

    // ------------------------------------------------------------------
    // Escrita
    // ------------------------------------------------------------------

    /**
     * Marca a especie como vista.
     *
     * @return {@code true} se ela ainda nao era conhecida
     */
    public boolean marcarVista(EspecieCriatura especie) {
        if (especie == null) return false;
        Registro atual = registros.get(especie.getId());
        if (atual != null) return false; // ja era VISTA ou CAPTURADA
        registros.put(especie.getId(), Registro.VISTA);
        return true;
    }

    public boolean marcarVista(Criatura criatura) {
        return criatura != null && marcarVista(criatura.getEspecie());
    }

    /**
     * Marca a especie como capturada. Uma especie capturada tambem conta como
     * vista, mesmo que o encontro nao tenha sido registrado antes.
     *
     * @return {@code true} se ela ainda nao estava capturada
     */
    public boolean marcarCapturada(EspecieCriatura especie) {
        if (especie == null) return false;
        Registro atual = registros.get(especie.getId());
        if (atual == Registro.CAPTURADA) return false;
        registros.put(especie.getId(), Registro.CAPTURADA);
        return true;
    }

    public boolean marcarCapturada(Criatura criatura) {
        return criatura != null && marcarCapturada(criatura.getEspecie());
    }

    /** Reaplica um estado vindo do jogo salvo. Ids desconhecidos sao ignorados. */
    public void restaurar(String idEspecie, Registro registro) {
        if (registro == null || registro == Registro.NAO_VISTA) return;
        if (Especies.porId(idEspecie) == null) return;
        registros.put(idEspecie, registro);
    }

    /** Esvazia o bestiario — usado ao comecar uma jornada nova. */
    public void limpar() { registros.clear(); }

    // ------------------------------------------------------------------
    // Leitura
    // ------------------------------------------------------------------

    public Registro estado(EspecieCriatura especie) {
        if (especie == null) return Registro.NAO_VISTA;
        return registros.getOrDefault(especie.getId(), Registro.NAO_VISTA);
    }

    public Registro estado(Criatura criatura) {
        return criatura == null ? Registro.NAO_VISTA : estado(criatura.getEspecie());
    }

    public boolean foiVista(EspecieCriatura especie) { return estado(especie) != Registro.NAO_VISTA; }

    public boolean foiCapturada(EspecieCriatura especie) { return estado(especie) == Registro.CAPTURADA; }

    /** Quantas especies o jogador ja encontrou — capturadas incluidas. */
    public int totalVistas() { return registros.size(); }

    /** Quantas especies o jogador ja capturou. */
    public int totalCapturadas() {
        int n = 0;
        for (Registro r : registros.values()) if (r == Registro.CAPTURADA) n++;
        return n;
    }

    /** Quantas especies existem no jogo — o denominador do bestiario. */
    public int totalDeEspecies() { return Especies.total(); }

    /** As especies ja encontradas, na ordem do registro de especies. */
    public List<EspecieCriatura> vistas() { return filtrar(false); }

    /** As especies ja capturadas, na ordem do registro de especies. */
    public List<EspecieCriatura> capturadas() { return filtrar(true); }

    private List<EspecieCriatura> filtrar(boolean somenteCapturadas) {
        List<EspecieCriatura> lista = new ArrayList<>();
        for (EspecieCriatura e : Especies.todas()) {
            Registro r = estado(e);
            if (r == Registro.NAO_VISTA) continue;
            if (somenteCapturadas && r != Registro.CAPTURADA) continue;
            lista.add(e);
        }
        return Collections.unmodifiableList(lista);
    }

    /** Os registros crus, para a persistencia gravar. */
    public Map<String, Registro> getRegistros() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(registros));
    }
}
