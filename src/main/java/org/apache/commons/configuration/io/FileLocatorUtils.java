/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A utility class providing helper methods related to locating files.
 * </p>
 * <p>
 * The methods of this class are used behind the scenes when retrieving
 * configuration files based on different criteria, e.g. URLs, files, or more
 * complex search strategies. They also implement functionality required by the
 * default {@link FileSystem} implementations. Most methods are intended to be
 * used internally only by other classes in the {@code io} package.
 * </p>
 *
 * @version $Id: $
 * @since 2.0
 */
public final class FileLocatorUtils
{
    /**
     * Constant for the default {@code FileSystem}. This file system is used by
     * operations of this class if no specific file system is provided. An
     * instance of {@link DefaultFileSystem} is used.
     */
    public static final FileSystem DEFAULT_FILE_SYSTEM =
            new DefaultFileSystem();

    /** Constant for the file URL protocol */
    private static final String FILE_SCHEME = "file:";

    /** The logger.*/
    private static final Log LOG = LogFactory.getLog(ConfigurationUtils.class);

    /**
     * Private constructor so that no instances can be created.
     */
    private FileLocatorUtils()
    {
    }

    /**
     * Tries to convert the specified URL to a file object. If this fails,
     * <b>null</b> is returned.
     *
     * @param url the URL
     * @return the resulting file object
     */
    public static File fileFromURL(URL url)
    {
        return FileUtils.toFile(url);
    }

    /**
     * Returns an uninitialized {@code FileLocatorBuilder} which can be used
     * for the creation of a {@code FileLocator} object. This method provides
     * a convenient way to create file locators using a fluent API as in the
     * following example:
     * <pre>
     * FileLocator locator = FileLocatorUtils.fileLocator()
     *     .basePath(myBasePath)
     *     .fileName("test.xml")
     *     .create();
     * </pre>
     * @return a builder object for defining a {@code FileLocator}
     */
    public static FileLocatorImpl.FileLocatorBuilder fileLocator()
    {
        return fileLocator(null);
    }

    /**
     * Returns a {@code FileLocatorBuilder} which is already initialized with
     * the properties of the passed in {@code FileLocator}. This builder can
     * be used to create a {@code FileLocator} object which shares properties
     * of the original locator (e.g. the {@code FileSystem} or the encoding),
     * but points to a different file. An example use case is as follows:
     * <pre>
     * FileLocator loc1 = ...
     * FileLocator loc2 = FileLocatorUtils.fileLocator(loc1)
     *     .setFileName("anotherTest.xml")
     *     .create();
     * </pre>
     * @param src the source {@code FileLocator} (may be <b>null</b>)
     * @return an initialized builder object for defining a {@code FileLocator}
     */
    public static FileLocatorImpl.FileLocatorBuilder fileLocator(FileLocator src)
    {
        return new FileLocatorImpl.FileLocatorBuilder(src);
    }

    /**
     * Obtains a non-<b>null</b> {@code FileSystem} object from the passed in
     * {@code FileLocator}. If the passed in {@code FileLocator} has a
     * {@code FileSystem} object, it is returned. Otherwise, result is the
     * default {@code FileSystem}.
     *
     * @param locator the {@code FileLocator} (may be <b>null</b>)
     * @return the {@code FileSystem} to be used for this {@code FileLocator}
     */
    public static FileSystem obtainFileSystem(FileLocator locator)
    {
        return (locator != null) ? ObjectUtils.defaultIfNull(
                locator.getFileSystem(), DEFAULT_FILE_SYSTEM)
                : DEFAULT_FILE_SYSTEM;
    }

    /**
     * Checks whether the specified {@code FileLocator} contains enough
     * information to locate a file. This is the case if a file name or a URL is
     * defined. If the passed in {@code FileLocator} is <b>null</b>, result is
     * <b>false</b>.
     *
     * @param locator the {@code FileLocator} to check
     * @return a flag whether a file location is defined by this
     *         {@code FileLocator}
     */
    public static boolean isLocationDefined(FileLocator locator)
    {
        return (locator != null)
                && (locator.getFileName() != null || locator.getSourceURL() != null);
    }

    /**
     * Returns a flag whether all components of the given {@code FileLocator}
     * describing the referenced file are defined. In order to reference a file,
     * it is not necessary that all components are filled in (for instance, the
     * URL alone is sufficient). For some use cases however, it might be of
     * interest to have different methods for accessing the referenced file.
     * Also, depending on the filled out properties, there is a subtle
     * difference how the file is accessed: If only the file name is set (and
     * optionally the base path), each time the file is accessed a
     * {@code locate()} operation has to be performed to uniquely identify the
     * file. If however the URL is determined once based on the other components
     * and stored in a fully defined {@code FileLocator}, it can be used
     * directly to identify the file. If the passed in {@code FileLocator} is
     * <b>null</b>, result is <b>false</b>.
     *
     * @param locator the {@code FileLocator} to be checked (may be <b>null</b>)
     * @return a flag whether all components describing the referenced file are
     *         initialized
     */
    public static boolean isFullyInitialized(FileLocator locator)
    {
        if (locator == null)
        {
            return false;
        }
        return locator.getBasePath() != null && locator.getFileName() != null
                && locator.getSourceURL() != null;
    }

