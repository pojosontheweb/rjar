package com.rvkb.util.jar

class StreamUtil {

    private static final BUFFER_SIZE = 1024

    static int transferStreams(InputStream from, OutputStream to) {
        return transferStreams(from, to, true)

    }

    static int transferStreams(InputStream from, OutputStream to, boolean closeOutputStream) {
        byte[] buf = new byte[BUFFER_SIZE]
        int count = 0
        int c
        while ((c = from.read(buf)) != -1) {
           to.write( buf, 0, c)
           count += c
        }
        to.flush()
        if (closeOutputStream) {
            to.close()
        }
        return count
    }

}