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
package rapaio.workspace;

import rapaio.data.Frame;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author tutuianu
 */
public class WorkspaceData implements Serializable {

    private final HashMap<String, Frame> frames = new HashMap<>();
    private final Map<Class<?>, Map<String, Object>> map = new HashMap<>();
    private final List<WorkspaceDataListener> listeners = new LinkedList<>();

    public Frame getFrame(String name) {
        return frames.get(name);
    }

    public Frame putFrame(String name, Frame df) {
        Frame prev = frames.put(name, df);
        for (WorkspaceDataListener listener : listeners) {
            listener.onPutFrames(name);
        }
        return prev;
    }

    public void removeFrames(String... names) {
        for (String name : names) {
            frames.remove(name);
        }
        for (WorkspaceDataListener listener : listeners) {
            listener.onRemoveFrames(names);
        }
    }

    public <T> T get(Class<T> clazz, String name) {
        if (!map.containsKey(clazz)) {
            return null;
        }
        return (T) map.get(clazz).get(name);
    }

    public <T> void put(Class<T> clazz, String name, T value) {
        if (!map.containsKey(clazz)) {
            map.put(clazz, new HashMap<String, Object>());
        }
        map.get(clazz).put(name, value);
    }

    public void addListener(WorkspaceDataListener listener) {
        listeners.add(listener);
    }

    public Collection<WorkspaceDataListener> getListeners() {
        return listeners;
    }
    
    public void clearListeners() {
        listeners.clear();
    }
}
