package net.visualcoding.ts3serverquery;

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;

public class TS3Writer extends BufferedWriter {

    public TS3Writer(Writer out) {
        super(out);
    }

    public TS3Writer(Writer out, int size) {
        super(out, size);
    }

    public void newLine() throws IOException {
        write("\n\r");
    }

}
