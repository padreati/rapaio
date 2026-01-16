/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package org.jupyter.extension;

import java.util.List;

import org.jupyter.extension.table.FrameDisplayTransformer;
import org.jupyter.extension.table.VarTableDisplayTransformer;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.spi.DisplayProvider;

public class RapaioTableDisplayProvider implements DisplayProvider {

    @Override
    public List<DisplayRenderer> getDisplayRenderers() {
        return List.of();
    }

    @Override
    public List<DisplayTransformer> getDisplayTransformers() {
        return List.of(
                new FrameDisplayTransformer(),
                new VarTableDisplayTransformer()
        );
    }
}
