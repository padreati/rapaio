/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.gsh

import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/9/15.
 */
class Rapaio extends CommandSupport {


    protected Rapaio(Groovysh shell) {
        super(shell, 'rapaio', ':rp')
    }

    @Override
    Object execute(List<String> list) {

        """
import static rapaio.sys.WS.*
import rapaio.data.*
import rapaio.core.*
import rapaio.io.json.*
import static rapaio.graphics.Plotter.*
import static rapaio.graphics.opt.GOpt.*
import rapaio.datasets.*
        """

        def imports = shell.imports
        imports.add("static rapaio.sys.WS.*")
        imports.add("rapaio.data.*")
        imports.add("rapaio.core.*")
        imports.add("rapaio.io.json.*")
        imports.add("static rapaio.graphics.Plotter.*")
        imports.add("static rapaio.graphics.opt.GOpt.*")
        imports.add("rapaio.datasets.*")
        shell.imports = imports
        return null
    }
}
