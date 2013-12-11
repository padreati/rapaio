/*
 Apache License
 Version 2.0, January 2004
 http://www.apache.org/licenses/

 Copyright 2013 Aurelian Tutuianu

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package rapaio.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassMarshaller extends ClassLoader {

    public void marshall(Class<? extends RapaioCmd> clazz, OutputStream out) throws IOException {
        InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            while(true) {
                int ch = is.read();
                if(ch==-1) break;
                baos.write(ch);
            }
            bytes = baos.toByteArray();
        }
        ClassBytes cb = new ClassBytes(clazz.getCanonicalName(), bytes);
        try(ObjectOutputStream oos2 = new ObjectOutputStream(out)) {
            oos2.writeObject(cb);
            oos2.flush();
        }
        
    }
    
    public Class<?> unmarshall(InputStream in) throws IOException, ClassNotFoundException {
        ClassBytes cb;
        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            cb = (ClassBytes) ois.readObject();
        }
        return (Class<?>) defineClass(cb.getName(), cb.getBytes(), 0, cb.getBytes().length);
    }
}
