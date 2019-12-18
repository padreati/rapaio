package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.Var;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VApplyLabelTest {

    @Test
    void testApplyLabel() {
        Var l1 = VarNominal.copy("ana", "?", "are", "?", "mere");
        Var l2 = l1.fapply(VApplyLabel.with(l -> {
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

        assertEquals("ana", l2.getLabel(0));
        assertEquals("missing", l2.getLabel(1));
        assertEquals("era", l2.getLabel(2));
        assertEquals("missing", l2.getLabel(3));
        assertEquals("erem", l2.getLabel(4));

    }
}
