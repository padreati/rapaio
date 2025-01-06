/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.datasets;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.TensorManager;

public class MNISTDatasets {

    private static final int IMAGES_MAGIC_NUMBER = 2051;
    private static final int LABELS_MAGIC_NUMBER = 2049;
    private static final int BUFF_LEN = 64 * 1024;

    private static final String trainImages = "mnist/train-images-idx3-ubyte.gz";
    private static final String trainLabels = "mnist/train-labels-idx1-ubyte.gz";
    private static final String testImages = "mnist/t10k-images-idx3-ubyte.gz";
    private static final String testLabels = "mnist/t10k-labels-idx1-ubyte.gz";

    private final TensorManager tm;

    public MNISTDatasets(TensorManager tm) {
        this.tm = tm;
    }

    public TabularDataset train() throws IOException {
        return new TabularDataset(tm, readData(trainImages, trainLabels));
    }

    public TabularDataset test() throws IOException {
        return new TabularDataset(tm, readData(testImages, testLabels));
    }

    private DArray<?>[] readData(String dataFilePath, String labelFilePath) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Datasets.resourceAsStream(dataFilePath), BUFF_LEN), BUFF_LEN));

        int imageMagicNumber = dataInputStream.readInt();
        int numberOfItems = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        if (imageMagicNumber != IMAGES_MAGIC_NUMBER) {
            throw new IOException(
                    String.format("Invalid magic number for images: %d, should be: %d", imageMagicNumber, IMAGES_MAGIC_NUMBER));
        }

        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(Datasets.resourceAsStream(labelFilePath), BUFF_LEN), BUFF_LEN));
        int labelMagicNumber = labelInputStream.readInt();
        int numberOfLabels = labelInputStream.readInt();

        if (labelMagicNumber != LABELS_MAGIC_NUMBER) {
            throw new IOException(
                    String.format("Invalid magic number for labels: %d, should be: %d", labelMagicNumber, LABELS_MAGIC_NUMBER));
        }
        if (numberOfLabels != numberOfItems) {
            throw new IOException("Number of labels does not match with the number of images.");
        }

        DArray<?> labels = tm.zerosArray(Shape.of(numberOfLabels));
        DArray<?> images = tm.zerosArray(Shape.of(numberOfItems, rows, cols));

        for (int i = 0; i < numberOfItems; i++) {
            labels.setDouble(labelInputStream.readUnsignedByte(), i);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    images.setDouble(dataInputStream.readUnsignedByte(), i, r, c);
                }
            }
        }

        dataInputStream.close();
        labelInputStream.close();
        return new DArray[] {images, labels};
    }
}
