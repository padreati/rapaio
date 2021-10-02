/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.opt;

import java.awt.Color;
import java.util.HashMap;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/21.
 */
public class NamedColors {

    private static NamedColors instance;

    public static NamedColors getInstance() {
        if (instance == null) {
            instance = new NamedColors();
        }
        return instance;
    }

    private final HashMap<String, Color> map = new HashMap<>();

    private NamedColors() {

        // base colors

        map.put("b", new Color(0f, 0f, 1f));
        map.put("g", new Color(0f, 0.5f, 0f));
        map.put("r", new Color(1f, 0f, 0f));
        map.put("c", new Color(0f, 0.75f, 0.75f));
        map.put("m", new Color(0.75f, 0f, 0.75f));
        map.put("y", new Color(0.75f, 0.75f, 0f));
        map.put("k", new Color(0f, 0f, 0f));
        map.put("w", new Color(1f, 1f, 1f));

        // tableau colors

        map.put("tab:blue", new Color(0x1f77b4));
        map.put("tab:orange", new Color(0xff7f0e));
        map.put("tab:green", new Color(0x2ca02c));
        map.put("tab:red", new Color(0xd62728));
        map.put("tab:purple", new Color(0x9467bd));
        map.put("tab:brown", new Color(0x8c564b));
        map.put("tab:pink", new Color(0xe377c2));
        map.put("tab:gray", new Color(0x7f7f7f));
        map.put("tab:olive", new Color(0xbcbd22));
        map.put("tab:cyan", new Color(0x17becf));

        // CSS4 COLOURS

        map.put("aliceblue", new Color(0xF0F8FF));
        map.put("antiquewhite", new Color(0xFAEBD7));
        map.put("aqua", new Color(0x00FFFF));
        map.put("aquamarine", new Color(0x7FFFD4));
        map.put("azure", new Color(0xF0FFFF));
        map.put("beige", new Color(0xF5F5DC));
        map.put("bisque", new Color(0xFFE4C4));
        map.put("black", new Color(0x000000));
        map.put("blanchedalmond", new Color(0xFFEBCD));
        map.put("blue", new Color(0x0000FF));
        map.put("blueviolet", new Color(0x8A2BE2));
        map.put("brown", new Color(0xA52A2A));
        map.put("burlywood", new Color(0xDEB887));
        map.put("cadetblue", new Color(0x5F9EA0));
        map.put("chartreuse", new Color(0x7FFF00));
        map.put("chocolate", new Color(0xD2691E));
        map.put("coral", new Color(0xFF7F50));
        map.put("cornflowerblue", new Color(0x6495ED));
        map.put("cornsilk", new Color(0xFFF8DC));
        map.put("crimson", new Color(0xDC143C));
        map.put("cyan", new Color(0x00FFFF));
        map.put("darkblue", new Color(0x00008B));
        map.put("darkcyan", new Color(0x008B8B));
        map.put("darkgoldenrod", new Color(0xB8860B));
        map.put("darkgray", new Color(0xA9A9A9));
        map.put("darkgreen", new Color(0x006400));
        map.put("darkgrey", new Color(0xA9A9A9));
        map.put("darkkhaki", new Color(0xBDB76B));
        map.put("darkmagenta", new Color(0x8B008B));
        map.put("darkolivegreen", new Color(0x556B2F));
        map.put("darkorange", new Color(0xFF8C00));
        map.put("darkorchid", new Color(0x9932CC));
        map.put("darkred", new Color(0x8B0000));
        map.put("darksalmon", new Color(0xE9967A));
        map.put("darkseagreen", new Color(0x8FBC8F));
        map.put("darkslateblue", new Color(0x483D8B));
        map.put("darkslategray", new Color(0x2F4F4F));
        map.put("darkslategrey", new Color(0x2F4F4F));
        map.put("darkturquoise", new Color(0x00CED1));
        map.put("darkviolet", new Color(0x9400D3));
        map.put("deeppink", new Color(0xFF1493));
        map.put("deepskyblue", new Color(0x00BFFF));
        map.put("dimgray", new Color(0x696969));
        map.put("dimgrey", new Color(0x696969));
        map.put("dodgerblue", new Color(0x1E90FF));
        map.put("firebrick", new Color(0xB22222));
        map.put("floralwhite", new Color(0xFFFAF0));
        map.put("forestgreen", new Color(0x228B22));
        map.put("fuchsia", new Color(0xFF00FF));
        map.put("gainsboro", new Color(0xDCDCDC));
        map.put("ghostwhite", new Color(0xF8F8FF));
        map.put("gold", new Color(0xFFD700));
        map.put("goldenrod", new Color(0xDAA520));
        map.put("gray", new Color(0x808080));
        map.put("green", new Color(0x008000));
        map.put("greenyellow", new Color(0xADFF2F));
        map.put("grey", new Color(0x808080));
        map.put("honeydew", new Color(0xF0FFF0));
        map.put("hotpink", new Color(0xFF69B4));
        map.put("indianred", new Color(0xCD5C5C));
        map.put("indigo", new Color(0x4B0082));
        map.put("ivory", new Color(0xFFFFF0));
        map.put("khaki", new Color(0xF0E68C));
        map.put("lavender", new Color(0xE6E6FA));
        map.put("lavenderblush", new Color(0xFFF0F5));
        map.put("lawngreen", new Color(0x7CFC00));
        map.put("lemonchiffon", new Color(0xFFFACD));
        map.put("lightblue", new Color(0xADD8E6));
        map.put("lightcoral", new Color(0xF08080));
        map.put("lightcyan", new Color(0xE0FFFF));
        map.put("lightgoldenrodyellow", new Color(0xFAFAD2));
        map.put("lightgray", new Color(0xD3D3D3));
        map.put("lightgreen", new Color(0x90EE90));
        map.put("lightgrey", new Color(0xD3D3D3));
        map.put("lightpink", new Color(0xFFB6C1));
        map.put("lightsalmon", new Color(0xFFA07A));
        map.put("lightseagreen", new Color(0x20B2AA));
        map.put("lightskyblue", new Color(0x87CEFA));
        map.put("lightslategray", new Color(0x778899));
        map.put("lightslategrey", new Color(0x778899));
        map.put("lightsteelblue", new Color(0xB0C4DE));
        map.put("lightyellow", new Color(0xFFFFE0));
        map.put("lime", new Color(0x00FF00));
        map.put("limegreen", new Color(0x32CD32));
        map.put("linen", new Color(0xFAF0E6));
        map.put("magenta", new Color(0xFF00FF));
        map.put("maroon", new Color(0x800000));
        map.put("mediumaquamarine", new Color(0x66CDAA));
        map.put("mediumblue", new Color(0x0000CD));
        map.put("mediumorchid", new Color(0xBA55D3));
        map.put("mediumpurple", new Color(0x9370DB));
        map.put("mediumseagreen", new Color(0x3CB371));
        map.put("mediumslateblue", new Color(0x7B68EE));
        map.put("mediumspringgreen", new Color(0x00FA9A));
        map.put("mediumturquoise", new Color(0x48D1CC));
        map.put("mediumvioletred", new Color(0xC71585));
        map.put("midnightblue", new Color(0x191970));
        map.put("mintcream", new Color(0xF5FFFA));
        map.put("mistyrose", new Color(0xFFE4E1));
        map.put("moccasin", new Color(0xFFE4B5));
        map.put("navajowhite", new Color(0xFFDEAD));
        map.put("navy", new Color(0x000080));
        map.put("oldlace", new Color(0xFDF5E6));
        map.put("olive", new Color(0x808000));
        map.put("olivedrab", new Color(0x6B8E23));
        map.put("orange", new Color(0xFFA500));
        map.put("orangered", new Color(0xFF4500));
        map.put("orchid", new Color(0xDA70D6));
        map.put("palegoldenrod", new Color(0xEEE8AA));
        map.put("palegreen", new Color(0x98FB98));
        map.put("paleturquoise", new Color(0xAFEEEE));
        map.put("palevioletred", new Color(0xDB7093));
        map.put("papayawhip", new Color(0xFFEFD5));
        map.put("peachpuff", new Color(0xFFDAB9));
        map.put("peru", new Color(0xCD853F));
        map.put("pink", new Color(0xFFC0CB));
        map.put("plum", new Color(0xDDA0DD));
        map.put("powderblue", new Color(0xB0E0E6));
        map.put("purple", new Color(0x800080));
        map.put("rebeccapurple", new Color(0x663399));
        map.put("red", new Color(0xFF0000));
        map.put("rosybrown", new Color(0xBC8F8F));
        map.put("royalblue", new Color(0x4169E1));
        map.put("saddlebrown", new Color(0x8B4513));
        map.put("salmon", new Color(0xFA8072));
        map.put("sandybrown", new Color(0xF4A460));
        map.put("seagreen", new Color(0x2E8B57));
        map.put("seashell", new Color(0xFFF5EE));
        map.put("sienna", new Color(0xA0522D));
        map.put("silver", new Color(0xC0C0C0));
        map.put("skyblue", new Color(0x87CEEB));
        map.put("slateblue", new Color(0x6A5ACD));
        map.put("slategray", new Color(0x708090));
        map.put("slategrey", new Color(0x708090));
        map.put("snow", new Color(0xFFFAFA));
        map.put("springgreen", new Color(0x00FF7F));
        map.put("steelblue", new Color(0x4682B4));
        map.put("tan", new Color(0xD2B48C));
        map.put("teal", new Color(0x008080));
        map.put("thistle", new Color(0xD8BFD8));
        map.put("tomato", new Color(0xFF6347));
        map.put("turquoise", new Color(0x40E0D0));
        map.put("violet", new Color(0xEE82EE));
        map.put("wheat", new Color(0xF5DEB3));
        map.put("white", new Color(0xFFFFFF));
        map.put("whitesmoke", new Color(0xF5F5F5));
        map.put("yellow", new Color(0xFFFF00));
        map.put("yellowgreen", new Color(0x9ACD32));
    }

    public Color getColor(char name) {
        return getColor(String.valueOf(name));
    }

    public Color getColor(String name) {
        return map.get(name);
    }
}
