package com.rvkb.util.jar

import java.util.jar.JarFile

class JUCategoryTest extends GroovyTestCase {

    String pathToJar = System.getProperty("path.to.test.jar")

    void testCategory() {
        use(JUJarCategory) {

            JarFile jar = new JarFile(new File(pathToJar))
            def sb

            println "*** eachEntry ***"
            sb = new StringBuffer()
            jar.eachEntry {
                sb << "$it.fullName\n"
            }
            assert sb =~ /mywebapp\.war/

            println "*** eachEntry count ***"
            int count = 0
            jar.eachEntry {
                count++
                return true
            }
            println "count=$count"
            assert count > 0

            println "*** eachEntryRecursive ***"
            sb = new StringBuffer()
            jar.eachEntryRecursive {e ->
                sb << "$e.fullName\n"
            }
            assert sb =~ /test.jsp/

            // count entries recursive
            println "*** eachEntryRecursive count ***"
            int count2 = 0
            jar.eachEntryRecursive {
                count2++
                return true
            }
            assert count2 > count

            // stop iteration
            println "*** stop iteration ***"
            int count3 = 0
            jar.eachEntry {
                count3++
                // return false when more than 2 entries have been traversed already
                return count3 < 2
            }
            assert count3 == 2

            // stop iteration recursive
            println "*** stop iteration recursive with count ***"
            int count4 = 0
            jar.eachEntryRecursive {
                count4++
                return count4 <= 10
            }
            assert count4 == 11

            // stop iteration recursive
            println "*** stop iteration recursive with entry name ***"
            jar.eachEntryRecursive {
                return !(it.fullName.equals("mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/META-INF/MANIFEST.MF"))
            }
            assert count4 == 11
        }
    }
}
