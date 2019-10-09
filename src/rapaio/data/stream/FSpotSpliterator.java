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

package rapaio.data.stream;

import rapaio.data.Frame;

import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/18.
 */
public class FSpotSpliterator extends Spliterators.AbstractSpliterator<FSpot> {

    private static final int CHARACTERISTICS = SIZED | SUBSIZED | CONCURRENT | NONNULL | IMMUTABLE;

    private final Frame source;
    private int end;
    private int row;

    public FSpotSpliterator(Frame source, int start, int end, int row) {
        super(end - row, CHARACTERISTICS);
        this.source = source;
        this.end = end;
        this.row = row;
    }

    @Override
    public boolean tryAdvance(Consumer<? super FSpot> action) {
        if (row < end) {
            action.accept(new FSpot(source, row));
            row++;
            return true;
        }
        return false;
    }

    @Override
    public long getExactSizeIfKnown() {
        return end - row;
    }

    @Override
    public int characteristics() {
        return CHARACTERISTICS;
    }
}
