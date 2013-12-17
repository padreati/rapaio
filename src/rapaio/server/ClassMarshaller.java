/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassMarshaller {

    public void marshallRemote(OutputStream out, Class<? extends RapaioCmd> clazz) throws IOException {
        InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while (true) {
                int ch = is.read();
                if (ch == -1)
                    break;
                baos.write(ch);
            }
            bytes = baos.toByteArray();
        }
        CommandBytes cb = CommandBytes.newRemote(clazz.getCanonicalName(), bytes);
        try (ObjectOutputStream oos2 = new ObjectOutputStream(out)) {
            oos2.writeObject(cb);
            oos2.flush();
        }
    }

    public void marshallPrint(OutputStream out, String name, String value) throws IOException {
        CommandBytes cb = CommandBytes.newPrint(name, value);
        try (ObjectOutputStream oos2 = new ObjectOutputStream(out)) {
            oos2.writeObject(cb);
            oos2.flush();
        }
    }

    public void marshallDraw(OutputStream out, Figure figure, int width, int height) throws IOException {
        BufferedImage image = ImageUtility.buildImage(figure, width, height);
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            bytes = baos.toByteArray();
        }
        CommandBytes cb = CommandBytes.newDraw(bytes);
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
