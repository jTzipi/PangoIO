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

import org.apache.commons.io.file.PathUtils;

import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * IOUtils.
 * <p>
 *     All kind of I/O related things.
 * </p>
 * @author jTzipi
 */
public final class IOUtils {

    // Logger
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IOUtils.class);
    // File System View for File system dependent things
    private static final FileSystemView FSV = FileSystemView.getFileSystemView();

    /**
     * Description of a Directory.
     */
    public static final String _DIR_ = "[DIR]";
    /**
     * Description of a not available property.
     */
    public static final String _NA_ = "<NA>";

    private IOUtils() {
        throw new AssertionError("--__--");
    }

    /**
     * Read  resource bundle.
     *
     * @param cls             class from which location the resource loaded
     * @param resourceFileStr name of resource
     * @return resource bundle
     * @throws IOException           if ioe
     * @throws NullPointerException  if {@code cls}|{@code resourceFileStr} is null
     * @throws IllegalStateException if resource is not readable
     */
    public static ResourceBundle loadResourceBundle(final Class<?> cls, final String resourceFileStr ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull(resourceFileStr);

        ResourceBundle resBu;
        try ( final InputStream resIS = cls.getResourceAsStream( resourceFileStr ) ) {
            if ( null == resIS ) {
                throw new IllegalStateException( "ResourceBundle[='" + resourceFileStr + "'] not readable" );
            }
            resBu = new PropertyResourceBundle( resIS );

        }
        return resBu;
    }
    /**
     * Return home dir of user.
     *
     * @return user home or '.' if failed to read
     */
    public static Path getHomeDir() {

        return Paths.get( OS.readSysProp().getOrDefault( "user.home", "." ) );
    }

    /**
     * Return user dir.
     *
     * @return user dir or '.' if failed to read
     */
    public static Path getUserDir() {

        return Paths.get( OS.readSysProp().getOrDefault( "user.dir", "." ) );
    }
    /**
     * Format bytes.
     *
     * @param bytes byte
     * @param si    standard unit
     * @return formatted file size
     */
    public static String formatFileSize( final long bytes, final boolean si ) {
        // no formatting
        if ( 0 >= bytes ) {
            LOG.info("Format File Size byte {} < 0!",bytes);
            return "0 B";
        }
        final int unit = si ? 1000 : 1024;
        // if no need to format
        if ( unit > bytes ) {
            return bytes + " B";
        }

        final String unitSymbol = si ? "kMGT" : "KMGT";

        final int exp = ( int ) ( Math.log( bytes ) / Math.log( unit ) );

        final double ri = bytes / Math.pow( unit, exp );


        final String pre = unitSymbol.charAt( exp - 1 ) + ( si ? "" : "i" );
        return String.format( "%.1f %sB", ri, pre );
    }

    /**
     * Read the path's file name prefix.
     *
     * @param path path to file
     * @return prefix of a file or {@code null} if failed or {@linkplain #_DIR_} if a directory
     * @throws NullPointerException if {@code path} is null
     * @see PathUtils#getFileNameString(Path)
     */
    public static String getFileNamePrefix( final Path path )  {
        Objects.requireNonNull(path);
        if(isDir(path)) {
            return _DIR_;
        }
        return PathUtils.getFileNameString(path);
    }

    /**
     * Return the path's file suffix.
     * That is the part after the last {@literal .} or the empty String if
     * there is no dot in the path file name.
     *
     * @param path path to file
     * @return the suffix of the file if some exist or '' or {@linkplain #_DIR_} if a directory
     * @throws NullPointerException if {@code path} is null
     * @see PathUtils#getExtension(Path) 
     */
    public static String getFileNameSuffix( final Path path ) {
        Objects.requireNonNull(path);
        if(isDir(path)) {
            return _DIR_;
        }
        return PathUtils.getExtension(path);
    }


    /**
     * Return whether a path exist.
     *
     * @param path path
     * @return {@code true} if this file is existing
     * @throws NullPointerException {@code path} is null
     * @see File#exists()
     */
    public static boolean isExisting( final Path path ) {

        return tof( path ).exists();
    }

    /**
     * Return whether a path to a file is a <em>regular</em> file.
     * That is the fil is <u>not</u> a directory and existing.
     * @param path path to a file
     * @return {@code true} if the file exists, is not a directory and not any kind of special file
     * @throws NullPointerException if {@code path} is null
     * @see File#isFile()
     */
    public static boolean isRegular( final Path path ) {
        return tof(path).isFile();
    }

    /**
     * Test whether a path is a directory.
     *
     * @param path path
     * @return {@code true} if {@code path} is a directory
     * @throws NullPointerException if {@code} path
     *
     * @see PathUtils#isDirectory(Path, LinkOption...)
     */
    public static boolean isDir(final Path path, LinkOption... option) {
        Objects.requireNonNull(path);
        return PathUtils.isDirectory(path, option);
    }

    /**
     * Return whether path is hidden.
     *
     * @param path path
     * @return {@code true} if the {@code path} is file system hidden
     * @throws NullPointerException if {@code path}
     */
    public static boolean isHidden( final Path path ) {

        return tof( path ).isHidden();
    }
    /**
     * Return whether a file exists and file  can read.
     * This may return {@code true} but  unprivileged user
     * can <u>not</u> enter.
     *
     * @param path path
     * @return {@code true} if the {@code path}  is readable
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isReadable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canRead();
    }

    /**
     * Return whether a file is writable.
     * @param path path to file
     * @return {@code true} if the {@code path} is writable
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isWritable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canWrite();
    }

    /**
     * Return whether a file is executable.
     * @param path path to file
     * @return {@code true} if the {@code path} is executable
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isExecutable( final Path path ) {

        final File file = tof( path );

        return file.exists() && file.canExecute();
    }

    /**
     * Is the path donating to a file system drive.
     * @param path path to file
     * @return {@code true} if the {@code path} is a system drive
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isPathToDrive(final Path path) {

        return FSV.isDrive(tof(path));
    }
    /**
     * Is the path donating to the file system root.
     * @param path path to file
     * @return {@code true} if the {@code path} is the system root
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isPathToSystemRoot(final Path path) {
        return FSV.isFileSystemRoot(tof(path));
    }
    /**
     * is the path donating to a "system node".s
     * @param path path to file
     * @return {@code true} if the {@code path} is a system node
     * @throws NullPointerException if {@code path} is null
     */
    public static boolean isPathToFileSystemNode(final Path path ) {
        return FSV.isComputerNode(tof(path));
    }

    /**
     * Converts a path to a file.
     * @param path path to file
     * @return file equivalent of {@code path}
     * @throws NullPointerException if {@code path} is null
     */
    private static File tof(final Path path ) {

        return Objects.requireNonNull( path ).toFile();
    }
}