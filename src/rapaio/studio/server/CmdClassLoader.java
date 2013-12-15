package rapaio.studio.server;

import rapaio.server.ClassBytes;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CmdClassLoader extends ClassLoader {

    private final ClassBytes cb;

    public CmdClassLoader(ClassBytes cb, ClassLoader parent) {
        super(parent);
        this.cb = cb;
    }
    
    @Override
    public Class<?> findClass(String name) {
        if(cb.getName().equals(name)) {
            return defineClass(name, cb.getBytes(), 0, cb.getBytes().length);
        }
        return null;
    }
}
