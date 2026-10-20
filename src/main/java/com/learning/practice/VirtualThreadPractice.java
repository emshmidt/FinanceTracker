package com.learning.practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class VirtualThreadPractice {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Фиксированный пул");
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            runTasks(executor);
        }

        System.out.println("Виртуальные потоки");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runTasks(executor);
        }
    }

    static void runTasks(ExecutorService executor) throws ExecutionException, InterruptedException {

        long before = System.nanoTime();
        List<Future<Integer>> futures = new ArrayList<>();


        for (int i = 0; i < 6; i++) {
            int taskNumber = i + 1;

            Callable<Integer> task = () -> {
                System.out.println(taskNumber + " задача");
                System.out.println(Thread.currentThread().isVirtual());
                Thread.sleep(200);

                return taskNumber;
            };

            Future<Integer> future = executor.submit(task);
            futures.add(future);
        }

        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            results.add(futures.get(i).get());
        }
        System.out.println(results);
        long after = System.nanoTime();
        long dif = (after - before);
        double difMS = (double) dif /1000000;

        System.out.println("Time difference: " + difMS + " ms");



    }
}
