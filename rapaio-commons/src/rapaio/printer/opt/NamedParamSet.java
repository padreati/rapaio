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

package rapaio.printer.opt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class NamedParamSet<S extends NamedParamSet<S, P>, P extends NamedParam<S, ?>> {

    protected final Map<String, P> defaults;
    protected final Map<String, P> params = new HashMap<>();
    protected S parent;

    protected NamedParamSet(S parent) {
        this.parent = parent;
        if (parent != null) {
            defaults = null;
        } else {
            defaults = new HashMap<>();
            register(this.getClass());
        }
    }

    public void setParent(S parent) {
        this.parent = parent;
    }

    public final S apply(P... parameters) {
        for (var p : parameters) {
            params.put(p.getName(), p);
        }
        return (S) this;
    }

    private void register(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (var field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(null);
                if(!(fieldValue instanceof NamedParam)) {
                    continue;
                }
                P np = (P) fieldValue;
                if (defaults.containsKey(np.getName())) {
                    throw new IllegalArgumentException("Duplicate keys in named values.");
                }
                defaults.put(np.getName(), np);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Object getParamValue(P p) {
        S root = (S) this;
        S ref = (S) this;
        while(true) {
            if (ref.params.containsKey(p.getName())) {
                return ref.params.get(p.getName()).getValue(root);
            }
            if (ref.parent != null) {
                ref = ref.parent;
                continue;
            }
            return ref.defaults.get(p.getName()).getValue(root);
        }
    }

    private S getRoot() {
        S root = (S) this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public abstract S bind(P... parameters);

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + getRoot().defaults.values().stream().map(p -> {
            Object value = getParamValue(p);
            return p.getName() + ":" + (value == null ? "null" : value);
        }).collect(Collectors.joining(",", "{", "}"));
    }
}
