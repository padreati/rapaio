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
package rapaio.ml.classification.colselect;

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.data.Frame;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RandomColSelector implements ColSelector {

	private int mcols = -1;
	private String[] candidates;

	public RandomColSelector(Frame df, ColRange except, int mcols) {
		this.mcols = mcols;
		List<Integer> exceptColumns = except.parseColumnIndexes(df);
		candidates = new String[df.getColCount() - exceptColumns.size()];
		int pos = 0;
		int expos = 0;
		for (int i = 0; i < df.getColCount(); i++) {
			if (expos < exceptColumns.size() && i == exceptColumns.get(expos)) {
				expos++;
				continue;
			}
			candidates[pos++] = df.getColNames()[i];
		}
	}

	@Override
	public synchronized String[] nextColNames() {
		String[] result = new String[mcols];
		if (mcols < 1) {
			throw new RuntimeException("Uniform random column selector not initialized");
		}
		for (int i = 0; i < mcols; i++) {
			int next = RandomSource.nextInt(candidates.length - i);
			result[i] = candidates[next];
			candidates[next] = candidates[candidates.length - 1 - i];
			candidates[candidates.length - 1 - i] = result[i];
		}
		return result;
	}
}
