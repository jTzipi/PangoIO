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

import javafx.scene.text.Font;
import org.apache.commons.io.file.PathUtils;

import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    public static final double FONT_MIN_SIZE = 11.0D;
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
        if(Files.isDirectory(path)) {
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
        if(Files.isDirectory(path)) {
            return _DIR_;
        }
        return PathUtils.getExtension(path);
    }



    /* ------------------------ File System View related ----------------------------------*/

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

    /* -------------------------------------- Loading Images/Fonts/Resources --------------------------------------- */

    /**
     * Try to load a file as a BufferedImage.
     *
     * @param path path to file
     * @return buffered image
     * @throws IOException          if file is not readable or failed to read as image
     * @throws NullPointerException if  {@code path} is null
     */
    public static java.awt.image.BufferedImage loadBufferedImage( final Path path ) throws IOException {

        Objects.requireNonNull( path );
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path[='" + path + "'] is not readable" );
        }
        return ImageIO.read( path.toFile() );
    }

    /**
     * Try to load an image via resources.
     *
     * @param cls class
     * @param fileNameStr file
     * @return buffered image
     * @throws IOException Failed
     * @throws IllegalStateException if {@code fileNameStr} is not readable hence the stream is null
     * @throws NullPointerException if {@code cls} | {@code fileNameStr}
     */
    public static java.awt.image.BufferedImage loadBufferedImageFromResource( final Class<?> cls, final String fileNameStr ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileNameStr );
        try( InputStream is = cls.getResourceAsStream( fileNameStr ) ) {
            if( null == is ) {
                throw new IllegalStateException("file [='"+fileNameStr+"'] not found for class [='"+cls+"']");
            }
            return ImageIO.read( is );
        }

    }

    /**
     * Try to load a file as a javafx image.
     *
     * @param path path to image
     * @return image
     * @throws IOException          failed to read file
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.image.Image loadJavaFXImage( final Path path ) throws IOException {

        Objects.requireNonNull( path, "Path is null" );

        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path[='" + path + "'] is not readable" );
        }

        try ( InputStream fis = Files.newInputStream( path ) ) {
            return new javafx.scene.image.Image( fis );
        }
    }

    /**
     * Load a JavaFX font from path.
     *
     * @param path path
     * @param size size
     * @return Font object
     * @throws IOException          io loading font or {@code path} is not readable
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.text.Font loadFont( final Path path, final double size ) throws IOException {

        Objects.requireNonNull( path );
        // Error
        if ( !Files.isReadable( path ) ) {
            throw new IOException( "Path [='" + path + "'] is not readable" );
        }

        // try
        final javafx.scene.text.Font font;
        try ( final InputStream io = Files.newInputStream( path ) ) {

            font = javafx.scene.text.Font.loadFont( io, size );
        }

        return font;

    }

    /**
     * Try to load a JavaFX font or return default system font.
     *
     * @param path path to font
     * @param fontSize font size
     * @return font or system default
     * @throws NullPointerException if {@code path} is null
     */
    public static javafx.scene.text.Font loadFontSafe( final Path path, double fontSize ) {

        Objects.requireNonNull( path );
        fontSize = Math.max(fontSize, FONT_MIN_SIZE);
        Font font;
        try {
            font = loadFont( path, fontSize );
        } catch ( final IOException ioe ) {
            LOG.info( "Failed to load font for path[='{}']",path );
            font = javafx.scene.text.Font.getDefault();
        }
        return font;
    }


    /**
     * Load a font from resource.
     * @param cls class
     * @param fileNameStr file name
     * @param fontSize font size [{@linkplain #FONT_MIN_SIZE} .. ]
     * @return font
     * @throws IOException fail to load font
     * @throws IllegalStateException if file name is not found
     * @throws NullPointerException if {@code cls} | {@code fileNameStr}
     */
    public static javafx.scene.text.Font loadFontFromResource( final Class<?> cls, final String fileNameStr, double fontSize ) throws
            IOException {

        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileNameStr );
        fontSize = Math.max( fontSize, FONT_MIN_SIZE );

        try( InputStream is = cls.getResourceAsStream( fileNameStr ) ) {
            if( null == is ) {
                throw new IllegalStateException("Font InputStream is null");
            }

            return javafx.scene.text.Font.loadFont( is, fontSize );
        }
    }
    /* -------------------------------------- Loading Properties --------------------------------------- */
    /**
     * Load Properties from path.
     * If {@code prop} is {@code null} we create a new one.
     * @param pathToProp path to properties
     * @param prop       properties
     * @throws IOException              if {@code pathToProp} !readable
     * @throws NullPointerException     if {@code pathToProp} is null
     * @throws IllegalArgumentException if {@code pathToProp} is dir
     */
    public static void loadProperties( final Path pathToProp, Properties prop ) throws IOException {

        Objects.requireNonNull( pathToProp );
        //
        if ( !Files.isReadable( pathToProp ) ) {
            throw new IOException( "Path[='" + pathToProp + "'] not readable" );
        }
        // If no file throw
        if ( Files.isDirectory( pathToProp ) ) {
            throw new IllegalArgumentException( "You try to read properties from dir[='" + pathToProp + "']" );
        }
        //
        if ( null == prop ) {
            prop = new Properties();
        }

        try ( final InputStream inStream = Files.newInputStream( pathToProp ) ) {
            prop.load( inStream );
        }

    }

    /**
     * Try to read the properties contained in the file.
     *
     * @param path path to file
     * @return properties
     * @throws IOException              read of path failed
     * @throws NullPointerException     if {@code path} is null
     * @throws IllegalArgumentException if {@code pathToProp} is dir
     */
    public static Properties loadProperties( final Path path ) throws IOException {

        Properties prop = new Properties();

        loadProperties( path, prop );

        return prop;
    }

    /**
     * Read a '.properties'-file as a resource located file.
     *
     * @param cls     cls
     * @param fileStr file name
     * @return properties
     * @throws IOException           if file not readable
     * @throws IllegalStateException if stream is null
     * @throws NullPointerException  if {@code cls}|{@code fileStr} is null
     */
    public static Properties loadPropertiesFromResource( Class<?> cls, String fileStr ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileStr );
        Properties prop = new Properties();
        loadPropertiesFromResource( cls, fileStr, prop );
        return prop;

    }
    /**
     * Read a '.properties'-file as a resource located file.
     *
     * @param cls     cls
     * @param fileStr file name
     * @param properties properties
     * @throws IOException           if file not readable
     * @throws IllegalStateException if stream is null
     * @throws NullPointerException  if {@code cls}|{@code fileStr}|{@code properties} is null
     */
    public static void loadPropertiesFromResource( final Class<?> cls, final String fileStr, final Properties properties ) throws IOException {
        Objects.requireNonNull( cls );
        Objects.requireNonNull( fileStr );
        Objects.requireNonNull( properties );

        LOG.info( "try to load '{}' from '{}'", fileStr, cls.getSimpleName() );
        try ( InputStream is = cls.getResourceAsStream( fileStr ) ) {

            if ( null == is ) {
                throw new IllegalStateException( "File '"+ fileStr+"' not found or not readable" );
            }
            properties.load( is );

        }
        LOG.info( "'{}' loaded Okay!", fileStr );


    }

    /**
     * Load a resource as a string wrapped in an StringBuilder.
     *
     * @param cls           cls to load from
     * @param fileStr       file name
     * @param appendNewLine append new line ({@literal \n}
     * @return String Builder with file content
     * @throws IOException          fail to load
     * @throws NullPointerException if {@code cls}|{@code fileStr}
     */
    public static StringBuilder loadResourceString( final Class<?> cls, String fileStr, boolean appendNewLine ) throws IOException {
        Objects.requireNonNull( cls, "class is null" );
        Objects.requireNonNull( fileStr, "File is null!" );
        StringBuilder sb = new StringBuilder();

        try ( InputStream resIs = cls.getResourceAsStream( fileStr ) ) {
            if ( null == resIs ) {
                throw new IOException( "InputStream for resource '" + fileStr + "' can not created!Was null!" );
            }

            try (Scanner scan = new Scanner(resIs)) {
                while (scan.hasNextLine()) {
                    sb.append(scan.nextLine());
                    if (appendNewLine) {
                        sb.append('\n');
                    }
                }
            }
        }

        return sb;
    }

    /**
     * Write global JaMeLime properties file.
     *
     * @param path Path to write
     * @param prop    properties
     * @param commentStr comment (optional)
     * @throws IOException          io
     * @throws NullPointerException if {@code path}|{@code prop} is null
     */
    public static void writeProperties(Path path, Properties prop, String commentStr ) throws IOException {
        Objects.requireNonNull( path );
        Objects.requireNonNull( prop );

        if ( null == commentStr ) {
            commentStr = "<Auto Generated Comment!>";
        }

        try ( BufferedWriter bw = Files.newBufferedWriter( path ) ) {
            prop.store( bw, commentStr );
        }

        LOG.info( "Wrote to '{}' okay!!", path );
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