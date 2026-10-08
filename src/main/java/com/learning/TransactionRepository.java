package com.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    /**
     * Сохраняет новую транзакцию.
     *
     * После успешного сохранения транзакция доступна через
     * findById() и findAll().
     *  При отклонении транзакции состояние хранилища не меняется.
     *
     * @param transaction транзакция для сохранения
     * @throws IllegalArgumentException если transaction равна null
     *         или транзакция с таким UUID уже существует
     */
    void save(Transaction transaction);
    /**
     * Находит транзакцию по id.
     * Поиск не изменяет состояние хранилища.
     *
     * @param id идентификатор транзакции
     * @return найденная транзакция или {@code Optional.empty()},
     *         если транзакция отсутствует
     * @throws IllegalArgumentException  если id равен null
     */
    Optional<Transaction> findById (UUID id);
    /**
     * Возвращает все транзакции в порядке добавления.
     *
     * Последующие изменения хранилища не изменяют
     * ранее полученный список.
     *
     * @return неизменяемый снимок транзакций;
     *         пустой список, если хранилище пустое
     */

    List<Transaction> findAll();
    /**
     * Удаляет транзакцию по id.
     *
     * Если транзакция отсутствует, метод ничего не меняет
     * и не выбрасывает исключение.
     * Остальные транзакции сохраняются.
     *
     * @param id id транзакции
     * @throws IllegalArgumentException если id равен null
     */
    void deleteById(UUID id);
}
