package com.rvkb.util.swing

import javax.swing.tree.TreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.DefaultTreeModel

class TreeModelCategory {

    static def eachNodeRecursive(TreeModel self, Closure closure) {
        if (self.root) {
            return self.root.eachNodeRecursive(closure)
        } else {
            return null
        }
    }

    static def eachNodeRecursive(TreeNode self, Closure closure) {
        Enumeration c = self.children()
        def res = closure.call(self)
        if (res) {
            while (c.hasMoreElements() && res) {
                def child = c.nextElement()
                res = child.eachNodeRecursive(closure)
            }
        }
        return res
    }

    static int getNodeCount(TreeModel self) {
        int count = 0
        self.eachNodeRecursive {
            count++
            return true
        }
        return count
    }

    /**
     * Filter the tree model by applying passed closure for each node
     * in the tree. If the closure returns true, then the node is kept.
     * If the closure returns false, then the node is removed (unless it has
     * matching childs)
     */
    static def filter(DefaultTreeModel self, Closure closure) {
        // compute list of not matching nodes
        def notMatching = []
        def matching = []
        self.eachNodeRecursive { node ->
            def matches = closure.call(node)
            if (matches) {
                matching << node
            } else {
                notMatching << node
            }
            return true
        }
//        println "matching: $matching"
//        println "notMatching: $notMatching"
        // now check for each not matching node if it has
        // at least one child matching...
        def notMatchingNoChilds = []
        notMatching.each { node ->
//            println "checking if node $node has at least one matching child"
            def noMatchingChild = node.eachNodeRecursive { child ->
                boolean b = matching.contains(child)
//                println "matching.contains($child) returns $b"
                return !b
            }
//            println "noMatchingChild=$noMatchingChild"
            if (noMatchingChild) {
                notMatchingNoChilds << node
            }
        }
        // and finally remove all nodes that don't match and have no children
        notMatchingNoChilds.each { node ->
            if (node.parent) {
                self.removeNodeFromParent node
            } else {
                self.setRoot null
            }
        }
        return self
    }

}