    /**
     * Returns a {@code FileLocator} object based on the passed in one whose
     * location is fully defined. This method ensures that all components of the
     * {@code FileLocator} pointing to the file are set in a consistent way. In
     * detail it behaves as follows:
     * <ul>
     * <li>If the {@code FileLocator} has no location (refer to
     * {@link #isLocationDefined(FileLocator)}), result is <b>null</b>.</li>
     * <li>If the {@code FileLocator} has already all components set which
     * define the file, it is returned unchanged. <em>Note:</em> It is not
     * checked whether all components are really consistent!</li>
     * <li>If a source URL is set, the file name and base path are determined
     * based on this URL.</li>
     * <li>Otherwise, an attempt to locate the URL of the file based on the
     * information available is made. A {@code FileLocator} with the resulting
     * information is returned; this may be incomplete if it was not possible to
     * determine the URL.</li>
     * </ul>
     *
     * @param locator the {@code FileLocator} to be completed
     * @return a {@code FileLocator} with a fully initialized location if
     *         possible
     */
    public static FileLocator fullyInitializedLocator(FileLocator locator)
    {
        if (!isLocationDefined(locator))
        {
            return null;
        }

        if (isFullyInitialized(locator))
        {
            // already fully initialized
            return locator;
        }

        if (locator.getSourceURL() != null)
        {
            return fullyInitializedLocatorFromURL(locator);
        }
        return fullyInitializedLocatorFromPathAndName(locator);
    }

    /**
     * Return the location of the specified resource by searching the user home
     * directory, the current classpath and the system classpath.
     *
     * @param fileSystem the FileSystem to use.
     * @param base the base path of the resource
     * @param name the name of the resource
     *
     * @return the location of the resource
     */
    public static URL locate(FileSystem fileSystem, String base, String name)
    {
        if (LOG.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();
            buf.append("ConfigurationUtils.locate(): base is ").append(base);
            buf.append(", name is ").append(name);
            LOG.debug(buf.toString());
        }

        if (name == null)
        {
            // undefined, always return null
            return null;
        }

        // attempt to create an URL directly

        URL url = fileSystem.locateFromURL(base, name);

        // attempt to load from an absolute path
        if (url == null)
        {
            File file = new File(name);
            if (file.isAbsolute() && file.exists()) // already absolute?
            {
                try
                {
                    url = toURL(file);
                    LOG.debug("Loading configuration from the absolute path " + name);
                }
                catch (MalformedURLException e)
                {
                    LOG.warn("Could not obtain URL from file", e);
                }
            }
        }

        // attempt to load from the base directory
        if (url == null)
        {
            try
            {
                File file = constructFile(base, name);
                if (file != null && file.exists())
                {
                    url = toURL(file);
                }

                if (url != null)
                {
                    LOG.debug("Loading configuration from the path " + file);
                }
            }
            catch (MalformedURLException e)
            {
                LOG.warn("Could not obtain URL from file", e);
            }
        }

        // attempt to load from the user home directory
        if (url == null)
        {
            try
            {
                File file = constructFile(System.getProperty("user.home"), name);
                if (file != null && file.exists())
                {
                    url = toURL(file);
                }

                if (url != null)
                {
                    LOG.debug("Loading configuration from the home path " + file);
                }

            }
            catch (MalformedURLException e)
            {
                LOG.warn("Could not obtain URL from file", e);
            }
        }

        // attempt to load from classpath
        if (url == null)
        {
            url = locateFromClasspath(name);
        }
        return url;
    }

    /**
     * Return the path without the file name, for example http://xyz.net/foo/bar.xml
     * results in http://xyz.net/foo/
     *
     * @param url the URL from which to extract the path
     * @return the path component of the passed in URL
     */
    static String getBasePath(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String s = url.toString();
        if (s.startsWith(FILE_SCHEME) && !s.startsWith("file://"))
        {
            s = "file://" + s.substring(FILE_SCHEME.length());
        }

        if (s.endsWith("/") || StringUtils.isEmpty(url.getPath()))
        {
            return s;
        }
        else
        {
            return s.substring(0, s.lastIndexOf("/") + 1);
        }
    }

