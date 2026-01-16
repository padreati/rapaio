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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

import rapaio.datasets.Datasets;

public class RapaioTableDisplayProviderTest {

    @BeforeEach
    void beforeEach() {
        Global.options().reset();
    }

    @AfterEach
    void afterEach() {
        Global.options().reset();
    }

    @Test
    void smokeTest() {
        Global.displaySystem().refreshSpiDisplayHandlers();
        DisplayData dd = Global.displaySystem().render(Datasets.loadRandom(new Random(42)));

        assertNotNull(dd);

        String mimeHtml = MimeType.HTML.longType();
        assertTrue(dd.data().containsKey(mimeHtml));

        String html = dd.data().get(mimeHtml).toString();
        assertTrue(html.contains("td"));
    }
}
