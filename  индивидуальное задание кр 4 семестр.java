
import .awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.*;

// Model
class CalculatorModel {
    private List<String> history = new ArrayList<>();
    private final String historyFilePath = "calculator_history.txt";

    public CalculatorModel() {
        loadHistory();
    }

    public double calculate(String expression) {
        try {
            double result = evaluateExpression(expression);
            history.add(expression + " = " + result);
            saveHistory();
            return result;
        } catch (Exception e) {
            history.add(expression + " = Error: " + e.getMessage());
            saveHistory();
            throw new IllegalArgumentException("Invalid expression: " + e.getMessage());
        }
    }

    // Реализация алгоритма вычисления математического выражения
    private double evaluateExpression(String expression) {
        return shuntingYard(expression);
    }


    //Shunting Yard Algorithm - Преобразование инфиксной нотации в постфиксную (RPN)
    private double shuntingYard(String expression) {
        StringBuilder output = new StringBuilder();
        Stack<Character> operators = new Stack<>();

        expression = expression.replaceAll("\\s+", ""); //Удаляем пробелы

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i));
                    i++;
                }
                i--; // Вернуться на один символ назад
                output.append(number).append(" ");
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    output.append(operators.pop()).append(" ");
                }
                operators.pop(); // Удалить '('
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && operators.peek() != '(' && precedence(c) <= precedence(operators.peek())) {
                    output.append(operators.pop()).append(" ");
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            output.append(operators.pop()).append(" ");
        }

        return evaluateRPN(output.toString());
    }


    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            case '^':
                return 3;
            default:
                return 0;
        }
    }

    private double evaluateRPN(String rpnExpression) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = rpnExpression.split(" ");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.' || (token.length() > 1 && token.charAt(0) == '-' && Character.isDigit(token.charAt(1)))) {
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token.charAt(0))) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid RPN expression");
                }
                double operand2 = stack.pop();
                double operand1 = stack.pop();

                switch (token.charAt(0)) {
                    case '+':
                        stack.push(operand1 + operand2);
                        break;
                    case '-':
                        stack.push(operand1 - operand2);
                        break;
                    case '*':
                        stack.push(operand1 * operand2);
                        break;
                    case '/':
                        if (operand2 == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        stack.push(operand1 / operand2);
                        break;
                    case '^':
                        stack.push(Math.pow(operand1, operand2));
                        break;
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid RPN expression");
        }
        return stack.pop();
    }


    public List<String> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию для безопасности
    }

    public void saveHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(historyFilePath))) {
            for (String entry : history) {
                writer.println(entry);
            }
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    private void loadHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            // Файл может не существовать при первом запуске
            System.err.println("History file not found, creating new one.");
        }
    }

    public void saveSelectedHistory(List<Integer> selectedIndices, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (int index : selectedIndices) {
                if (index >= 0 && index < history.size()) {
                    writer.println(history.get(index));
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving selected history: " + e.getMessage());
        }
    }
}

// View
class CalculatorView extends JFrame {

    private JTextField display;
    private JTextArea historyArea;
    private JButton calculateButton, saveHistoryButton, saveSelectedHistoryButton, exitButton;
    private JList<String> historyList;
    private DefaultListModel<String> historyListModel;


    public CalculatorView() {
        setTitle("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLayout(new BorderLayout());

        display = new JTextField();
        display.setHorizontalAlignment(JTextField.RIGHT);
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4));
        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+"
        };
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            buttonPanel.add(button);
            button.addActionListener(e -> {
                String text = ((JButton) e.getSource()).getText();
                if (text.equals("=")) {
                    // Передаем управление контроллеру
                } else {
                    display.setText(display.getText() + text);
                }
            });
        }
        add(buttonPanel, BorderLayout.CENTER);

        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        add(historyScrollPane, BorderLayout.EAST);


        JPanel controlPanel = new JPanel();
        calculateButton = new JButton("Calculate");
        saveHistoryButton = new JButton("Save History");
        saveSelectedHistoryButton = new JButton("Save Selected");
        exitButton = new JButton("Exit");

        controlPanel.add(calculateButton);
        controlPanel.add(saveHistoryButton);
        controlPanel.add(saveSelectedHistoryButton);
        controlPanel.add(exitButton);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public String getExpression() {
        return display.getText();
    }

    public void setDisplay(String value) {
        display.setText(value);
    }

    public void setHistory(List<String> history) {
        historyListModel.clear();
        for (String entry : history) {
            historyListModel.addElement(entry);
        }
    }

    public JList<String> getHistoryList() {
        return historyList;
    }

    public void addCalculateListener(ActionListener listener) {
        calculateButton.addActionListener(listener);
    }

    public void addSaveHistoryListener(ActionListener listener) {
        saveHistoryButton.addActionListener(listener);
    }

     public void addSaveSelectedHistoryListener(ActionListener listener) {
        saveSelectedHistoryButton.addActionListener(listener);
    }


    public void addExitListener(ActionListener listener) {
        exitButton.addActionListener(listener);
    }


    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}

