package arenaelemental.modelo;

import arenaelemental.mundo.Area;
import arenaelemental.mundo.Habitats;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * A ficha de uma especie: tudo o que e' igual em todas as criaturas dela.
 *
 * <p>Antes esses dados ficavam espalhados — os status base e a tabela de
 * aprendizado em constantes da subclasse, o tipo no construtor, os nomes e o
 * mapeamento tipo&rarr;classe em {@code switch} dentro da fabrica. Agora tudo
 * mora aqui, e {@link Especies} guarda uma ficha por especie.
 *
 * <p>Criar uma especie nova e' declarar um bloco em {@link Especies}:
 *
 * <pre>
 * public static final EspecieCriatura PEDRISCO = registrar(
 *         EspecieCriatura.novo("pedrisco", "Pedrisco")
 *                 .tipo(TipoElemental.PLANTA)
 *                 .base(60, 55, 70, 40, 50, 45).rendimentoExp(65)
 *                 .aparencia(new Color(140, 120, 90), Adorno.ESPINHOS)
 *                 .habilidade("Casca Dura: reduz o dano recebido")
 *                 .nomes("Pedrisco", "Rochedo")
 *                 .aprende(1, Golpes.INVESTIDA)
 *                 .aprende(10, Golpes.CHICOTE_VINHA)
 *                 .construir());
 * </pre>
 *
 * <p>Sem {@link Builder#construtor(Construtor)} a especie usa
 * {@link CriaturaComum}, que nao tem habilidade especial — nenhuma classe nova
 * precisa ser escrita. Para dar uma habilidade a ela, crie a subclasse de
 * {@link Criatura} e passe o construtor dela em {@code construtor(...)}.
 */
public final class EspecieCriatura {

    /** Como instanciar a criatura desta especie (a subclasse que carrega a habilidade). */
    @FunctionalInterface
    public interface Construtor {
        Criatura novaInstancia(EspecieCriatura especie, String nome, int nivel);
    }

    private final String id;
    private final String nome;
    private final TipoElemental tipo;
    private final TipoElemental tipoSecundario;
    private final List<TipoElemental> tipos;
    private final Set<Area> habitat;
    private final EstatisticasBase base;
    private final GrupoExperiencia grupoExp;
    private final List<AprendizadoGolpe> aprendizado;
    private final String descricaoHabilidade;
    private final List<String> nomesSugeridos;
    private final Aparencia aparencia;
    private final boolean inicial;
    private final Construtor construtor;

    private EspecieCriatura(Builder b) {
        if (b.tipoSecundario != null && b.tipoSecundario == b.tipo) {
            throw new IllegalArgumentException("A especie '" + b.id + "' repete o tipo " + b.tipo.nomeExibicao());
        }
        this.id = b.id;
        this.nome = b.nome;
        this.tipo = b.tipo;
        this.tipoSecundario = b.tipoSecundario;
        this.tipos = b.tipoSecundario == null
                ? Collections.singletonList(b.tipo)
                : Collections.unmodifiableList(Arrays.asList(b.tipo, b.tipoSecundario));
        this.habitat = calcularHabitat(tipos, b.habitat);
        this.base =new EstatisticasBase(b.hp, b.ataque, b.defesa, b.ataqueEsp, b.defesaEsp,
                b.velocidade, b.rendimentoExp);
        this.grupoExp = b.grupoExp;
        this.aprendizado = AprendizadoGolpe.tabela(b.aprendizado.toArray(new AprendizadoGolpe[0]));
        this.descricaoHabilidade = b.descricaoHabilidade;
        this.nomesSugeridos = b.nomesSugeridos.isEmpty()
                ? Collections.singletonList(b.nome)
                : Collections.unmodifiableList(new ArrayList<>(b.nomesSugeridos));
        this.aparencia = b.aparencia != null
                ? b.aparencia
                : new Aparencia(b.tipo.corPrincipal(), Aparencia.Adorno.NENHUM);
        this.inicial = b.inicial;
        this.construtor = b.construtor != null ? b.construtor : CriaturaComum::new;
    }

    /** Identificador estavel, usado no jogo salvo e no bestiario. Nunca mude. */
    public String getId() { return id; }

    /** Nome de exibicao da especie. Pode ser reescrito sem quebrar saves. */
    public String getNome() { return nome; }

    /**
     * O tipo principal: o unico, ou o primeiro de uma especie de dois tipos. E'
     * o que decide a cor da especie na interface.
     */
    public TipoElemental getTipo() { return tipo; }

    /** O segundo tipo, ou {@code null} se a especie tiver um tipo so. */
    public TipoElemental getTipoSecundario() { return tipoSecundario; }

    /** Os tipos da especie, o principal primeiro: um ou dois. */
    public List<TipoElemental> getTipos() { return tipos; }

    /** Se este e' um dos tipos da especie. */
    public boolean temTipo(TipoElemental t) { return tipos.contains(t); }

    /** "Fogo", ou "Fogo / Pedra" para uma especie de dois tipos. */
    public String nomeDosTipos() {
        return tipoSecundario == null
                ? tipo.nomeExibicao()
                : tipo.nomeExibicao() + " / " + tipoSecundario.nomeExibicao();
    }

    /**
     * As areas onde a especie vive: as que tem todos os tipos dela, segundo
     * {@link Habitats}, e — se o registro pediu — so as de {@code .habitat(...)}.
     */
    public Set<Area> getHabitat() { return habitat; }

    /** Se a especie pode ser encontrada nesta area. */
    public boolean viveEm(Area area) { return habitat.contains(area); }

    private static Set<Area> calcularHabitat(List<TipoElemental> tipos, Set<Area> restricao) {
        Set<Area> areas = EnumSet.allOf(Area.class);
        for (TipoElemental t : tipos) areas.retainAll(Habitats.areasDoTipo(t));
        if (!restricao.isEmpty()) areas.retainAll(restricao);
        return Collections.unmodifiableSet(areas);
    }

    public EstatisticasBase getEstatisticasBase() { return base; }
    public GrupoExperiencia getGrupoExperiencia() { return grupoExp; }
    public List<AprendizadoGolpe> getAprendizado() { return aprendizado; }
    public String getDescricaoHabilidade() { return descricaoHabilidade; }
    public List<String> getNomesSugeridos() { return nomesSugeridos; }
    public Aparencia getAparencia() { return aparencia; }

    /** Se a especie aparece na tela de escolha da criatura inicial. */
    public boolean ehInicial() { return inicial; }

    /** Cria uma criatura desta especie com o nome informado. */
    public Criatura criar(String nome, int nivel) {
        return construtor.novaInstancia(this, nome, nivel);
    }

    /** Cria uma criatura desta especie com o nome canonico da especie. */
    public Criatura criar(int nivel) { return criar(nome, nivel); }

    /** Cria uma criatura com um dos apelidos sugeridos, sorteado. */
    public Criatura criarComNomeSorteado(int nivel, Random rng) {
        return criar(nomesSugeridos.get(rng.nextInt(nomesSugeridos.size())), nivel);
    }

    @Override
    public String toString() { return nome + " [" + id + "]"; }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------

    public static Builder novo(String id, String nome) { return new Builder(id, nome); }

    public static final class Builder {
        private final String id;
        private final String nome;
        private TipoElemental tipo = TipoElemental.NORMAL;
        private TipoElemental tipoSecundario;
        private final Set<Area> habitat = EnumSet.noneOf(Area.class);
        private int hp = 50, ataque = 50, defesa = 50, ataqueEsp = 50, defesaEsp = 50, velocidade = 50;
        private int rendimentoExp = 60;
        private GrupoExperiencia grupoExp = GrupoExperiencia.MEDIO_RAPIDO;
        private final List<AprendizadoGolpe> aprendizado = new ArrayList<>();
        private String descricaoHabilidade = "Sem habilidade especial";
        private final List<String> nomesSugeridos = new ArrayList<>();
        private Aparencia aparencia;
        private boolean inicial;
        private Construtor construtor;

        private Builder(String id, String nome) {
            this.id = Objects.requireNonNull(id, "id da especie");
            this.nome = Objects.requireNonNull(nome, "nome da especie");
        }

        public Builder tipo(TipoElemental t) { this.tipo = t; return this; }

        /** Dois tipos: o principal, que da a cor da especie, e o secundario. */
        public Builder tipos(TipoElemental principal, TipoElemental secundario) {
            this.tipo = principal;
            this.tipoSecundario = secundario;
            return this;
        }

        /**
         * Restringe a especie a estas areas, alem do que os tipos dela ja
         * decidem — para uma especie rara de um lugar so, por exemplo. Nao
         * acrescenta areas: uma area fora do habitat dos tipos continua fora.
         */
        public Builder habitat(Area... areas) {
            habitat.addAll(Arrays.asList(areas));
            return this;
        }

        /** Os seis status base, na ordem HP, Atk, Def, SpA, SpD, Spe. */
        public Builder base(int hp, int ataque, int defesa, int ataqueEsp, int defesaEsp, int velocidade) {
            this.hp = hp; this.ataque = ataque; this.defesa = defesa;
            this.ataqueEsp = ataqueEsp; this.defesaEsp = defesaEsp; this.velocidade = velocidade;
            return this;
        }

        /** Quanta experiencia a especie rende ao ser derrotada. */
        public Builder rendimentoExp(int b) { this.rendimentoExp = b; return this; }

        public Builder grupoExp(GrupoExperiencia g) { this.grupoExp = g; return this; }

        /** Acrescenta uma entrada a tabela de aprendizado. A ordem nao importa. */
        public Builder aprende(int nivel, Golpe golpe) {
            aprendizado.add(AprendizadoGolpe.em(nivel, golpe));
            return this;
        }

        public Builder habilidade(String descricao) { this.descricaoHabilidade = descricao; return this; }

        /** Apelidos possiveis; o primeiro e' o nome canonico dos encontros. */
        public Builder nomes(String... apelidos) {
            nomesSugeridos.addAll(Arrays.asList(apelidos));
            return this;
        }

        public Builder aparencia(Color corCorpo, Aparencia.Adorno adorno) {
            this.aparencia = new Aparencia(corCorpo, adorno);
            return this;
        }

        /** Marca a especie como escolha inicial na tela de comeco de jornada. */
        public Builder inicial() { this.inicial = true; return this; }

        /** A subclasse de {@link Criatura} que carrega a habilidade da especie. */
        public Builder construtor(Construtor c) { this.construtor = c; return this; }

        public EspecieCriatura construir() { return new EspecieCriatura(this); }
    }
}
