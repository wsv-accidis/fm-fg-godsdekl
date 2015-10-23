package se.accidis.fmfg.app.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Utils for reading/writing files.
 */
public final class IOUtils {
    private static final int BUFFER_SIZE = 8192;

    private IOUtils() {
    }

    public static String readToEnd(InputStream inputStream) throws IOException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream);

            StringBuilder string = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int numRead;
            while (0 <= (numRead = reader.read(buffer))) {
                string.append(buffer, 0, numRead);
            }

            return string.toString();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static void writeToStream(OutputStream outputStream, String content) throws IOException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(outputStream);
            writer.write(content);
            writer.flush();
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
