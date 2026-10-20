package com.learning.practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JvmMemoryPractice {

    public static void main(String[] args) {
        List<byte[]> first = new ArrayList<>();
        first.add(new byte[] {1, 2, 3});

        List<byte[]> second = first;

        System.out.println("Один объект: " + (first == second));
        System.out.println("Размер first: " + first.size());
        System.out.println("Размер second: " + second.size());

        second = null;

        System.out.println("Размер first после null: " + first.size());

        change(first);
        System.out.println("Размер first после change: " + first.size());

        System.out.println("increment: " + increment(7));

        observeMemory();
    }

    static void change(List<byte[]> list) {
        list.clear();

        list = new ArrayList<>();
        list.add(new byte[] {5});
    }

    static int increment(int value) {
        return value + 1;
    }

    static void observeMemory() {
        Scanner scanner = new Scanner(System.in);

        System.out.println(ProcessHandle.current().pid());
        List<byte[]> list= new ArrayList<>();

        System.out.println("Этап 1: список пуст");
        scanner.nextLine();

        for (int i = 0; i < 20; i++) {
            list.add(new byte[1024 * 1024]);
        }

        System.out.println("Этап 2: список заполнен");
        System.out.println(list.size());
        scanner.nextLine();

        list.clear();
        System.out.println("Этап 3: список очищен");
        System.out.println(list.size());
        scanner.nextLine();

        return;
    }

}
