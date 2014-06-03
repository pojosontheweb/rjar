import com.rvkb.util.jar.* 

class JarUtilMainTest extends GroovyTestCase {

    String pathToJar = System.getProperty("path.to.test.jar")
    String expandTo = System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis()

    void testWithoutArgs() {
        println "Without args :"        
        JarUtil.main(new String[0])
    }

    void testHelp() {
        println "With -h :"
        String[] arguments = new String[1]
        arguments[0] = "-h"
        JarUtil.main(arguments)
    }

    void testListNoFile() {
        println "With t and no file :"
        String[] arguments = new String[1]
        arguments[0] = "t"
        JarUtil.main(arguments)
    }

    void testList() {
        println "With t :"
        String[] arguments = new String[2]
        arguments[0] = "t"
        arguments[1] = pathToJar
        JarUtil.main(arguments)
    }

    void testExpandNoFile() {
        println "With x $pathToJar"
        String[] arguments = new String[2]
        arguments[0] = "x"
        arguments[1] = pathToJar
        JarUtil.main(arguments)
    }

    void testExpand() {
        println "With x $pathToJar $expandTo"
        String[] arguments = new String[3]
        arguments[0] = "x"
        arguments[1] = pathToJar
        arguments[2] = expandTo
        JarUtil.main(arguments)        
    }

}
