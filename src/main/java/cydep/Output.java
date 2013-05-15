package cydep;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

public final class Output {

    private final File file;
    private final ImageWriter writer;

    public Output(String pathname) throws IOException {
        this.file = new File(pathname);
        ImageWriter imageWriter = null;
        int prevLastIndex = pathname.length();
        while (null == imageWriter) {
            int nextLastIndex = pathname.lastIndexOf('.', prevLastIndex);
            String suffix = pathname.substring(nextLastIndex + 1, prevLastIndex);
            prevLastIndex = nextLastIndex;
            Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(suffix);
            if (it.hasNext()) {
                imageWriter = it.next();
            }
        }
        this.writer = imageWriter;
        writer.setOutput(ImageIO.createImageOutputStream(file));
    }

    ImageWriter getImageWriter() {
        return writer;
    }
}
