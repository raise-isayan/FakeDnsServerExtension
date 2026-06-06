package fakedns;

import extension.helpers.StringUtil;
import fakedns.model.FakeDnsProperty;
import fakedns.model.HostNameItem;
import fakedns.server.DnsHandler;
import fakedns.server.MessageHandler;
import java.io.IOException;
import java.net.BindException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author isayan
 */
public class ThreadServerTest {

    private final static Logger logger = Logger.getLogger(ThreadServerTest.class.getName());

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final FakeDnsProperty option = new FakeDnsProperty();

    public ThreadServerTest() {
        this.option.setBindInterface("192.168.137.1");
        this.option.setFakeIPv4("192.168.137.1");
        HostNameItem item = new HostNameItem(true, "www.example.com");
        List<HostNameItem> fakeDomains = List.of(item);
        this.option.setFakeDomains(fakeDomains);
    }

    private Future<Boolean> future;

    public void startThread() {
        final DnsHandler dnsHandler = new DnsHandler(this.option);
        dnsHandler.setEventHandler(new MessageHandler() {

            @Override
            public void message(String message) {
                System.out.println(message);
            }

            @Override
            public void catchException(Thread t, Throwable ex) {
                String exMessage = "";
                if (ex instanceof BindException) {
                    exMessage = "Bind Error: " + StringUtil.getStackTrace(ex.getMessage(), ex);
                } else if (ex instanceof IOException) {
                    exMessage = "Fatal Error: " + StringUtil.getStackTrace(ex.getMessage(), ex);
                }
                System.err.println(exMessage);
            }
        });
        this.future = this.executor.submit(dnsHandler, Boolean.TRUE);
    }

    public void startExecute() {
        final DnsHandler dnsHandler = new DnsHandler(this.option);
        dnsHandler.setEventHandler(new MessageHandler() {

            @Override
            public void message(String message) {
                System.out.println(message);
            }

            @Override
            public void catchException(Thread t, Throwable ex) {
                String exMessage = "";
                if (ex instanceof BindException) {
                    exMessage = "Bind Error: " + StringUtil.getStackTrace(ex.getMessage(), ex);
                } else if (ex instanceof IOException) {
                    exMessage = "Fatal Error: " + StringUtil.getStackTrace(ex.getMessage(), ex);
                }
                System.err.println(exMessage);
            }
        });
        FutureTask task = new FutureTask(dnsHandler, Boolean.TRUE);
        this.executor.execute(task);
    }

    public void joinFuture() {
        try {
            Boolean result = this.future.get(Long.MAX_VALUE, TimeUnit.SECONDS);
            System.out.println("join:" + result);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void joinThread() {
        try {
            this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void stopThread() {
        this.executor.shutdown();
    }

    public static void main(String[] args) {
        System.out.println("start");
        ThreadServerTest t = new ThreadServerTest();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            t.stopThread();
        }));
        t.startThread();
//        t.joinThread();
        t.joinThread();
        System.out.println("end");
    }

}
