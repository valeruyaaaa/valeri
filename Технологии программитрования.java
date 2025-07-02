
import x.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

// 1. Интерфейс для инструментов рисования
interface DrawingTool {
    void draw(Graphics g, int x1, int y1, int x2, int y2);
}

// 2. Конкретные реализации инструментов
class Pencil implements DrawingTool {
    @Override
    public void draw(Graphics g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }
}

class Brush implements DrawingTool {
    private int brushSize = 5; // Размер кисти

    public Brush(int brushSize) {
        this.brushSize = brushSize;
    }

    @Override
    public void draw(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        g2d.setStroke(new BasicStroke(1)); // Возвращаем значение по умолчанию
    }
}

class Eraser implements DrawingTool {
    private int eraserSize = 10;

    public Eraser(int eraserSize) {
        this.eraserSize = eraserSize;
    }

    @Override
    public void draw(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE); // Цвет ластика - белый (цвет фона)
        g2d.setStroke(new BasicStroke(eraserSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        g2d.setColor(Color.BLACK); // Возвращаем цвет по умолчанию
        g2d.setStroke(new BasicStroke(1));
    }
}

// 3. Фабричный метод для создания инструментов
class ToolFactory {
    public static DrawingTool createTool(String toolType) {
        return createTool(toolType, 1); // используем размер по умолчанию для кисти и ластика
    }
    public static DrawingTool createTool(String toolType, int size) {
        switch (toolType) {
            case "Pencil":
                return new Pencil();
            case "Brush":
                return new Brush(size);
            case "Eraser":
                return new Eraser(size);
            default:
                throw new IllegalArgumentException("Неизвестный тип инструмента: " + toolType);
        }
    }
}

// 4. Класс для рисования
class DrawingPanel extends JPanel {
    private BufferedImage image; // Для хранения нарисованного изображения
    private DrawingTool currentTool = ToolFactory.createTool("Pencil"); // Инструмент по умолчанию
    private int x1, y1, x2, y2;

    public DrawingPanel(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height); // Фон по умолчанию - белый
        g2d.setColor(Color.BLACK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x1 = e.getX();
                y1 = e.getY();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getX();
                y2 = e.getY();
                currentTool.draw(image.getGraphics(), x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
                repaint(); // Перерисовываем панель
            }
        });
    }

    // Метод для установки текущего инструмента
    public void setCurrentTool(DrawingTool tool) {
        this.currentTool = tool;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}

// 5. Главный класс приложения
public class DrawingApp extends JFrame {

    private DrawingPanel drawingPanel;

    public DrawingApp() {
        setTitle("Drawing App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        drawingPanel = new DrawingPanel(600, 500);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.NORTH);

        // Создание кнопок для выбора инструментов
        JButton pencilButton = new JButton("Pencil");
        JButton brushButton = new JButton("Brush");
        JButton eraserButton = new JButton("Eraser");

        buttonPanel.add(pencilButton);
        buttonPanel.add(brushButton);
        buttonPanel.add(eraserButton);

        // ActionListener для кнопок
        pencilButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.setCurrentTool(ToolFactory.createTool("Pencil"));
            }
        });

        brushButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.setCurrentTool(ToolFactory.createTool("Brush", 10));
            }
        });

        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingPanel.setCurrentTool(ToolFactory.createTool("Eraser", 20));
            }
        });



        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DrawingApp::new);
    }
}
