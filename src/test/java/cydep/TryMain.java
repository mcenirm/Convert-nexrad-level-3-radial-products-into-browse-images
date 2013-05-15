package cydep;

import java.io.IOException;
import org.junit.Test;

public class TryMain {

    @Test
    public void tryDPR() throws IOException {
        String config = Resources.getFileResourceAsPathname("DPR.conf");
        String input = Resources.getDPRPathname();
        String output = input + ".png";
        String[] args = {config, input, output};
        Main.main(args);
    }
}
