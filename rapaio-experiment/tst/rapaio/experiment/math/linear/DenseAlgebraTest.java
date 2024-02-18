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

package rapaio.experiment.math.linear;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Bernoulli;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.math.linear.base.DMatrixBase;
import rapaio.experiment.math.linear.base.DVectorBase;
import rapaio.experiment.math.linear.dense.DMatrixDenseC;
import rapaio.experiment.math.linear.dense.DMatrixDenseR;
import rapaio.experiment.math.linear.dense.DVectorDense;
import rapaio.experiment.math.linear.dense.DVectorStride;
import rapaio.experiment.math.linear.dense.DVectorVar;
import rapaio.printer.Format;
import rapaio.printer.opt.POpt;
import rapaio.printer.opt.POpts;
import rapaio.util.DoubleComparators;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

public class DenseAlgebraTest {

    private static final POpt<?>[] P_OPTS = new POpt[] {POpts.textWidth(120)};

    private static final MatrixFactory[] matrixFactories = new MatrixFactory[] {
            // base matrix - used only for tests
            (rows, cols) -> {
                double[] array = new double[rows * cols];
                DMatrix m = new DMatrixBase(rows, cols, array);
                setMatrixValues(m, rows, cols);
                return m;
            },
            // dense matrix row wise
            (rows, cols) -> {
                DMatrix m = new DMatrixDenseR(rows, cols);
                setMatrixValues(m, rows, cols);
                return m;
            },
            // dense matrix column wise
            (rows, cols) -> {
                DMatrix m = new DMatrixDenseC(rows, cols);
                setMatrixValues(m, rows, cols);
                return m;
            },
            // dense matrix column wise with stride
            (rows, cols) -> {
                int offset = 10;
                int colStride = rows + 5;
                double[] array = new double[offset + colStride * cols];
                DMatrixDenseC m = new DMatrixDenseC(offset, rows, cols, colStride, array);
                setMatrixValues(m, rows, cols);
                return m;
            },
            // dense matrix row wise with stride
            (rows, cols) -> {
                int offset = 10;
                int rowStride = rows + 5;
                double[] array = new double[offset + rowStride * cols];
                DMatrixDenseR m = new DMatrixDenseR(offset, rows, cols, rowStride, array);
                setMatrixValues(m, rows, cols);
                return m;
            },
            // map matrix
            (rows, cols) -> {
                DMatrix m = new DMatrixDenseR(rows, cols);
                setMatrixValues(m, rows, cols);
                return m
                        .mapRows(IntArrays.newSeq(rows))
                        .mapCols(IntArrays.newSeq(cols))
                        .mapRows(IntArrays.newSeq(rows));
            }
    };

