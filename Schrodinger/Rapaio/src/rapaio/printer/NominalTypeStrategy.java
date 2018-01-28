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

package rapaio.printer;

import rapaio.data.Frame;
import rapaio.data.Var;

public class NominalTypeStrategy implements TypeStrategy {

	@Override
	public void getVarSummary(Frame df, Var v, String[][] first, String[][] second, int th) {
		// TODO Auto-generated method stub
		int[] hits = new int[v.levels().length];
        int[] indexes = new int[v.levels().length];
        for (int j = 0; j < df.rowCount(); j++) {
            hits[v.index(j)]++;
            indexes[v.index(j)] = j;
        }
        int[] tophit = new int[6];
        int[] topindex = new int[6];
        for (int j = 1; j < hits.length; j++) {
            if (hits[j] != 0) {
                for (int l = 0; l < tophit.length; l++) {
                    if (tophit[l] < hits[j]) {
                        for (int m = tophit.length - 1; m > l; m--) {
                            tophit[m] = tophit[m - 1];
                            topindex[m] = topindex[m - 1];
                        }
                        tophit[l] = hits[j];
                        topindex[l] = j;
                        break;
                    }
                }
            }
        }
        int nas = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        int other = df.rowCount();
        int pos = 0;
        for (int j = 0; j < 6; j++) {
            if (tophit[j] != 0) {
                other -= tophit[j];
                first[th][j] = v.label(indexes[topindex[j]]);
                second[th][j] = String.valueOf(tophit[j]);
                pos++;
            }
        }
        if (nas != 0) {
            if (other - nas != 0) {
                if (pos == 6) {
                    pos--;
                }
                first[th][pos] = "(Other)";
                second[th][pos] = String.valueOf(other - nas);
                pos++;
            }
            first[th][pos] = "NA's";
            second[th][pos] = String.valueOf(nas);
        } else {
            if (other != 0) {
                first[th][pos] = "(Other)";
                second[th][pos] = String.valueOf(other);
            }
        }
	}

	@Override
	public void getPrintSummary(Var v, String[] first, String[] second) {
		int[] hits = new int[v.rowCount() + 1];
        int[] indexes = new int[v.rowCount() + 1];
        for (int j = 0; j < v.rowCount(); j++) {
            hits[v.index(j)]++;
            indexes[v.index(j)] = j;
        }
        int[] tophit = new int[6];
        int[] topindex = new int[6];
        for (int j = 1; j < hits.length; j++) {
            if (hits[j] != 0) {
                for (int l = 0; l < tophit.length; l++) {
                    if (tophit[l] < hits[j]) {
                        for (int m = tophit.length - 1; m > l; m--) {
                            tophit[m] = tophit[m - 1];
                            topindex[m] = topindex[m - 1];
                        }
                        tophit[l] = hits[j];
                        topindex[l] = j;
                        break;
                    }
                }
            }
        }
        int nas = 0;
        for (int j = 0; j < v.rowCount(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        int other = v.rowCount();
        int pos = 0;
        for (int j = 0; j < 6; j++) {
            if (tophit[j] != 0) {
                other -= tophit[j];
                first[j] = v.label(indexes[topindex[j]]);
                second[j] = String.valueOf(tophit[j]);
                pos++;
            }
        }
        if (nas != 0) {
            if (other - nas != 0) {
                if (pos == 6) {
                    pos--;
                }
                first[pos] = "(Other)";
                second[pos] = String.valueOf(other - nas);
                pos++;
            }
            first[pos] = "NA's";
            second[pos] = String.valueOf(nas);
        } else {
            if (other != 0) {
                first[pos] = "(Other)";
                second[pos] = String.valueOf(other);
            }
        }
	}

}
