package arenaelemental.modelo;

/**
 * Os seis status base de uma especie, mais o rendimento de experiencia que ela
 * entrega ao ser derrotada.
 *
 * <p>Sao os valores fixos da especie: os status reais de uma criatura saem
 * daqui combinados com o nivel — ver {@link Criatura#recalcularStatus()}.
 */
public class EstatisticasBase {
    private final int hp, ataque, defesa, ataqueEsp, defesaEsp, velocidade;
    private final int rendimentoExp;

    public EstatisticasBase(int hp, int ataque, int defesa,
                            int ataqueEsp, int defesaEsp, int velocidade,
                            int rendimentoExp) {
        this.hp = hp;
        this.ataque = ataque;
        this.defesa = defesa;
        this.ataqueEsp = ataqueEsp;
        this.defesaEsp = defesaEsp;
        this.velocidade = velocidade;
        this.rendimentoExp = rendimentoExp;
    }

    public int getHp() { return hp; }
    public int getAtaque() { return ataque; }
    public int getDefesa() { return defesa; }
    public int getAtaqueEsp() { return ataqueEsp; }
    public int getDefesaEsp() { return defesaEsp; }
    public int getVelocidade() { return velocidade; }
    public int getRendimentoExp() { return rendimentoExp; }

    /** Soma dos seis status base (BST), util para comparar especies. */
    public int total() {
        return hp + ataque + defesa + ataqueEsp + defesaEsp + velocidade;
    }
}
