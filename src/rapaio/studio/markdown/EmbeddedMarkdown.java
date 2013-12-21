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

package rapaio.studio.markdown;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.lookup.ServiceProvider;
import rapaio.studio.printer.StandardIOPrinter;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@ServiceProvider(service = LanguageProvider.class)
public class EmbeddedMarkdown extends LanguageProvider {

    private Language embeddedLanguage;
    public static final String START_FRAGMENT = "/*{";
    public static final String END_FRAGMENT = "}*/";
    public static final String START_P = "?";
    public static final String END_P = "?";

    @Override
    public Language<?> findLanguage(String mimeType) {
        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        initLanguage();
        if (JavaTokenId.BLOCK_COMMENT == token.id()) {
            if (token.text() != null
                    && TokenUtilities.startsWith(token.text(), START_FRAGMENT)
                    && TokenUtilities.endsWith(token.text(), END_FRAGMENT)) {
                return LanguageEmbedding.create(embeddedLanguage, START_FRAGMENT.length(), END_FRAGMENT.length());
            }
        }
        if ("string".equals(token.id().primaryCategory())) {
            StandardIOPrinter.getIO().getOut().print("tokentext: " + token.text());
            StandardIOPrinter.getIO().select();
        }
        return null;
    }

    private void initLanguage() {
        embeddedLanguage = MimeLookup.getLookup("text/html").lookup(Language.class);
        if (embeddedLanguage == null) {
            throw new NullPointerException("Can't find language for embedding");
        }
    }
}
