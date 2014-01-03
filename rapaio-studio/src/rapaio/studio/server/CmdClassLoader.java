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
package rapaio.studio.server;

import rapaio.server.CommandBytes;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CmdClassLoader extends ClassLoader {

    private final CommandBytes cb;

    public CmdClassLoader(CommandBytes cb, ClassLoader parent) {
        super(parent);
        this.cb = cb;
    }

    @Override
    public Class<?> findClass(String name) {
        if (cb.getName().equals(name)) {
            return defineClass(name, cb.getBytes(), 0, cb.getBytes().length);
        }
        return null;
    }
}
