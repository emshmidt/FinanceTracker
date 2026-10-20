package com.learning.practice;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPractice {
    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException{
        interruptWorker();
    }

    static void interruptWorker() throws InterruptedException{
        Runnable task = () -> {
            int i = 0;
            while (true) {
                System.out.println("in progress..  "+ i);
                i++;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.println("Interrupt request");
                    System.out.println(Thread.currentThread().isInterrupted());
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().isInterrupted());

                    return;
                }
            }
        };

        Thread worker = new Thread(task, "worker");
        worker.start();

        worker.interrupt();
        worker.join();
        System.out.println("Worker finished");
    }

    static void threadsFlag() throws InterruptedException {
        PlainCounter counter = new PlainCounter();
        Scanner scanner = new Scanner(System.in);

        Runnable task = () -> {
            while(running) {
                for (int i = 0; i < 100000; i++) {
                    counter.increment();
                }
            }
        };
        Thread workerFlag = new Thread(task, "workerFlag");

        workerFlag.start();
        scanner.nextLine();
        running = false;

        workerFlag.join();
        System.out.println("Process finished " + counter.get());
    }

    static class PlainCounter {
        int count = 0;
        public void  increment() {
            count++;
        }
        public int get() {
            return count;
        }
    }

    static class VolatileCounter {
        volatile int count = 0;
        public void  increment() {
            count++;
        }
        public int get() {
            return count;
        }
    }

    static class SynchronizedCounter {
        int count = 0;

        synchronized public void  increment() {
            count++;
        }
        public int get() {
            return count;
        }
    }

    static class AtomicCounter {
        AtomicInteger count = new AtomicInteger(0);

        public void  increment() {
            count.incrementAndGet();
        }
        public int get() {
            return count.get();
        }
    }

    static void threads2() throws InterruptedException{
        AtomicCounter counter = new AtomicCounter();
        Runnable task = () -> {
                    for (int i = 0; i < 100_000; i++) {
                        counter.increment();
                    }
                };

        Thread worker1 = new Thread(task, "worker1");
        Thread worker2 = new Thread(task, "worker2");

        worker1.start();
        worker2.start();

        worker1.join();
        worker2.join();

        System.out.println("Expected: 200 000");
        System.out.println("ACtual: "+ counter.get());
    }

    static void threads() throws InterruptedException {
        Runnable task = () -> {
            for (int i = 1; i < 6; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
            }
        };

        Thread worker1 = new Thread(task, "worker1");
        Thread worker2 = new Thread(task, "worker2");

        worker1.start();
        worker2.start();

        worker1.join();
        worker2.join();

        System.out.println("Both tasks completed");
    }
}
