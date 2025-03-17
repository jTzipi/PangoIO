/*
 * Copyright (c) 2025 Tim Langhammer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.jpangolin.modules.io;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Enum of possible <b>O</b>perating <b>S</b>ystems.
 *
 * @author jTzipi
 */
public enum OS {

    /**
     * Linux Unix.
     */
    LINUX( "/" ),
    /**
     * Windows.
     */
    WINDOWS( System.getenv( "COMPUTERNAME" ) ),
    /**
     * DOS.
     */
    DOS( "C:" ),
    /**
     * MacOS.
     */
    MAC( "/" ),
    /**
     * Solaris.
     */
    SOLARIS( "/" ),
    /**
     * Other.
     */
    OTHER( null );

    private static final Properties SYS_PROP = System.getProperties();
    private static final Map<String, String> SYS_ENV = System.getenv();
    // root path
    private final String path;

    OS(final String rootPathStr) {
        this.path = rootPathStr;
    }

    /**
     * Try to guess the OS.
     * @return depending on the OS name the corresponding Operating System
     */
    public static OS getSystemOS() {

        final String ostr = OS.getOSName().toLowerCase();

        final OS os;
        // Linux Unix
        if ( ostr.matches( ".*(nix|nux|aix).*" ) ) {
            os = LINUX;
        } else if ( ostr.matches( ".*sunos.*" ) ) {
            os = SOLARIS;
        } else if ( ostr.matches( ".*mac.*" ) ) {
            os = MAC;
        } else if ( ostr.matches( ".*win.*" ) ) {
            os = WINDOWS;
        } else if ( ostr.matches( ".*dos.*" ) ) {
            os = DOS;
        } else {
            os = OTHER;
        }

        return os;
    }


    /**
     * Return OS name.
     *
     * @return name of OS or if not readable '<NA>'
     */
    public static String getOSName() {

        return SYS_PROP.getProperty( "os.name", IOUtils._NA_ );
    }

    /**
     * Return system environment properties.
     *
     * @return map system env
     */
    public static Map<String, String> readSysProp() {
        return SYS_ENV;
    }

    /**
     * Is a Desktop GUI component supported.
     *
     * @return {@code true} if a desktop is supported
     */
    public static boolean isDesktopSupported() {
        return Desktop.isDesktopSupported();
    }

    /**
     * Try to open a local folder - with the system file manager - in which this file reside.
     * @param path path to open folder
     * @throws IOException Failed to open folder or this operation is not supported
     * @throws NullPointerException if {@code path}
     */
    public static void openDirByPlatform(Path path) throws IOException {
        Objects.requireNonNull(path);

        if( !isDesktopSupported()) {
            throw new IOException("This operation is not supported on this OS!");
        }

        try {
            java.awt.Desktop desktop = Desktop.getDesktop();
            desktop.browseFileDirectory(path.toFile());
        } catch (UnsupportedOperationException unsupOperE) {

            throw new IOException("This platform does not support this!", unsupOperE);
        }
    }
    /**
     * Root path.
     *
     * @return path to system root
     */
    public String getRootPathStr() {

        return path;
    }
}