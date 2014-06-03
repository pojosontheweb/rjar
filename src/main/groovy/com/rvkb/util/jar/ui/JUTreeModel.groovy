package com.rvkb.util.jar.ui

import javax.swing.tree.DefaultTreeModel
import java.util.jar.JarFile
import javax.swing.tree.DefaultMutableTreeNode
import com.rvkb.util.jar.JUJarCategory
import com.rvkb.util.jar.JUJarEntry

class JUTreeModel extends DefaultTreeModel {

    JarFile jarFile
    def entriesAndNodes

    // create the tree model for passed jar file
    public JUTreeModel(JarFile jarFile, def closures = null) {
        super(new JURootNode(jarFile))
        this.jarFile = jarFile
        loadFromJar(closures)
    }                     

    void loadFromJar(def closures = null) {
        // clear all data if any
        def toBeRemoved = []
        root.children.each { toBeRemoved << it }
        toBeRemoved.each { removeNodeFromParent it }
        entriesAndNodes = [:]
        // populate the tree model 
        use(JUJarCategory) {
            jarFile.eachEntryRecursive { entry ->
                // create a node for this new entry
                def treeNode = new JUTreeNode(entry)
                entriesAndNodes[entry] = treeNode
                def parentTreeNode
                if (entry.parent) {
                    // grab parent (should have been already created)
                    parentTreeNode = entriesAndNodes[entry.parent]
                } else {
                    // add to root
                    parentTreeNode = root
                }
                insertNodeInto(treeNode, parentTreeNode, parentTreeNode.childCount)
                closures.each { closure ->
                    closure.call(entry)
                }
            }
        }        
    }
    
}

class JUBaseNode extends DefaultMutableTreeNode {

    public JUBaseNode(Object o) {
        super(o)
    }

    void eachNodeRecurse(Closure closure) {
        closure.call(this)
        children.each {
            it.eachNodeRecurse closure
        }
    }

}

class JURootNode extends JUBaseNode {

    public JURootNode(JarFile jarFile) {
        super(jarFile)
    }

    JarFile getJarFile() {
        return (JarFile)userObject
    }

    String getName() {
        return JUJarCategory.getFileNameNoPath(jarFile.name)
    }

     String toString() {
        return "[JURootNode $name]"
    }
}

class JUTreeNode extends JUBaseNode {

    boolean matched

    public JUTreeNode(JUJarEntry juJarEntry) {
        super(juJarEntry)
    }

    JUJarEntry getEntry() {
        return (JUJarEntry)getUserObject()
    }

    String getName() {
        return entry.name
    }

    String toString() {
        return "[JUTreeNode $name]"
    }


}


