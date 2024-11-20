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

package rapaio.narray;

public enum Order {
    C("C-style row major order"),
    F("Fortran-style col major order"),
    S("Storage order"),
    A("Automatic order, default value depending on the operation");

    public static Order defaultOrder() {
        return C;
    }

    public static Order autoFC(Order askOrder) {
        return switch (askOrder) {
            case F, C -> askOrder;
            default -> throw new IllegalArgumentException();
        };
    }

    private final String description;

    Order(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
