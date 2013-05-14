package cydep;

import java.io.IOException;
import java.util.Formatter;

/**
 * Create a browse image from a NEXRAD level III radial product
 */
public final class Main {
    
    private static final String USAGE =
            "Usage: %s CONFIG INPUT OUTPUT\n"
            + "  where  CONFIG  configuration file\n"
            + "         INPUT   input NIDS file\n"
            + "         OUTPUT  output image file\n";
    
    public static void main(String[] args) throws IOException {
        Formatter err = new Formatter(System.err);
        if (args.length != 3) {
            err.format(USAGE, Main.class.getName());
            System.exit(1);
        }
        Configuration config = new Configuration(args[0]);
        Input input = new Input(args[1], config.getDataVariableName(), err);
        Output output = new Output(args[2]);
        Cydep cydep = new Cydep(config, input, output);
        cydep.draw();
    }
}
