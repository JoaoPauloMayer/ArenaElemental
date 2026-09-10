package arenaelemental.persistencia;

import arenaelemental.modelo.CondicaoStatus;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;
import arenaelemental.modelo.Especies;
import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.Golpes;
import arenaelemental.mundo.Area;
import arenaelemental.treinador.Bestiario;
import arenaelemental.treinador.Treinador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le e grava a partida em disco.
 *
 * <p>O arquivo e' texto UTF-8, uma chave por linha, legivel e editavel a mao:
 *
 * <pre>
 * versao=3
 * salvoEm=2026-09-01T11:30:00
 * treinador=Treinador
 * area=floresta
 * criaturas=2
 * criatura.0.especie=braseiro
 * criatura.0.nome=Ignivo
 * criatura.0.nivel=12
 * criatura.0.exp=1750
 * criatura.0.vida=30
 * criatura.0.golpes=investida,brasa,presa-ignea
 * criatura.0.pp=investida:30,brasa:12,presa-ignea:15
 * criatura.0.condicao=QUEIMADURA
 * bestiario.braseiro=CAPTURADA
 * bestiario.marulho=VISTA
 * </pre>
 *
 * <p>As chaves {@code pp}, {@code condicao} e {@code turnosDeSono} sao
 * opcionais: um save da versao 1, gravado antes de PP e condicoes de status
 * existirem, carrega com os PP cheios e sem condicao — que e' o que ele
 * significava. Do mesmo jeito, um save sem {@code area} (versoes 1 e 2, de
 * antes do mapa) recomeca na area inicial.
 *
 * <p>Nada de serializacao binaria de objetos Java: um {@code .ser} quebra assim
 * que uma classe muda de campo, e este projeto vai mudar muito. Aqui o que se
 * grava sao os <b>ids</b> da especie e dos golpes, que sao estaveis por
 * contrato — o save sobrevive a renomear classes, reequilibrar status ou trocar
 * o nome de exibicao de uma especie.
 *
 * <p>A gravacao e' atomica: escreve num arquivo temporario ao lado e so entao
 * o move por cima do save. Um desligamento no meio da gravacao deixa o save
 * anterior intacto, em vez de um arquivo pela metade.
 */
public class RepositorioJogo {

    /** Versao do formato gravada nos saves novos. */
    public static final int VERSAO = 3;

    /**
     * Versao mais antiga que ainda abre. Da 1 para a 2 entraram PP e condicao
     * de status, e da 2 para a 3 a area do mapa — todas chaves opcionais. Um
     * save da 1 carrega com os PP cheios, sem condicao e na area inicial, que
     * e' exatamente o que ele significava.
     */
    public static final int VERSAO_MINIMA_SUPORTADA = 1;

    private static final String PASTA_PADRAO = ".arenaelemental";
    private static final String ARQUIVO_PADRAO = "jogo.save";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path arquivo;

    /** Repositorio no local padrao: {@code <pasta do usuario>/.arenaelemental/jogo.save}. */
    public RepositorioJogo() { this(caminhoPadrao()); }

    /** Repositorio em um caminho especifico — usado pelos testes. */
    public RepositorioJogo(Path arquivo) { this.arquivo = arquivo; }

    public static Path caminhoPadrao() {
        return Paths.get(System.getProperty("user.home"), PASTA_PADRAO, ARQUIVO_PADRAO);
    }

    public Path getArquivo() { return arquivo; }

    public boolean existeSave() { return Files.isRegularFile(arquivo); }

