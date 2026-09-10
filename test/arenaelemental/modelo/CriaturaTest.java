package arenaelemental.modelo;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CriaturaTest {

    @Test
    void statusSaemDaFormulaOficialComBaseENivel() {
        // Braseiro base: HP 44, Atk 58, Def 42, SpA 62, SpD 48, Spe 66; IV fixo 31, EV 0
        Criatura b = new Braseiro("Braseiro", 50);
        // HP     = floor((2*44 + 31) * 50/100) + 50 + 10 = 59 + 60 = 119
        assertEquals(119, b.getHpMaximo(), "HP no nivel 50");
        // Outros = floor((2*Base + 31) * 50/100) + 5
        assertEquals(78, b.getAtaque(), "Ataque no nivel 50");
        assertEquals(62, b.getDefesa(), "Defesa no nivel 50");
        assertEquals(82, b.getAtaqueEsp(), "Ataque Especial no nivel 50");
        assertEquals(68, b.getDefesaEsp(), "Defesa Especial no nivel 50");
        assertEquals(86, b.getVelocidade(), "Velocidade no nivel 50");
    }

    @Test
    void statusCrescemComONivel() {
        Criatura baixo = new Marulho("A", 5);
        Criatura alto = new Marulho("B", 50);
        assertTrue(alto.getHpMaximo() > baixo.getHpMaximo());
        assertTrue(alto.getAtaque() > baixo.getAtaque());
        assertTrue(alto.getVelocidade() > baixo.getVelocidade());
    }

    @Test
    void ataqueEDefesaEfetivosSeguemACategoriaDoGolpe() {
        Criatura c = new Braseiro("Braseiro", 50);
        assertEquals(c.getAtaque(), c.ataqueEfetivo(CategoriaGolpe.FISICO));
        assertEquals(c.getAtaqueEsp(), c.ataqueEfetivo(CategoriaGolpe.ESPECIAL));
        assertEquals(c.getDefesa(), c.defesaEfetiva(CategoriaGolpe.FISICO));
        assertEquals(c.getDefesaEsp(), c.defesaEfetiva(CategoriaGolpe.ESPECIAL));
    }

    @Test
    void oRepertorioInicialSegueATabelaDeAprendizado() {
        Criatura novato = new Folharal("Folharal", 5);
        assertEquals(List.of(Golpes.INVESTIDA, Golpes.CHICOTE_VINHA), novato.getGolpes(),
                "no nivel 5 so entram os golpes de nivel 1 e 5");
        assertFalse(novato.getGolpes().contains(Golpes.BOMBA_SEMENTE),
                "Bomba Semente so e aprendida no nivel 35");
    }

    @Test
    void criaturaDeNivelAltoJaChegaComOsGolpesFortes() {
        Criatura veterano = new Folharal("Folharal", 40);
        assertEquals(4, veterano.getGolpes().size());
        assertTrue(veterano.getGolpes().contains(Golpes.BOMBA_SEMENTE));
    }

    @Test
    void aprendeGolpeNovoAoSubirDeNivel() {
        Criatura c = new Braseiro("Braseiro", 14);
        assertFalse(c.getGolpes().contains(Golpes.PRESA_IGNEA));

        c.ganharExp(GrupoExperiencia.MEDIO_RAPIDO.expTotalParaNivel(15) - c.getExpTotal());

        assertEquals(15, c.getNivel());
        assertTrue(c.getGolpes().contains(Golpes.PRESA_IGNEA));
        assertEquals(List.of(Golpes.PRESA_IGNEA), c.getGolpesAprendidosAgora());
    }

    @Test
    void comORepertorioCheioOGolpeNovoSubstituiOMaisAntigo() {
        Criatura c = new Braseiro("Braseiro", 34);
        assertEquals(4, c.getGolpes().size());
        // qual e' o mais antigo depende da tabela de aprendizado, que pode mudar:
        // o teste pergunta ao proprio repertorio em vez de fixar um nome
        Golpe maisAntigo = c.getGolpes().get(0);

        c.ganharExp(GrupoExperiencia.MEDIO_RAPIDO.expTotalParaNivel(35) - c.getExpTotal());

        assertEquals(4, c.getGolpes().size(), "o limite de quatro golpes continua valendo");
        assertTrue(c.getGolpes().contains(Golpes.LABAREDA), "Labareda entrou no nivel 35");
        assertFalse(c.getGolpes().contains(maisAntigo),
                maisAntigo.getNome() + ", o mais antigo, saiu para dar lugar a Labareda");
    }

    @Test
    void naoAprendeMaisDeQuatroGolpes() {
        Criatura c = new Folharal("Folharal", 40);
        assertEquals(4, c.getGolpes().size());
        assertFalse(c.aprenderGolpe(Golpes.LABAREDA), "o quinto golpe deve ser recusado");
        assertEquals(4, c.getGolpes().size());
    }

    @Test
    void criaturaMorreQuandoVidaChegaAZero() {
        Criatura c = new Marulho("Teste", 5);
        assertTrue(c.estaViva());
        c.receberDano(c.getHpMaximo() + 50);
        assertEquals(0, c.getVidaAtual(), "a vida nao pode ficar negativa");
        assertFalse(c.estaViva());
    }

    @Test
    void vidaNuncaUltrapassaOMaximoAoCurar() {
        Criatura c = new Folharal("Teste", 20);
        c.receberDano(5);
        c.curar(1000);
        assertEquals(c.getHpMaximo(), c.getVidaAtual());
    }

    @Test
    void curvaDeExperienciaMedioRapidoEhNivelAoCubo() {
        GrupoExperiencia g = GrupoExperiencia.MEDIO_RAPIDO;
        assertEquals(0, g.expTotalParaNivel(1));
        assertEquals(125, g.expTotalParaNivel(5));
        assertEquals(216, g.expTotalParaNivel(6));
        assertEquals(1000000, g.expTotalParaNivel(100));
    }

    @Test
    void ganharExperienciaSuficienteSobeDeNivel() {
        Criatura c = new Braseiro("Braseiro", 5);
        assertEquals(125, c.getExpTotal(), "no nivel 5 a experiencia acumulada e 5^3");
        assertEquals(91, c.getExpFaltando(), "faltam 216 - 125 para o nivel 6");

        assertEquals(0, c.ganharExp(90), "90 pontos ainda nao chegam ao nivel 6");
        assertEquals(5, c.getNivel());

        assertEquals(1, c.ganharExp(1), "o ponto seguinte fecha o nivel");
        assertEquals(6, c.getNivel());
    }

    @Test
    void subirDeNivelAumentaHpMaximoESomaAVidaAtual() {
        Criatura c = new Marulho("Marulho", 5);
        int hpAntes = c.getHpMaximo();
        c.receberDano(3);
        int vidaAntes = c.getVidaAtual();

        c.ganharExp(GrupoExperiencia.MEDIO_RAPIDO.expTotalParaNivel(6) - c.getExpTotal());

        assertEquals(6, c.getNivel());
        assertTrue(c.getHpMaximo() > hpAntes, "o HP maximo cresce ao subir de nivel");
        assertEquals(vidaAntes + (c.getHpMaximo() - hpAntes), c.getVidaAtual(),
                "a vida atual sobe junto, na mesma quantidade que o HP maximo");
    }

    @Test
    void experienciaParaDeAcumularNoNivelMaximo() {
        Criatura c = new Braseiro("Braseiro", 99);
        c.ganharExp(50_000_000);
        assertEquals(Criatura.NIVEL_MAXIMO, c.getNivel());
        assertEquals(0, c.getExpFaltando());
        assertEquals(1.0, c.percentualExp(), 0.0001);
        assertEquals(0, c.ganharExp(1000), "no nivel 100 nao ha mais niveis a ganhar");
    }

    @Test
    void barraDeExperienciaVaiDeZeroAUm() {
        Criatura c = new Folharal("Folharal", 5);
        assertEquals(0.0, c.percentualExp(), 0.0001, "recem-chegado ao nivel, a barra comeca vazia");
        c.ganharExp(45); // metade dos 91 que faltam
        assertTrue(c.percentualExp() > 0.4 && c.percentualExp() < 0.6);
    }
}
