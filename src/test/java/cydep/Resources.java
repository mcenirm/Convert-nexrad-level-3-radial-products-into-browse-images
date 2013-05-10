package cydep;

import java.net.URL;

/**
 * Utilities for dealing with loading test resources
 */
public class Resources {

    public static URL getResource(String resourceName) {
        return Resources.class.getClassLoader().getResource(resourceName);
    }

    public static String getFileResourceAsPathname(String resourceName) {
        final URL resourceURL = getResource(resourceName);
        if ("file".equals(resourceURL.getProtocol())) {
            return resourceURL.getPath();
        } else {
            return null;
        }
    }
}
