
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

        if (!isBalanced(expression)) {
            throw new IllegalArgumentException("Несбалансированные скобки.");
        }

        return shuntingYard(expression);
    }

    private boolean isValidExpression(String expression) {
        // Проверка на допустимые символы
        if (!Pattern.matches("^[0-9+\\-*/^().!log]+$", expression)) {
            return false;
        }

        // Проверка количества операндов (ограничение по заданию - до 15 слагаемых) - грубая оценка.  Сложно точно посчитать без парсинга.
        String[] parts = expression.split("[+\\-]");
        if (parts.length > 15) { //  Простая оценка количества слагаемых
            return false;
        }

        return true;
    }

    private boolean isBalanced(String expression) {
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
            }
            if (balance < 0) {
                return false; // Закрывающая скобка без открывающей
            }
        }
        return balance == 0; // Все скобки должны быть закрыты
    }



    private double shuntingYard(String expression) {
        Deque<Double> numbers = new ArrayDeque<>();
        Deque<String> operators = new ArrayDeque<>();

        Pattern tokenPattern = Pattern.compile("(!|log|exp|\\*\\*|\\^|[+\\-*/()])|(\\d+(\\.\\d+)?)");  //Улучшенное регулярное выражение
        Matcher matcher = tokenPattern.matcher(expression);

        while (matcher.find()) {
            String operator = matcher.group(1);
            String number = matcher.group(2);

            if (number != null) {
                numbers.push(Double.parseDouble(number));
            } else if (operator != null) {
                if (operator.equals("!")) {
                    //  Обработка факториала
                    if (numbers.isEmpty()) {
                        throw new IllegalArgumentException("Некорректное выражение: Факториал от пустого значения.");
                    }
                    double operand = numbers.pop();
                    numbers.push(factorial((int) operand)); //  Факториал только для целых чисел
                } else if (operator.equals("log")) {
                    if (numbers.size() < 1) {
                         throw new IllegalArgumentException("Некорректное выражение: Недостаточно аргументов для log()");
                    }
                    double operand = numbers.pop();
                    numbers.push(Math.log(operand) / Math.log(2)); // log2
                } else if (operator.equals("exp")) {
                    if (numbers.size() < 1) {
                         throw new IllegalArgumentException("Некорректное выражение: Недостаточно аргументов для exp()");
                    }
                    double operand = numbers.pop();
                    numbers.push(Math.exp(operand));
                } else if (isOperator(operator)) {
                    if(operator.equals("**")){
                       operator = "^";  // Замена ** на ^
                    }
                    while (!operators.isEmpty() && hasPrecedence(operator, operators.peek())) {
                        numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                    }
                    operators.push(operator);
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

    // Факториал
    private double factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Факториал определен только для неотрицательных целых чисел.");
        }
        double result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
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
