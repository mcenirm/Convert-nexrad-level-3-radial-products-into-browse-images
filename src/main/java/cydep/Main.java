package cydep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Create a browse image from a NEXRAD level III radial product
 */
public class Main {
    
    public static void main(String[] args) throws IOException {
        Cydep.drawToKml(args[0], new FileOutputStream(new File(args[1])));
    }
}