    /** Apaga o jogo salvo, se houver. */
    public void apagar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new ErroDePersistencia("Não foi possível apagar o jogo salvo: " + e.getMessage(), e);
        }
    }

    /** Quando o save foi gravado, ou {@code null} se nao houver save legivel. */
    public LocalDateTime dataDoSave() {
        if (!existeSave()) return null;
        try {
            String valor = lerLinhas().get("salvoEm");
            return valor == null ? null : LocalDateTime.parse(valor, HORA);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Gravar
    // ------------------------------------------------------------------

    public void salvar(Treinador treinador, Bestiario bestiario) {
        if (treinador == null) throw new ErroDePersistencia("Não há jornada em andamento para salvar.");

        StringBuilder sb = new StringBuilder();
        sb.append("# Arena Elemental — jogo salvo\n");
        sb.append("versao=").append(VERSAO).append('\n');
        sb.append("salvoEm=").append(LocalDateTime.now().withNano(0).format(HORA)).append('\n');
        sb.append("treinador=").append(umaLinha(treinador.getNome())).append('\n');
        sb.append("area=").append(treinador.getArea().getId()).append('\n');

        List<Criatura> equipe = treinador.getEquipe();
        sb.append("criaturas=").append(equipe.size()).append('\n');
        for (int i = 0; i < equipe.size(); i++) {
            Criatura c = equipe.get(i);
            String p = "criatura." + i + '.';
            sb.append(p).append("especie=").append(c.getEspecie().getId()).append('\n');
            sb.append(p).append("nome=").append(umaLinha(c.getNome())).append('\n');
            sb.append(p).append("nivel=").append(c.getNivel()).append('\n');
            sb.append(p).append("exp=").append(c.getExpTotal()).append('\n');
            sb.append(p).append("vida=").append(c.getVidaAtual()).append('\n');
            sb.append(p).append("golpes=").append(idsDosGolpes(c)).append('\n');
            sb.append(p).append("pp=").append(ppDosGolpes(c)).append('\n');
            if (c.temCondicao()) {
                sb.append(p).append("condicao=").append(c.getCondicao().name()).append('\n');
                if (c.getTurnosDeSono() > 0) {
                    sb.append(p).append("turnosDeSono=").append(c.getTurnosDeSono()).append('\n');
                }
            }
        }

        if (bestiario != null) {
            for (Map.Entry<String, Bestiario.Registro> e : bestiario.getRegistros().entrySet()) {
                sb.append("bestiario.").append(e.getKey()).append('=').append(e.getValue().name()).append('\n');
            }
        }

        gravarAtomicamente(sb.toString());
    }

    private static String idsDosGolpes(Criatura c) {
        StringBuilder sb = new StringBuilder();
        for (Golpe g : c.getGolpes()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(g.getId());
        }
        return sb.toString();
    }

    /** Os PP restantes, no formato {@code id:restantes,id:restantes}. */
    private static String ppDosGolpes(Criatura c) {
        StringBuilder sb = new StringBuilder();
        for (Golpe g : c.getGolpes()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(g.getId()).append(':').append(c.getPp(g));
        }
        return sb.toString();
    }

    /** Troca quebras de linha por espaco: uma chave nunca pode virar duas linhas. */
    private static String umaLinha(String texto) {
        return texto == null ? "" : texto.replace('\r', ' ').replace('\n', ' ');
    }

    private void gravarAtomicamente(String conteudo) {
        Path pasta = arquivo.toAbsolutePath().getParent();
        Path temporario = null;
        try {
            if (pasta != null) Files.createDirectories(pasta);
            temporario = Files.createTempFile(pasta, "jogo", ".save.tmp");
            Files.write(temporario, conteudo.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporario, arquivo,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (UnsupportedOperationException | IOException naoAtomico) {
                // alguns sistemas de arquivos nao suportam ATOMIC_MOVE
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
            temporario = null;
        } catch (IOException e) {
            throw new ErroDePersistencia("Não foi possível salvar o jogo: " + e.getMessage(), e);
        } finally {
            if (temporario != null) {
                try { Files.deleteIfExists(temporario); } catch (IOException ignorado) { }
            }
        }
    }

    // ------------------------------------------------------------------
    // Carregar
    // ------------------------------------------------------------------

    public JogoSalvo carregar() {
        if (!existeSave()) throw new ErroDePersistencia("Não há jogo salvo em " + arquivo + ".");

        Map<String, String> dados = lerLinhas();
        int versao = inteiro(dados.get("versao"), -1);
        if (versao < VERSAO_MINIMA_SUPORTADA || versao > VERSAO) {
            throw new ErroDePersistencia("O jogo salvo é da versão " + versao
                    + " e esta versão do jogo lê da " + VERSAO_MINIMA_SUPORTADA + " à " + VERSAO + ".");
        }

        List<String> avisos = new ArrayList<>();
        String nomeTreinador = dados.getOrDefault("treinador", "Treinador");
        Treinador treinador = new Treinador(nomeTreinador.isEmpty() ? "Treinador" : nomeTreinador);
        treinador.restaurarArea(lerArea(dados.get("area"), avisos));

        int quantas = inteiro(dados.get("criaturas"), 0);
        for (int i = 0; i < quantas; i++) {
            Criatura c = lerCriatura(dados, "criatura." + i + '.', avisos);
            if (c != null && !treinador.adicionarNaEquipe(c)) {
                avisos.add("A equipe salva tinha mais de " + Treinador.getTamanhoMaxEquipe()
                        + " criaturas; " + c.getNome() + " ficou de fora.");
            }
        }

        Bestiario bestiario = new Bestiario();
        for (Map.Entry<String, String> e : dados.entrySet()) {
            if (!e.getKey().startsWith("bestiario.")) continue;
            String id = e.getKey().substring("bestiario.".length());
            if (Especies.porId(id) == null) {
                avisos.add("O bestiário salvo cita a espécie '" + id + "', que não existe mais.");
                continue;
            }
            try {
                bestiario.restaurar(id, Bestiario.Registro.valueOf(e.getValue()));
            } catch (IllegalArgumentException invalido) {
                avisos.add("Registro de bestiário desconhecido para '" + id + "': " + e.getValue());
            }
        }

        LocalDateTime salvoEm;
        try {
            salvoEm = LocalDateTime.parse(dados.getOrDefault("salvoEm", ""), HORA);
        } catch (RuntimeException semData) {
            salvoEm = null;
        }
        return new JogoSalvo(treinador, bestiario, salvoEm, avisos);
    }

    /** A area salva. Ausente (saves de antes do mapa) ou desconhecida vira a inicial. */
    private static Area lerArea(String id, List<String> avisos) {
        if (id == null || id.trim().isEmpty()) return Area.INICIAL;
        Area area = Area.porId(id.trim());
        if (area == null) {
            avisos.add("A área salva '" + id.trim() + "' não existe mais; a jornada continua em "
                    + Area.INICIAL.getNome() + ".");
            return Area.INICIAL;
        }
        return area;
    }

    private Criatura lerCriatura(Map<String, String> dados, String prefixo, List<String> avisos) {
        String idEspecie = dados.get(prefixo + "especie");
        EspecieCriatura especie = idEspecie == null ? null : Especies.porId(idEspecie);
        if (especie == null) {
            avisos.add("A espécie '" + idEspecie + "' não existe mais; a criatura foi descartada.");
            return null;
        }

        String nome = dados.getOrDefault(prefixo + "nome", especie.getNome());
        int nivel = inteiro(dados.get(prefixo + "nivel"), 1);
        Criatura c = especie.criar(nome.isEmpty() ? especie.getNome() : nome, nivel);

        List<Golpe> golpes = new ArrayList<>();
        String lista = dados.getOrDefault(prefixo + "golpes", "");
        for (String idGolpe : lista.split(",")) {
            idGolpe = idGolpe.trim();
            if (idGolpe.isEmpty()) continue;
            Golpe g = Golpes.porId(idGolpe);
            if (g == null) {
                avisos.add("O golpe '" + idGolpe + "' de " + nome + " não existe mais.");
                continue;
            }
            golpes.add(g);
        }

        c.restaurarEstado(nivel,
                inteiro(dados.get(prefixo + "exp"), c.getExpTotal()),
                inteiro(dados.get(prefixo + "vida"), c.getHpMaximo()),
                golpes);

        lerPp(c, dados.get(prefixo + "pp"), avisos);
        lerCondicao(c, dados.get(prefixo + "condicao"),
                inteiro(dados.get(prefixo + "turnosDeSono"), 1), avisos);
        return c;
    }

    /** Aplica os PP salvos. Ausentes (saves da versao 1) ficam cheios. */
    private void lerPp(Criatura c, String lista, List<String> avisos) {
        if (lista == null || lista.trim().isEmpty()) return;
        for (String par : lista.split(",")) {
            String[] partes = par.trim().split(":");
            if (partes.length != 2) continue;
            Golpe g = Golpes.porId(partes[0].trim());
            if (g == null) continue; // o golpe sumido ja virou aviso na leitura do repertorio
            c.definirPp(g, inteiro(partes[1], g.getPpMaximo()));
        }
    }

    /** Aplica a condicao de status salva. Ausente significa nenhuma. */
    private void lerCondicao(Criatura c, String nome, int turnosDeSono, List<String> avisos) {
        if (nome == null || nome.trim().isEmpty()) return;
        try {
            c.definirCondicao(CondicaoStatus.valueOf(nome.trim()), turnosDeSono);
        } catch (IllegalArgumentException desconhecida) {
            avisos.add("Condição de status desconhecida em " + c.getNome() + ": " + nome);
        }
    }

    private Map<String, String> lerLinhas() {
        Map<String, String> dados = new LinkedHashMap<>();
        List<String> linhas;
        try {
            linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErroDePersistencia("Não foi possível ler o jogo salvo: " + e.getMessage(), e);
        }
        for (String linha : linhas) {
            String limpa = linha.trim();
            if (limpa.isEmpty() || limpa.startsWith("#")) continue;
            int igual = limpa.indexOf('=');
            if (igual <= 0) continue;
            dados.put(limpa.substring(0, igual).trim(), limpa.substring(igual + 1).trim());
        }
        return dados;
    }

    private static int inteiro(String valor, int padrao) {
        if (valor == null) return padrao;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
