import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphPanel extends JPanel {
	
    private RationalFunctionEngine.RationalFunction current; // current function
    
    // visible window in math coordinates
    private double xMin = -10;
    private double xMax = 10;
    private double yMin = -10;
    private double yMax = 10;
    
    // drag state
    private int lastDragX;
    private int lastDragY;
    private boolean dragging = false;
    
    
    public GraphPanel() {
    	
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500,400));
        
        // mouse drag to move view
        MouseAdapter adapter = new MouseAdapter() {
        	
            @Override
            public void mousePressed(MouseEvent e) {
            	
                dragging = true;
                lastDragX = e.getX();
                lastDragY = e.getY();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            
            @Override
            public void mouseReleased(MouseEvent e) {
            	
                dragging = false;
                setCursor(Cursor.getDefaultCursor());
            }

            
            @Override
            public void mouseDragged(MouseEvent e) {
            	
                if (!dragging) return;
                
                int dx = e.getX() - lastDragX;
                int dy = e.getY() - lastDragY;
                
                // convert screen move to world move
                double worldDx = -dx * (xMax - xMin) / getWidth();
                double worldDy =  dy * (yMax - yMin) / getHeight();
                
                xMin += worldDx;
                xMax += worldDx;
                yMin += worldDy;
                yMax += worldDy;
                
                lastDragX = e.getX();
                lastDragY = e.getY();
                
                repaint();
            }
        };
        
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }
    
    
    public void setFunction(RationalFunctionEngine.RationalFunction rf) {
    	
        this.current = rf;
        repaint();
    }

    
    @Override
    protected void paintComponent(Graphics g) {
    	
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        // always draw axes
        drawAxes(g2);
        
        // only draw function stuff if one exists
        if (current != null) {
            drawVerticalAsymptotes(g2);
            drawFunction(g2);
            drawRoots(g2);
        }
    }

    
    // draw x and y axes
    private void drawAxes(Graphics2D g2) {
    	
        int w = getWidth();
        int h = getHeight();
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(0,h / 2,w,1);
        g2.fillRect(w / 2,0,1,h);
        
        g2.setColor(Color.BLACK);
        // thicker central axes at x = 0, y = 0
        g2.fillRect(0,worldToScreenY(0) - 1,w,3);
        g2.fillRect(worldToScreenX(0) - 1,0,3,h);
    }

    
    // draw dotted vertical lines at denominator roots (vertical asymptotes)
    private void drawVerticalAsymptotes(Graphics2D g2) {
    	
        if (current == null) return;
        
        g2.setColor(Color.RED);
        
        float[] dash = {5f,5f};
        BasicStroke oldStroke = (BasicStroke) g2.getStroke();
        
        g2.setStroke(
            new BasicStroke(
                1f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0f,
                dash,
                0f
            )
        );
        
        // each denom root -> one vertical line
        for (int root : current.denom.roots) {
            int sx = worldToScreenX(root);
            g2.drawLine(sx,0,sx,getHeight());
        }
        
        g2.setStroke(oldStroke);
    }

    
    // draw the rational function curve
    private void drawFunction(Graphics2D g2) {
    	
        int w = getWidth();
        g2.setColor(new Color(30,144,255)); // blue
        
        double lastX = Double.NaN;
        double lastY = Double.NaN;
        
        for (int px = 0; px < w; px++) {
        	
            double x = screenToWorldX(px);
            double y;
            
            try {
                y = current.valueAt(x);
            }
            catch (ArithmeticException ex) {
                lastX = Double.NaN;
                lastY = Double.NaN;
                continue;
            }
            
            if (Double.isNaN(y) || Double.isInfinite(y)) {
                lastX = Double.NaN;
                lastY = Double.NaN;
                continue;
            }
            
            int sx = px;
            int sy = worldToScreenY(y);
            
            if (!Double.isNaN(lastX) && !Double.isNaN(lastY)) {
            	
                int lastSx = worldToScreenX(lastX);
                int lastSy = worldToScreenY(lastY);
                
                // avoid huge jumps across asymptotes
                if (Math.abs(y - lastY) < (yMax - yMin) / 2.0) {
                    g2.drawLine(lastSx,lastSy,sx,sy);
                }
            }
            
            lastX = x;
            lastY = y;
        }
    }

    
    // draw x intercepts as small circles
    private void drawRoots(Graphics2D g2) {
    	
        if (current == null) return;
        
        g2.setColor(Color.GREEN.darker());
        int r = 5; // radius in pixels
        
        for (int root : current.numer.roots) {
        	
            int sx = worldToScreenX(root);
            int sy = worldToScreenY(0);
            
            g2.fillOval(sx - r,sy - r,2 * r,2 * r);
        }
    }

    
    // convert world x to screen x
    private int worldToScreenX(double x) {
    	
        int w = getWidth();
        return (int) ((x - xMin) / (xMax - xMin) * w);
    }

    
    // convert world y to screen y
    private int worldToScreenY(double y) {
    	
        int h = getHeight();
        return (int) ((yMax - y) / (yMax - yMin) * h);
    }

    
    // convert screen x to world x
    private double screenToWorldX(int px) {
    	
        int w = getWidth();
        return xMin + (xMax - xMin) * (px / (double) w);
    }
}
