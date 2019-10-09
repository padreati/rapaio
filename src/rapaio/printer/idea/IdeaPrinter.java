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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import rapaio.graphics.base.*;
import rapaio.printer.*;
import rapaio.printer.standard.*;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IdeaPrinter extends StandardPrinter {

    private static final Logger logger = Logger.getLogger(IdeaPrinter.class.getName());

    public static final int DEFAULT_PORT = 56339;

    @Override
    public int graphicWidth() {
        return 600;
    }

    @Override
    public Printer withGraphicShape(int width, int height) {
        return this;
    }

    @Override
    public int graphicHeight() {
        return 400;
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        draw(figure);
    }

    @Override
    public void draw(Figure figure) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallDraw(s.getOutputStream(), figure);
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }
    }
}
