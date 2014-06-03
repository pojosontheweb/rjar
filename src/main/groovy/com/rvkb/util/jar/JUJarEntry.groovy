package com.rvkb.util.jar

import java.util.jar.*

class JUJarEntry {

    JUJarEntry parent
    JarEntry jarEntry
    JarFile jarFile

    static boolean hasJarSuffix(String path) {
        return path =~ /.*\.[jwers]ar$/
    }

    boolean isNestedJar() {
        return hasJarSuffix(jarEntry.name) 
    }

    boolean isDirectory() {
        return jarEntry.isDirectory()
    }

    String getName() {
        return jarEntry.name
    }

    void climbParents(Closure closure) {
        if (parent) {
            closure.call(parent)
            parent.climbParents(closure)
        }
    }

    String getFullName() {
        def parents = []
        climbParents { parents << it }
        def sb = new StringBuffer()
        parents.reverseEach {
            sb.append(it.name)
            sb.append(File.separator)
        }
        sb.append(name)
        return sb.toString()
    }

}