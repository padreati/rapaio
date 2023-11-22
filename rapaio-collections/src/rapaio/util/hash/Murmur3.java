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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.util.hash;

import java.nio.ByteBuffer;

/**
 * The MurmurHash3 algorithm was created by Austin Appleby and placed in the public domain.
 * This java port was authored by Yonik Seeley and also placed into the public domain.
 * The author hereby disclaims copyright to this source code.
 * <p>
 * This produces exactly the same hash values as the final C++
 * version of MurmurHash3 and is thus suitable for producing the same hash values across
 * platforms.
 * <p>
 * The 32 bit x86 version of this hash should be the fastest variant for relatively short keys like ids.
 * murmurhash3_x64_128 is a good choice for longer strings or if you need more than 32 bits of hash.
 * <p>
 * Note - The x86 and x64 versions do _not_ produce the same results, as the
 * algorithms are optimized for their respective platforms.
 */
public final class Murmur3 {

    public static int murmur3A(double value) {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).putDouble(value);
        return murmur3A(buffer, 0, 8, 0);
    }

    public static int murmur3A(double value, int seed) {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).putDouble(value);
        return murmur3A(buffer, 0, 8, seed);
    }

    public static int murmur3A(int value) {
        byte[] data = new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        return murmur3A(data, 0, 4, 0);
    }

    public static int murmur3A(int value, int seed) {
        byte[] data = new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
        return murmur3A(data, 0, 4, seed);
    }

    /**
     * Returns the MurmurHash3_x86_32 hash.
     */
    public static int murmur3A(byte[] data, int offset, int len, int seed) {

        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int h1 = seed;
        int roundedEnd = offset + (len & 0xfffffffc);  // round down to 4 byte block

        for (int i = offset; i < roundedEnd; i += 4) {
            // little endian load order
            int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8) | ((data[i + 2] & 0xff) << 16) | (data[i + 3] << 24);
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
            k1 *= c2;

            h1 ^= k1;
            h1 = (h1 << 13) | (h1 >>> 19);  // ROTL32(h1,13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail
        int k1 = 0;

        switch (len & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
                // fallthrough
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
                // fallthrough
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= len;

        // fmix(h1);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }


    /**
     * Returns the MurmurHash3_x86_32 hash of the UTF-8 bytes of the String without actually encoding
     * the string to a temporary buffer.  This is more than 2x faster than hashing the result
     * of String.getBytes().
     */
    public static int murmur3A(CharSequence data, int offset, int len, int seed) {

        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int h1 = seed;

        int pos = offset;
        int end = offset + len;
        int k1 = 0;
        int shift = 0;
        int nBytes = 0;   // length in UTF8 bytes


        while (pos < end) {
            int k2;
            int bits;
            int code = data.charAt(pos++);
            if (code < 0x80) {
                k2 = code;
                bits = 8;
            } else if (code < 0x800) {
                k2 = (0xC0 | (code >> 6))
                        | ((0x80 | (code & 0x3F)) << 8);
                bits = 16;
            } else if (code < 0xD800 || code > 0xDFFF || pos >= end) {
                // we check for pos>=end to encode an unpaired surrogate as 3 bytes.
                k2 = (0xE0 | (code >> 12))
                        | ((0x80 | ((code >> 6) & 0x3F)) << 8)
                        | ((0x80 | (code & 0x3F)) << 16);
                bits = 24;
            } else {
                // surrogate pair
                // int utf32 = pos < end ? (int) data.charAt(pos++) : 0;
                int utf32 = data.charAt(pos++);
                utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
                k2 = (0xff & (0xF0 | (utf32 >> 18)))
                        | ((0x80 | ((utf32 >> 12) & 0x3F))) << 8
                        | ((0x80 | ((utf32 >> 6) & 0x3F))) << 16
                        | (0x80 | (utf32 & 0x3F)) << 24;
                bits = 32;
            }


            k1 |= k2 << shift;

            // int used_bits = 32 - shift;  // how many bits of k2 were used in k1.
            // int unused_bits = bits - used_bits; //  (bits-(32-shift)) == bits+shift-32  == bits-newshift

            shift += bits;
            if (shift >= 32) {
                // mix after we have a complete word

                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
                k1 *= c2;

                h1 ^= k1;
                h1 = (h1 << 13) | (h1 >>> 19);  // ROTL32(h1,13);
                h1 = h1 * 5 + 0xe6546b64;

                shift -= 32;
                // unfortunately, java won't let you shift 32 bits off, so we need to check for 0
                if (shift != 0) {
                    k1 = k2 >>> (bits - shift);   // bits used == bits - newshift
                } else {
                    k1 = 0;
                }
                nBytes += 4;
            }

        } // inner

        // handle tail
        if (shift > 0) {
            nBytes += shift >> 3;
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
            k1 *= c2;
            h1 ^= k1;
        }

        // finalization
        h1 ^= nBytes;

        // fmix(h1);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }


    /**
     * Returns the MurmurHash3_x64_128 hash, placing the result in "out".
     */
    public static long[] murmur3F(byte[] key, int offset, int len, int seed) {
        // The original algorithm does have a 32 bit unsigned seed.
        // We have to mask to match the behavior of the unsigned types and prevent sign extension.
        long h1 = seed & 0x00000000FFFFFFFFL;
        long h2 = seed & 0x00000000FFFFFFFFL;

        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        int roundedEnd = offset + (len & 0xFFFFFFF0);  // round down to 16 byte block
        for (int i = offset; i < roundedEnd; i += 16) {
            long k1 = getLongLittleEndian(key, i);
            long k2 = getLongLittleEndian(key, i + 8);
            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;
            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        long k1 = 0;
        long k2 = 0;

        switch (len & 15) {
            case 15:
                k2 = (key[roundedEnd + 14] & 0xffL) << 48;
            case 14:
                k2 |= (key[roundedEnd + 13] & 0xffL) << 40;
            case 13:
                k2 |= (key[roundedEnd + 12] & 0xffL) << 32;
            case 12:
                k2 |= (key[roundedEnd + 11] & 0xffL) << 24;
            case 11:
                k2 |= (key[roundedEnd + 10] & 0xffL) << 16;
            case 10:
                k2 |= (key[roundedEnd + 9] & 0xffL) << 8;
            case 9:
                k2 |= (key[roundedEnd + 8] & 0xffL);
                k2 *= c2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= c1;
                h2 ^= k2;
            case 8:
                k1 = ((long) key[roundedEnd + 7]) << 56;
            case 7:
                k1 |= (key[roundedEnd + 6] & 0xffL) << 48;
            case 6:
                k1 |= (key[roundedEnd + 5] & 0xffL) << 40;
            case 5:
                k1 |= (key[roundedEnd + 4] & 0xffL) << 32;
            case 4:
                k1 |= (key[roundedEnd + 3] & 0xffL) << 24;
            case 3:
                k1 |= (key[roundedEnd + 2] & 0xffL) << 16;
            case 2:
                k1 |= (key[roundedEnd + 1] & 0xffL) << 8;
            case 1:
                k1 |= (key[roundedEnd] & 0xffL);
                k1 *= c1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        }

        //----------
        // finalization

        h1 ^= len;
        h2 ^= len;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[] {h1, h2};
    }

    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    /**
     * Gets a long from a byte buffer in little endian byte order.
     */
    private static long getLongLittleEndian(byte[] buf, int offset) {
        return ((long) buf[offset + 7] << 56) | ((buf[offset + 6] & 0xffL) << 48)
                | ((buf[offset + 5] & 0xffL) << 40) | ((buf[offset + 4] & 0xffL) << 32)
                | ((buf[offset + 3] & 0xffL) << 24) | ((buf[offset + 2] & 0xffL) << 16)
                | ((buf[offset + 1] & 0xffL) << 8) | ((buf[offset] & 0xffL));
    }

}