/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.printer.local;

import rapaio.graphics.Figure;
import rapaio.image.ImageUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FigurePanel extends JPanel {

    private static final long serialVersionUID = 6956337145639708156L;

    private final Figure figure;
    protected volatile BufferedImage currentImage;
    protected SwingWorker<BufferedImage, Object> drawWorker;
    boolean forceRedraw = true;

    public FigurePanel(Figure figure) {
        this.figure = figure;
    }

    public FigurePanel(BufferedImage image) {
        this.figure = null;
        this.currentImage = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
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
        drawWorker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                if (figure == null) {
                    return currentImage;
                }
                return ImageUtility.buildImage(figure, getWidth(), getHeight());
            }

            @Override
            protected void done() {

                SwingUtilities.invokeLater(() -> {
                    try {
                        currentImage = get();
                        drawWorker = null;
                        revalidate();
                        repaint();
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                });

            }
        };

        drawWorker.execute();
    }
}