    private static void setMatrixValues(DMatrix matrix, int m, int n) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                matrix.set(i, j, i * m + j + 1);
            }
        }
    }

    private static final VectorFactory[] vectorFactories = new VectorFactory[] {
            // base vector
            n -> new DVectorBase(DoubleArrays.newSeq(1, n + 1)),
            // dense vector
            n -> {
                double[] values = new double[10 + n];
                for (int i = 0; i < n; i++) {
                    values[10 + i] = i + 1;
                }
                return new DVectorDense(10, n, values);
            },
            // stride vector
            n -> {
                double[] values = new double[10 + 2 * n];
                for (int i = 0; i < n; i++) {
                    values[10 + i * 2] = i + 1;
                }
                return new DVectorStride(10, 2, n, values);
            },
            // map vector
            n -> {
                double[] values = new double[10 + 2 * n];
                int[] indexes = new int[n];
                for (int i = 0; i < n; i++) {
                    values[10 + i * 2] = i + 1;
                    indexes[i] = 10 + i * 2;
                }
                return new DVectorDense(0, 10 + 2 * n, values).map(indexes);
            },
            // var vector
            n -> new DVectorVar<>(VarDouble.wrap(DoubleArrays.newSeq(1, n + 1)))
    };

    @FunctionalInterface
    interface VectorFactory {

        default DVector newInstance() {
            return newInstance(11);
        }

        DVector newInstance(int size);

    }

    @FunctionalInterface
    interface MatrixFactory {

        default DMatrix newMatrix() {
            return newMatrix(11, 11);
        }

        DMatrix newMatrix(int rows, int cols);

    }

    private interface Function1matrix {
        void apply(DMatrix m);
    }

    private void test1matrix(MatrixFactory mf, Function1matrix f) {
        f.apply(mf.newMatrix());
    }

    @Test
    void testOneMatrix() {
        for (MatrixFactory mf : matrixFactories) {
            String message = "matrix: %s".formatted(mf.newMatrix());
            test1matrix(mf, m -> assertTrue(m.addNew(10).deepEquals(m.add(10))));
            test1matrix(mf, m -> assertTrue(m.subNew(10).deepEquals(m.sub(10))));
            test1matrix(mf, m -> assertTrue(m.mulNew(10).deepEquals(m.mul(10))));
            test1matrix(mf, m -> assertTrue(m.divNew(10).deepEquals(m.div(10))));

            test1matrix(mf, m -> assertTrue(m.mapValues(0, m.argmax(0)).deepEquals(m.max(0)), message));
            test1matrix(mf, m -> assertTrue(m.mapValues(1, m.argmax(1)).deepEquals(m.max(1)), message));
            test1matrix(mf, m -> assertTrue(m.mapValues(0, m.argmin(0)).deepEquals(m.min(0)), message));
            test1matrix(mf, m -> assertTrue(m.mapValues(1, m.argmin(1)).deepEquals(m.min(1)), message));

            int[] inRows = new int[] {1, 3};
            int[] outRows = IntArrays.removeIndexesFromDenseSequence(0, mf.newMatrix().rows(), inRows);

            int[] inCols = new int[] {1, 3};
            int[] outCols = IntArrays.removeIndexesFromDenseSequence(0, mf.newMatrix().cols(), inCols);

            test1matrix(mf, m -> assertTrue(m.mapRows(inRows).deepEquals(m.removeRows(outRows))));
            test1matrix(mf, m -> assertTrue(m.mapCols(inCols).deepEquals(m.removeCols(outCols))));

            test1matrix(mf, m -> assertTrue(m.mapRowsTo(new DMatrixDenseC(2, m.cols()), inRows)
                    .deepEquals(m.removeRowsTo(new DMatrixDenseC(2, m.cols()), outRows))));
            test1matrix(mf, m -> assertTrue(m.mapColsTo(new DMatrixDenseC(m.rows(), 2), inCols)
                    .deepEquals(m.removeColsTo(new DMatrixDenseC(m.rows(), 2), outCols))));

            test1matrix(mf, m -> assertEquals(IntStream.range(0, 11).mapToObj(i -> i * 12 + 1).mapToInt(i -> i).sum(), m.trace()));

            test1matrix(mf, m -> assertTrue(
                    DVector.wrap(IntStream.range(0, 11).mapToDouble(i -> i * 12 + 1).toArray()).deepEquals(m.diag())));

            test1matrix(mf, m -> assertTrue(m.scatter().deepEquals(m.copy().scatter())));

            test1matrix(mf, m -> assertEquals(7381, m.sum()));
            test1matrix(mf, m -> assertEquals(m.sum(0).sum(), m.sum(1).sum()));
            test1matrix(mf, m -> assertEquals(1230.166666666666668, m.variance()));
            test1matrix(mf, m -> assertTrue(DVectorDense.fill(11, 1331).deepEquals(m.variance(0)), message));
            test1matrix(mf, m -> assertTrue(DVectorDense.fill(11, 11).deepEquals(m.variance(1))));

            test1matrix(mf, m -> assertEquals(Mean.of(VarDouble.wrap(m.valueStream().toArray())).value(), m.mean()));
            test1matrix(mf, m -> assertEquals(Mean.of(m.mapRowNew(2).dv()).value(), m.mean(1).get(2)));
            test1matrix(mf, m -> assertEquals(Mean.of(m.mapColNew(2).dv()).value(), m.mean(0).get(2)));

            test1matrix(mf, m -> assertEquals(Variance.of(VarDouble.wrap(m.valueStream().toArray())).sdValue(), m.sd()));
            test1matrix(mf, m -> assertEquals(Variance.of(m.mapRowNew(2).dv()).sdValue(), m.sd(1).get(2)));
            test1matrix(mf, m -> assertEquals(Variance.of(m.mapColNew(2).dv()).sdValue(), m.sd(0).get(2)));

            test1matrix(mf, m -> assertTrue(m.applyNew(StrictMath::sqrt).deepEquals(m.apply(StrictMath::sqrt))));
            test1matrix(mf, m -> assertTrue(m.tNew().tNew().deepEquals(m.t().t())));
            test1matrix(mf, m -> assertEquals(m.sum(), m.valueStream().sum()));

            test1matrix(mf, m -> assertTrue(m.deepEquals(m.resizeCopy(11, 11, 100))));
            test1matrix(mf, m -> assertEquals(m.sum(), m.resizeCopy(19, 19, 0).sum()));
            test1matrix(mf, m -> assertEquals(m.rangeCols(0, 5).rangeRows(0, 5).sum(), m.resizeCopy(5, 5, 0).sum()));

            test1matrix(mf, m -> {
                DMatrix copy = m.copy();
                for (int i = 0; i < m.rows(); i++) {
                    for (int j = 0; j < m.cols(); j++) {
                        m.inc(i, j, 1);
                        assertEquals(copy.get(i, j) + 1, m.get(i, j));
                    }
                }
            });

            test1matrix(mf, m -> assertEquals(m.getClass().getSimpleName() + """
                    {rowCount:11, colCount:11, values:
                    [
                     [   1   2   3   4   5   6   7   8   9  10 .. ],\s
                     [  12  13  14  15  16  17  18  19  20  21 .. ],\s
                     [  23  24  25  26  27  28  29  30  31  32 .. ],\s
                     [  34  35  36  37  38  39  40  41  42  43 .. ],\s
                     [  45  46  47  48  49  50  51  52  53  54 .. ],\s
                     [  56  57  58  59  60  61  62  63  64  65 .. ],\s
                     [  67  68  69  70  71  72  73  74  75  76 .. ],\s
                     [  78  79  80  81  82  83  84  85  86  87 .. ],\s
                     [  89  90  91  92  93  94  95  96  97  98 .. ],\s
                     [ 100 101 102 103 104 105 106 107 108 109 .. ],\s
                     [ 111 112 113 114 115 116 117 118 119 120 .. ],\s
                    ]}""", m.toString()));

            test1matrix(mf, m -> assertEquals("""
                         [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]\s
                     [0]   1   2   3   4   5   6   7   8   9  10  11   [4]  45  46  47  48  49  50  51  52  53  54  55   [8]  89  90  91  92  93  94  95  96  97  98  99 \s
                     [1]  12  13  14  15  16  17  18  19  20  21  22   [5]  56  57  58  59  60  61  62  63  64  65  66   [9] 100 101 102 103 104 105 106 107 108 109 110 \s
                     [2]  23  24  25  26  27  28  29  30  31  32  33   [6]  67  68  69  70  71  72  73  74  75  76  77  [10] 111 112 113 114 115 116 117 118 119 120 121 \s
                     [3]  34  35  36  37  38  39  40  41  42  43  44   [7]  78  79  80  81  82  83  84  85  86  87  88 \s
                    """, m.toContent(P_OPTS)));
            test1matrix(mf, m -> assertEquals("""
                         [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]\s
                     [0]   1   2   3   4   5   6   7   8   9  10  11   [4]  45  46  47  48  49  50  51  52  53  54  55   [8]  89  90  91  92  93  94  95  96  97  98  99 \s
                     [1]  12  13  14  15  16  17  18  19  20  21  22   [5]  56  57  58  59  60  61  62  63  64  65  66   [9] 100 101 102 103 104 105 106 107 108 109 110 \s
                     [2]  23  24  25  26  27  28  29  30  31  32  33   [6]  67  68  69  70  71  72  73  74  75  76  77  [10] 111 112 113 114 115 116 117 118 119 120 121 \s
                     [3]  34  35  36  37  38  39  40  41  42  43  44   [7]  78  79  80  81  82  83  84  85  86  87  88 \s
                    """, m.toFullContent(P_OPTS)));

            test1matrix(mf, m -> assertEquals("""
                         [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]      [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10]\s
                     [0]   1   2   3   4   5   6   7   8   9  10  11   [4]  45  46  47  48  49  50  51  52  53  54  55   [8]  89  90  91  92  93  94  95  96  97  98  99 \s
                     [1]  12  13  14  15  16  17  18  19  20  21  22   [5]  56  57  58  59  60  61  62  63  64  65  66   [9] 100 101 102 103 104 105 106 107 108 109 110 \s
                     [2]  23  24  25  26  27  28  29  30  31  32  33   [6]  67  68  69  70  71  72  73  74  75  76  77  [10] 111 112 113 114 115 116 117 118 119 120 121 \s
                     [3]  34  35  36  37  38  39  40  41  42  43  44   [7]  78  79  80  81  82  83  84  85  86  87  88 \s
                    """, m.toSummary(P_OPTS)));

            assertEquals("""
                           [0]   [1]   [2]   [3]   [4]   [5]   [6]   [7]   [8]   [9]  [10]  [11]  [12]  [13]  [14]  [15]  [16]  [17]  [18]  [19] ...  [98]   [99]\s
                     [0]     1     2     3     4     5     6     7     8     9    10    11    12    13    14    15    16    17    18    19    20 ...    99    100\s
                     [1]   101   102   103   104   105   106   107   108   109   110   111   112   113   114   115   116   117   118   119   120 ...   199    200\s
                     [2]   201   202   203   204   205   206   207   208   209   210   211   212   213   214   215   216   217   218   219   220 ...   299    300\s
                     [3]   301   302   303   304   305   306   307   308   309   310   311   312   313   314   315   316   317   318   319   320 ...   399    400\s
                     [4]   401   402   403   404   405   406   407   408   409   410   411   412   413   414   415   416   417   418   419   420 ...   499    500\s
                     [5]   501   502   503   504   505   506   507   508   509   510   511   512   513   514   515   516   517   518   519   520 ...   599    600\s
                     [6]   601   602   603   604   605   606   607   608   609   610   611   612   613   614   615   616   617   618   619   620 ...   699    700\s
                     [7]   701   702   703   704   705   706   707   708   709   710   711   712   713   714   715   716   717   718   719   720 ...   799    800\s
                     [8]   801   802   803   804   805   806   807   808   809   810   811   812   813   814   815   816   817   818   819   820 ...   899    900\s
                     [9]   901   902   903   904   905   906   907   908   909   910   911   912   913   914   915   916   917   918   919   920 ...   999  1,000\s
                    [10] 1,001 1,002 1,003 1,004 1,005 1,006 1,007 1,008 1,009 1,010 1,011 1,012 1,013 1,014 1,015 1,016 1,017 1,018 1,019 1,020 ... 1,099  1,100\s
                    [11] 1,101 1,102 1,103 1,104 1,105 1,106 1,107 1,108 1,109 1,110 1,111 1,112 1,113 1,114 1,115 1,116 1,117 1,118 1,119 1,120 ... 1,199  1,200\s
                    [12] 1,201 1,202 1,203 1,204 1,205 1,206 1,207 1,208 1,209 1,210 1,211 1,212 1,213 1,214 1,215 1,216 1,217 1,218 1,219 1,220 ... 1,299  1,300\s
                    [13] 1,301 1,302 1,303 1,304 1,305 1,306 1,307 1,308 1,309 1,310 1,311 1,312 1,313 1,314 1,315 1,316 1,317 1,318 1,319 1,320 ... 1,399  1,400\s
                    [14] 1,401 1,402 1,403 1,404 1,405 1,406 1,407 1,408 1,409 1,410 1,411 1,412 1,413 1,414 1,415 1,416 1,417 1,418 1,419 1,420 ... 1,499  1,500\s
                    [15] 1,501 1,502 1,503 1,504 1,505 1,506 1,507 1,508 1,509 1,510 1,511 1,512 1,513 1,514 1,515 1,516 1,517 1,518 1,519 1,520 ... 1,599  1,600\s
                    [16] 1,601 1,602 1,603 1,604 1,605 1,606 1,607 1,608 1,609 1,610 1,611 1,612 1,613 1,614 1,615 1,616 1,617 1,618 1,619 1,620 ... 1,699  1,700\s
                    [17] 1,701 1,702 1,703 1,704 1,705 1,706 1,707 1,708 1,709 1,710 1,711 1,712 1,713 1,714 1,715 1,716 1,717 1,718 1,719 1,720 ... 1,799  1,800\s
                    [18] 1,801 1,802 1,803 1,804 1,805 1,806 1,807 1,808 1,809 1,810 1,811 1,812 1,813 1,814 1,815 1,816 1,817 1,818 1,819 1,820 ... 1,899  1,900\s
                    [19] 1,901 1,902 1,903 1,904 1,905 1,906 1,907 1,908 1,909 1,910 1,911 1,912 1,913 1,914 1,915 1,916 1,917 1,918 1,919 1,920 ... 1,999  2,000\s
                    ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...   ...  ...  ...   ...  \s
                    [98] 9,801 9,802 9,803 9,804 9,805 9,806 9,807 9,808 9,809 9,810 9,811 9,812 9,813 9,814 9,815 9,816 9,817 9,818 9,819 9,820 ... 9,899  9,900\s
                    [99] 9,901 9,902 9,903 9,904 9,905 9,906 9,907 9,908 9,909 9,910 9,911 9,912 9,913 9,914 9,915 9,916 9,917 9,918 9,919 9,920 ... 9,999 10,000\s
                    """, mf.newMatrix(100, 100).toContent(P_OPTS));

            assertEquals("""
                                        [0]                [1]                 [2]                [3]\s
                    [0] 0.14285714285714285 0.2857142857142857 0.42857142857142855 0.5714285714285714\s
                    [1] 0.7142857142857143  0.8571428571428571 1                   1.1428571428571428\s
                    [2] 1.2857142857142858  1.4285714285714286 1.5714285714285714  1.7142857142857142\s
                    [3] 1.8571428571428572  2                  2.142857142857143   2.2857142857142856\s
                    """, mf.newMatrix(4, 4).div(7).toContent(P_OPTS));

            assertEquals("""
                          [0]   [1]   [2]   [3]\s
                    [0] 0.143 0.286 0.429 0.571\s
                    [1] 0.714 0.857 1.000 1.143\s
                    [2] 1.286 1.429 1.571 1.714\s
                    [3] 1.857 2.000 2.143 2.286\s
                    """, mf.newMatrix(4, 4).div(7).toContent(POpts.textWidth(100), POpts.floatFormat(Format.floatShort())));

            assertEquals("""
                              [0]       [1]       [2]       [3]\s
                    [0] 0.1428571 0.2857143 0.4285714 0.5714286\s
                    [1] 0.7142857 0.8571429 1.0000000 1.1428571\s
                    [2] 1.2857143 1.4285714 1.5714286 1.7142857\s
                    [3] 1.8571429 2.0000000 2.1428571 2.2857143\s
                    """, mf.newMatrix(4, 4).div(7).toContent(POpts.textWidth(100), POpts.floatFormat(Format.floatMedium())));

            assertEquals("""
                                                     [0]                              [1]                              [2]\s
                    [0] 0.142857142857142850000000000000 0.285714285714285700000000000000 0.428571428571428550000000000000\s
                    [1] 0.714285714285714300000000000000 0.857142857142857100000000000000 1.000000000000000000000000000000\s
                    [2] 1.285714285714285800000000000000 1.428571428571428600000000000000 1.571428571428571400000000000000\s
                    [3] 1.857142857142857200000000000000 2.000000000000000000000000000000 2.142857142857143000000000000000\s
                                        
                                                     [3]\s
                    [0] 0.571428571428571400000000000000\s
                    [1] 1.142857142857142800000000000000\s
                    [2] 1.714285714285714200000000000000\s
                    [3] 2.285714285714285600000000000000\s
                                        
                    """, mf.newMatrix(4, 4).div(7).toContent(POpts.textWidth(100), POpts.floatFormat(Format.floatLong())));
        }
    }

    private interface Function1matrix1vector {
        void apply(DMatrix m, DVector v);
    }

    void test1matrix1vector(MatrixFactory mf, VectorFactory vf, Function1matrix1vector f) {
        DMatrix m = mf.newMatrix();
        DVector v = vf.newInstance();
        f.apply(m, v);
    }

    @Test
    void testOneMatrixOneVector() {
        for (MatrixFactory mf : matrixFactories) {
            for (var vf : vectorFactories) {
                String message = "matrix: %s, vector: %s".formatted(
                        mf.newMatrix().toString(),
                        vf.newInstance().toString()
                );
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.addNew(v, 0).deepEquals(m.add(v, 0)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.addNew(v, 1).deepEquals(m.add(v, 1)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.subNew(v, 0).deepEquals(m.sub(v, 0)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.subNew(v, 1).deepEquals(m.sub(v, 1)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.mulNew(v, 0).deepEquals(m.mul(v, 0)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.mulNew(v, 1).deepEquals(m.mul(v, 1)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.divNew(v, 0).deepEquals(m.div(v, 0)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.divNew(v, 1).deepEquals(m.div(v, 1)), message));
                test1matrix1vector(mf, vf, (m, v) -> assertTrue(m.dot(v).deepEquals(m.copy().dot(v.copy())), message));

                test1matrix1vector(mf, vf,
                        (m, v) -> Assertions.assertThrows(IllegalArgumentException.class, () -> m.dot(new DVectorDense(v.size() - 1))));
            }
        }
    }

    private interface Function2matrices {
        void apply(DMatrix m1, DMatrix m2);
    }

    private void test2matrices(MatrixFactory mf1, MatrixFactory mf2, Function2matrices f) {
        DMatrix m1 = mf1.newMatrix();
        DMatrix m2 = mf2.newMatrix();
        f.apply(m1, m2);
    }

    @Test
    void testTwoMatrices() {

        for (MatrixFactory mf1 : matrixFactories) {
            for (MatrixFactory mf2 : matrixFactories) {
                String message = "m1: %s, m2: %s".formatted(
                        mf1.newMatrix().toString(),
                        mf2.newMatrix().toString());
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.addNew(m2).deepEquals(m1.add(m2))));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.subNew(m2).deepEquals(m1.sub(m2))));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.mulNew(m2).deepEquals(m1.mul(m2))));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.divNew(m2).deepEquals(m1.div(m2))));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.dot(m2).deepEquals(m1.copy().dot(m2.copy())), message));
                int[] in = new int[] {0, 1, 3, 4, 5};
                int[] out = new int[] {2, 6, 7, 8, 9, 10};
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.mapRows(in).deepEquals(m2.removeRows(out)), message));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.mapRowsNew(in).deepEquals(m2.removeRowsNew(out)), message));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.mapCols(in).deepEquals(m2.removeCols(out)), message));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.mapColsNew(in).deepEquals(m2.removeColsNew(out)), message));

                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.rangeRows(2, 6).deepEquals(m2.rangeRows(2, 6)), message));
                test2matrices(mf1, mf2,
                        (m1, m2) -> assertTrue(m1.rangeRowsNew(2, 6).deepEquals(m2.rangeRowsNew(2, 6)), message));
                test2matrices(mf1, mf2, (m1, m2) -> assertTrue(m1.rangeCols(2, 6).deepEquals(m2.rangeCols(2, 6)), message));
                test2matrices(mf1, mf2,
                        (m1, m2) -> assertTrue(m1.rangeColsNew(2, 6).deepEquals(m2.rangeColsNew(2, 6)), message));
            }
        }
    }

    private interface Function1vector {
        void apply(DVector v);
    }

    private void test1vector(VectorFactory vf, Function1vector f) {
        DVector v = vf.newInstance();
        f.apply(v);
    }

    @Test
    void testOneVector() {

        for (var vf : vectorFactories) {
            String message = "vector: %s".formatted(vf.newInstance().toString());
            test1vector(vf, v -> assertTrue(v.addNew(10).deepEquals(v.add(10))));
            test1vector(vf, v -> assertTrue(v.subNew(10).deepEquals(v.sub(10))));
            test1vector(vf, v -> assertTrue(v.mulNew(10).deepEquals(v.mul(10))));
            test1vector(vf, v -> assertTrue(v.divNew(10).deepEquals(v.div(10))));

            test1vector(vf, v -> assertTrue(DVectorDense.fill(11, 7).deepEquals(v.copy().fill(7))));

            test1vector(vf, v -> {
                double[] array1 = v.logNew().denseCopy().array();
                double[] array2 = v.denseCopy().log().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.log(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.log1pNew().denseCopy().array();
                double[] array2 = v.denseCopy().log1p().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.log1p(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.log10New().denseCopy().array();
                double[] array2 = v.denseCopy().log10().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.log10(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.absNew().denseCopy().array();
                double[] array2 = v.denseCopy().abs().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.abs(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.negNew().denseCopy().array();
                double[] array2 = v.denseCopy().neg().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = -copy[i];
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.cosNew().denseCopy().array();
                double[] array2 = v.denseCopy().cos().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.cos(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.acosNew().denseCopy().array();
                double[] array2 = v.denseCopy().acos().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.acos(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.coshNew().denseCopy().array();
                double[] array2 = v.denseCopy().cosh().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.cosh(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.sinNew().denseCopy().array();
                double[] array2 = v.denseCopy().sin().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.sin(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.sinhNew().denseCopy().array();
                double[] array2 = v.denseCopy().sinh().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.sinh(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.asinNew().denseCopy().array();
                double[] array2 = v.denseCopy().asin().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.asin(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.tanNew().denseCopy().array();
                double[] array2 = v.denseCopy().tan().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.tan(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.tanhNew().denseCopy().array();
                double[] array2 = v.denseCopy().tanh().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.tanh(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.atanNew().denseCopy().array();
                double[] array2 = v.denseCopy().atan().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.atan(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.expNew().denseCopy().array();
                double[] array2 = v.denseCopy().exp().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.exp(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.expm1New().denseCopy().array();
                double[] array2 = v.denseCopy().expm1().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.expm1(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.sqrtNew().denseCopy().array();
                double[] array2 = v.denseCopy().sqrt().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.sqrt(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });
            test1vector(vf, v -> {
                double[] array1 = v.cbrtNew().denseCopy().array();
                double[] array2 = v.denseCopy().cbrt().array();
                double[] copy = v.denseCopy().solidArrayCopy();
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = StrictMath.cbrt(copy[i]);
                }
                Assertions.assertArrayEquals(array1, copy, 1e-15);
                Assertions.assertArrayEquals(array2, copy, 1e-15);
            });

            test1vector(vf, v -> assertTrue(v.cutNew(Double.NaN, 0.5).deepEquals(v.cut(Double.NaN, 0.5))));
            test1vector(vf, v -> assertTrue(v.cutNew(0.5, Double.NaN).deepEquals(v.cut(0.5, Double.NaN))));
            test1vector(vf, v -> assertTrue(v.cutNew(0.1, 0.5).deepEquals(v.cut(0.1, 0.5))));
            test1vector(vf, v -> assertTrue(v.cutNew(Double.NaN, Double.NaN).deepEquals(v.cut(Double.NaN, Double.NaN)), message));

            test1vector(vf, v -> {
                boolean before = false;
                boolean after = false;

                for (double value : v) {
                    if (value < 5 || value > 7) {
                        before = true;
                        break;
                    }
                }
                v.cut(5, 7);
                for (double value : v) {
                    if (value < 5 || value > 7) {
                        after = true;
                        break;
                    }
                }
                Assertions.assertNotEquals(before, after);
            });

            test1vector(vf, v -> assertEquals(v.norm(1), v.sum()));
            test1vector(vf, v -> assertEquals(11, v.norm(Double.POSITIVE_INFINITY)));
            test1vector(vf, v -> assertEquals(66, v.sum()));
            test1vector(vf, v -> assertEquals(66, v.nansum()));
            test1vector(vf, v -> assertEquals(3.99168E7, v.prod()));
            test1vector(vf, v -> assertEquals(3.99168E7, v.nanprod()));
            test1vector(vf, v -> assertEquals(11, v.nancount()));
            test1vector(vf, v -> assertEquals(6, v.mean()));
            test1vector(vf, v -> assertEquals(6, v.nanmean()));
            test1vector(vf, v -> assertEquals(11, v.variance()));
            test1vector(vf, v -> assertEquals(11, v.nanvariance()));

            test1vector(vf, v -> assertEquals(0, v.argmin()));
            test1vector(vf, v -> assertEquals(1, v.min()));
            test1vector(vf, v -> assertEquals(10, v.argmax()));
            test1vector(vf, v -> assertEquals(11, v.max()));

            test1vector(vf, v -> assertTrue(v.applyNew((row, x) -> row * x).deepEquals(v.apply((row, x) -> row * x))));

            int[] indexes = new int[] {2, 7, 3, 2};
            test1vector(vf, v -> assertArrayEquals(indexes, v.map(indexes).valueStream().mapToInt(x -> (int) x - 1).toArray(), message));
            test1vector(vf, v -> assertArrayEquals(indexes, v.mapNew(indexes).valueStream().mapToInt(x -> (int) x - 1).toArray(), message));

            test1vector(vf, v -> assertTrue(v.applyNew(x -> x + 1).deepEquals(v.apply(x -> x + 1))));

            test1vector(vf, v -> {
                DVector cumsum = v.copy();
                for (int i = 1; i < cumsum.size(); i++) {
                    cumsum.inc(i, cumsum.get(i - 1));
                }
                assertTrue(cumsum.deepEquals(v.cumsum()));
            });

            test1vector(vf, v -> {
                var cumprod = v.copy();
                for (int i = 1; i < cumprod.size(); i++) {
                    cumprod.set(i, cumprod.get(i) * cumprod.get(i - 1));
                }
                assertTrue(cumprod.deepEquals(v.cumprod()));
            });

            test1vector(vf, v -> assertTrue(v.normalizeNew(3).deepEquals(v.normalize(3))));

            test1vector(vf, v -> assertTrue(v.sortValuesNew().deepEquals(v.sortValues())));
            test1vector(vf, v -> assertTrue(v.sortValuesNew(DoubleComparators.OPPOSITE_COMPARATOR)
                    .deepEquals(v.sortValues(DoubleComparators.OPPOSITE_COMPARATOR))));
            test1vector(vf, v -> assertTrue(v.sortValuesNew(false).deepEquals(v.sortValues(false))));

            test1vector(vf, v -> {
                int[] indexes1 = IntArrays.newSeq(v.size());
                int[] indexes2 = IntArrays.newSeq(v.size());
                int[] indexes3 = IntArrays.newSeq(v.size());

                v.sortIndexes(indexes1);
                v.sortIndexes(true, indexes2);
                v.sortIndexes(DoubleComparators.NATURAL_COMPARATOR, indexes3);

                Assertions.assertArrayEquals(indexes1, indexes2);
                Assertions.assertArrayEquals(indexes2, indexes3);
            });

            test1vector(vf, v -> assertTrue(v.asMatrix().mapCol(0).deepEquals(v),
                    "vtype: %s".formatted(vf.newInstance().getClass().getName())));

            test1vector(vf, v -> assertEquals(v.getClass().getSimpleName() + "{size:11, values:[1,2,3,4,5,6,7,8,9,10,11]}", v.toString()));
            test1vector(vf, v -> assertEquals("""
                     [0]  1  [4]  5  [8]  9\s
                     [1]  2  [5]  6  [9] 10\s
                     [2]  3  [6]  7 [10] 11\s
                     [3]  4  [7]  8\s
                    """, v.toContent(P_OPTS)));
            assertEquals("""
                     [0]   1  [6]   7 [12]  13 [18]  19\s
                     [1]   2  [7]   8 [13]  14 [19]  20\s
                     [2]   3  [8]   9 [14]  15 ...  ...\s
                     [3]   4  [9]  10 [15]  16 [98]  99\s
                     [4]   5 [10]  11 [16]  17 [99] 100\s
                     [5]   6 [11]  12 [17]  18\s
                    """, vf.newInstance(100).toContent(P_OPTS));
            test1vector(vf, v -> assertEquals("""
                     [0]  1  [4]  5  [8]  9\s
                     [1]  2  [5]  6  [9] 10\s
                     [2]  3  [6]  7 [10] 11\s
                     [3]  4  [7]  8\s
                    """, v.toFullContent(P_OPTS)));
            test1vector(vf, v -> assertEquals("""
                     [0]  1  [4]  5  [8]  9\s
                     [1]  2  [5]  6  [9] 10\s
                     [2]  3  [6]  7 [10] 11\s
                     [3]  4  [7]  8\s
                    """, v.toSummary(P_OPTS)));

            test1vector(vf, v -> {
                v = v.copy().sortValues();
                for (int i = 1; i < v.size(); i++) {
                    Assertions.assertTrue(v.get(i - 1) <= v.get(i));
                }
            });

            test1vector(vf, v -> v.dv().deepEquals(VarDouble.seq(1, 10)));

            test1vector(vf, v -> Assertions.assertThrows(IllegalArgumentException.class, () -> v.dot(DVectorDense.fill(v.size() - 1, 0))));
        }
    }

    private interface Function2vector {
        void apply(DVector v1, DVector v2);
    }

    private void test2vector(VectorFactory vf1, VectorFactory vf2, Function2vector fun) {
        DVector v1 = vf1.newInstance();
        DVector v2 = vf2.newInstance();
        fun.apply(v1, v2);
    }

    @Test
    void testTwoVectors() {
        for (VectorFactory vf1 : vectorFactories) {
            for (VectorFactory vf2 : vectorFactories) {

                String msg = "v1: %s, v2: %s".formatted(vf1.newInstance(), vf2.newInstance());

                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.addNew(v2).deepEquals(v1.add(v2)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.subNew(v2).deepEquals(v1.sub(v2)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.mulNew(v2).deepEquals(v1.mul(v2)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.divNew(v2).deepEquals(v1.div(v2)), msg));

                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.addTo(v2, 10).deepEquals(v1.addNew(10)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.subTo(v2, 10).deepEquals(v1.subNew(10)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.mulTo(v2, 10).deepEquals(v1.mulNew(10)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.divTo(v2, 10).deepEquals(v1.divNew(10)), msg));

                test2vector(vf1, vf2, (v1, v2) -> assertTrue(v1.fmaNew(10, v2).deepEquals(v1.fma(10, v2)), msg));

                test2vector(vf1, vf2, (v1, v2) -> assertEquals(506.0, v1.dot(v2), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertEquals(506.0, v1.dotBilinear(DMatrix.eye(11), v2), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertEquals(506.0, v1.dotBilinear(DMatrix.eye(11)), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertEquals(506.0, v1.dotBilinearDiag(DMatrix.eye(11), v2), msg));
                test2vector(vf1, vf2, (v1, v2) -> assertEquals(506.0, v1.dotBilinearDiag(DVector.ones(11)), msg));

                test2vector(vf1, vf2, (v1, v2) -> {
                    DMatrix m = v1.outer(v2);
                    assertEquals(v1.size(), m.rows());
                    assertEquals(v2.size(), m.cols());
                    for (int i = 0; i < v1.size(); i++) {
                        for (int j = 0; j < v2.size(); j++) {
                            assertEquals(v1.get(i) * v2.get(j), m.get(i, j));
                        }
                    }
                });

            }
        }
    }

    private interface Function1matrix2vectors {
        void apply(DVector v1, DVector v2, DMatrix m);
    }

    private void test1matrix2vectors(VectorFactory vf1, VectorFactory vf2, MatrixFactory mf, Function1matrix2vectors f) {
        DVector v1 = vf1.newInstance();
        DVector v2 = vf2.newInstance();
        DMatrix m = mf.newMatrix();
        f.apply(v1, v2, m);
    }

    @Test
    void testOneMatrixTwoVectors() {
        for (var vf1 : vectorFactories) {
            for (var vf2 : vectorFactories) {
                for (MatrixFactory mf : matrixFactories) {
                    test1matrix2vectors(vf1, vf2, mf, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(m.diag()).mul(v2).sum(), v1.dotBilinearDiag(m, v2)));
                    test1matrix2vectors(vf1, vf2, mf, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(m.diag()).mul(v1).sum(), v1.dotBilinearDiag(m)));
                    test1matrix2vectors(vf1, vf2, mf, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(v1).mul(v2).sum(), v1.dotBilinearDiag(v2, v1)));
                    test1matrix2vectors(vf1, vf2, mf, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(v1).mul(v2).sum(), v1.dotBilinearDiag(v2)));
                }
            }
        }
    }

    @Test
    void testDVectorInterface() {
        DVector vector = DVector.random(10);
        assertEquals(10, vector.size());
        vector.valueStream().forEach(value -> {
            Assertions.assertTrue(value > -100);
            Assertions.assertTrue(value < 100);
        });

        vector = DVector.random(20, Bernoulli.of(0.5));
        assertEquals(20, vector.size());
        vector.valueStream().forEach(value -> Assertions.assertTrue(value == 0 || value == 1));

        vector = DVector.from(15, i -> i * 10.0);
        assertEquals(15, vector.size());
        for (int i = 0; i < vector.size(); i++) {
            assertEquals(i * 10, vector.get(i));
        }
    }

    @Test
    void testDMatrixInterface() {
        DMatrix m = DMatrix.diagonal(DVector.fill(10, 2));
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(i == j ? 2.0 : 0, m.get(i, j));
            }
        }

        m = DMatrix.fill(2, 3, 4);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(4.0, m.get(i, j));
            }
        }

        m = DMatrix.fill(2, 3, (i, j) -> i * j + 3);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(i * j + 3, m.get(i, j));
            }
        }

        m = DMatrix.random(3, 4);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                Assertions.assertTrue(m.get(i, j) > -100);
                Assertions.assertTrue(m.get(i, j) < 100);
            }
        }

        m = DMatrix.random(3, 4, Bernoulli.of(0.5));
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                Assertions.assertTrue(m.get(i, j) == 0 || m.get(i, j) == 1);
            }
        }

        double[][] liffe = new double[][] {
                {1, 2, 3, 7, 8, 9},
                {4, 5, 6, 4, 3, 2},
                {2, 3, 4, 5, 6, 7},
                {1, 1, 1, 1, 1, 1}
        };

        m = DMatrix.copy(liffe);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(liffe[i][j], m.get(i, j));
            }
        }

        m = DMatrix.copy(true, liffe);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(liffe[i][j], m.get(i, j));
            }
        }

        m = DMatrix.copy(false, liffe);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(liffe[j][i], m.get(i, j));
            }
        }

        m = DMatrix.copy(1, 3, 1, 3, false, liffe);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(liffe[j + 1][i + 1], m.get(i, j));
            }
        }

        m = DMatrix.copy(1, 3, 1, 3, true, liffe);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                assertEquals(liffe[i + 1][j + 1], m.get(i, j));
            }
        }

        Var v1 = VarDouble.copy(1, 2, 3).name("x1");
        Var v2 = VarDouble.copy(4, 5, 6).name("x2");

        Frame df = SolidFrame.byVars(v1, v2);

        m = DMatrix.copy(df);
        assertEquals(3, m.rows());
        assertEquals(2, m.cols());
        assertEquals(1, m.get(0, 0));
        assertEquals(6, m.get(2, 1));

        m = DMatrix.copy(v1, v2);
        assertEquals(3, m.rows());
        assertEquals(2, m.cols());
        assertEquals(1, m.get(0, 0));
        assertEquals(6, m.get(2, 1));

//        m = DMatrix.copy(false, v1.dv(), v2.dv());
//        assertEquals(3, m.rows());
//        assertEquals(2, m.cols());
//        assertEquals(1, m.get(0, 0));
//        assertEquals(6, m.get(2, 1));
//
//        m = DMatrix.copy(true, v1.dv(), v2.dv());
//        assertEquals(2, m.rows());
//        assertEquals(3, m.cols());
//        assertEquals(1, m.get(0, 0));
//        assertEquals(6, m.get(1, 2));

    }
}