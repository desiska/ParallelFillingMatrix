import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Matrix {
    private int[][] matrix;
    private final int countThreads;
    private int[] randomNumbersToFill;

    public Matrix(int countThreads){
        this.countThreads = countThreads;
        this.matrix = new int[this.countThreads][this.countThreads];
        generateRandomNumbers();
    }


    //Основната функционалност на матрицата
    public String execute() throws InterruptedException {
        StringBuilder result = new StringBuilder();
        result.append(printGenerateNumbers());

        long start, finish;
        start = Calendar.getInstance().getTimeInMillis();

        result.append(concurrentFilling());
        finish = Calendar.getInstance().getTimeInMillis();
        result.append("Time for parallel filling in milliseconds: " + (finish - start));
        //result.append("\nMatrix:\n" + getMatrix() + '\n');

        generateRandomNumbers();
        result.append(printGenerateNumbers());

        start = Calendar.getInstance().getTimeInMillis();

        result.append(linearFilling());
        finish = Calendar.getInstance().getTimeInMillis();
        result.append("Time for linear filling in milliseconds: " + (finish - start));
        //result.append("\nMatrix:\n" + getMatrix() + '\n');

        return result.toString();
    }

    //Генериране на произволни числа
    private void generateRandomNumbers(){
        this.randomNumbersToFill = IntStream.generate(() -> new Random().nextInt(100)).limit(this.countThreads).toArray();
    }

    //Печатане на генерираните числа
    public String printGenerateNumbers(){
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < this.countThreads; ++i){
            result.append(this.randomNumbersToFill[i] + " ");
        }

        result.append('\n');

        return result.toString();
    }

    //Помощна функция
    private void rowFilling(int row){
        for(int col = 0; col < this.countThreads; ++col)
            this.matrix[row][col] = this.randomNumbersToFill[row];
    }

    //Последователно запълване на матрицата
    private String linearFilling(){
        StringBuilder result = new StringBuilder();

        result.append("Linear filling:\n");
        for(int i = 0; i < this.countThreads; ++i){
            rowFilling(i);
        }
        result.append("Matrix is filled!\n");

        return result.toString();
    }

    //Помощна функция
    private String concurrentFillingRow(int row){
        long threadID = Thread.currentThread().getId();
        StringBuilder result = new StringBuilder();

        result.append("Currently filling row " + row + " by thread with ID " + threadID + " with number " + this.randomNumbersToFill[row] + '\n');
        rowFilling(row);

        return result.toString();
    }

    //Запълване на матрицата с нишки
    private String concurrentFilling() throws InterruptedException {
        AtomicInteger row = new AtomicInteger(0);
        ExecutorService service = Executors.newFixedThreadPool(this.countThreads);
        StringBuilder result = new StringBuilder();

        result.append("ConcurrentFilling:\n");

        for(int i = 0; i < this.countThreads; ++i){
            service.execute(new Runnable() {
                @Override
                public void run() {
                    result.append(concurrentFillingRow(row.getAndIncrement()));
                }
            });
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);

        result.append("Matrix is filled!\n");

        return result.toString();
    }

    //Печатане на матрицата
    private String getMatrix(){
        StringBuilder result = new StringBuilder();

        for(int row = 0; row < this.countThreads; ++row){
            result.append("Row " + row + ":\t");

            for(int col = 0; col < this.countThreads; ++col){
                result.append(this.matrix[row][col] + " ");
            }

            result.append('\n');
        }

        return result.toString();
    }
}
