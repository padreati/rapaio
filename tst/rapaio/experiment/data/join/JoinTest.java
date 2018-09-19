package rapaio.experiment.data.join;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.filter.frame.FFRefSort;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/22/18.
 */
public class JoinTest {

    private Frame df1;
    private Frame df2;

    @Before
    public void setUp() {
        df1 = SolidFrame.byVars(
                VarNominal.copy("a", "b", "c", "a", "b").withName("id"),
                VarInt.copy(20, 20, 40, 30, 40).withName("age"),
                VarDouble.wrap(1, 2, 3, 0, 0).withName("children")
        );

        df2 = SolidFrame.byVars(
                VarNominal.copy("a", "c", "d", "a", "d").withName("id"),
                VarNominal.copy("Iasi", "Iasi", "Bucharest", "Bucharest", "Constanta").withName("city")
        );
    }

    @Test
    public void leftJoinTest() {
        assertEquals("     id age children    city  \n" +
                        " [0]  a  20      1.0      Iasi\n" +
                        " [1]  a  30      0.0      Iasi\n" +
                        " [2]  a  20      1.0 Bucharest\n" +
                        " [3]  a  30      0.0 Bucharest\n" +
                        " [4]  b  20      2.0         ?\n" +
                        " [5]  b  40      0.0         ?\n" +
                        " [6]  c  40      3.0      Iasi\n",
                Join.from(df1, df2, VRange.of("id"), VRange.of("id"), Join.Type.LEFT).lines(7));
        assertEquals("     id    city   age children\n" +
                        " [0]  a      Iasi  20      1.0\n" +
                        " [1]  a Bucharest  20      1.0\n" +
                        " [2]  a      Iasi  30      0.0\n" +
                        " [3]  a Bucharest  30      0.0\n" +
                        " [4]  c      Iasi  40      3.0\n" +
                        " [5]  d Bucharest   ?        ?\n" +
                        " [6]  d Constanta   ?        ?\n",
                Join.from(df2, df1, VRange.of("id"), VRange.of("id"), Join.Type.LEFT).lines(7));

        assertTrue(Join.leftJoin(df1, df2).deepEquals(Join.from(df1, df2, VRange.of("id"), VRange.of("id"), Join.Type.LEFT)));
        assertTrue(Join.leftJoin(df2, df1).deepEquals(Join.leftJoin(df2, df1, VRange.of("id"))));
    }

    @Test
    public void rightJoinTest() {

        Frame a1 = Join.leftJoin(df1, df2);
        a1 = a1.fitApply(new FFRefSort(a1.rvar(0).refComparator(), a1.rvar(0).refComparator()));
        Frame b1 = Join.rightJoin(df2, df1);
        b1 = b1.fitApply(new FFRefSort(b1.rvar(0).refComparator(), b1.rvar(0).refComparator()));
        a1.printLines();
        b1.printLines();
        assertTrue(a1.deepEquals(b1));

        assertTrue(Join.rightJoin(df1, df2).deepEquals(Join.leftJoin(df2, df1)));
    }

}
