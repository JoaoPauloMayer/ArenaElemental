package arenaelemental.persistencia;

/** Falha ao ler ou gravar o jogo salvo, com uma mensagem que pode ir para a tela. */
public class ErroDePersistencia extends RuntimeException {

    public ErroDePersistencia(String mensagem) { super(mensagem); }

    public ErroDePersistencia(String mensagem, Throwable causa) { super(mensagem, causa); }
}
