import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CollatzParallel {
    private static final int N = 10_000_000;
    // розмір пакету для аналізу
    private static final int BATCH_SIZE = 10_000;
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        // кількість потоків
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Запуск обчислень на " + numThreads + " потоках.");

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        // список для зберіганя результатів виконання задач
        List<Future<Long>> futures = new ArrayList<>();

        // головний потік генерує задачі (розбиває 10 млн на пакети)
        for (int i = 1; i <= N; i+= BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE - 1, N);
            // створення та відправка задачі в пул
            Callable<Long> task = new CallableBathcTask(i, end);
            futures.add(executor.submit(task));
        }
        // заг. к-ть кроків
        long total = 0L;
        try {
            for (Future<Long> future : futures) {
                // get() блокує виконання поки конкретна задача не завершиться
                total += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        long end = System.currentTimeMillis();

        // середня к-ть кроків
        double average = (double) total / N;

        System.out.println("Обчислення завершено.");
        System.out.println("Всього перевірено чисел: " + N);
        System.out.println("Загальна кількість кроків: " + total);
        System.out.println("Середня кількість кроків: " + average);
        System.out.println("Час виконання: " + (end - start) + " мс");
    }
}

// задача, яка обробляє діапазон чисел і повертає суму кроків для цього діапазону.
class CallableBathcTask implements Callable<Long> {
    private final int startNum;
    private final int endNum;

    public CallableBathcTask(int startNum, int endNum) {
        this.startNum = startNum;
        this.endNum = endNum;
    }

    @Override
    public Long call() {
        long batchTotal = 0L;
        for (int i = startNum; i <= endNum; i++) {
            batchTotal += calculateSteps(i);
        }
        return batchTotal;
    }

    // логіка обчислення кроків для одного числа
    private long calculateSteps(long number) {
        int steps = 0;
        while (number != 1) {
            if ((number & 1 ) == 0) {
                number >>= 1; // бітовий зсув (еквівалент ділення на два)
            } else {
                number = number * 3 + 1 ;
            }
            steps++;
        }
        return steps;
    }
}