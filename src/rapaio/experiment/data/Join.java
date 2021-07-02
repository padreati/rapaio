/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.data;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/19.
 */
public class Join {

    private final Frame left;
    private final Frame right;

    private String[] bothKeys = new String[0];
    private String[] leftKeys = new String[0];
    private String[] rightKeys = new String[0];
    private boolean leftUnique = false;
    private boolean rightUnique = false;
    private boolean leftNa = false;
    private boolean rightNa = false;

    private Join(Frame left, Frame right) {
        this.left = left;
        this.right = right;
    }

    public Join withBothKeys(String[] bothKeys) {
        this.bothKeys = bothKeys;
        return this;
    }

    public Join withLeftKeys(String[] leftKeys) {
        this.leftKeys = leftKeys;
        return this;
    }

    public Join withRightKeys(String[] rightKeys) {
        this.rightKeys = rightKeys;
        return this;
    }

    public Join withLeftUnique(boolean leftUnique) {
        this.leftUnique = leftUnique;
        return this;
    }

    public Join withRightUnique(boolean rightUnique) {
        this.rightUnique = rightUnique;
        return this;
    }

    public Join withLeftNa(boolean leftNa) {
        this.leftNa = leftNa;
        return this;
    }

    public Join withRightNa(boolean rightNa) {
        this.rightNa = rightNa;
        return this;
    }

    public JoinFrame toFrame() {
        String[] varNames = new String[0];
        boolean[] side = new boolean[0];
        int[] leftRows = new int[0];
        int[] rightRows = new int[0];

        return new JoinFrame(left, right, varNames, side, leftRows, rightRows);
    }

    public SolidFrame toSolidFrame() {
        return null;
    }
}
