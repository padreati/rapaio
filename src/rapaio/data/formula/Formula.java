/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.formula;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.util.Pin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class Formula {

    private final List<FrameFilter> frameFilters = new ArrayList<>();

    public void add(FrameFilter frameFilter) {
        frameFilters.add(frameFilter);
    }

    public Frame apply(Frame frame) {
        if (frame instanceof MappedFrame) {
            throw new IllegalArgumentException("Cannot apply filters on mapped frames");
        }
        Pin<Frame> df = new Pin<>(frame);
        frameFilters.stream().forEach(f -> df.set(f.apply(df.get())));
        return df.get();
    }
}
