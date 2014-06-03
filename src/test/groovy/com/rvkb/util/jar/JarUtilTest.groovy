import com.rvkb.util.jar.JarUtil
import java.util.jar.JarFile
import com.rvkb.util.jar.JUJarCategory
import java.util.jar.JarOutputStream
import java.util.jar.JarEntry
import com.rvkb.util.jar.StreamUtil

class JarUtilTest extends GroovyTestCase {

    String pathToJar = System.getProperty("path.to.test.jar")
    String pathToJar2 = System.getProperty("path.to.test.jar2")

    void testList() {
        println "pathToJar=$pathToJar"
        
        JarFile jarFile = new JarFile(new File(pathToJar))
        StringWriter writer = new StringWriter()
        JarUtil jarUtil = new JarUtil(jarFile:jarFile, output: writer,failOnError:true)
        jarUtil.list()
        def s = writer.toString()
        assert s =~ /META-INF/
        assert s =~ /MANIFEST\.MF/
        assert s =~ /mywebapp\.war/
        assert s =~ /mywebapp\.war/
        assert s =~ /HtmlSelectManyListboxTag/        
    }

    String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    void testExpand() {
        String expandDir = getTmpDir() + File.separator + 'toto'
        try {
            JarFile jar = new JarFile(new File(pathToJar))
            JarUtil jarUtil = new JarUtil(jarFile: jar, outputEnabled: true,failOnError: true)
            jarUtil.extractAll expandDir
            // make sure our files have been extracted
            File jarDir = new File(expandDir + File.separator + JUJarCategory.getFileNameNoPath(jar.name))
            assert jarDir.exists()
            assert jarDir.isDirectory()
            boolean foundManifest = false
            jarDir.eachFileRecurse {
                if (it.name =~ /MANIFEST.MF/) {
                    if (!foundManifest) {
                        foundManifest = true
                    }
                }
            }
            assert foundManifest
            File mywebappDir = new File(jarDir.absolutePath + File.separator + "mywebapp.war")
            println "webapp dir = $mywebappDir.absolutePath"
            assert mywebappDir.exists()
            assert mywebappDir.isDirectory()
            File webxml = new File(mywebappDir.absolutePath + File.separator + "WEB-INF" + File.separator +
                    "web.xml")
            println "webxml = $webxml.absolutePath"
            assert webxml.exists()
        } finally {
            new File(expandDir).delete()
        }
    }

    void testReCreateRJar() {
        // first off, let's unpack the test jar for our tests
        JarFile j = new JarFile(new File(pathToJar2))
        JarUtil ju = new JarUtil(jarFile:j, failOnError:true)
        def tmpDir = getTmpDir() + File.separator + 'test-recreated'
        try {
            File tmpDirFile = new File(tmpDir)
            if (!tmpDirFile.exists()) {
                tmpDirFile.mkdirs();
            }
            ju.extractAll(tmpDir)

            // ok now repack this and compare two files
            def pathToExpandedRJar = tmpDir + File.separator + 'top.jar'
            def resultPath = tmpDir + File.separator + 'result'
            def pathToCreatedJar = ju.reCreateRJar(pathToExpandedRJar, resultPath);
            assert pathToCreatedJar != null
            diffArchives(pathToJar2, pathToCreatedJar)
        } finally {
            new File(tmpDir).delete()
        }
    }

    String entriesAsList(String jarFileName) {
        JarFile jf = new JarFile(jarFileName)
        StringWriter out = new StringWriter()
        JarUtil ju = new JarUtil(jarFile: jf, output: out,failOnError:true)
        ju.list()
        return out.toString()        
    }

    void diffArchives(String jar1, String jar2) {
      // we check that we have the same entries independently of order and separators
      JarFile jf1 = new JarFile(new File(jar1));
      JarFile jf2 = new JarFile(new File(jar2));
      def entries1 = []
      def entries2 = []
      use(JUJarCategory) {
        jf1.eachEntryRecursive {
          entries1 << removeSeparators(it.fullName)
        }
        jf2.eachEntryRecursive {
          entries2 << removeSeparators(it.fullName)
        }
      }
      assert entries1.size() == entries2.size()
      entries1.each {
        assert entries2.contains(it)
      }
    }

    private String removeSeparators(String s) {
      return s.replaceAll("/", "-").replaceAll("\\\\", "-")
    }
        
    public void testJarWithNoIntermediateDirs() {
        // create a jar file with no intermediate entries
        String tmpJarFilePath = getTmpDir() + File.separator + 'tmpJar.jar'
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmpJarFilePath))
        String entryName = "A" + File.separator + "B" + File.separator + "test"
        (1..5).each {
            String s = entryName + it + ".txt"
            JarEntry entry = new JarEntry(s)
            println "Adding entry $s"
            jos.putNextEntry entry
            byte[] entryData = "data$it".getBytes()
            jos.write entryData
            jos.closeEntry()
        }
        jos.close()
        println "Written jar : $tmpJarFilePath"

        // put the previous jar inside another jar with sub-folders
        String tmpJarFilePath2 = getTmpDir() + File.separator + 'tmpJar2.jar'
        JarOutputStream jos2 = new JarOutputStream(new FileOutputStream(tmpJarFilePath2))
        jos2.putNextEntry(new JarEntry("X" + File.separator + "Y" + File.separator + 'tmpJar.jar'))
        StreamUtil.transferStreams(new FileInputStream(tmpJarFilePath), jos2, false)
        jos2.closeEntry()
        jos2.flush()
        jos2.close()

        // try to expand (throws exception on error)
        JarUtil jarUtil = new JarUtil(jarFile : new JarFile(new File(tmpJarFilePath2)), failOnError : true)
        String expandDir = getTmpDir() + File.separator + "tmpJarExpanded"
        println "Expanding $tmpJarFilePath2 in $expandDir"
        jarUtil.extractAll expandDir

        // make sure files are there...
        int c = 0
        new File(expandDir + File.separator + "tmpJar2.jar").eachFileRecurse {
            if (!it.isDirectory()) {
                println "Found entry $it.absolutePath"
                c++
                assert it.name =~ /test/
            }
        }
        assert c == 5
    }


}