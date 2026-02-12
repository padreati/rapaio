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

package rapaio.experiment.jogl;

import java.nio.FloatBuffer;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

public class JoglStart implements GLEventListener {

    static void main() {

        JoglStart app = new JoglStart();

        JFrame frame = new JFrame("Minimal OpenGL");
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);


        GLCanvas canvas = new GLCanvas(app.capabilities);
        canvas.addGLEventListener(app);
        canvas.requestFocusInWindow();
        frame.getContentPane().add(canvas);

        frame.setVisible(true);
    }

    private static final int width = 600;
    private static final int height = 600;

    private final GLProfile profile;
    private final GLCapabilities capabilities;

    private int program;

    private static final float[] vertices_position = {
            0.0f, 0.0f,
            0.5f, 0.0f,
            0.5f, 0.5f,
    };

    public JoglStart() {
        profile = GLProfile.get(GLProfile.GL4);
        capabilities = new GLCapabilities(profile);
    }

    @Override
    public void init(GLAutoDrawable glad) {
        GL4 gl = glad.getGL().getGL4();
        program = gl.glCreateProgram();
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
    }

    int[] vertexbuffer = new int[1];
    FloatBuffer bufPos = Buffers.newDirectFloatBuffer(vertices_position);

    @Override
    public void display(GLAutoDrawable glad) {

        GL4 gl = glad.getGL().getGL4();

        gl.glClearColor(0.392f, 0.584f, 0.929f, 1.0f);

        //     gl.glFlush();

        gl.glGenBuffers(1, vertexbuffer, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexbuffer[0]);
        gl.glBufferData( GL.GL_ARRAY_BUFFER, bufPos.capacity(), bufPos, GL.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexbuffer[0]);

        gl.glVertexAttribPointer(
                0,
                3,
                GL.GL_FLOAT,
                false,
                0,
                0
        );

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
    }
}