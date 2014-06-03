package com.rvkb.util.jar

import java.util.jar.JarFile
import com.rvkb.util.jar.ui.JarUtilGUI
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * Top-level class, encapsulates the details of JUJarEntry etc.
 */
class JarUtil {

    static final String DISABLE_GLOBAL_OUTPUT_SYSPROP = "rjar.disable.output"
    
    Writer output = new PrintWriter(System.out)
    boolean outputEnabled = true
    JarFile jarFile
    boolean failOnError = false

    private void log(String message) {
        output << "$message\n"
        output.flush()
    }

    private Closure printClosure = { JUJarEntry juJarEntry ->
        if (outputEnabled) {
            output << "$juJarEntry.fullName\n"
        }
        return true
    }

    private void recurse(Closure closure) {
        use(JUJarCategory) {
            jarFile.eachEntryRecursive {
                return closure.call(it)
            }
        }
    }

    void list() {
        recurse(printClosure)
        if (outputEnabled) {
            output.flush()
        }
    }

    void extractAll(String expandTo) {
        File targetDir = new File(expandTo + File.separator + JUJarCategory.getFileNameNoPath(jarFile.name))
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        recurse { juJarEntry ->
            try {
                String fileName = targetDir.absolutePath + File.separator + juJarEntry.fullName
                if (juJarEntry.isDirectory()  || juJarEntry.nestedJar) {
                    new File(fileName).mkdirs()
                    if (outputEnabled) {
                        output << "created folder $fileName"
                        if (juJarEntry.nestedJar) {
                            output << " (nested jar)"
                        }
                        output << "\n"
                        output.flush()
                    }
                } else {
                    // make all intermediate dirs...
                    int i = fileName.lastIndexOf(File.separator)
                    if (i!=-1) {
                        String folder = fileName.substring(0,i)
                        File folderFile = new File(folder)
                        if (!folderFile.exists()) {
                            folderFile.mkdirs()
                            if (outputEnabled) {
                                log "created folder $folder"
                            }
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(fileName)
                    InputStream is = juJarEntry.jarFile.getInputStream( juJarEntry.jarEntry )
                    int count = StreamUtil.transferStreams(is, fos)
                    if (outputEnabled) {
                        log "$count byte(s) written to $fileName"
                    }
                }
                if (outputEnabled) {
                    output.flush()
                }
            } catch(Throwable t) {
                if (outputEnabled) {
                    output << "[ERROR] error while extracting entry $juJarEntry.fullName : $t"
                    t.printStackTrace output
                    output.flush()
                }
                if (failOnError) {
                    throw new RuntimeException(t)
                }
            }
            return true
        }
        if (outputEnabled) {
            output.flush()
        }
    }

    /**
     * Re-creates a JAR file from a RJAR expanded archive. Typical usage is to first
     * expand an archive using RJAR, change some files in the generated folders structure,
     * and then recreate the original archive.
     *
     * @param from the path to the top-level <b>folder</b> of an <b>expanded</b> rjar archive (e.g. the folder /some/path/myear.ear/)
     * @param to the path of the new jar file to create (e.g. pass /other/path : this would generate the jar file /other/path/myear.ear)
     * @return the fully qualified path of the created archive (e.g. /other/path/myear.ear)
     */
    String reCreateRJar(String from, String to) {
        File toDir = new File(to)
        if (toDir.exists()) {
            if (!toDir.isDirectory()) {
                if (outputEnabled) {
                    log "$to isn't a directory, nothing done"
                }
                return null
            }
        } else {
            toDir.mkdirs()
        }
        if (outputEnabled) {
            log "Recreating jar(s) from $from to folder $to"
        }
        // return null if "from" is not a jar file
        if (!JUJarEntry.hasJarSuffix(from)) {
            if (outputEnabled) {
                log "$from doesn't seem to be an expanded jar folder, nothing done"
            }
            return null
        } else {
            // seems to be a Jar, let's start recreating it !
            int indx = from.lastIndexOf(File.separator)
            String jarName = from
            if (indx!=-1) {
                jarName = from.substring(indx + 1)
            }
            String jarFilePath = to + File.separator + jarName
            File f = new File(jarFilePath)
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(f))
            fillJarRecursive(f, from, jos, null)
            jos.close()
            return jarFilePath
        }
    }

    void fillJarRecursive(File f, String jarFolderPath, JarOutputStream os, String relativePath) {
        if (outputEnabled) {
            log "Iterating on files in $jarFolderPath, adding to jar file $f.absolutePath"
        }
        new File(jarFolderPath).eachFile { nestedFile ->
            String fullFileName = nestedFile.absolutePath
            // trim start and get path relative to jar root
            String entryName = fullFileName.substring(jarFolderPath.length())
            if (relativePath!=null) {
                entryName = relativePath + entryName
            }
            if (entryName.startsWith(File.separator)) {
                entryName = entryName.substring(File.separator.length())
            }
            if (outputEnabled) {
                log "Handling entry $entryName (file $fullFileName)"
            }
            if (nestedFile.isFile()) {
                // add new entry
                JarEntry entry = new JarEntry(entryName)
                os.putNextEntry entry
                // write contents of file to jar out stream
                InputStream is = new FileInputStream(fullFileName)
                int transferred = StreamUtil.transferStreams(is, os, false)
                is.close()
                if (outputEnabled) {
                    log "File entry, $transferred byte(s) added in $f.absolutePath"
                }
                os.flush()
            } else {
                // directory is it a nested jar or regular folder ?
                if (JUJarEntry.hasJarSuffix(nestedFile.name)) {
                    // nested jar, recreate it by recursion
                    String nestedTmpFilePath = JUJarCategory.RJAR_TMP_DIR + File.separator + nestedFile.name
                    File nestedTmpFile = new File(nestedTmpFilePath)
                    JarOutputStream osNested = new JarOutputStream(new FileOutputStream(nestedTmpFile))
                    if (outputEnabled) {
                        log "Nested jar folder, re-creating jar in $nestedTmpFilePath"
                    }
                    fillJarRecursive(nestedTmpFile, nestedFile.absolutePath, osNested, null)
                    try {
                        osNested.close()
                    } catch(Exception e) {
                        // let go, just log the error
                        if (output) {
                            log("[ERROR] Unable to close output stream for $nestedTmpFilePath : $e")
                        }
                        if (failOnError) {
                            throw new RuntimeException(e)
                        }
                    }
                    // create entry in parent
                    JarEntry entry = new JarEntry(entryName)
                    os.putNextEntry entry
                    InputStream is = new FileInputStream(nestedTmpFilePath)
                    int transferred = StreamUtil.transferStreams(is, os, false)
                    is.close()
                    if (outputEnabled) {
                        log "Nested jar recreated, added $transferred byte(s) to parent in $f.absolutePath"
                    }
                    os.flush()
                } else {
                    // directory, add entry and recurse in subfolders
                    JarEntry entry = new JarEntry(entryName + File.separator)
                    os.putNextEntry entry
                    fillJarRecursive(f, nestedFile.absolutePath, os, entryName)
                    os.flush()
                }
            }
        }
        if (outputEnabled) {
            log "Recreated jar from $jarFolderPath in $f.absolutePath"
        }
    }

    private static JarUtil createJarUtil(String path2jar) {
        JarUtil ju = new JarUtil(jarFile: new JarFile(new File(path2jar)))
        ju.outputEnabled = !System.getProperty(DISABLE_GLOBAL_OUTPUT_SYSPROP)
        return ju
    }

    public static void main(String[] args) {
        if (args.length==0) {
            usage()
        } else {
            def argsMap = [:]
            for (int i=0 ; i<args.length ; i++) {
                String a = args[i]
                if (a.equals("-h")) {
                    usage()
                } else if (a.equals("t")) {
                    if (i==args.length-1) {
                        println "ERROR : jar file must be specified"
                        break
                    } else {
                        String path2jar = args[i+1]
                        JarUtil ju = createJarUtil(path2jar)
                        ju.list()
                        break
                    }
                } else if (a.equals("x")) {
                    if (args.length-i < 3) {
                        println "ERROR : jar file and target path must be specified"
                        break
                    } else {
                        String path2jar = args[i+1]
                        String targetPath = args[i+2]
                        JarUtil ju = createJarUtil(path2jar)
                        ju.extractAll(targetPath)
                        break
                    }
                } else if (a.equals("r")) {
                    if (args.length-i < 3) {
                        println "ERROR : expanded jar and target path must be specified"
                        break
                    } else {
                        String from = args[i+1]
                        String to = args[i+2]
                        JarUtil ju = new JarUtil()
                        ju.reCreateRJar(from, to);
                        break
                    }
                } else if (a.equals("ui")) {
                    JarUtilGUI gui = new JarUtilGUI()
                    gui.frame.show()
                    // file(s) specified on the command line, open them
                    for (int j=1 ; j<args.length ; j++) {
                        String fileName = args[j]
                        println "opening file $fileName"
                        File f = new File(fileName)
                        gui.openFileInTab f
                    }
                    break
                } else {
                    println "ERROR : unrecognized option $a"
                    break
                }
            }
        }
    }

    private static void usage() {
        println '''
*************************************************
* rjar : recursive jar manipulation tool (v0.5) *
* http://rjar.googlecode.com                    *
*************************************************

Usage :
    rjar t jar_file : list the contents of a jar and nested jars if any
    rjar x jar_file target_path : extract the jar, including nested jars, to the specified folder
    rjar r expanded_rjar_folder target_path : recreate the top-level jar (including all nested jar(s) if any) into specified folder
    rjar ui [jar_file]* : open the graphical front end with specified jar(s) if any 
'''
    }

}