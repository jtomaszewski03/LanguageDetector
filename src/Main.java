import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private static List<Perceptron> perceptrons;

    public Main() {
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        JLabel label = new JLabel("Wklej tekst");
        JTextArea textArea = new JTextArea();
        JButton button = new JButton("Sprawdź język");
        JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.BOLD, 20));
        scrollPane.setPreferredSize(new Dimension(450, 450));
        JPanel topPanel = new JPanel();
        topPanel.add(label);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(button);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(null);
        button.addActionListener(e -> {
            if (!(textArea.getText().length() < 150)) {
                double[] vector = intoVector(textArea.getText());
                JOptionPane.showMessageDialog(null, getLanguage(vector, perceptrons), "Wykryty język", JOptionPane.INFORMATION_MESSAGE);
            } else JOptionPane.showMessageDialog(null, "Zbyt krótki tekst");
        });
    }

    public static void main(String[] args) {
        List<Map.Entry<double[], String>> trainingData = fillWithData("./training_data");
        List<Map.Entry<double[], String>> testData = fillWithData("./test_data");
        Set<String> languages = new HashSet<>();
        for (Map.Entry<double[], String> row : trainingData) {
            languages.add(row.getValue());
        }
        perceptrons = new ArrayList<>();
        for (String language : languages) {
            perceptrons.add(new Perceptron(language));
        }
        int decision;
        int error;
        for (Perceptron perceptron : perceptrons) {
            do {
                error = 0;
                for (Map.Entry<double[], String> row : trainingData) {
                    String language = row.getValue();
                    if (language.equalsIgnoreCase(perceptron.getLanguage())) decision = 1;
                    else decision = 0;
                    if (perceptron.compute(row.getKey()) != decision) {
                        error++;
                        perceptron.learn(row.getKey(), decision);
                    }
                }
            } while (error > 0);
            System.out.println(perceptron.getLanguage() + " " + perceptron.getSteps());
        }
        for (Perceptron perceptron : perceptrons) {
            int correct = 0;
            int count = 0;
            for (Map.Entry<double[], String> row : testData) {
                String name = row.getValue();
                if (name.equalsIgnoreCase(perceptron.getLanguage())) decision = 1;
                else decision = 0;
                if (perceptron.getLanguage().equalsIgnoreCase(name)) {
                    count++;
                    if (perceptron.compute(row.getKey()) == decision) correct++;
                }
            }
            System.out.println("Accuracy for " + perceptron.getLanguage() + ": " + correct + "/" + count);
        }
        SwingUtilities.invokeLater(Main::new);
    }

    private static List<Map.Entry<double[], String>> fillWithData(String path) {
        List<Map.Entry<double[], String>> data = new ArrayList<>();
        try {
            Files.walk(Paths.get(path)).filter(Files::isDirectory).skip(1).forEach(directory -> {
                String name = directory.getFileName().toString();
                try {
                    Files.walk(directory).filter(Files::isRegularFile).forEach(file -> {
                        try {
                            String text = String.join("", Files.readAllLines(file));
                            data.add(Map.entry(intoVector(text), name));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private static double[] intoVector(String text) {
        Map<Character, Double> map = new HashMap<>();
        char[] chars = text.toCharArray();
        int count = 0;
        for (char c : chars) {
            c = String.valueOf(c).toLowerCase().charAt(0);
            if (c >= 'a' && c <= 'z') {
                map.put(c, map.getOrDefault(c, 0.0) + 1);
                count++;
            }
        }
        for (Map.Entry<Character, Double> entry : map.entrySet()) {
            map.put(entry.getKey(), map.get(entry.getKey()) / count);
        }
        int iter = 0;
        double[] result = new double[26];
        for (char i = 'a'; i <= 'z'; i++) {
            map.putIfAbsent(i, 0.0);
            result[iter++] = map.get(i);
        }
        return result;
    }

    public String getLanguage(double[] vector, List<Perceptron> perceptrons) {
        StringBuilder language = new StringBuilder();
        for (Perceptron perceptron : perceptrons) {
            if (perceptron.compute(vector) == 1) {
                language.append(perceptron.getLanguage()).append(" ");
            }
        }
        if (language.isEmpty()) return "Nie udało się rozpoznać języka";
        return language.toString();
    }
}