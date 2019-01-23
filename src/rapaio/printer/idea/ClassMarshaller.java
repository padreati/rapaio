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

import rapaio.graphics.base.Figure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassMarshaller {

    public void marshallDraw(OutputStream out, Figure figure) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                oos.writeObject(figure);
                oos.flush();
                bytes = baos.toByteArray();
            }
        }
        CommandBytes cb = CommandBytes.newDraw(bytes);
        try (ObjectOutputStream oos2 = new ObjectOutputStream(out)) {
            oos2.writeObject(cb);
            oos2.flush();
        }
    }

    public void marshallImage(OutputStream out, BufferedImage image) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            bytes = baos.toByteArray();
        }
        CommandBytes cb = CommandBytes.newImage(bytes);
        try (ObjectOutputStream oos2 = new ObjectOutputStream(out)) {
            oos2.writeObject(cb);
            oos2.flush();
        }
    }

    public void marshallConfig(OutputStream out, CommandBytes cb) throws IOException {
        if (cb == null) {
            cb = CommandBytes.newConfig();
        }
        ObjectOutputStream oos2 = new ObjectOutputStream(out);
        oos2.writeObject(cb);
        oos2.flush();
    }

    public void marshallConfig(OutputStream out) throws IOException {
        marshallConfig(out, CommandBytes.newConfig());
    }

    public CommandBytes unmarshall(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        return (CommandBytes) ois.readObject();
    }
}
