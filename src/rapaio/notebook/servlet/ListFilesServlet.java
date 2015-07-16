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

package rapaio.notebook.servlet;

import rapaio.notebook.NotebookServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/14/15.
 */
public class ListFilesServlet extends HttpServlet {

    private static final long serialVersionUID = 8170267743174353638L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"listFiles\": [");

        String rootPath = NotebookServer.getInstance().getConfig().getRoot();
        File root = new File(rootPath);
        if (!root.exists()) {
            throw new IllegalArgumentException(String.format("root folder <%s> does not exists", rootPath));
        }
        File[] children = root.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(children[i].getName()).append("\" ");
        }

        sb.append("] }");
        resp.setContentType("application/json");
        resp.getWriter().write(sb.toString());
    }
}
