package cydep;

import java.io.File;
import java.net.URL;

/**
 * Utilities for dealing with loading test resources
 */
public class Resources {

    static final String DPRNAME = "Level3_KDVN_DPR_20130418_0408";
    static final String DPRNIDS = DPRNAME + ".nids";
    static final String DPRVARNAME = "DigitalInstantaneousPrecipitationRate";

    static URL getResource(String resourceName) {
        return Resources.class.getClassLoader().getResource(resourceName);
    }

    static String getFileResourceAsPathname(String resourceName) {
        final URL resourceURL = getResource(resourceName);
        if ("file".equals(resourceURL.getProtocol())) {
            return resourceURL.getPath();
        } else {
            return null;
        }
    }

    static String getDPRPathname() {
        return getFileResourceAsPathname(DPRNIDS);
    }

    static File getResourceDirectory() {
        return new File(getDPRPathname()).getParentFile();
    }
}
