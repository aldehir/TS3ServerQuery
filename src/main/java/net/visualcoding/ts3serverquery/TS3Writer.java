package net.visualcoding.ts3serverquery;

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;

/**
 * TS3Writer class extends the BufferedWriter class and overwrites the
 * {@code newLine()} method to return the line-endings used in the TS3 Server
 * Query. This class also implements a basic {@code writeLine()} method that
 * writes the string, appends the line-ending, and flushes the buffer.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3Writer extends BufferedWriter {

    /**
     * Constructor that initializes this writer with the given Writer object.
     *
     * @param out Writer to write to
     */
    public TS3Writer(Writer out) {
        super(out);
    }

    /**
     * Constructor that initializes this writer with the given Writer object
     * and the size of the buffer to {@code size}.
     *
     * @param out  Writer to write to
     * @param size Size of the buffer
     */
    public TS3Writer(Writer out, int size) {
        super(out, size);
    }

    /**
     * Writes the TS3 Server Query line-ending (line-feed followed by a carriage
     * return) to the buffer.
     *
     * @throw IOException
     */
    public void newLine() throws IOException {
        write("\n\r");
    }
   
    /**
     * Writes the entire line to the buffer, appends the TS3 Server Query line
     * ending, and flushes the buffer.
     *
     * @throw IOException
     */
    public void writeLine(String line) throws IOException {
        write(line);
        newLine();
        flush();
    }

}
