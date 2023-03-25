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

package rapaio.experiment.asm;

import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

public class CustomClassLoader extends ClassLoader {

    private final boolean verbose;

    public CustomClassLoader(boolean verbose) {
        this.verbose = verbose;
    }

    public static void printByteCode(String name) throws IOException {
        ClassReader cr = new ClassReader(name);
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    public static void printByteCode(byte[] buffer) {
        ClassReader cr = new ClassReader(buffer);
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    @Override
    protected Class<?> findClass(String name) {

        ClassReader cr;
        try {
            cr = new ClassReader(name);
            if (verbose) {
                System.out.println("BEFORE TRANSFORM BYTE CODE");
                printByteCode(name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        TailRecTransformer tr = new TailRecTransformer(cw);
        cr.accept(tr, 0);

        byte[] buffer = cw.toByteArray();

        if (verbose) {
            System.out.println("AFTER TRANSFORM BYTE CODE");
            printByteCode(buffer);
        }

        return defineClass(name, buffer, 0, buffer.length);
    }

    public <T> T newTailRecInstance(Class<T> external, Class<?> internal)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Class<?> c = findClass(internal.getCanonicalName());
        return (T) c.getConstructor().newInstance();
    }
}

class TailRecTransformer extends ClassNode {

    private static final String METHOD_SUFFIX = "TailRec";

    public TailRecTransformer(ClassVisitor cv) {
        super(ASM9);
        this.cv = cv;
    }

    @Override
    public void visitEnd() {
        // we optimize all methods which ends with TailRec for simplicity
        methods.stream().filter(mn -> mn.name.endsWith(METHOD_SUFFIX))
                .forEach(this::transformTailRec);
        accept(cv);
    }

    void transformTailRec(MethodNode methodNode) {
        // method argument types
        Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
        // iterator over instructions
        var it = methodNode.instructions.iterator();
        LabelNode firstLabel = null;

        while (it.hasNext()) {
            var inode = it.next();
            // locate the first label
            // this label will be used to jump instead of recursive call
            if (firstLabel == null && inode instanceof LabelNode labelNode) {
                firstLabel = labelNode;
                continue;
            }
            if (inode instanceof FrameNode) {
                // remove all frames since we recompute them all at writing
                it.remove();
                continue;
            }
            if (inode instanceof MethodInsnNode methodInsnNode &&
                    methodInsnNode.name.equals(methodNode.name) &&
                    methodInsnNode.desc.equals(methodNode.desc)) {
                // find the recursive call which has to have
                // same signature and be followed by return
                // check if the next instruction is return of proper type
                var nextInstruction = it.next();
                Type returnType = Type.getReturnType(methodNode.desc);
                if (!(nextInstruction.getOpcode() ==
                        returnType.getOpcode(IRETURN))) {
                    continue;
                }

                // remove the return and recursive call from instructions
                it.previous();
                it.previous();
                it.remove();
                it.next();
                it.remove();

                // pop values from stack and store them in local
                // variables in reverse order
                for (int i = argumentTypes.length - 1; i >= 0; i--) {
                    Type type = argumentTypes[i];
                    it.add(new VarInsnNode(type.getOpcode(ISTORE), i + 1));
                }

                // add a new jump instruction to the first label
                it.add(new JumpInsnNode(GOTO, firstLabel));
                // finally remove the instruction which loaded 'this'
                // since it was required by the recursive call
                while (it.hasPrevious()) {
                    AbstractInsnNode node = it.previous();
                    if (node instanceof VarInsnNode varInsnNode) {
                        if (varInsnNode.getOpcode() == Opcodes.ALOAD &&
                                varInsnNode.var == 0) {
                            it.remove();
                            // we remove only the last instruction of this kind
                            // we don't touch it other similar instructions
                            // to not break the existent code
                            break;
                        }
                    }
                }

            }
        }
    }
}
