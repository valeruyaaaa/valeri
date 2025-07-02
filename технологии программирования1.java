import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

// Интерфейс инструмента для рисования
interface DrawingTool {
    void draw(Graphics2D g, Point start, Point end);
}

// Конкретные инструменты
class Pencil implements DrawingTool {
    @Override
    public void draw(Graphics2D g, Point start, Point end) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}

class Brush implements DrawingTool {
    @Override
    public void draw(Graphics2D g, Point start, Point end) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(5));
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}

class Eraser implements DrawingTool {
    @Override
    public void draw(Graphics2D g, Point start, Point end) {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(10));
        g.drawLine(start.x, start.y, end.x, end.y);
    }
}

// Фабрика инструментов
interface ToolFactory {
    DrawingTool createTool();
}

// Конкретные фабрики
class PencilFactory implements ToolFactory {
    @Override
    public DrawingTool createTool() {
        return new Pencil();
    }
}

class BrushFactory implements ToolFactory {
    @Override
    public DrawingTool createTool() {
        return new Brush();
    }
}

class EraserFactory implements ToolFactory {
    @Override
    public DrawingTool createTool() {
        return new Eraser();
    }
}

// Основной класс приложения
public class DrawingApp extends JFrame {
    private DrawingTool currentTool;
    private Point lastPoint;
    private BufferedImage canvas;
    
    public DrawingApp() {
        setTitle("Рисовалка");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Инициализация холста
        canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 800, 600);
        g2d.dispose();
        
        // Панель для рисования
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
            }
        };
        
        // Обработка событий мыши
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });
        
        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentTool != null && lastPoint != null) {
                    Graphics2D g2d = canvas.createGraphics();
                    currentTool.draw(g2d, lastPoint, e.getPoint());
                    g2d.dispose();
                    lastPoint = e.getPoint();
                    drawingPanel.repaint();
                }
            }
        });
        
        // Панель инструментов
        JToolBar toolBar = new JToolBar();
        
        // Создаем фабрики
        ToolFactory pencilFactory = new PencilFactory();
        ToolFactory brushFactory = new BrushFactory();
        ToolFactory eraserFactory = new EraserFactory();
        
        // Кнопки инструментов
        JButton pencilBtn = new JButton("Карандаш");
        pencilBtn.addActionListener(e -> currentTool = pencilFactory.createTool());
        
        JButton brushBtn = new JButton("Кисть");
        brushBtn.addActionListener(e -> currentTool = brushFactory.createTool());
        
        JButton eraserBtn = new JButton("Ластик");
        eraserBtn.addActionListener(e -> currentTool = eraserFactory.createTool());
        
        // Кнопка очистки
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> {
            Graphics2D g2d = canvas.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2d.dispose();
            drawingPanel.repaint();
        });
        
        toolBar.add(pencilBtn);
        toolBar.add(brushBtn);
        toolBar.add(eraserBtn);
        toolBar.add(clearBtn);
        
        add(toolBar, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        
        // Выбираем карандаш по умолчанию
        currentTool = pencilFactory.createTool();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawingApp app = new DrawingApp();
            app.setVisible(true);
        });
    }
}