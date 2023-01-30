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

package rapaio.experiment.io.json.stream.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Integer encoding used in lz json format
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/9/15.
 */
@Deprecated
public interface IntEncoding {

    int readInt(DataInputStream is) throws IOException;

    int countLen(int x);

    void writeInt(int x, DataOutputStream os) throws IOException;

    IntEncoding ENCODE_255 = new IntEncoding() {

        @Override
        public int readInt(DataInputStream is) throws IOException {
            int len = 0;
            int last = 255;
            while (last == 255) {
                last = is.readUnsignedByte();
                len += last;
            }
            return len;
        }

        @Override
        public int countLen(int x) {
            int count = 0;
            while (x >= 255) {
                count++;
                x -= 255;
            }
            return count + 1;
        }

        @Override
        public void writeInt(int x, DataOutputStream os) throws IOException {
            while (x >= 255) {
                os.writeByte(255);
                x -= 255;
            }
            os.writeByte(x);
        }
    };

    IntEncoding ENCODE_MIX = new IntEncoding() {

        // first byte is encoded as it is if is <= 251
        // if first byte is 255, the value is next byte + 251
        // if first byte is 254, the value is next 2 bytes + 251
        // if first byte is 253, the value is next 3 bytes + 251
        // if first byte is 252, the value is next 4 bytes + 251

        @Override
        public int readInt(DataInputStream is) throws IOException {
            int first = is.readUnsignedByte();
            if (first <= 251) {
                return first;
            } else
                switch (first) {
                    case 255:
                        return is.readUnsignedByte() + 251;
                    case 254:
                        return is.readUnsignedByte() * 256
                                + is.readUnsignedByte() + 251;
                    case 253:
                        return is.readUnsignedByte() * 256 * 256
                                + is.readUnsignedByte() * 256
                                + is.readUnsignedByte() + 251;
                    case 252:
                        return is.readUnsignedByte() * 256 * 256 * 256
                                + is.readUnsignedByte() * 256 * 256
                                + is.readUnsignedByte() * 256
                                + is.readUnsignedByte() + 251;
                }
            return 0;
        }

        @Override
        public int countLen(int x) {
            if (x <= 251)
                return 1;
            x -= 251;
            if (x < 256)
                return 2;
            if (x < 256 * 256)
                return 3;
            if (x < 256 * 256 * 256)
                return 4;
            return 5;
        }

        @Override
        public void writeInt(int x, DataOutputStream os) throws IOException {
            if (x <= 251) {
                os.writeByte(x);
                return;
            }
            x -= 251;
            if (x < 256) {
                os.writeByte(255);
                os.writeByte(x);
                return;
            }
            if (x < 256 * 256) {
                os.writeByte(254);
                os.writeByte((x & 0xFF00) >> 8);
                os.writeByte(x & 0xFF);
                return;
            }
            if (x < 256 * 256 * 253) {
                os.writeByte(254);
                os.writeByte((x & 0xFF0000) >> 16);
                os.writeByte((x & 0xFF00) >> 8);
                os.writeByte((x & 0xFF));
                return;
            }
            os.writeByte(252);
            os.writeByte((x & 0xFF000000) >> 24);
            os.writeByte((x & 0xFF0000) >> 16);
            os.writeByte((x & 0xFF00) >> 8);
            os.writeByte((x & 0xFF));
        }
    };
}
