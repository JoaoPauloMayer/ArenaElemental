package arenaelemental.modelo;

import java.util.Objects;
import java.util.Random;

/**
 * Um golpe: identificador, nome, tipo, natureza, poder, precisao, PP e o efeito
 * que ele pode deixar no alvo.
 *
 * <p>O {@code id} e' o nome estavel do golpe, usado no arquivo de jogo salvo.
 * Ele nunca muda: o {@code nome} pode ser reescrito a vontade sem quebrar os
 * saves de quem ja esta jogando.
 *
 * <p>A <b>precisao</b> e' a chance de acertar, de 0 a 100; 100 acerta sempre.
 * Os <b>PP</b> sao contados por criatura, e nao aqui — dois Braseiros gastam os
 * PP de Brasa separadamente. Este objeto guarda so o maximo.
 */
public class Golpe {

    /** Precisao de um golpe que nunca erra. */
    public static final int PRECISAO_INFALIVEL = 100;

    private final String id;
    private final String nome;
    private final TipoElemental tipo;
    private final CategoriaGolpe categoria;
    private final int poder;
    private final int precisao;
    private final int ppMaximo;
    private final CondicaoStatus efeitoStatus;
    private final int chanceStatus;
    private final int recuoPercentualDoHpMaximo;
    private final boolean semLimiteDePp;

    /** Golpe simples: sempre acerta, 20 PP, sem efeito secundario. */
    public Golpe(String id, String nome, TipoElemental tipo, CategoriaGolpe categoria, int poder) {
        this(novo(id, nome).tipo(tipo).categoria(categoria).poder(poder));
    }

    private Golpe(Builder b) {
        this.id = Objects.requireNonNull(b.id, "id do golpe");
        this.nome = b.nome;
        this.tipo = b.tipo;
        this.categoria = b.categoria;
        this.poder = b.poder;
        this.precisao = Math.max(0, Math.min(PRECISAO_INFALIVEL, b.precisao));
        this.ppMaximo = Math.max(0, b.ppMaximo);
        this.efeitoStatus = b.efeitoStatus == null ? CondicaoStatus.NENHUMA : b.efeitoStatus;
        this.chanceStatus = Math.max(0, Math.min(100, b.chanceStatus));
        this.recuoPercentualDoHpMaximo = Math.max(0, b.recuo);
        this.semLimiteDePp = b.semLimiteDePp;
    }

    /** Identificador estavel, usado na persistencia. */
    public String getId() { return id; }

    public String getNome() { return nome; }
    public TipoElemental getTipo() { return tipo; }
    public CategoriaGolpe getCategoria() { return categoria; }
    public int getPoder() { return poder; }

    /** Chance de acertar, de 0 a 100. */
    public int getPrecisao() { return precisao; }

    /** Quantas vezes o golpe pode ser usado antes de precisar descansar. */
    public int getPpMaximo() { return ppMaximo; }

    /** Se o golpe pode ser usado sem gastar PP (o caso do Esforço). */
    public boolean isSemLimiteDePp() { return semLimiteDePp; }

    /** A condicao que o golpe pode deixar no alvo, ou {@link CondicaoStatus#NENHUMA}. */
    public CondicaoStatus getEfeitoStatus() { return efeitoStatus; }

    /** Chance de o efeito pegar, de 0 a 100. */
    public int getChanceStatus() { return chanceStatus; }

    /** Recuo sofrido pelo atacante, em porcentagem do HP maximo dele. */
    public int getRecuoPercentualDoHpMaximo() { return recuoPercentualDoHpMaximo; }

    public boolean temEfeitoStatus() { return efeitoStatus != CondicaoStatus.NENHUMA; }

    public boolean causaDano() { return categoria.causaDano() && poder > 0; }

    /** Sorteia se o golpe acertou. Um golpe de precisao 100 nunca erra. */
    public boolean acerta(Random rng) {
        return precisao >= PRECISAO_INFALIVEL || rng.nextInt(100) < precisao;
    }

    /** Sorteia se o efeito secundario pegou nesta vez. */
    public boolean efeitoPega(Random rng) {
        if (!temEfeitoStatus()) return false;
        return chanceStatus >= 100 || rng.nextInt(100) < chanceStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Golpe)) return false;
        return id.equals(((Golpe) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return nome + " (" + tipo.nomeExibicao() + " · " + categoria.nomeExibicao()
                + " · " + poder + " · " + precisao + "%)";
    }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------

    public static Builder novo(String id, String nome) { return new Builder(id, nome); }

    public static final class Builder {
        private final String id;
        private final String nome;
        private TipoElemental tipo = TipoElemental.NORMAL;
        private CategoriaGolpe categoria = CategoriaGolpe.FISICO;
        private int poder;
        private int precisao = PRECISAO_INFALIVEL;
        private int ppMaximo = 20;
        private CondicaoStatus efeitoStatus = CondicaoStatus.NENHUMA;
        private int chanceStatus;
        private int recuo;
        private boolean semLimiteDePp;

        private Builder(String id, String nome) { this.id = id; this.nome = nome; }

        public Builder tipo(TipoElemental t) { this.tipo = t; return this; }
        public Builder categoria(CategoriaGolpe c) { this.categoria = c; return this; }
        public Builder poder(int p) { this.poder = p; return this; }
        public Builder precisao(int p) { this.precisao = p; return this; }
        public Builder pp(int pp) { this.ppMaximo = pp; return this; }

        /** O golpe pode deixar esta condicao no alvo, com esta chance em %. */
        public Builder causa(CondicaoStatus condicao, int chance) {
            this.efeitoStatus = condicao;
            this.chanceStatus = chance;
            return this;
        }

        /** O atacante perde esta porcentagem do proprio HP maximo ao acertar. */
        public Builder recuo(int percentualDoHpMaximo) { this.recuo = percentualDoHpMaximo; return this; }

        /** O golpe nao gasta PP — usado pelo Esforço, que existe justamente quando os PP acabam. */
        public Builder semLimiteDePp() { this.semLimiteDePp = true; return this; }

        public Golpe construir() { return new Golpe(this); }
    }
}
