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

package rapaio.printer.opt;

import java.io.Serial;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public class POptionTextWidth implements POption<Integer> {

    @Serial
    private static final long serialVersionUID = 2485016171417227463L;
    private final int textWidth;

    public POptionTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    @Override
    public void bind(POpts opts) {
        opts.setTextWidth(this);
    }

    @Override
    public Integer apply(POpts opts) {
        return textWidth;
    }
}
