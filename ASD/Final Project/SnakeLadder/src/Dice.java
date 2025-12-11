import java.util.Random;

public class Dice {
    private final Random random = new Random();

    public int rollMain() { return random.nextInt(6) + 1; }
    public int rollModifier() { return (random.nextDouble() < 0.80) ? 1 : -1; }
}