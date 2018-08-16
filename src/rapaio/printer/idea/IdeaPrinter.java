/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.printer.idea;

import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.printer.StandardPrinter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IdeaPrinter extends StandardPrinter {
    public static final int DEFAULT_PORT = 56339;

    @Override
    public int graphicWidth() {
        return 600;
    }

    @Override
    public void withGraphicWidth(int width) {
    }

    @Override
    public int graphicHeight() {
        return 400;
    }

    @Override
    public void withGraphicHeight(int height) {
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        draw(figure);
    }

    @Override
    public void draw(Figure figure) {
        boolean sendFigure = true;
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            if (sendFigure) {
                new ClassMarshaller().marshallDraw(s.getOutputStream(), figure);
            } else {
                new ClassMarshaller().marshallConfig(s.getOutputStream());
                CommandBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());
                if (!(cb.getGraphicalWidth() == 0 || cb.getGraphicalHeight() == 0)) {
                    BufferedImage image = ImageUtility.buildImage(
                            figure,
                            cb.getGraphicalWidth(),
                            cb.getGraphicalHeight());
                    new ClassMarshaller().marshallImage(s.getOutputStream(), image);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