    /**
     * Extract the file name from the specified URL.
     *
     * @param url the URL from which to extract the file name
     * @return the extracted file name
     */
    static String getFileName(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String path = url.getPath();

        if (path.endsWith("/") || StringUtils.isEmpty(path))
        {
            return null;
        }
        else
        {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }

    /**
     * Tries to convert the specified base path and file name into a file object.
     * This method is called e.g. by the save() methods of file based
     * configurations. The parameter strings can be relative files, absolute
     * files and URLs as well. This implementation checks first whether the passed in
     * file name is absolute. If this is the case, it is returned. Otherwise
     * further checks are performed whether the base path and file name can be
     * combined to a valid URL or a valid file name. <em>Note:</em> The test
     * if the passed in file name is absolute is performed using
     * {@code java.io.File.isAbsolute()}. If the file name starts with a
     * slash, this method will return <b>true</b> on Unix, but <b>false</b> on
     * Windows. So to ensure correct behavior for relative file names on all
     * platforms you should never let relative paths start with a slash. E.g.
     * in a configuration definition file do not use something like that:
     * <pre>
     * &lt;properties fileName="/subdir/my.properties"/&gt;
     * </pre>
     * Under Windows this path would be resolved relative to the configuration
     * definition file. Under Unix this would be treated as an absolute path
     * name.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the file object (<b>null</b> if no file can be obtained)
     */
    static File getFile(String basePath, String fileName)
    {
        // Check if the file name is absolute
        File f = new File(fileName);
        if (f.isAbsolute())
        {
            return f;
        }

        // Check if URLs are involved
        URL url;
        try
        {
            url = new URL(new URL(basePath), fileName);
        }
        catch (MalformedURLException mex1)
        {
            try
            {
                url = new URL(fileName);
            }
            catch (MalformedURLException mex2)
            {
                url = null;
            }
        }

        if (url != null)
        {
            return fileFromURL(url);
        }

        return constructFile(basePath, fileName);
    }

    /**
     * Convert the specified file into an URL. This method is equivalent
     * to file.toURI().toURL(). It was used to work around a bug in the JDK
     * preventing the transformation of a file into an URL if the file name
     * contains a '#' character. See the issue CONFIGURATION-300 for
     * more details. Now that we switched to JDK 1.4 we can directly use
     * file.toURI().toURL().
     *
     * @param file the file to be converted into an URL
     */
    static URL toURL(File file) throws MalformedURLException
    {
        return file.toURI().toURL();
    }

    /**
     * Tries to find a resource with the given name in the classpath.
     *
     * @param resourceName the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     *         cannot be found
     */
    static URL locateFromClasspath(String resourceName)
    {
        URL url = null;
        // attempt to load from the context classpath
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null)
        {
            url = loader.getResource(resourceName);

            if (url != null)
            {
                LOG.debug("Loading configuration from the context classpath (" + resourceName + ")");
            }
        }

        // attempt to load from the system classpath
        if (url == null)
        {
            url = ClassLoader.getSystemResource(resourceName);

            if (url != null)
            {
                LOG.debug("Loading configuration from the system classpath (" + resourceName + ")");
            }
        }
        return url;
    }

    /**
     * Helper method for constructing a file object from a base path and a
     * file name. This method is called if the base path passed to
     * {@code getURL()} does not seem to be a valid URL.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    static File constructFile(String basePath, String fileName)
    {
        File file;

        File absolute = null;
        if (fileName != null)
        {
            absolute = new File(fileName);
        }

        if (StringUtils.isEmpty(basePath) || (absolute != null && absolute.isAbsolute()))
        {
            file = new File(fileName);
        }
        else
        {
            StringBuilder fName = new StringBuilder();
            fName.append(basePath);

            // My best friend. Paranoia.
            if (!basePath.endsWith(File.separator))
            {
                fName.append(File.separator);
            }

            //
            // We have a relative path, and we have
            // two possible forms here. If we have the
            // "./" form then just strip that off first
            // before continuing.
            //
            if (fileName.startsWith("." + File.separator))
            {
                fName.append(fileName.substring(2));
            }
            else
            {
                fName.append(fileName);
            }

            file = new File(fName.toString());
        }

        return file;
    }

    /**
     * Creates a fully initialized {@code FileLocator} based on a URL.
     *
     * @param locator the source {@code FileLocator}
     * @return the fully initialized {@code FileLocator}
     */
    private static FileLocator fullyInitializedLocatorFromURL(
            FileLocator locator)
    {
        return createFullyInitializedLocator(locator, locator.getSourceURL());
    }

    /**
     * Creates a fully initialized {@code FileLocator} based on a base path and
     * file name combination.
     *
     * @param locator the source {@code FileLocator}
     * @return the fully initialized {@code FileLocator}
     */
    private static FileLocator fullyInitializedLocatorFromPathAndName(
            FileLocator locator)
    {
        URL url =
                locate(obtainFileSystem(locator), locator.getBasePath(),
                        locator.getFileName());
        if (url == null)
        {
            return locator;
        }
        return createFullyInitializedLocator(locator, url);
    }

    /**
     * Creates a fully initialized {@code FileLocator} based on the specified
     * URL.
     *
     * @param src the source {@code FileLocator}
     * @param url the URL
     * @return the fully initialized {@code FileLocator}
     */
    private static FileLocator createFullyInitializedLocator(FileLocator src,
            URL url)
    {
        return fileLocator(src).sourceURL(url).fileName(getFileName(url))
                .basePath(getBasePath(url)).create();
    }
}