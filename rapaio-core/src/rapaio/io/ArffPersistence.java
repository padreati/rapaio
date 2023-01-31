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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;

/**
 * Class for loading ARFF files. ARFF is a human readable file format used by
 * Weka.
 * <a href="http://www.cs.waikato.ac.nz/ml/weka/arff.html">About Weka</a>
 *
 * @author Aurelian Tutuianu
 */
@Deprecated
public class ArffPersistence {

    public final Frame read(String fileName) throws IOException {
        return read(new File(fileName));
    }

    /**
     * Uses the given file path to load a data set from an ARFF file.
     *
     * @param file the path to the ARFF file to load
     * @return the data set from the ARFF file, or null if the file could not be
     * loaded.
     * @throws java.io.IOException
     */
    public final Frame read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public final Frame read(InputStream stream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;

            ArrayList<Var> vars = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            HashMap<String, List<String>> nomValueMap = new HashMap<>();
            ArrayList<String> data = new ArrayList<>();

            boolean ondata = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("%") || line.trim().isEmpty()) {
                    continue;
                }
                if (line.startsWith("@") && !ondata) {

                    if (line.toLowerCase().startsWith("@relation")) {
                        continue;
                    }

                    if (line.toLowerCase().startsWith("@data")) {
                        // process column definitions
                        ondata = true;
                        continue;
                    }

                    if (line.toLowerCase().startsWith("@attribute")) {
                        line = line.substring("@attribute".length()).trim();//Remove the space, it could be multiple spaces

                        String variableName;
                        line = line.replace("\t", " ");
                        if (line.startsWith("'")) {
                            Pattern p = Pattern.compile("'.+?'");
                            Matcher m = p.matcher(line);
                            m.find();
                            variableName = fullTrim(m.group());
                            line = line.replaceFirst("'.+?'", "placeHolder");
                        } else {
                            variableName = fullTrim(line.trim().replaceAll("\\s+.*", ""));
                        }
                        names.add(variableName);

                        String[] tmp = line.split("\\s+", 2);
                        if (tmp[1].trim().equalsIgnoreCase("real") || tmp[1].trim().equals("isNumeric") || tmp[1].trim().startsWith("integer")) {
                            vars.add(VarDouble.empty());
                        } else//Not correct, but we aren't supporting anything other than real and categorical right now
                        {
                            String cats = tmp[1].replace("{", "").replace("}", "").trim();
                            if (cats.endsWith(",")) {
                                cats = cats.substring(0, cats.length() - 1);
                            }
                            String[] catValsRaw = cats.split(",");
                            List<String> tempMap = new ArrayList<>();
                            for (String catVal : catValsRaw) {
                                tempMap.add(fullTrim(catVal));
                            }
                            nomValueMap.put(variableName, tempMap);
                            vars.add(VarNominal.empty(0, tempMap));
                        }
                        continue;
                    }
                }
                data.add(line.trim());
            }

            List<Var> newvectors = new ArrayList<>();
            for (int i = 0; i < vars.size(); i++) {
                if (vars.get(i) instanceof VarDouble) {
                    newvectors.add(VarDouble.empty(data.size()));
                }
                if (vars.get(i) instanceof VarNominal) {
                    newvectors.add(VarNominal.empty(data.size(), nomValueMap.get(names.get(i))));
                }
            }
            for (int i = 0; i < newvectors.size(); i++) {
                newvectors.get(i).name(names.get(i));
            }
            Frame df = SolidFrame.byVars(data.size(), newvectors);

            // process data
            for (int i = 0; i < data.size(); i++) {
                String[] tmp = data.get(i).split(",");
                for (int j = 0; j < tmp.length; j++) {
                    if ("?".equals(tmp[j])) {
                        continue;
                    }
                    if (VarType.isNumeric(df.rvar(j).type())) {
                        df.rvar(j).setDouble(i, Double.parseDouble(tmp[j]));
                    }
                    if (df.rvar(j).type().equals(VarType.NOMINAL)) {
                        df.rvar(j).setLabel(i, fullTrim(tmp[j]));
                    }
                }
            }
            return df;
        }
    }

    /**
     * Removes the quotes at the end and front of a string if there are any, as
     * well as spaces at the front and end
     *
     * @param in
     * @return
     */
    private String fullTrim(String in) {
        in = in.trim();
        if (in.startsWith("'") || in.startsWith("\"")) {
            in = in.substring(1);
        }
        if (in.endsWith("'") || in.startsWith("\"")) {
            in = in.substring(0, in.length() - 1);
        }
        return in.trim();
    }
}
