import java.util.Arrays;

public class Perceptron {
    private final double[] weights;
    private final double threshold;
    private final String language;
    private int steps;

    public Perceptron(String language) {
        this.weights = new double[26];
        Arrays.fill(weights, 1);
        this.threshold = 1;
        this.language = language;
        this.steps = 0;
    }

    public int compute(double[] input) {
        double sum = 0;
        for (int i = 0; i < input.length; i++) {
            sum += input[i] * weights[i];
        }
        if (sum >= threshold) return 1;
        return 0;
    }

    public void learn(double[] input, int decision) {
        int output = compute(input);
        int error = decision - output;
        for (int i = 0; i < weights.length; i++) {
            weights[i] += error * input[i] * 0.1;
        }
        steps++;
    }

    public String getLanguage() {
        return language;
    }

    public int getSteps() {
        return steps;
    }
}
