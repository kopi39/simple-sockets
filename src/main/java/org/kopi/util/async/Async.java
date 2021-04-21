package org.kopi.util.async;

import java.util.Arrays;
import java.util.List;

public class Async {

    public static Thread start(ThrowableRunnable throwableRunnable) {
        Thread thread = new Thread(() -> wrapRunnable(throwableRunnable));
        thread.start();
        return thread;
    }

    public static void stopAllWhenFirstEnds(int interval, ThrowableRunnable onClose, Thread... threads) {
        try {
            boolean wait = true;
            while (wait) {
                for (Thread thread : threads) {
                    if (!thread.isAlive()) {
                        wait = false;
                        break;
                    }
                }
                Thread.sleep(interval);
            }
            interruptAll(threads);
            onClose.run();
            joinAll(threads);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void wrapRunnable(ThrowableRunnable throwableRunnable) {
        try {
            throwableRunnable.run();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void joinAll(Thread... threads) {
        joinAll(Arrays.asList(threads));
    }

    public static void joinAll(List<Thread> threads) {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void interruptAll(Thread... threads) {
        interruptAll(Arrays.asList(threads));
    }

    public static void interruptAll(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }

}
