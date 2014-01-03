package rapaio.studio;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import rapaio.graphics.base.Figure;
import rapaio.printer.FigurePanel;
import rapaio.printer.Printer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioGraphicsToolWindowFactory implements ToolWindowFactory, Printer {

    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent;
    private JPanel contentPanel;
    private JToolBar actionToolBar;
    private FigurePanel figurePanel;

    public RapaioGraphicsToolWindowFactory() {
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);


    }

    public void setFigure(Figure figure) {
        if (figurePanel != null) {
            contentPanel.remove(figurePanel);
        }
        figurePanel = new FigurePanel(figure);
        figurePanel.setVisible(true);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(figurePanel, BorderLayout.CENTER);
    }

    public void setImage(BufferedImage image) {
        if(figurePanel != null) {
            contentPanel.remove(figurePanel);
        }
        figurePanel = new FigurePanel(image);
        figurePanel.setVisible(true);
        contentPanel.add(figurePanel, BorderLayout.CENTER);
    }

    @Override
    public int getTextWidth() {
        return 80;
    }

    @Override
    public void setTextWidth(int i) {
    }

    @Override
    public int getGraphicWidth() {
        return contentPanel.getSize().width;
    }

    @Override
    public void setGraphicWidth(int i) {
    }

    @Override
    public int getGraphicHeight() {
        return contentPanel.getSize().height;
    }

    @Override
    public void setGraphicHeight(int i) {
    }

    @Override
    public void print(String s) {
    }

    @Override
    public void println() {
    }

    @Override
    public void error(String s, Throwable throwable) {
    }

    @Override
    public void preparePrinter() {
    }

    @Override
    public void closePrinter() {
    }

    @Override
    public void heading(int i, String s) {
    }

    @Override
    public void code(String s) {
    }

    @Override
    public void p(String s) {
    }

    @Override
    public void eqn(String s) {
    }

    @Override
    public void draw(Figure figure, int i, int i2) {
        draw(figure);
    }

    @Override
    public void draw(Figure figure) {
        setFigure(figure);
    }
}
