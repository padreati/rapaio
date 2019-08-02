package rapaio.ml.regression.simple;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.datasets.*;
import rapaio.ml.regression.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/19.
 */
public class L1RegressionModelTest {

    private static final double TOL = 1e-20;
    private Frame df;
    private Frame bigDf;

    @Before
    public void setUp() throws IOException {
        df = Datasets.loadISLAdvertising();
        Var[] vars = new Var[30];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VarDouble.seq(30).withName("x" + (i + 1));
        }
        bigDf = SolidFrame.byVars(vars);
    }

    @Test
    public void testNaming() {
        L1RegressionModel model = L1RegressionModel.newL1();
        assertEquals("L1Regression", model.name());
        assertEquals("L1Regression()", model.fullName());

        assertEquals("L1Regression(); not fitted", model.toString());
        assertEquals("L1Regression(); fitted values={Sales:12.9,Radio:22.9}", model.fit(df, "Sales,Radio").toString());
        assertEquals("L1Regression(); fitted values={x1:15,x2:15,x3:15,x4:15,x5:15,...}", model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8").toString());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. ID        nominal \n" +
                "2. TV        double  \n" +
                "3. Radio     double  \n" +
                "4. Newspaper double  \n" +
                "> target variables: \n" +
                "1. Sales double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "    Target Fitted value \n" +
                "[0]  Sales     12.9     \n" +
                "\n", model.fit(df, "Sales").content());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. ID        nominal \n" +
                "2. TV        double  \n" +
                "3. Radio     double  \n" +
                "4. Newspaper double  \n" +
                "> target variables: \n" +
                "1. Sales double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "    Target Fitted value \n" +
                "[0]  Sales     12.9     \n" +
                "\n", model.fit(df, "Sales").fullContent());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. ID        nominal \n" +
                "2. TV        double  \n" +
                "3. Radio     double  \n" +
                "4. Newspaper double  \n" +
                "> target variables: \n" +
                "1. Sales double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "    Target Fitted value \n" +
                "[0]  Sales     12.9     \n" +
                "\n", model.fit(df, "Sales").summary());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. x23 double \n" +
                "2. x24 double \n" +
                "3. x25 double \n" +
                "4. x26 double \n" +
                "5. x27 double \n" +
                "6. x28 double \n" +
                "7. x29 double \n" +
                "8. x30 double \n" +
                "> target variables: \n" +
                " 1. x1  double \n" +
                " 2. x2  double \n" +
                " 3. x3  double \n" +
                " 4. x4  double \n" +
                " 5. x5  double \n" +
                " 6. x6  double \n" +
                " 7. x7  double \n" +
                " 8. x8  double \n" +
                " 9. x9  double \n" +
                "10. x10 double \n" +
                "11. x11 double \n" +
                "12. x12 double \n" +
                "13. x13 double \n" +
                "14. x14 double \n" +
                "15. x15 double \n" +
                "16. x16 double \n" +
                "17. x17 double \n" +
                "18. x18 double \n" +
                "19. x19 double \n" +
                "20. x20 double \n" +
                "21. x21 double \n" +
                "22. x22 double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "     Target Fitted value      Target Fitted value      Target Fitted value \n" +
                " [0]     x1      15       [6]     x7      15      [18]    x19      15      \n" +
                " [1]     x2      15       [7]     x8      15      [19]    x20      15      \n" +
                " [2]     x3      15       [8]     x9      15      [20]    x21      15      \n" +
                " [3]     x4      15       [9]    x10      15      [21]    x22      15      \n" +
                " [4]     x5      15            ...       ...      \n" +
                " [5]     x6      15      [17]    x18      15      \n" +
                "\n", model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").content());
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. x23 double \n" +
                "2. x24 double \n" +
                "3. x25 double \n" +
                "4. x26 double \n" +
                "5. x27 double \n" +
                "6. x28 double \n" +
                "7. x29 double \n" +
                "8. x30 double \n" +
                "> target variables: \n" +
                " 1. x1  double \n" +
                " 2. x2  double \n" +
                " 3. x3  double \n" +
                " 4. x4  double \n" +
                " 5. x5  double \n" +
                " 6. x6  double \n" +
                " 7. x7  double \n" +
                " 8. x8  double \n" +
                " 9. x9  double \n" +
                "10. x10 double \n" +
                "11. x11 double \n" +
                "12. x12 double \n" +
                "13. x13 double \n" +
                "14. x14 double \n" +
                "15. x15 double \n" +
                "16. x16 double \n" +
                "17. x17 double \n" +
                "18. x18 double \n" +
                "19. x19 double \n" +
                "20. x20 double \n" +
                "21. x21 double \n" +
                "22. x22 double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "     Target Fitted value      Target Fitted value      Target Fitted value      Target Fitted value \n" +
                " [0]     x1      15       [6]     x7      15      [12]    x13      15      [18]    x19      15      \n" +
                " [1]     x2      15       [7]     x8      15      [13]    x14      15      [19]    x20      15      \n" +
                " [2]     x3      15       [8]     x9      15      [14]    x15      15      [20]    x21      15      \n" +
                " [3]     x4      15       [9]    x10      15      [15]    x16      15      [21]    x22      15      \n" +
                " [4]     x5      15      [10]    x11      15      [16]    x17      15      \n" +
                " [5]     x6      15      [11]    x12      15      [17]    x18      15      \n" +
                "\n", model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").fullContent());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: L1Regression\n" +
                "Model instance: L1Regression()\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. x23 double \n" +
                "2. x24 double \n" +
                "3. x25 double \n" +
                "4. x26 double \n" +
                "5. x27 double \n" +
                "6. x28 double \n" +
                "7. x29 double \n" +
                "8. x30 double \n" +
                "> target variables: \n" +
                " 1. x1  double \n" +
                " 2. x2  double \n" +
                " 3. x3  double \n" +
                " 4. x4  double \n" +
                " 5. x5  double \n" +
                " 6. x6  double \n" +
                " 7. x7  double \n" +
                " 8. x8  double \n" +
                " 9. x9  double \n" +
                "10. x10 double \n" +
                "11. x11 double \n" +
                "12. x12 double \n" +
                "13. x13 double \n" +
                "14. x14 double \n" +
                "15. x15 double \n" +
                "16. x16 double \n" +
                "17. x17 double \n" +
                "18. x18 double \n" +
                "19. x19 double \n" +
                "20. x20 double \n" +
                "21. x21 double \n" +
                "22. x22 double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "     Target Fitted value      Target Fitted value      Target Fitted value \n" +
                " [0]     x1      15       [6]     x7      15      [18]    x19      15      \n" +
                " [1]     x2      15       [7]     x8      15      [19]    x20      15      \n" +
                " [2]     x3      15       [8]     x9      15      [20]    x21      15      \n" +
                " [3]     x4      15       [9]    x10      15      [21]    x22      15      \n" +
                " [4]     x5      15            ...       ...      \n" +
                " [5]     x6      15      [17]    x18      15      \n" +
                "\n", model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").summary());
    }

    @Test
    public void testPrediction() {
        L1RegressionModel model = L1RegressionModel.newL1().newInstance().fit(df, "Sales");
        RegressionResult result = model.predict(df);
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(model.getMedians()[0], result.firstPrediction().getDouble(i), TOL);
        }
    }
}
