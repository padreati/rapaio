/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

import java.util.stream.IntStream;

/**
 * This class offers different algorithms for matrix multiplication.
 *
 * @author Martin Thoma
 */
public class MatrixMultiplication {
    static int LEAF_SIZE = 256;

    public static RM ijkAlgorithm(RM A, RM B) {
        // initialise C
        RM C = SolidRM.empty(A.rowCount(), B.colCount());
        for (int i = 0; i < A.rowCount(); i++) {
            for (int j = 0; j < B.colCount(); j++) {
                for (int k = 0; k < A.colCount(); k++) {
                    C.increment(i, j, A.get(i, k) * B.get(k, j));
                }
            }
        }
        return C;
    }

    public static RM ijkParallel(RM A, RM B) {
        // initialise C
        RM C = SolidRM.empty(A.rowCount(), B.colCount());
        IntStream.range(0, A.rowCount()).parallel().forEach(i -> {
            for (int j = 0; j < B.colCount(); j++) {
                for (int k = 0; k < A.colCount(); k++) {
                    C.increment(i, j, A.get(i, k) * B.get(k, j));
                }
            }
        });
        return C;
    }

    public static RM ikjAlgorithm(RM A, RM B) {
        // initialise C
        RM C = SolidRM.empty(A.rowCount(), B.colCount());
        for (int i = 0; i < A.rowCount(); i++) {
            for (int k = 0; k < A.colCount(); k++) {
                if (A.get(i, k) == 0)
                    continue;
                for (int j = 0; j < B.colCount(); j++) {
                    C.increment(i, j, A.get(i, k) * B.get(k, j));
                }
            }
        }
        return C;
    }

    public static RM ikjParallel(RM A, RM B) {
        // initialise C
        RM C = SolidRM.empty(A.rowCount(), B.colCount());
        IntStream.range(0, A.rowCount()).parallel().forEach(i -> {
            for (int k = 0; k < A.colCount(); k++) {
                if (A.get(i, k) == 0)
                    continue;
                for (int j = 0; j < B.colCount(); j++) {
                    C.increment(i, j, A.get(i, k) * B.get(k, j));
                }
            }
        });
        return C;
    }

    public static RM tiledAlgorithm(RM A, RM B) {
        RM C = SolidRM.empty(A.rowCount(), B.colCount());

//        Pick a tile size T = Θ(√M)
        int T = 256;

//        For I from 1 to n in steps of T:
        for (int I = 0; I < A.rowCount(); I += T) {
            // For J from 1 to p in steps of T:
            for (int J = 0; J < B.colCount(); J += T) {
                // For K from 1 to m in steps of T:
                for (int K = 0; K < A.colCount(); K += T) {
                    // Multiply AI:I+T, K:K+T and BK:K+T, J:J+T into CI:I+T, J:J+T, that is:
                    // For i from I to min(I + T, n):
                    for (int i = I; i < Math.min(I + T, A.rowCount()); i++) {
                        // For j from J to min(J + T, p):
                        for (int j = J; j < Math.min(J + T, B.colCount()); j++) {
                            // Let sum = 0
                            double sum = 0;
                            // For k from K to min(K + T, m):
                            for (int k = K; k < Math.min(K + T, A.colCount()); k++) {
                                // Set sum ← sum + Aik × Bkj
                                sum += A.get(i, k) * B.get(k, j);
                            }
                            // Set Cij ← sum
                            C.increment(i, j, sum);

                        }
                    }

                }
            }
        }
        return C;


    }

    private static RM add(RM A, RM B) {
        RM C = SolidRM.empty(A.rowCount(), A.colCount());
        for (int i = 0; i < A.rowCount(); i++) {
            for (int j = 0; j < A.colCount(); j++) {
                C.set(i, j, A.get(i, j) + B.get(i, j));
            }
        }
        return C;
    }

