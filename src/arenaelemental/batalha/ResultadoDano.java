package arenaelemental.batalha;

import arenaelemental.modelo.Golpe;
import arenaelemental.modelo.TipoElemental;

/** O que aconteceu em um golpe: o dano e os multiplicadores que o produziram. */
public class ResultadoDano {
    private final Golpe golpe;
    private final int dano;
    private final boolean critico;
    private final double efetividade;
    private final boolean stab;
    private final int rolagem;

    ResultadoDano(Golpe golpe, int dano, boolean critico, double efetividade, boolean stab, int rolagem) {
        this.golpe = golpe;
        this.dano = dano;
        this.critico = critico;
        this.efetividade = efetividade;
        this.stab = stab;
        this.rolagem = rolagem;
    }

    public Golpe getGolpe() { return golpe; }
    public int getDano() { return dano; }
    public boolean foiCritico() { return critico; }
    public double getEfetividade() { return efetividade; }
    public boolean teveStab() { return stab; }

    /** O valor sorteado do damage roll, de 85 a 100. */
    public int getRolagem() { return rolagem; }

    /** Se o golpe nao causou dano nenhum por imunidade de tipo. */
    public boolean semEfeito() { return efetividade == 0.0; }

    /** Mensagens de combate a acrescentar ao log, na ordem em que aparecem. */
    public String textoEfetividade() {
        return TipoElemental.textoEfetividade(efetividade);
    }
}
