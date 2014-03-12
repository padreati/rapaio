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

import rapaio.data.Vector;
import rapaio.data.mapping.Mapping;

import java.util.Map;
import java.util.Set;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface VIterator {

	public boolean next();

	public void reset();

	public int getRowId();

	public int getRow();

	public double getValue();

	public void setValue(double value);

	public int getIndex();

	public void setIndex(int value);

	public String getLabel();

	public void setLabel(String value);

	public boolean isMissing();

	public void setMissing();

	public void appendToMapping();

	public void appendToMapping(String key);

	public int getMappingsCount();

	public Set<String> getMappingsKeys();

	public Mapping getMapping();

	public Mapping getMapping(String key);

	public Vector getMappedVector();

	public Vector getMappedVector(String key);

	public Map<String, Vector> getMappedVectors();
}
