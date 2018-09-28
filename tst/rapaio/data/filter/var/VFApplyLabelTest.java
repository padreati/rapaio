package rapaio.data.filter.var;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Var;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VFApplyLabelTest {

    @Test
    public void testApplyLabel() {
        Var l1 = VarNominal.copy("ana", "?", "are", "?", "mere");
        Var l2 = l1.fapply(VFApplyLabel.with(l -> {
            if (l.equals("?")) {
                return "missing";
            }

            char[] msg = l.toCharArray();
            for (int i = 0; i < msg.length / 2; i++) {
                char tmp = msg[i];
                msg[i] = msg[msg.length - i - 1];
                msg[msg.length - i - 1] = tmp;
            }
            return String.copyValueOf(msg);
        }));

        Assert.assertEquals("ana", l2.getLabel(0));
        Assert.assertEquals("missing", l2.getLabel(1));
        Assert.assertEquals("era", l2.getLabel(2));
        Assert.assertEquals("missing", l2.getLabel(3));
        Assert.assertEquals("erem", l2.getLabel(4));

    }
}
