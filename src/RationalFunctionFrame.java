import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;

public class RationalFunctionFrame extends JFrame {

    private final JSpinner degreeSpinner;
    private final JRadioButton easyBtn;
    private final JRadioButton medBtn;
    private final JRadioButton hardBtn;
    private final JTextField seedField;
    private final JButton generateButton;
    private final JButton answerButton;
    private final JButton graphButton;
    private final JTextArea outputArea;
    private final GraphPanel graphPanel;

    private RationalFunctionEngine.RationalFunction currentFunction;
    private final Random rng = new Random();

    public RationalFunctionFrame() {
        super("Rational Function Practice Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== top controls =====
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        degreeSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
        easyBtn = new JRadioButton("Easy");
        medBtn = new JRadioButton("Medium", true);
        hardBtn = new JRadioButton("Hard");

        ButtonGroup diffGroup = new ButtonGroup();
        diffGroup.add(easyBtn);
        diffGroup.add(medBtn);
        diffGroup.add(hardBtn);

        seedField = new JTextField(8);

        generateButton = new JButton("Generate Problem");
        answerButton = new JButton("Show Answer Key");
        graphButton = new JButton("Show Graph");

        int col = 0;

        gbc.gridx = col++;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Max degree:"), gbc);
        gbc.gridx = col++;
        topPanel.add(degreeSpinner, gbc);

        gbc.gridx = col++;
        topPanel.add(new JLabel("Difficulty:"), gbc);

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        diffPanel.add(easyBtn);
        diffPanel.add(medBtn);
        diffPanel.add(hardBtn);

        gbc.gridx = col++;
        topPanel.add(diffPanel, gbc);

        gbc.gridx = col++;
        topPanel.add(new JLabel("Seed (optional):"), gbc);

        gbc.gridx = col++;
        topPanel.add(seedField, gbc);

        gbc.gridx = col++;
        topPanel.add(generateButton, gbc);

        gbc.gridx = col++;
        topPanel.add(answerButton, gbc);

        gbc.gridx = col++;
        topPanel.add(graphButton, gbc);

        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        // ===== center: output + graph =====
        outputArea = new JTextArea(24, 60); // doubled rows
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 16)); // larger font
        JScrollPane outputScroll = new JScrollPane(outputArea);

        graphPanel = new GraphPanel();
        graphPanel.setBorder(BorderFactory.createTitledBorder("Graph"));

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        centerPanel.add(outputScroll, BorderLayout.CENTER);
        centerPanel.add(graphPanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize()); // allow resize, including horizontal

        generateButton.addActionListener(e -> onGenerate());
        answerButton.addActionListener(e -> onShowAnswer());
        graphButton.addActionListener(e -> onShowGraph());
    }

    private void onGenerate() {
        int maxDegree = (Integer) degreeSpinner.getValue();
        String difficulty = getDifficulty();
        Random localRng;

        String seedText = seedField.getText().trim();
        if (!seedText.isEmpty()) {
            try {
                int seed = Integer.parseInt(seedText);
                localRng = new Random(seed);
            } catch (NumberFormatException ex) {
                outputArea.append("Invalid seed. Using random seed instead.\n");
                localRng = rng;
            }
        } else {
            localRng = rng;
        }

        RationalFunctionEngine.RationalFunction rf = null;
        int attempts = 0;
        while (attempts < 200 && rf == null) {
            rf = RationalFunctionEngine.generateRational(maxDegree, difficulty, localRng);
            attempts++;
        }

        if (rf == null) {
            outputArea.setText("Too many attempts. Try again.\n");
            currentFunction = null;
            graphPanel.setFunction(null);
            return;
        }

        currentFunction = rf;
        String problemText = RationalFunctionEngine.buildProblemText(rf);
        outputArea.setText(problemText);
        graphPanel.setFunction(null);
    }

    private void onShowAnswer() {
        if (currentFunction == null) {
            outputArea.append("\nNo function generated yet.\n");
            return;
        }
        String ans = RationalFunctionEngine.buildAnswerKey(currentFunction);
        outputArea.append("\n" + ans);
    }

    private void onShowGraph() {
        if (currentFunction == null) {
            outputArea.append("\nNo function generated to graph.\n");
            return;
        }
        graphPanel.setFunction(currentFunction);
    }

    private String getDifficulty() {
        if (easyBtn.isSelected()) return "EASY";
        if (hardBtn.isSelected()) return "HARD";
        return "MEDIUM";
    }
}
