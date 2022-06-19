/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/14/21.
 */
public final class Images {

    private Images() {
    }

    public static BufferedImage readImage(File input) throws IOException {
        return ImageIO.read(input);
    }

    public static BufferedImage readImage(URL url) throws IOException {
        return ImageIO.read(url);
    }

    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    public static BufferedImage readImage(ImageInputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

}
