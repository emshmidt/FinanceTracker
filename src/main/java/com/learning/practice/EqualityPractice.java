package com.learning.practice;

import com.learning.Category;
import com.learning.Transaction;
import com.learning.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class EqualityPractice {
    static final class ExpenseGroupKey {
        private final Category category;
        private final YearMonth month;

        ExpenseGroupKey(Category category, YearMonth month) {
            if (category == null) {
                throw new IllegalArgumentException("Category must not be null");
            }
            if (month == null) {
                throw new IllegalArgumentException("Month must not be null");
            }

            this.category = category;
            this.month = month;
        }

        public Category getCategory() {
            return category;
        }

        public YearMonth getMonth() {
            return month;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ExpenseGroupKey other)) {
                return false;
            }

            return category == other.category
                    && month.equals(other.month);
        }

        @Override
        public int hashCode() {
            return Objects.hash(category, month);
        }
    }

    static final class MutableKey {
        int code;

        public MutableKey(int code) {
            this.code = code;
        }

        public void setCode(int newCode) {
            code = newCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof MutableKey other)) {
                return false;
            }

            return code == other.code;
        }

        @Override
        public int hashCode() {
            return Objects.hash(code);
        }
    }

    public static void main(String[] args) {
        ExpenseGroupKey first =
                new ExpenseGroupKey(Category.FOOD, YearMonth.of(2026, 10));

        ExpenseGroupKey same =
                new ExpenseGroupKey(Category.FOOD, YearMonth.of(2026, 10));

        ExpenseGroupKey different =
                new ExpenseGroupKey(Category.TRANSPORT, YearMonth.of(2026, 10));

        System.out.println(first == same); //false
        System.out.println(first.equals(same)); //true
        System.out.println(same.equals(first)); //true
        System.out.println(first.hashCode() == same.hashCode()); //true


        System.out.println(first.equals(different)); //false
        System.out.println(first.equals(null)); //false
        System.out.println(first.equals("String")); //false



        Set<ExpenseGroupKey> set = new HashSet<>();
        set.add(first);
        set.add(same);
        set.add(different);
        System.out.println(set.size()); //2

        Map<ExpenseGroupKey, Integer> map = new HashMap<>();
        map.put(first, 1);
        System.out.println(map.get(same)); //1

        Map<ExpenseGroupKey, Integer> map2 = new HashMap<>();
        map2.put(first, 1);
        map2.put(different, 2);
        System.out.println(map2.size());
        System.out.println(map2.get(first));
        System.out.println(map2.get(different));


        System.out.println("Mutable key");
        MutableKey key = new MutableKey(1);
        Map<MutableKey, String> map3 = new HashMap<>();
        map3.put(key, "First string");
        System.out.println(map3.get(key));

        key.setCode(2);
        System.out.println(map3.get(key));
        System.out.println(map3.containsKey(key));
        System.out.println(map3.entrySet());


        System.out.println("Transactions");
        Transaction transaction = new Transaction(
                "a", new BigDecimal("100.00"),
                TransactionType.INCOME, LocalDate.of(2025, 1,1),
                Category.TRANSPORT);

        Transaction restored = Transaction.restore(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDate(),
                transaction.getCategory()
        );
        System.out.println(transaction == restored);
        System.out.println(transaction.equals(restored));
        System.out.println(transaction.getId().compareTo(restored.getId()) == 0);

        Set<Transaction> transactionSet = new HashSet<>();
        transactionSet.add(transaction);
        transactionSet.add(restored);

        Set<UUID> idSet = new HashSet<>();
        idSet.add(transaction.getId());
        idSet.add(restored.getId());
        System.out.println(transactionSet.size()); //2
        System.out.println(idSet.size()); //1

        BigDecimal decimal1 = new BigDecimal("10.0");
        BigDecimal decimal2 = new BigDecimal("10.00");

        System.out.println(decimal1.equals(decimal2)); //false
        System.out.println(decimal1.compareTo(decimal2) == 0); //true

        Set<BigDecimal> decimalSet = new HashSet<>();
        decimalSet.add(decimal1);
        decimalSet.add(decimal2);

        Map<BigDecimal, Integer> decimalMap = new TreeMap<>();
        decimalMap.put(decimal1, 1);
        decimalMap.put(decimal2, 2);

        System.out.println(decimalSet.size()); //2
        System.out.println(decimalMap.size()); //1




//        Создай транзакцию и второй экземпляр через Transaction.restore() с теми же ID и полями.
//        Сравни ссылки, equals() и UUID.
//        Добавь оба экземпляра в HashSet<Transaction>, а их UUID — в HashSet<UUID>. Сравни размеры.
//        Для BigDecimal("10.0") и BigDecimal("10.00") проверь equals(), compareTo(), размер HashSet и размер TreeMap после двух вставок.

    }


}
