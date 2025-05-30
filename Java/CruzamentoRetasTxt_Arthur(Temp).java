package javaapplication2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class CruzamentoRetasTxt_Arthur extends JPanel {
    static class Line {
        double a, b;
        int x1, y1, x2, y2;

        Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            if (x2 != x1) {
                this.a = (double)(y2 - y1) / (x2 - x1);
                this.b = y1 - this.a * x1;
            } else {
                this.a = Double.POSITIVE_INFINITY;
                this.b = x1;
            }
        }
    }

    static class Intersection {
        double x, y;
        int line1, line2;

        Intersection(double x, double y, int line1, int line2) {
            this.x = x;
            this.y = y;
            this.line1 = line1;
            this.line2 = line2;
        }
    }

    static class LineVariation {
        int lineIndex;
        double variation;

        LineVariation(int lineIndex, double variation) {
            this.lineIndex = lineIndex;
            this.variation = variation;
        }
    }

    private static List<Line> lines = new ArrayList<>();
    private static List<Intersection> intersections = new ArrayList<>();
    private static List<Intersection> firstQuadrantIntersections = new ArrayList<>();
    private static LineVariation maxVariation = null;
    private static final String FILE_PATH;
    private double scale = 20;
    private int centerX;
    private int centerY;
    private int lastX, lastY;

    static {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        String documentsFolder = os.contains("win") ? "Documents" : "Documentos";
        FILE_PATH = userHome + System.getProperty("file.separator") + documentsFolder + System.getProperty("file.separator") + "input.txt";
    }

    public CruzamentoRetasTxt_Arthur() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    scale *= 1.1;
                } else {
                    scale /= 1.1;
                }
                scale = Math.max(scale, 1.0);
                repaint();
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;
                centerX += dx;
                centerY += dy;
                lastX = e.getX();
                lastY = e.getY();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        });
    }

    public static void main(String[] args) {
        readLinesFromFile(FILE_PATH);
        findAllIntersections();
        findFirstQuadrantIntersections();
        findMaxLineVariation();

        JFrame frame = new JFrame("Gráfico das Retas com Interseções");
        CruzamentoRetasTxt_Arthur panel = new CruzamentoRetasTxt_Arthur();
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void readLinesFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 4) {
                    int x1 = Integer.parseInt(parts[0].replace("(", "").replace(",", ""));
                    int y1 = Integer.parseInt(parts[1].replace(")", ""));
                    int x2 = Integer.parseInt(parts[2].replace("(", "").replace(",", ""));
                    int y2 = Integer.parseInt(parts[3].replace(")", ""));
                    
                    lines.add(new Line(x1, y1, x2, y2));
                    Line addedLine = lines.get(lines.size() - 1);
                    
                    if (x2 != x1) {
                        System.out.println("Reta " + lines.size() + ": y = " + String.format("%.2f", addedLine.a) + "x + " + String.format("%.2f", addedLine.b));
                    } else {
                        System.out.println("Reta " + lines.size() + ": x = " + addedLine.b + " (vertical)");
                    }
                    
                    System.out.println("Pontos: (" + x1 + "," + y1 + ") e (" + x2 + "," + y2 + ")");
                }
            }
            System.out.println("\nTotal de retas encontradas: " + lines.size());
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
    
    private static void findAllIntersections() {
        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                Line line1 = lines.get(i);
                Line line2 = lines.get(j);
                
                if (line1.x1 == line1.x2 && line2.x1 == line2.x2) {
                    if (line1.x1 == line2.x1) {
                        System.out.println("Retas " + (i + 1) + " e " + (j + 1) + " são coincidentes (verticais).");
                    } else {
                        System.out.println("Retas " + (i + 1) + " e " + (j + 1) + " são paralelas (verticais).");
                    }
                } else if (line1.x1 == line1.x2) {
                    double x = line1.x1;
                    double y = line2.a * x + line2.b;
                    intersections.add(new Intersection(x, y, i, j));
                    System.out.println("Interseção entre reta " + (i + 1) + " e reta " + (j + 1) + ": (" + 
                                       String.format("%.2f", x) + ", " + String.format("%.2f", y) + ")");
                } else if (line2.x1 == line2.x2) {
                    double x = line2.x1;
                    double y = line1.a * x + line1.b;
                    intersections.add(new Intersection(x, y, i, j));
                    System.out.println("Interseção entre reta " + (i + 1) + " e reta " + (j + 1) + ": (" + 
                                       String.format("%.2f", x) + ", " + String.format("%.2f", y) + ")");
                } else {
                    double a1 = line1.a, b1 = line1.b;
                    double a2 = line2.a, b2 = line2.b;

                    if (Math.abs(a1 - a2) < 0.0001) {
                        if (Math.abs(b1 - b2) < 0.0001) {
                            System.out.println("Retas " + (i + 1) + " e " + (j + 1) + " são coincidentes.");
                        } else {
                            System.out.println("Retas " + (i + 1) + " e " + (j + 1) + " são paralelas.");
                        }
                    } else {
                        double x = (b2 - b1) / (a1 - a2);
                        double y = a1 * x + b1;
                        intersections.add(new Intersection(x, y, i, j));
                        System.out.println("Interseção entre reta " + (i + 1) + " e reta " + (j + 1) + ": (" + 
                                           String.format("%.2f", x) + ", " + String.format("%.2f", y) + ")");
                    }
                }
            }
        }
        System.out.println("\nTotal de interseções encontradas: " + intersections.size());
    }

    private static void findFirstQuadrantIntersections() {
        for (Intersection inter : intersections) {
            if (inter.x > 0 && inter.y > 0) {
                firstQuadrantIntersections.add(inter);
                System.out.println("Interseção no primeiro quadrante entre reta " + (inter.line1 + 1) + 
                                   " e reta " + (inter.line2 + 1) + ": (" + 
                                   String.format("%.2f", inter.x) + ", " + String.format("%.2f", inter.y) + ")");
            }
        }
        System.out.println("Total de interseções no primeiro quadrante: " + firstQuadrantIntersections.size());
    }

    private static void findMaxLineVariation() {
        if (firstQuadrantIntersections.isEmpty()) {
            System.out.println("Nenhuma interseção no primeiro quadrante. Não há variação para calcular.");
            return;
        }

        // Collect unique line indices involved in first-quadrant intersections
        Set<Integer> involvedLines = new HashSet<>();
        for (Intersection inter : firstQuadrantIntersections) {
            involvedLines.add(inter.line1);
            involvedLines.add(inter.line2);
        }

        double maxVar = -1;
        int maxVarLineIndex = -1;

        // For each involved line, calculate its y-range in the first quadrant
        for (int lineIndex : involvedLines) {
            Line line = lines.get(lineIndex);
            double variation;

            if (Double.isInfinite(line.a)) { // Vertical line: x = b
                if (line.b <= 0) continue; // Line not in first quadrant
                // Find min and max y among intersections on this line in first quadrant
                double minY = Double.MAX_VALUE;
                double maxY = 0;
                for (Intersection inter : firstQuadrantIntersections) {
                    if (inter.line1 == lineIndex || inter.line2 == lineIndex) {
                        if (Math.abs(inter.x - line.b) < 0.0001) { // Point lies on this vertical line
                            minY = Math.min(minY, inter.y);
                            maxY = Math.max(maxY, inter.y);
                        }
                    }
                }
                variation = (maxY > 0 && minY < Double.MAX_VALUE) ? maxY - minY : 0;
            } else { // Non-vertical line: y = ax + b
                // Find y-range in first quadrant
                List<Double> yValues = new ArrayList<>();
                // y at x = 0 (if y > 0)
                double yAtX0 = line.b;
                if (yAtX0 > 0) {
                    yValues.add(yAtX0);
                }
                // y at intersections in first quadrant
                for (Intersection inter : firstQuadrantIntersections) {
                    if (inter.line1 == lineIndex || inter.line2 == lineIndex) {
                        // Verify the point lies on the line
                        double expectedY = line.a * inter.x + line.b;
                        if (Math.abs(expectedY - inter.y) < 0.0001) {
                            yValues.add(inter.y);
                        }
                    }
                }
                // y at x where y = 0 (if x > 0)
                double xAtY0 = -line.b / line.a;
                if (xAtY0 > 0) {
                    yValues.add(0.0);
                }
                // Find min and max y
                if (yValues.isEmpty()) {
                    variation = 0;
                } else {
                    double minY = yValues.stream().min(Double::compare).get();
                    double maxY = yValues.stream().max(Double::compare).get();
                    variation = maxY - minY;
                }
            }

            System.out.println("Variação da reta " + (lineIndex + 1) + " no primeiro quadrante: " + 
                               String.format("%.2f", variation));

            if (variation > maxVar) {
                maxVar = variation;
                maxVarLineIndex = lineIndex;
            }
        }

        if (maxVarLineIndex >= 0) {
            maxVariation = new LineVariation(maxVarLineIndex, maxVar);
            System.out.println("Maior variação no primeiro quadrante: Reta " + (maxVarLineIndex + 1) + 
                               " com variação " + String.format("%.2f", maxVar));
        } else {
            System.out.println("Nenhuma variação válida encontrada no primeiro quadrante.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (centerX == 0 && centerY == 0) {
            centerX = getWidth() / 2;
            centerY = getHeight() / 2;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int panelLineHeight = 20;
        int headerHeight = 25;
        int sectionSeparator = 25;
        int padding = 15;
        
        int totalHeight = headerHeight + panelLineHeight * lines.size() + sectionSeparator +
                          panelLineHeight + panelLineHeight * intersections.size() + 
                          sectionSeparator + panelLineHeight * firstQuadrantIntersections.size() + 
                          sectionSeparator + panelLineHeight + padding;
        
        int panelWidth = 250;
        if (lines.size() > 10 || intersections.size() > 15 || firstQuadrantIntersections.size() > 10) {
            panelWidth = 300;
        }
        
        int maxPanelHeight = getHeight() - 10;
        boolean needScrolling = totalHeight > maxPanelHeight;
        
        int actualPanelHeight = needScrolling ? maxPanelHeight : totalHeight;
        
        g2d.setColor(new Color(240, 240, 240, 220));
        g2d.fillRect(5, 5, panelWidth, actualPanelHeight);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(5, 5, panelWidth, actualPanelHeight);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, centerY, getWidth(), centerY);
        g2d.drawLine(centerX, 0, centerX, getHeight());

        int tickSize = 5;
        int interval = 5;
        for (int x = -(int)(centerX / scale); x <= (getWidth() - centerX) / scale; x += interval) {
            int xPos = centerX + (int)(x * scale);
            g2d.drawLine(xPos, centerY - tickSize, xPos, centerY + tickSize);
            if (x != 0) {
                g2d.drawString(String.valueOf(x), xPos - 5, centerY + 20);
            }
        }
        for (int y = -(int)(centerY / scale); y <= (getHeight() - centerY) / scale; y += interval) {
            int yPos = centerY - (int)(y * scale);
            g2d.drawLine(centerX - tickSize, yPos, centerX + tickSize, yPos);
            if (y != 0) {
                g2d.drawString(String.valueOf(y), centerX + 10, yPos + 5);
            }
        }
        
        g2d.drawString("0", centerX + 5, centerY + 15);

        double minX = -centerX / scale;
        double maxX = (getWidth() - centerX) / scale;
        double minY = -(getHeight() - centerY) / scale;
        double maxY = centerY / scale;

        Color[] colors = {
            new Color(65, 105, 225),
            new Color(34, 139, 34),
            new Color(178, 34, 34),
            new Color(218, 165, 32),
            new Color(72, 61, 139),
            new Color(0, 139, 139),
            new Color(205, 92, 92),
            new Color(85, 107, 47),
            new Color(139, 69, 19)
        };
        
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Color lineColor = colors[i % colors.length];
            g2d.setColor(lineColor);
            
            if (line.x1 == line.x2) {
                int screenX = centerX + (int)(line.x1 * scale);
                g2d.drawLine(screenX, 0, screenX, getHeight());
            } else {
                int x1Screen = centerX + (int)(line.x1 * scale);
                int y1Screen = centerY - (int)(line.y1 * scale);
                int x2Screen = centerX + (int)(line.x2 * scale);
                int y2Screen = centerY - (int)(line.y2 * scale);
                
                g2d.setStroke(new java.awt.BasicStroke(2.5f));
                g2d.drawLine(x1Screen, y1Screen, x2Screen, y2Screen);
                
                double slope = line.a;
                double intercept = line.b;
                
                double leftY = slope * minX + intercept;
                int leftYScreen = centerY - (int)(leftY * scale);
                
                double rightY = slope * maxX + intercept;
                int rightYScreen = centerY - (int)(rightY * scale);
                
                g2d.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_BUTT,
                                                    java.awt.BasicStroke.JOIN_MITER, 10.0f,
                                                    new float[]{8.0f, 4.0f}, 0.0f));
                g2d.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 100));
                g2d.drawLine(0, leftYScreen, getWidth(), rightYScreen);
                
                g2d.setStroke(new java.awt.BasicStroke(1.0f));
            }
            
            int pointSize = 8;
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX + (int)(line.x1 * scale) - pointSize/2, centerY - (int)(line.y1 * scale) - pointSize/2,
                        pointSize, pointSize);
            g2d.fillOval(centerX + (int)(line.x2 * scale) - pointSize/2, centerY - (int)(line.y2 * scale) - pointSize/2,
                        pointSize, pointSize);
            
            g2d.setColor(lineColor.darker());
            g2d.drawOval(centerX + (int)(line.x1 * scale) - pointSize/2, centerY - (int)(line.y1 * scale) - pointSize/2,
                        pointSize, pointSize);
            g2d.drawOval(centerX + (int)(line.x2 * scale) - pointSize/2, centerY - (int)(line.y2 * scale) - pointSize/2,
                        pointSize, pointSize);
            
            String p1Label = "(" + line.x1 + "," + line.y1 + ")";
            String p2Label = "(" + line.x2 + "," + line.y2 + ")";
            
            Font originalFont = g2d.getFont();
            Font smallFont = new Font(originalFont.getName(), Font.PLAIN, 10);
            g2d.setFont(smallFont);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(p1Label, centerX + (int)(line.x1 * scale) + 5, centerY - (int)(line.y1 * scale) - 5);
            g2d.drawString(p2Label, centerX + (int)(line.x2 * scale) + 5, centerY - (int)(line.y2 * scale) - 5);
            
            g2d.setFont(originalFont);
        }

        int intersectSize = 10;
        for (Intersection inter : intersections) {
            int ix = centerX + (int)(inter.x * scale) - intersectSize/2;
            int iy = centerY - (int)(inter.y * scale) - intersectSize/2;
            
            if (ix >= -intersectSize && ix <= getWidth() + intersectSize &&
                iy >= -intersectSize && iy <= getHeight() + intersectSize) {
                g2d.setColor(firstQuadrantIntersections.contains(inter) ? Color.RED : Color.YELLOW);
                g2d.fillOval(ix, iy, intersectSize, intersectSize);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(ix, iy, intersectSize, intersectSize);
                
                String interLabel = "(" + String.format("%.1f", inter.x) + "," + String.format("%.1f", inter.y) + ")";
                g2d.drawString(interLabel, ix + intersectSize, iy);
            }
        }

        g2d.setClip(5, 5, panelWidth, actualPanelHeight);
        
        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Informações do Gráfico", 15, 20);
        
        g2d.drawLine(15, 25, panelWidth - 15, 25);
        
        int currentY = 45;
        
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            String eq;
            g2d.setColor(colors[i % colors.length]);
            
            if (line.x1 == line.x2) {
                eq = String.format("Reta %d: x = %d (vertical)", i + 1, line.x1);
            } else {
                eq = String.format("Reta %d: y = %.2fx + %.2f", i + 1, line.a, line.b);
            }
            
            if (currentY < actualPanelHeight - 10) {
                g2d.drawString(eq, 15, currentY);
                currentY += panelLineHeight;
            }
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawLine(15, currentY, panelWidth - 15, currentY);
            currentY += 15;
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.drawString("Interseções", 15, currentY);
            currentY += 20;
        }
        
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int displayedIntersections = 0;
        
        for (int i = 0; i < intersections.size() && currentY < actualPanelHeight - 10; i++) {
            Intersection inter = intersections.get(i);
            String intersect = String.format("R%d ∩ R%d: (%.2f, %.2f)",
                                          inter.line1 + 1, inter.line2 + 1, inter.x, inter.y);
            g2d.setColor(Color.BLACK);
            g2d.drawString(intersect, 15, currentY);
            currentY += panelLineHeight;
            displayedIntersections++;
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawLine(15, currentY, panelWidth - 15, currentY);
            currentY += 15;
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.drawString("Interseções 1º Quadrante", 15, currentY);
            currentY += 20;
        }
        
        for (int i = 0; i < firstQuadrantIntersections.size() && currentY < actualPanelHeight - 10; i++) {
            Intersection inter = firstQuadrantIntersections.get(i);
            String intersect = String.format("R%d ∩ R%d: (%.2f, %.2f)",
                                          inter.line1 + 1, inter.line2 + 1, inter.x, inter.y);
            g2d.setColor(Color.BLACK);
            g2d.drawString(intersect, 15, currentY);
            currentY += panelLineHeight;
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawLine(15, currentY, panelWidth - 15, currentY);
            currentY += 15;
        }
        
        if (currentY < actualPanelHeight - 10) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.drawString("Maior Variação", 15, currentY);
            currentY += 20;
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            if (maxVariation != null) {
                String varText = String.format("R%d: %.2f", maxVariation.lineIndex + 1, maxVariation.variation);
                g2d.setColor(colors[maxVariation.lineIndex % colors.length]);
                g2d.drawString(varText, 15, currentY);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.drawString("Nenhuma variação válida", 15, currentY);
            }
            currentY += panelLineHeight;
        }
        
        if (displayedIntersections < intersections.size() || needScrolling) {
            g2d.setColor(new Color(50, 50, 50));
            g2d.setFont(new Font("Arial", Font.ITALIC, 11));
            g2d.drawString("... mais informações disponíveis", 15, actualPanelHeight - 10);
        }
        
        g2d.setClip(null);
    }
}