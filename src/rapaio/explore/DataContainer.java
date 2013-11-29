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
package rapaio.explore;

import rapaio.data.Frame;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author tutuianu
 */
public class DataContainer implements Serializable {

    private HashMap<String, Frame> frames = new HashMap<>();

    public Frame getFrame(String name) {
        return frames.get(name);
    }

    public Frame putFrame(String name, Frame df) {
        return frames.put(name, df);
    }

    public void dropFrames(String... names) {
        for (String name : names) {
            frames.remove(name);
        }
    }
}
