
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

package rapaio.math.linear.decomposition;

import java.util.stream.IntStream;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

/**
 * This class offers different algorithms for matrix multiplication.
 *
 * @author Martin Thoma
 */
public class MatrixMultiplication {

    public static DMatrix jama(DMatrix A, DMatrix B) {
        if (B.rows() != A.cols()) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        DMatrix X = DMatrix.empty(A.rows(), B.cols());
        double[] Bcolj = new double[A.cols()];
        for (int j = 0; j < B.cols(); j++) {
            for (int k = 0; k < A.cols(); k++) {
                Bcolj[k] = B.get(k, j);
            }
            for (int i = 0; i < A.rows(); i++) {
                double s = 0;
                for (int k = 0; k < A.cols(); k++) {
                    s += A.get(i, k) * Bcolj[k];
                }
                X.set(i, j, s);
            }
        }
        return X;
    }

    public static DMatrix ijkAlgorithm(DMatrix A, DMatrix B) {
        // Initialize c
        DMatrix C = DMatrix.empty(A.rows(), B.cols());
        for (int i = 0; i < A.rows(); i++) {
            for (int j = 0; j < B.cols(); j++) {
                for (int k = 0; k < A.cols(); k++) {
                    C.set(i, j, C.get(i, j) + A.get(i, k) * B.get(k, j));
                }
            }
        }
        return C;
    }

    public static DMatrix ijkParallel(DMatrix A, DMatrix B) {
        // initialize c
        DMatrix C = DMatrix.empty(A.rows(), B.cols());
        IntStream.range(0, A.rows()).parallel().forEach(i -> {
            for (int j = 0; j < B.cols(); j++) {
                for (int k = 0; k < A.cols(); k++) {
                    C.set(i, j, C.get(i, j) + A.get(i, k) * B.get(k, j));
                }
            }
        });
        return C;
    }

    public static DMatrix ikjAlgorithm(DMatrix A, DMatrix B) {
        // initialize c
        DMatrix C = DMatrix.empty(A.rows(), B.cols());
        for (int i = 0; i < A.rows(); i++) {
            for (int k = 0; k < A.cols(); k++) {
                if (A.get(i, k) == 0) {
                    continue;
                }
                for (int j = 0; j < B.cols(); j++) {
                    C.set(i, j, C.get(i, j) + A.get(i, k) * B.get(k, j));
                }
            }
        }
        return C;
    }

    public static DMatrix ikjParallel(DMatrix A, DMatrix B) {
        DMatrix C = DMatrix.empty(A.rows(), B.cols());
        IntStream.range(0, A.rows()).parallel().forEach(i -> {
            for (int k = 0; k < A.cols(); k++) {
                if (A.get(i, k) == 0) {
                    continue;
                }
                for (int j = 0; j < B.cols(); j++) {
                    C.inc(i, j, A.get(i, k) * B.get(k, j));
                }
            }
        });
        return C;
    }

