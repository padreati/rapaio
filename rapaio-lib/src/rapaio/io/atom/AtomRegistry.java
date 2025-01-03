/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.io.atom;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.darray.layout.StrideLayout;

public class AtomRegistry {

    public static AtomRegistry instance() {
        if (INSTANCE == null) {
            INSTANCE = new AtomRegistry();
        }
        return INSTANCE;
    }

    private static AtomRegistry INSTANCE;

    private final HashMap<Class<?>, AtomSerialization<?>> registry = new HashMap<>();

    private AtomRegistry() {
        register(Shape.Serialization.class);
        register(StrideLayout.Serialization.class);
        register(Storage.Serialization.class);
    }

    public <A> void register(Class<? extends AtomSerialization<A>> classT) {
        try {
            AtomSerialization<A> handler = classT.getConstructor().newInstance();
            registry.put(handler.getClassType(), handler);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Constructor for serialization handler is not available.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <A> AtomSerialization<A> getSerializationHandler(Class<A> clazz) {
        for (var c : registry.keySet()) {
            if (c.isAssignableFrom(clazz)) {
                return (AtomSerialization<A>) registry.get(c);
            }
        }
        throw new IllegalArgumentException("Serialization handler not found for class:" + clazz + ".");
    }
}
