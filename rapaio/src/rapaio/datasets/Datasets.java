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
package rapaio.datasets;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.VectorType;
import rapaio.data.filters.VectorFilters;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Datasets {

	public static Frame loadIrisDataset() throws IOException, URISyntaxException {
		Frame df = new CsvPersistence().read(Datasets.class, "iris.csv");
		Vector[] vectors = new Vector[df.getColCount()];
		vectors[vectors.length - 1] = df.getCol(vectors.length - 1);
		for (int i = 0; i < vectors.length - 1; i++) {
			vectors[i] = VectorFilters.toNumeric(df.getCol(i));
		}
		return new SolidFrame(df.getRowCount(), vectors, df.getColNames());
	}

	public static Frame loadPearsonHeightDataset() throws IOException, URISyntaxException {
		Frame df = new CsvPersistence().read(Datasets.class, "pearsonheight.csv");
		Vector[] vectors = new Vector[df.getColCount()];
		for (int i = 0; i < df.getColCount(); i++) {
			vectors[i] = VectorFilters.toNumeric(df.getCol(i));
		}
		return new SolidFrame(df.getRowCount(), vectors, df.getColNames());
	}

	public static Frame loadChestDataset() throws IOException, URISyntaxException {
		CsvPersistence persistence = new CsvPersistence();
		persistence.setColSeparator(',');
		persistence.setHasQuotas(false);
		Frame df = persistence.read(Datasets.class, "chest.csv");
		return VectorFilters.toNumeric(df);
	}

	public static Frame loadCarMpgDataset() throws IOException, URISyntaxException {
		CsvPersistence csv = new CsvPersistence();
		csv.setColSeparator(',');
		csv.setHasHeader(true);
		csv.setHasQuotas(true);
		csv.setDefaultTypeHint(VectorType.NUMERIC);
		return csv.read(Datasets.class, "carmpg.csv");
	}

	public static Frame loadSpamBase() throws IOException, URISyntaxException {
		CsvPersistence persistence = new CsvPersistence();
		persistence.setColSeparator(',');
		persistence.setHasHeader(true);
		persistence.getNominalFieldHints().add("spam");

		persistence.getNumericFieldHints().add("word_freq_make");
		persistence.getNumericFieldHints().add("word_freq_address");
		persistence.getNumericFieldHints().add("word_freq_all");
		persistence.getNumericFieldHints().add("word_freq_3d");
		persistence.getNumericFieldHints().add("word_freq_our");
		persistence.getNumericFieldHints().add("word_freq_over");
		persistence.getNumericFieldHints().add("word_freq_remove");
		persistence.getNumericFieldHints().add("word_freq_internet");
		persistence.getNumericFieldHints().add("word_freq_order");
		persistence.getNumericFieldHints().add("word_freq_mail");
		persistence.getNumericFieldHints().add("word_freq_receive");
		persistence.getNumericFieldHints().add("word_freq_will");
		persistence.getNumericFieldHints().add("word_freq_people");
		persistence.getNumericFieldHints().add("word_freq_report");
		persistence.getNumericFieldHints().add("word_freq_addresses");
		persistence.getNumericFieldHints().add("word_freq_free");
		persistence.getNumericFieldHints().add("word_freq_business");
		persistence.getNumericFieldHints().add("word_freq_email");
		persistence.getNumericFieldHints().add("word_freq_you");
		persistence.getNumericFieldHints().add("word_freq_credit");
		persistence.getNumericFieldHints().add("word_freq_your");
		persistence.getNumericFieldHints().add("word_freq_font");
		persistence.getNumericFieldHints().add("word_freq_000");
		persistence.getNumericFieldHints().add("word_freq_money");
		persistence.getNumericFieldHints().add("word_freq_hp");
		persistence.getNumericFieldHints().add("word_freq_hpl");
		persistence.getNumericFieldHints().add("word_freq_george");
		persistence.getNumericFieldHints().add("word_freq_650");
		persistence.getNumericFieldHints().add("word_freq_lab");
		persistence.getNumericFieldHints().add("word_freq_labs");
		persistence.getNumericFieldHints().add("word_freq_telnet");
		persistence.getNumericFieldHints().add("word_freq_857");
		persistence.getNumericFieldHints().add("word_freq_data");
		persistence.getNumericFieldHints().add("word_freq_415");
		persistence.getNumericFieldHints().add("word_freq_85");
		persistence.getNumericFieldHints().add("word_freq_technology");
		persistence.getNumericFieldHints().add("word_freq_1999");
		persistence.getNumericFieldHints().add("word_freq_parts");
		persistence.getNumericFieldHints().add("word_freq_pm");
		persistence.getNumericFieldHints().add("word_freq_direct");
		persistence.getNumericFieldHints().add("word_freq_cs");
		persistence.getNumericFieldHints().add("word_freq_meeting");
		persistence.getNumericFieldHints().add("word_freq_original");
		persistence.getNumericFieldHints().add("word_freq_project");
		persistence.getNumericFieldHints().add("word_freq_re");
		persistence.getNumericFieldHints().add("word_freq_edu");
		persistence.getNumericFieldHints().add("word_freq_table");
		persistence.getNumericFieldHints().add("word_freq_conference");
		persistence.getNumericFieldHints().add("char_freq_,");
		persistence.getNumericFieldHints().add("char_freq_(");
		persistence.getNumericFieldHints().add("char_freq_[");
		persistence.getNumericFieldHints().add("char_freq_!");
		persistence.getNumericFieldHints().add("char_freq_$");
		persistence.getNumericFieldHints().add("char_freq_#");
		persistence.getNumericFieldHints().add("capital_run_length_average");
		persistence.getNumericFieldHints().add("capital_run_length_longest");
		persistence.getNumericFieldHints().add("capital_run_length_total");
		return persistence.read(Datasets.class, "spam-base.csv");
	}

	public static Frame loadMushrooms() throws IOException {
		CsvPersistence persistence = new CsvPersistence();
		persistence.setColSeparator(',');
		persistence.setHasHeader(true);
		persistence.setHasQuotas(false);
		return persistence.read(Datasets.class, "mushrooms.csv");
	}

	public static Frame loadPlay() throws IOException {
		CsvPersistence persistence = new CsvPersistence();
		persistence.setColSeparator(',');
		persistence.setHasHeader(true);
		persistence.setHasQuotas(false);
		persistence.getNumericFieldHints().add("temp");
		persistence.getNumericFieldHints().add("humidity");
		return persistence.read(Datasets.class, "play.csv");
	}

	public static Frame loadOlympic() throws IOException {
		CsvPersistence csv = new CsvPersistence();
		csv.setHasHeader(true);
		csv.setHasQuotas(false);
		csv.getNumericFieldHints().add("Edition");
		return csv.read(Datasets.class, "olympic.csv");
	}

	public static Frame loadProstateCancer() throws IOException {
		CsvPersistence csv = new CsvPersistence();
		csv.setHasHeader(true);
		csv.setColSeparator('\t');
		csv.setDefaultTypeHint(VectorType.NUMERIC);
		return csv.read(Datasets.class, "prostate.csv");
	}
}
