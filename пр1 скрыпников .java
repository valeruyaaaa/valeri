
import .util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        CalculatorController controller = new CalculatorController(new CalculatorModel(), new CalculatorView());
        controller.start();
    }
}

// Модель (Model)
class CalculatorModel {

    public double calculate(String expression) {
        try {
            return evaluateExpression(expression);
        } catch (Exception e) {
            return Double.NaN; //  возвращаем NaN в случае ошибки
        }
    }

    private double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", ""); // Убираем пробелы
        if (!isValidExpression(expression)) {
            throw new IllegalArgumentException("Некорректное выражение.");
        }

        expression = convertIntegerDivision(expression); // Replace '//' with a custom operator
        return shuntingYard(expression);
    }


    private String convertIntegerDivision(String expression) {
        // Регулярное выражение для поиска "//"
        Pattern pattern = Pattern.compile("(\\d+)\\/\\/(\\d+)");
        Matcher matcher = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String dividend = matcher.group(1);
            String divisor = matcher.group(2);
            // Заменяем "//" на вызов метода integerDivide
            matcher.appendReplacement(sb, "integerDivide(" + dividend + "," + divisor + ")");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isValidExpression(String expression) {
        // Уравнение должно начинаться и заканчиваться числом.
        if (!Pattern.matches("^-?\\d+(\\.\\d+)?.*\\d+(\\.\\d+)?$", expression)) {
            return false;
        }

        // Проверка на допустимые символы
        if (!Pattern.matches("^[0-9+\\-*/^().//]*$", expression)) {
            return false;
        }

        // Проверка количества операндов (не более 100) - упрощенно (сложно точно посчитать без парсинга)
        if (expression.length() > 500) { //Очень грубая оценка, но помогает избежать перегрузки.
            return false;
        }

        return true;
    }


    private double shuntingYard(String expression) {
        Deque<Double> numbers = new ArrayDeque<>();
        Deque<String> operators = new ArrayDeque<>();

        Pattern tokenPattern = Pattern.compile("([+\\-*/^()]|integerDivide\\(\\d+,\\d+\\)|\\d+(\\.\\d+)?)");
        Matcher matcher = tokenPattern.matcher(expression);

        while (matcher.find()) {
            String token = matcher.group(1);

            if (Pattern.matches("\\d+(\\.\\d+)?", token)) {
                numbers.push(Double.parseDouble(token));
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop(); // Удаляем открывающую скобку
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(token);
            } else if (token.startsWith("integerDivide")) {
                //Вызов integerDivide обрабатывается сразу же.
                Pattern integerDividePattern = Pattern.compile("integerDivide\\((\\d+),(\\d+)\\)");
                Matcher integerDivideMatcher = integerDividePattern.matcher(token);
                if (integerDivideMatcher.find()) {
                    int num1 = Integer.parseInt(integerDivideMatcher.group(1));
                    int num2 = Integer.parseInt(integerDivideMatcher.group(2));
                    numbers.push((double) integerDivide(num1, num2));
                } else {
                    throw new IllegalArgumentException("Некорректный формат integerDivide.");
                }

            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
        }

        if (numbers.size() != 1) {
            throw new IllegalArgumentException("Некорректное выражение.");
        }

        return numbers.pop();
    }


    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("^");
    }

    private boolean hasPrecedence(String op1, String op2) {
        if (op2.equals("(") || op2.equals(")")) {
            return false;
        }
        if ((op1.equals("^")) && (op2.equals("*") || op2.equals("/") || op2.equals("+") || op2.equals("-"))) return false;
        if ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-"))) return false;
        return true;
    }


    private double applyOperator(String operator, double b, double a) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("Деление на ноль.");
                }
                return a / b;
            case "^":
                return Math.pow(a, b);
            default:
                throw new IllegalArgumentException("Неизвестный оператор: " + operator);
        }
    }

    // Деление нацело
    public int integerDivide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на ноль.");
        }
        return a / b;
    }

}


// Представление (View)
class CalculatorView {

    public void displayResult(double result) {
        if (Double.isNaN(result)) {
            System.out.println("Ошибка: некорректное выражение.");
        } else {
            System.out.println("Результат: " + result);
        }
    }

    public String getExpression() {
        System.out.print("Введите математическое выражение: ");
        return new java.util.Scanner(System.in).nextLine();
    }
}

// Контроллер (Controller)
class CalculatorController {

    private CalculatorModel model;
    private CalculatorView view;

    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;
    }

    public void start() {
        String expression = view.getExpression();
        double result = model.calculate(expression);
        view.displayResult(result);
    }
}
