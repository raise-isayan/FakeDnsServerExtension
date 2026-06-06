package fakedns.server;

/**
 *
 * @author isayan
 */
public interface MessageHandler {

    public void message(String message);

    public void catchException(Thread t, Throwable e);

}
