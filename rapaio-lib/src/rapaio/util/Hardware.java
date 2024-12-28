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

package rapaio.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Hardware {

    public static final Logger LOGGER = Logger.getLogger(Hardware.class.getSimpleName());
    private static final int DEFAULT_L2CACHE_SIZE = 256 * 1024;

    public enum OSType {
        LINUX,
        WIN,
        MAC
    }

    public final static OSType OS_TYPE;
    public final static int CORES = Runtime.getRuntime().availableProcessors();
    public final static int L2_CACHE_SIZE;

    static {
        String name = java.lang.System.getProperty("os.name");
        if (name.contains("Win")) {
            OS_TYPE = OSType.WIN;
        } else if (name.contains("Mac")) {
            OS_TYPE = OSType.MAC;
        } else if (name.contains("Linux")) {
            OS_TYPE = OSType.LINUX;
        } else {
            OS_TYPE = null;
        }

        L2_CACHE_SIZE = switch (OS_TYPE) {
            case WIN -> {
                try {
                    List<String> lines = readCmdLines(new String[] {"wmic", "cpu", "get", "L2CacheSize,", "NumberOfCores"});
                    int size = 0;
                    for (var line : lines) {
                        if (line.contains("L2CacheSize")) {
                            continue;
                        }
                        String[] tokens = line.split("\\s+");
                        size += 1024 * Integer.parseInt(tokens[0]) / Integer.parseInt(tokens[1]);
                    }
                    yield size == 0 ? DEFAULT_L2CACHE_SIZE : size;
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
                yield DEFAULT_L2CACHE_SIZE;
            }
            case MAC -> {
                try {
                    List<String> lines = readCmdLines(new String[] {"sysctl", "-a", "hw"});
                    for (var line : lines) {
                        String[] tokens = line.split("\\s+");
                        yield Integer.parseInt(tokens[1]);
                    }
                } catch (Exception e) {
                    LOGGER.warning(e.getMessage());
                }
                yield DEFAULT_L2CACHE_SIZE;
            }
            case LINUX -> {
                try {
                    List<String> lines = readCmdLines(new String[] {"cat", "/proc/cpuinfo"});
                    for (var line : lines) {
                        if (line.startsWith("cache size")) {
                            String[] tokens = line.substring(line.indexOf(':') + 1).trim().split(" ");
                            int size = Integer.parseInt(tokens[0]);
                            if (tokens[1].equals("KB")) {
                                size *= 1024;
                            } else if (tokens[1].equals("MB")) {
                                size *= 1024 * 1024;
                            }
                            yield size;
                        }
                    }
                } catch (Exception e) {
                    if (java.lang.System.getProperty("com.google.appengine.runtime.version") == null) {
                        LOGGER.warning(e.getMessage());
                    }
                }
                yield DEFAULT_L2CACHE_SIZE;
            }
            case null -> DEFAULT_L2CACHE_SIZE;
        };
    }

    private static List<String> readCmdLines(String[] cmd) {
        List<String> lines = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return lines;
    }
}