    public static DVector ikjParallel(DMatrix A, DVector b) {

        if (A.cols() != b.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix [%d,%d] and vector[%d,1] are not conform for multiplication.",
                            A.rows(), A.cols(), b.size()
                    ));
        }

        DVector C = DVector.zeros(A.rows());
        int len = Math.floorDiv(A.rows(), 16);
        IntStream.range(0, len + 1).parallel().forEach(s -> {
            for (int i = s * 16; i < Math.min((s + 1) * 16, A.rows()); i++) {
                for (int j = 0; j < A.cols(); j++) {
                    C.set(i, C.get(i) + A.get(i, j) * b.get(j));
                }
            }
        });
        return C;
    }

    public static DMatrix tiledAlgorithm(DMatrix A, DMatrix B) {
        DMatrix C = DMatrix.empty(A.rows(), B.cols());

        // Pick a tile size T = theta(sqrt(M))
        int T = 1;
        while (T < Math.sqrt(A.cols())) {
            T *= 2;
        }
        int TT = T;

//        For I from 1 to n in steps of T:
        for (int I = 0; I < A.rows(); I += T) {
            // For J from 1 to p in steps of T:
            for (int J = 0; J < B.cols(); J += T) {
                // For K from 1 to m in steps of T:
                for (int K = 0; K < A.cols(); K += T) {
                    // Multiply AI:I+T, K:K+T and BK:K+T, J:J+T into CI:I+T, J:J+T, that is:
                    // For i from I to min(I + T, n):
                    for (int i = I; i < Math.min(I + T, A.rows()); i++) {
                        // For j from J to min(J + T, p):
                        for (int j = J; j < Math.min(J + T, B.cols()); j++) {
                            // Let sum = 0
                            double sum = 0;
                            // For k from K to min(K + T, m):
                            for (int k = K; k < Math.min(K + T, A.cols()); k++) {
                                // Set sum := sum + Aik x Bkj
                                sum += A.get(i, k) * B.get(k, j);
                            }
                            // Set Cij := sum
                            C.set(i, j, C.get(i, j) + sum);
                        }
                    }

                }
            }
        }
        return C;
    }

    public static DMatrix copyParallel(DMatrix A, DMatrix B) {
        if (A.cols() != B.rows()) {
            throw new IllegalArgumentException("Matrices are not conformant for multiplication.");
        }
        DMatrix C = DMatrix.empty(A.rows(), B.cols());
        DVector[] as = new DVector[C.rows()];
        DVector[] bs = new DVector[C.cols()];
        for (int i = 0; i < C.rows(); i++) {
            as[i] = A.mapRow(i);
        }
        for (int i = 0; i < C.cols(); i++) {
            bs[i] = B.mapCol(i);
        }
        IntStream.range(0, C.rows()).parallel().forEach(i -> {
            for (int j = 0; j < C.cols(); j++) {
                C.set(i, j, as[i].dot(bs[j]));
            }
        });
        return C;
    }

    private static DMatrix add(DMatrix A, DMatrix B) {
        DMatrix C = DMatrix.empty(A.rows(), A.cols());
        for (int i = 0; i < A.rows(); i++) {
            for (int j = 0; j < A.cols(); j++) {
                C.set(i, j, A.get(i, j) + B.get(i, j));
            }
        }
        return C;
    }

    private static DMatrix subtract(DMatrix A, DMatrix B) {
        int n = A.rows();
        DMatrix C = DMatrix.empty(n, n);
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

    public static DMatrix strassen(DMatrix A, DMatrix B, int leafSize) {
        // Make the matrices bigger so that you can apply the strassen
        // algorithm recursively without having to deal with odd
        // matrix sizes
        int n = A.cols();
        int m = nextPowerOfTwo(n);
        DMatrix APrep = DMatrix.empty(m, m);
        DMatrix BPrep = DMatrix.empty(m, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                APrep.set(i, j, A.get(i, j));
                BPrep.set(i, j, B.get(i, j));
            }
        }

        DMatrix CPrep = strassenR(APrep, BPrep, leafSize);
        DMatrix C = DMatrix.empty(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C.set(i, j, CPrep.get(i, j));
            }
        }
        return C;
    }

    private static DMatrix strassenR(DMatrix A, DMatrix B, int leafSize) {
        int n = A.cols();

        if (n <= leafSize) {
            return ikjAlgorithm(A, B);
        } else {
            // initializing the new sub-matrices
            int newSize = n / 2;
            DMatrix a11 = DMatrix.empty(newSize, newSize);
            DMatrix a12 = DMatrix.empty(newSize, newSize);
            DMatrix a21 = DMatrix.empty(newSize, newSize);
            DMatrix a22 = DMatrix.empty(newSize, newSize);

            DMatrix b11 = DMatrix.empty(newSize, newSize);
            DMatrix b12 = DMatrix.empty(newSize, newSize);
            DMatrix b21 = DMatrix.empty(newSize, newSize);
            DMatrix b22 = DMatrix.empty(newSize, newSize);

            // dividing the matrices in 4 sub-matrices:
            for (int i = 0; i < newSize; i++) {
                for (int j = 0; j < newSize; j++) {
                    a11.set(i, j, A.get(i, j)); // top left
                    a12.set(i, j, A.get(i, j + newSize)); // top right
                    a21.set(i, j, A.get(i + newSize, j)); // bottom left
                    a22.set(i, j, A.get(i + newSize, j + newSize)); // bottom right

                    b11.set(i, j, B.get(i, j)); // top left
                    b12.set(i, j, B.get(i, j + newSize)); // top right
                    b21.set(i, j, B.get(i + newSize, j)); // bottom left
                    b22.set(i, j, B.get(i + newSize, j + newSize)); // bottom right
                }
            }

            // Calculating p1 to p7:
            DMatrix aResult = add(a11, a22);
            DMatrix bResult = add(b11, b22);
            DMatrix p1 = strassenR(aResult, bResult, leafSize);
            // p1 = (a11+a22) * (b11+b22)

            aResult = add(a21, a22); // a21 + a22
            DMatrix p2 = strassenR(aResult, b11, leafSize); // p2 = (a21+a22) * (b11)

            bResult = subtract(b12, b22); // b12 - b22
            DMatrix p3 = strassenR(a11, bResult, leafSize);
            // p3 = (a11) * (b12 - b22)

            bResult = subtract(b21, b11); // b21 - b11
            DMatrix p4 = strassenR(a22, bResult, leafSize);
            // p4 = (a22) * (b21 - b11)

            aResult = add(a11, a12); // a11 + a12
            DMatrix p5 = strassenR(aResult, b22, leafSize);
            // p5 = (a11+a12) * (b22)

            aResult = subtract(a21, a11); // a21 - a11
            bResult = add(b11, b12); // b11 + b12
            DMatrix p6 = strassenR(aResult, bResult, leafSize);
            // p6 = (a21-a11) * (b11+b12)

            aResult = subtract(a12, a22); // a12 - a22
            bResult = add(b21, b22); // b21 + b22
            DMatrix p7 = strassenR(aResult, bResult, leafSize);
            // p7 = (a12-a22) * (b21+b22)

            // calculating c21, c21, c11 e c22:
            DMatrix c12 = add(p3, p5); // c12 = p3 + p5
            DMatrix c21 = add(p2, p4); // c21 = p2 + p4

            aResult = add(p1, p4); // p1 + p4
            bResult = add(aResult, p7); // p1 + p4 + p7
            DMatrix c11 = subtract(bResult, p5);
            // c11 = p1 + p4 - p5 + p7

            aResult = add(p1, p3); // p1 + p3
            bResult = add(aResult, p6); // p1 + p3 + p6
            DMatrix c22 = subtract(bResult, p2);
            // c22 = p1 + p3 - p2 + p6

            // Grouping the results obtained in a single matrix:
            DMatrix C = DMatrix.empty(n, n);
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

    public static DMatrix mul(DMatrix A, double scalar) {
        DMatrix X = DMatrix.empty(A.rows(), A.cols());
        for (int i = 0; i < A.rows(); i++) {
            for (int j = 0; j < A.cols(); j++) {
                X.set(i, j, A.get(i, j) * scalar);
            }
        }
        return X;
    }
}