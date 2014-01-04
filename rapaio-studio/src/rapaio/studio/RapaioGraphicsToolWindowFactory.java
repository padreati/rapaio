package rapaio.studio;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import rapaio.graphics.base.Figure;
import rapaio.printer.FigurePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioGraphicsToolWindowFactory implements ToolWindowFactory, ExtendedPrinter {

    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent;
    private FigurePanel figurePanel;

    public RapaioGraphicsToolWindowFactory() {
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        this.myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);

        RapaioStudioServer.getInstance().setExtendedPrinter(this);
        Notifications.Bus.register("Rapaio", NotificationDisplayType.NONE);
    }

    public void setFigure(Figure figure) {
        if (figurePanel != null) {
            myToolWindowContent.remove(figurePanel);
        }
        figurePanel = new FigurePanel(figure);
        myToolWindowContent.setLayout(new BorderLayout());
        myToolWindowContent.add(figurePanel, BorderLayout.CENTER);
        figurePanel.setVisible(true);
        figurePanel.paintImmediately(myToolWindowContent.getVisibleRect());
    }

    public void setImage(BufferedImage image) {
        if (figurePanel != null) {
            myToolWindowContent.remove(figurePanel);
        }
        figurePanel = new FigurePanel(image);
        figurePanel.setVisible(true);
        myToolWindowContent.setLayout(new BorderLayout());
        myToolWindowContent.add(figurePanel, BorderLayout.CENTER);
        figurePanel.setVisible(true);
        figurePanel.paintImmediately(myToolWindowContent.getVisibleRect());
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
        return myToolWindow.getComponent().getWidth();
    }

    @Override
    public void setGraphicWidth(int i) {
    }

    @Override
    public int getGraphicHeight() {
        return myToolWindow.getComponent().getHeight();
    }

    @Override
    public void setGraphicHeight(int i) {
    }

    @Override
    public void print(String s) {
        Notifications.Bus.notify(new Notification("Rapaio", "", s, NotificationType.INFORMATION));
    }

    @Override
    public void println() {
        print("\n");
    }

    @Override
    public void error(String s, Throwable throwable) {
        print("Error:" + s);
    }

    @Override
    public void preparePrinter() {
    }

    @Override
    public void closePrinter() {
    }

    @Override
    public void heading(int i, String s) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append("*");
        }
        sb.append(" ").append(s).append(" ");
        for (int j = 0; j < i; j++) {
            sb.append("*");
        }
        sb.append("\n");
        print(sb.toString());
    }

    @Override
    public void code(String s) {
        print(s);
    }

    @Override
    public void p(String s) {
        print(s);
    }

    @Override
    public void eqn(String s) {
        print(s);
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