    private static RM subtract(RM A, RM B) {
        int n = A.rowCount();
        RM C = SolidRM.empty(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C.set(i, j, A.get(i, j) - B.get(i, j));
            }
        }
        return C;
    }

    private static int nextPowerOfTwo(int n) {
        int log2 = (int) Math.ceil(Math.log(n) / Math.log(2));
        return (int) Math.pow(2, log2);
    }

    /*
    public static RM strassen(RM A, RM B) {
        // Make the matrices bigger so that you can apply the strassen
        // algorithm recursively without having to deal with odd
        // matrix sizes

        int n = Math.max(A.rowCount(), Math.max(A.colCount(), B.colCount()));
        int m = nextPowerOfTwo(n);
        RM APrep = SolidRM.empty(m, m);
        RM BPrep = SolidRM.empty(m, m);
        for (int i = 0; i < A.rowCount(); i++) {
            for (int j = 0; j < A.colCount(); j++) {
                APrep.set(i, j, A.get(i, j));
            }
        }
        for (int i = 0; i < B.rowCount(); i++) {
            for (int j = 0; j < B.colCount(); j++) {
                BPrep.set(i, j, B.get(i, j));
            }
        }

        RM CPrep = strassenR(APrep, BPrep);
        RM C = SolidRM.empty(A.rowCount(), B.colCount());
        for (int i = 0; i < A.rowCount(); i++) {
            for (int j = 0; j < B.colCount(); j++) {
                C.set(i, j, CPrep.get(i, j));
            }
        }
        return C;
    }

    private static RM strassenR(RM A, RM B) {
        int n = A.rowCount();

        if (n <= LEAF_SIZE) {
            return ikjAlgorithm(A, B);
        } else {
            // initializing the new sub-matrices
            int newSize = n / 2;

//            RM a11 = Linear.newRMEmpty(newSize, newSize);
//            RM a12 = Linear.newRMEmpty(newSize, newSize);
//            RM a21 = Linear.newRMEmpty(newSize, newSize);
//            RM a22 = Linear.newRMEmpty(newSize, newSize);
//
//            RM b11 = Linear.newRMEmpty(newSize, newSize);
//            RM b12 = Linear.newRMEmpty(newSize, newSize);
//            RM b21 = Linear.newRMEmpty(newSize, newSize);
//            RM b22 = Linear.newRMEmpty(newSize, newSize);
//
//            RM aResult;
//            RM bResult;

            // dividing the matrices in 4 sub-matrices:
//            for (int i = 0; i < newSize; i++) {
//                for (int j = 0; j < newSize; j++) {
//                    a11.set(i, j, A.get(i, j)); // top left
//                    a12.set(i, j, A.get(i, j + newSize)); // top right
//                    a21.set(i, j, A.get(i + newSize, j)); // bottom left
//                    a22.set(i, j, A.get(i + newSize, j + newSize)); // bottom right
//
//                    b11.set(i, j, B.get(i, j)); // top left
//                    b12.set(i, j, B.get(i, j + newSize)); // top right
//                    b21.set(i, j, B.get(i + newSize, j)); // bottom left
//                    b22.set(i, j, B.get(i + newSize, j + newSize)); // bottom right
//                }
//            }

            RM a11 = A.rangeRows(0, newSize).rangeCols(0, newSize);
            RM a12 = A.rangeRows(0, newSize).rangeCols(newSize, 2 * newSize);
            RM a21 = A.rangeRows(newSize, 2 * newSize).rangeCols(0, newSize);
            RM a22 = A.rangeRows(newSize, 2 * newSize).rangeCols(newSize, 2 * newSize);

            RM b11 = B.rangeRows(0, newSize).rangeCols(0, newSize);
            RM b12 = B.rangeRows(0, newSize).rangeCols(newSize, 2 * newSize);
            RM b21 = B.rangeRows(newSize, 2 * newSize).rangeCols(0, newSize);
            RM b22 = B.rangeRows(newSize, 2 * newSize).rangeCols(newSize, 2 * newSize);

            RM aResult;
            RM bResult;

            // Calculating p1 to p7:
            aResult = add(a11, a22);
            bResult = add(b11, b22);
            RM p1 = strassenR(aResult, bResult);
            // p1 = (a11+a22) * (b11+b22)

            aResult = add(a21, a22); // a21 + a22
            RM p2 = strassenR(aResult, b11); // p2 = (a21+a22) * (b11)

            bResult = subtract(b12, b22); // b12 - b22
            RM p3 = strassenR(a11, bResult);
            // p3 = (a11) * (b12 - b22)

            bResult = subtract(b21, b11); // b21 - b11
            RM p4 = strassenR(a22, bResult);
            // p4 = (a22) * (b21 - b11)

            aResult = add(a11, a12); // a11 + a12
            RM p5 = strassenR(aResult, b22);
            // p5 = (a11+a12) * (b22)

            aResult = subtract(a21, a11); // a21 - a11
            bResult = add(b11, b12); // b11 + b12
            RM p6 = strassenR(aResult, bResult);
            // p6 = (a21-a11) * (b11+b12)

            aResult = subtract(a12, a22); // a12 - a22
            bResult = add(b21, b22); // b21 + b22
            RM p7 = strassenR(aResult, bResult);
            // p7 = (a12-a22) * (b21+b22)

            // calculating c21, c21, c11 e c22:
            RM c12 = add(p3, p5); // c12 = p3 + p5
            RM c21 = add(p2, p4); // c21 = p2 + p4

            aResult = add(p1, p4); // p1 + p4
            bResult = add(aResult, p7); // p1 + p4 + p7
            RM c11 = subtract(bResult, p5);
            // c11 = p1 + p4 - p5 + p7

            aResult = add(p1, p3); // p1 + p3
            bResult = add(aResult, p6); // p1 + p3 + p6
            RM c22 = subtract(bResult, p2);
            // c22 = p1 + p3 - p2 + p6

            // Grouping the results obtained in a single matrix:
            RM C = RM.empty(n, n);
            for (int i = 0; i < newSize; i++) {
                for (int j = 0; j < newSize; j++) {
                    C.set(i, j, c11.get(i, j));
                    C.set(i, j + newSize, c12.get(i, j));
                    C.set(i + newSize, j, c21.get(i, j));
                    C.set(i + newSize, j + newSize, c22.get(i, j));
                }
            }
            return C;
        }
    }
    */
}
