package net.visualcoding.ts3serverquery;

import java.lang.StringBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * TS3Reader, this class is simply a wrapper over the BufferedReader
 * that overwrites the readLine() method to handle responses
 * from the TS3 Server Query.
 *
 * @author Aldehir Rojas
 */
public class TS3Reader extends BufferedReader {

    /**
     * Constructor that initializes this reader with the given Reader object
     *
     * @param in Reader to read from
     */
    public TS3Reader(Reader in) {
        super(in);
    }

    /**
     * Constructor that initializes this reader with the given Reader object
     * and a buffer of size {@code size}.
     *
     * @param in   Reader to read from
     * @param size Size of the buffer
     */
    public TS3Reader(Reader in, int size) {
        super(in, size);
    }

    /**
     * Reads in a line. A line is considered to be terminated with
     * a linefeed immediately followed by a carriage return.
     *
     * @return A String containing the contents of the line, not including
     *         any line-termination characters, or null if the end of the
     *         stream has been reached.
     * @throws IOException
     */
    public String readLine() throws IOException {
        // Read in the line (up to the linefeed)
        String line = super.readLine();
        
        // If the line is not null (i.e. not the end of the stream),
        // then skip the next character, which will undoubtedly be
        // a carriage return.
        if(line != null) skip(1);

        // return our line
        return line;
    }

}
