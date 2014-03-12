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

package rapaio.data.collect;

import rapaio.data.Frame;
import rapaio.data.mapping.*;


import java.util.Map;
import java.util.Set;

/**
 * Frame iterator. Allows collecting in mappings for later use.
 * <p/>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface FIterator {

	public boolean next();

	public void reset();

	public int getRowId();

	public int getRow();

	public double getValue(int col);

	public double getValue(String colName);

	public void setValue(int col, double value);

	public void setValue(String colName, double value);

	public int getIndex(int col);

	public int getIndex(String colName);

	public void setIndex(int col, int value);

	public void setIndex(String colName, int value);

	public String getLabel(int col);

	public String getLabel(String colName);

	public void setLabel(int col, String value);

	public void setLabel(String colName, String value);

	public boolean isMissing();

	public boolean isMissing(int col);

	public boolean isMissing(String colName);

	public void setMissing(int col);

	public void setMissing(String colName);

	public void appendToMapping();

	public void appendToMapping(String key);

	public void appendToMapping(int key);

	public int getMappingsCount();

	public Set<String> getMappingsKeys();

	public Mapping getMapping();

	public Mapping getMapping(String key);

	public Mapping getMapping(int key);

	public Frame getMappedFrame();

	public Frame getMappedFrame(String key);

	public Frame getMappedFrame(int key);

	public Map<String, Frame> getMappedFrames();
}
