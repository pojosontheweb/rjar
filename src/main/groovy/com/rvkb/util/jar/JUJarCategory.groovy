package com.rvkb.util.jar

import java.util.jar.*

class JUJarCategory {

    /*
     * Utility methods and stuff...
     */

    public static final String RJAR_TMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + 'rjar'

    static String getFileNameNoPath(absolute) {
        int lastSlashIndex = absolute.lastIndexOf(File.separator)
        if (lastSlashIndex != -1) {
            return absolute.substring(lastSlashIndex + 1)
        } else {
            return absolute
        }
    }

    /*
     * JarFile enhancements
     */

    static def eachEntry(JarFile self, Closure closure) {
        Enumeration e = self.entries()
        boolean stop = false
        while(e.hasMoreElements() && !stop) {
            JarEntry entry = e.nextElement()
            def jue = new JUJarEntry(jarEntry:entry, jarFile: self)
            def res = closure.call(jue)
            if (res!=null && !res) {
                stop = true
            }
        }
    }

    static def eachEntryRecursive(JarFile self, Closure closure) {
        self.eachEntryRecursive(null, closure)
    }

    static def eachEntryRecursive(JarFile self, JUJarEntry parentEntry, Closure closure) {
        boolean stopped = false
        self.eachEntry { entry ->
            entry.parent = parentEntry

            // call passed closure and exit if it returns false
            stopped = !(closure.call(entry))
            if (stopped) {
                return false
            } else {
                // is the entry a nested jar ?
                if (entry.nestedJar) {                    
                    // nested jar !
                    // unpack...
                    File tmpDirFile = new File(RJAR_TMP_DIR)
                    if (!tmpDirFile.exists()) {
                        tmpDirFile.mkdirs()
                    }
                    String fileName = RJAR_TMP_DIR +
                            File.separator +
                            System.currentTimeMillis() +
                            '-' +
                            getFileNameNoPath(entry.name);
                    // special handling for windows !
                    int i = fileName.indexOf('\\')
                    if (i!=-1) {
                        // windows ! replace all / by \
                        fileName = fileName.replace('/', '\\')
                    }
                    // create intermediary dirs if needed
                    i = fileName.lastIndexOf(File.separator)
                    if (i!=-1) {
                        String filePath = fileName.substring(0, i);
                        File f = new File(filePath)
                        f.mkdirs()
                    }
                    File tmpFile = new File(fileName)
                    try {
                        OutputStream os = new FileOutputStream(tmpFile)
                        InputStream is = self.getInputStream(entry.jarEntry)
                        int count = StreamUtil.transferStreams(is, os)
                        // invoke on nested jar
                        JarFile nestedJar = new JarFile(tmpFile)
                        stopped = !nestedJar.eachEntryRecursive(entry, closure)
                    } finally {
                        tmpFile.delete()
                    }
                }
                return !stopped
            }
        }
        return !stopped
    }

}
