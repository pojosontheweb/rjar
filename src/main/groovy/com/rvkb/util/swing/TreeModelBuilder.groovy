package com.rvkb.util.swing

import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeModel

class TreeModelBuilder extends BuilderSupport {

    private DefaultTreeModel treeModel
    private DefaultMutableTreeNode rootNode

    boolean debug = false

    def indent = ' ' * 4
    int indentCount = -1

    protected void setParent(parent, child) {
        if (parent instanceof TreeModel) {
            parent = rootNode
        }
        treeModel.insertNodeInto(child, parent, parent.childCount)
    }

    def createNode(name) {
        return createNode(name, null, null)
    }

    def createNode(name, value) {
        return createNode(name, null, value)
    }

    def createNode(name, Map attrs) {
        return createNode(name, attrs, null)
    }

    def createNode(name, Map attrs, value) {
        indentCount++
        def res
        if (name=='model') {
            // we create the root node and model with it
            rootNode = new DefaultMutableTreeNode(value)
            treeModel = new DefaultTreeModel(rootNode)
            res = treeModel
        } else if (name=='node') {
            // create a tree node with passed user object
            res = new DefaultMutableTreeNode(value)
        } else {
            // create a tree node with the name of the pretended
            // method as the user object (a String)
            res = new DefaultMutableTreeNode(name)
        }
        if (debug) {
            println indent*indentCount + res
        }
        return res
    }

    protected void nodeCompleted(Object o, Object o1) {
        indentCount--
    }



}