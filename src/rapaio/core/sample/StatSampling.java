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

package rapaio.core.sample;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.collect.FIterator;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;

import java.util.ArrayList;
import java.util.List;

import static rapaio.data.filters.BaseFilters.shuffle;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StatSampling {

	public static List<Frame> randomSample(Frame frame, int splits) {
		int[] rowCounts = new int[splits - 1];
		for (int i = 0; i < splits - 1; i++) {
			rowCounts[i] = frame.getRowCount() / splits;
		}
		return randomSample(frame, rowCounts);
	}

	public static List<Frame> randomSample(Frame frame, int[] rowCounts) {
		int total = 0;
		for (int i = 0; i < rowCounts.length; i++) {
			total += rowCounts[i];
		}
		if (total > frame.getRowCount()) {
			throw new IllegalArgumentException("total counts greater than available number of getRowCount");
		}
		List<Frame> result = new ArrayList<>();
		Frame shuffle = shuffle(frame);
		FIterator it = shuffle.getIterator();
		for (int i = 0; i < rowCounts.length; i++) {
			for (int j = 0; j < rowCounts[i]; j++) {
				it.next();
				it.appendToMapping(i);
			}
			result.add(it.getMappedFrame(i));
		}
		while (it.next()) {
			it.appendToMapping(rowCounts.length);
		}
		if (it.getMappingsKeys().contains(String.valueOf(rowCounts.length))) {
			result.add(it.getMappedFrame(rowCounts.length));
		}
		return result;
	}

	public static Frame randomBootstrap(Frame frame) {
		return randomBootstrap(frame, frame.getRowCount());
	}

	public static Frame randomBootstrap(Frame frame, int size) {
		List<Integer> mapping = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int next = RandomSource.nextInt(frame.getRowCount());
			mapping.add(frame.getRowId(next));
		}
		return new MappedFrame(frame.getSourceFrame(), new Mapping(mapping));
	}
}
