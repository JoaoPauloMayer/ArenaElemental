package arenaelemental.modelo;

import java.awt.Color;

/**
 * Como a especie e' desenhada: a cor do corpo e o adorno que a distingue.
 *
 * <p>Fica junto da especie, e nao do tipo, para que duas especies do mesmo
 * tipo elementar nao apareçam identicas na tela.
 */
public class Aparencia {

    /** O enfeite desenhado sobre o corpo da criatura. */
    public enum Adorno { NENHUM, CHAMA, ONDAS, FOLHAS, ESPINHOS, CRISTAL, CHIFRES, AUREOLA, ANTENA, GALHOS, MAGMA }

    private final Color corCorpo;
    private final Adorno adorno;

    public Aparencia(Color corCorpo, Adorno adorno) {
        this.corCorpo = corCorpo;
        this.adorno = adorno;
    }

    public Color getCorCorpo() { return corCorpo; }
    public Adorno getAdorno() { return adorno; }
}