// Controller
class CalculatorController {

    private CalculatorModel model;
    private CalculatorView view;

    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;

        // Загрузка истории при инициализации
        updateViewFromModel();

        view.addCalculateListener(e -> calculate());
        view.addSaveHistoryListener(e -> saveHistoryToFile());
        view.addSaveSelectedHistoryListener(e -> saveSelectedHistory());
        view.addExitListener(e -> exitApplication());

    }


    private void calculate() {
        String expression = view.getExpression();
        try {
            double result = model.calculate(expression);
            view.setDisplay(String.valueOf(result));
            updateViewFromModel();
        } catch (IllegalArgumentException e) {
            view.showMessage("Error: " + e.getMessage());
            updateViewFromModel();
        }
    }

    private void saveHistoryToFile() {
        String filePath = view.showMessageInput("Enter file path to save history (leave blank for default):");
        if (filePath == null) return; // Пользователь отменил ввод

        if (filePath.trim().isEmpty()) {
            view.showMessage("History saved to default file: " + new File("calculator_history.txt").getAbsolutePath());
            return;
        }

        File file = new File(filePath);
        String absolutePath = file.getAbsolutePath();
        String fileName = file.getName();
        String parentPath = file.getParent();


        if (fileName.contains(".")) {  // Указано имя файла с расширением
            try {
                File outputFile = new File(absolutePath);
                model.saveSelectedHistory(getAllHistoryIndices(), absolutePath);
                view.showMessage("History saved to: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                view.showMessage("Error saving history: " + e.getMessage());
            }

        } else if (parentPath != null) { //указан только путь сохранения
            try {
                File outputFile = new File(filePath, "log.log");
                model.saveSelectedHistory(getAllHistoryIndices(), outputFile.getAbsolutePath());
                view.showMessage("History saved to: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                view.showMessage("Error saving history: " + e.getMessage());
            }

        } else {
            view.showMessage("Invalid file path.  History сохранена в " + new File("calculator_history.txt").getAbsolutePath());
        }
    }



    private List<Integer> getAllHistoryIndices() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < model.getHistory().size(); i++) {
            indices.add(i);
        }
        return indices;
    }


    private void saveSelectedHistory() {
        String filePath = view.showMessageInput("Enter file path to save selected history (leave blank for default):");
        if (filePath == null) return;

        if (filePath.trim().isEmpty()) {
            view.showMessage("Please enter file path.");
            return;
        }

        File file = new File(filePath);
        String absolutePath = file.getAbsolutePath();
        String fileName = file.getName();
        String parentPath = file.getParent();


        if (fileName.contains(".")) {  // Указано имя файла с расширением
            try {
                File outputFile = new File(absolutePath);
                List<Integer> selectedIndices = view.getHistoryList().getSelectedValuesList().stream()
                        .map(model.getHistory()::indexOf)
                        .toList();

                model.saveSelectedHistory(selectedIndices, absolutePath);
                view.showMessage("History saved to: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                view.showMessage("Error saving history: " + e.getMessage());
            }

        } else if (parentPath != null) { //указан только путь сохранения
            try {
                File outputFile = new File(filePath, "log.log");
                List<Integer> selectedIndices = view.getHistoryList().getSelectedValuesList().stream()
                        .map(model.getHistory()::indexOf)
                        .toList();
                model.saveSelectedHistory(selectedIndices, outputFile.getAbsolutePath());
                view.showMessage("History saved to: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                view.showMessage("Error saving history: " + e.getMessage());
            }

        } else {
            view.showMessage("Invalid file path.");
        }
    }



    private void exitApplication() {
        System.exit(0);
    }


    private void updateViewFromModel() {
        view.setHistory(model.getHistory());
    }
}


interface ShowMessageInterface {
    String showMessageInput(String message);
}

class CalculatorViewWithDialogs extends CalculatorView implements ShowMessageInterface {

    @Override
    public String showMessageInput(String message) {
        return JOptionPane.showInputDialog(this, message);
    }
}



// Main Class
public class Calculator {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorModel model = new CalculatorModel();
            CalculatorViewWithDialogs view = new CalculatorViewWithDialogs();
            new CalculatorController(model, view);
        });
    }
}
