package cydep;

import javax.imageio.stream.ImageOutputStream;

public final class Output {

    private final String pathname;

    public Output(String pathname) {
        this.pathname = pathname;
    }

    String getFormatName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ImageOutputStream getImageOutputStream() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
