package org.kopi.socket.util.async;

import java.util.Arrays;
import java.util.List;

public class Async {

    public static Thread start(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    public static void stopAllWhenFirstEnds(int interval, Runnable onClose, Thread... threads) {
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

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void interruptAll(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

}
