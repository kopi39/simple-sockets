package org.kopi.util.async;

public class Async {

    public static Thread start(ThrowableRunnable throwableRunnable) {
        Thread thread = new Thread(() -> wrapRunnable(throwableRunnable));
        thread.start();
        return thread;
    }

    public static void stopAllWhenFirstEnds(int interval, Thread... threads) {
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
            for (Thread thread : threads) {
                thread.interrupt();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void wrapRunnable(ThrowableRunnable throwableRunnable) {
        try {
            throwableRunnable.run();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }

}
