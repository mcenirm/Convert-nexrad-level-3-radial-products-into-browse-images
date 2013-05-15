package cydep;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestOutput {

    @Test
    public void testGif() throws IOException {
        Output output = new Output(new File(Resources.getResourceDirectory(), "foo.gif").getPath());
        ImageWriter imageWriter = output.getImageWriter();
        assertNotNull("null image writer", imageWriter);
    }
}