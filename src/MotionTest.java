import java.io.*;
import java.util.*;

/**
 * Інтерфейс для фабрикованих об'єктів.
 */
interface Displayable {
    void displayResults();
}

/**
 * Абстрактний клас MotionData.
 */
abstract class MotionData implements Serializable, Displayable {
    private static final long serialVersionUID = 1L;
    protected double v0, alpha;
    protected static final double G = 9.81;
    protected transient List<int[]> trajectory;

    public MotionData(double v0, double alpha) {
        this.v0 = v0;
        this.alpha = Math.toRadians(alpha);
        this.trajectory = new ArrayList<>();
    }

    public abstract void calculate(double totalTime, double step);
}

/**
 * Клас для даних про траєкторію руху.
 */
class TrajectoryData extends MotionData {
    public TrajectoryData(double v0, double alpha) {
        super(v0, alpha);
    }

    @Override
    public void calculate(double totalTime, double step) {
        trajectory = new ArrayList<>();
        for (double t = 0; t <= totalTime; t += step) {
            int x = (int) (v0 * Math.cos(alpha) * t);
            int y = (int) (v0 * Math.sin(alpha) * t - (G * t * t) / 2);
            if (y < 0) break;
            trajectory.add(new int[]{x, y});
        }
    }

    @Override
    public void displayResults() {
        trajectory.stream()
            .map(p -> String.format("x: %d, y: %d", p[0], p[1]))
            .forEach(System.out::println);
    }
}

/**
 * Інтерфейс для фабрикуючого методу.
 */
interface MotionDataFactory {
    MotionData createMotionData(double v0, double alpha);
}

/**
 * Фабрика для створення об'єктів TrajectoryData.
 */
class TrajectoryDataFactory implements MotionDataFactory {
    @Override
    public MotionData createMotionData(double v0, double alpha) {
        return new TrajectoryData(v0, alpha);
    }
}

/**
 * Клас Serializer для збереження та завантаження даних.
 */
class Serializer {
    public static void save(MotionData data, String file) throws IOException {
        try (var out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        }
    }

    public static MotionData load(String file) throws IOException, ClassNotFoundException {
        try (var in = new ObjectInputStream(new FileInputStream(file))) {
            return (MotionData) in.readObject();
        }
    }
}

/**
 * Головний клас для демонстрації роботи програми.
 */
public class MotionTest {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Введіть v0 і кут α через пробіл: ");
            double v0 = scanner.nextDouble(), alpha = scanner.nextDouble();

            MotionDataFactory factory = new TrajectoryDataFactory();
            MotionData data = factory.createMotionData(v0, alpha);
            data.calculate(2, 0.1);

            System.out.println("Траєкторія (текст):");
            data.displayResults();

            String filename = "motion_data.ser";
            try {
                Serializer.save(data, filename);
                System.out.println("Дані збережені у файл.");

                MotionData loaded = Serializer.load(filename);
                System.out.println("Об'єкт відновлено. Повторний розрахунок:");
                loaded.calculate(2, 0.1);
                loaded.displayResults();
            } catch (Exception e) {
                System.err.println("Помилка: " + e.getMessage());
            }
        }
    }
}
