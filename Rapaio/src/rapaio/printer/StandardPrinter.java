package rapaio.printer;

import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;


/**
 * @author tutuianu
 */
public final class StandardPrinter extends AbstractPrinter {

    public StandardPrinter() {
        setTextWidth(120);
        setGraphicWidth(800);
        setGraphicHeight(600);
    }

    private int textWidth = 120;
    private int graphicWidth = 800;
    private int graphicHeight = 600;

    @Override
    public int getTextWidth() {
        return textWidth;
    }

    @Override
    public void setTextWidth(int chars) {
        textWidth = chars;
    }

    @Override
    public int getGraphicWidth() {
        return graphicWidth;
    }

    @Override
    public void setGraphicWidth(int width) {
        graphicWidth = width;
    }

    @Override
    public int getGraphicHeight() {
        return graphicHeight;
    }

    @Override
    public void setGraphicHeight(int height) {
        graphicHeight = height;
    }

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (message != null) {
            System.out.println(message);
        }
        if (throwable != null) {
            System.out.println(throwable);
        }
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        FigurePanel figurePanel = new FigurePanel(figure);
        JDialog frame = new JDialog();
        frame.setContentPane(figurePanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setAutoRequestFocus(true);
        frame.setSize(width, height);
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
            if (!frame.isVisible()) {
                break;
            }
        }
    }
}

class FigurePanel extends JPanel {

    private final Figure figure;
    protected volatile BufferedImage currentImage;
    protected volatile SwingWorker<BufferedImage, Object> drawWorker;
    boolean forceRedraw = true;

    public FigurePanel(Figure figure) {
        this.figure = figure;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        final String drawingMessage = "Rendering Update...";
        FontMetrics fm = g.getFontMetrics();

        if (currentImage != null) {
            if (currentImage.getWidth() != getWidth() || currentImage.getHeight() != getHeight() || forceRedraw) {
                forceRedraw = false;
                if (drawWorker == null) {
                    createBackgroundImage();
                }

                g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
                g.drawString(drawingMessage, 3, getHeight() - fm.getHeight() / 2);
            } else {
                g.drawImage(currentImage, 0, 0, null);
            }
        } else if (currentImage == null) {
            if (drawWorker == null) {
                createBackgroundImage();
            }
            g.drawString(drawingMessage, getWidth() / 2 - fm.stringWidth(drawingMessage) / 2, getHeight() / 2 - fm.getHeight() / 2);
        }
    }

    /**
     * Creates a new worker to do the image rendering in the background.
     */
    private void createBackgroundImage() {
        drawWorker = new SwingWorker<BufferedImage, Object>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return ImageUtility.buildImage(figure, getWidth(), getHeight());
            }

            @Override
            protected void done() {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentImage = get();
                            drawWorker = null;
                            revalidate();
                            repaint();
                        } catch (InterruptedException | ExecutionException ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                        }
                    }
                });

            }
        };

        drawWorker.execute();
    }
}
