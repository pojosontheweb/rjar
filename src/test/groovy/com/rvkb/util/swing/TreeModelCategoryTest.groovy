package com.rvkb.util.swing

import javax.swing.tree.TreeModel

class TreeModelCategoryTest extends GroovyTestCase {

    def treeModel = new TreeModelBuilderTest().buildSampleTreeModel()

    public void testEachNodeRecursive() {
        use(TreeModelCategory) {
            int count = 0
            treeModel.eachNodeRecursive {
                count++
                return true
            }
            assert count == 10
        }

    }

    public void testFilter1() {
        def filter1Closure = {
            return it.userObject != 'child4'
        }
        use (TreeModelCategory) {
            int count1 = treeModel.getNodeCount()
            treeModel.filter(filter1Closure)
            int count2 = treeModel.getNodeCount()
            assert count2 == count1-1
        }

    }

    public void testFilter2() {
        use (TreeModelCategory) {
            // obtain the test tree model and count nodes
            int count1 = treeModel.getNodeCount()

            // filter the nodes
            // we assert on the count, and
            // also we make sure that nodes that should have been removed
            // are not in the tree any more...

            // compute nodes to remove (leaf nodes only)
            def nodesToRemove = []
            treeModel.eachNodeRecursive { node ->
                if (node.childCount==0) {
                    nodesToRemove << node
                }
                return true
            }
            def expectedCount = count1 - nodesToRemove.size()

            treeModel.filter { node ->
                // removes all nodes that have no childs (leaf nodes)
                return node.childCount > 0
            }
            int count2 = treeModel.getNodeCount()
            assert count2 < count1
            assert expectedCount == count2

            treeModel.eachNodeRecursive { node ->
                assert !(nodesToRemove.contains(node))
                return true
            }
        }
    }

    void testFilter3() {
        use(TreeModelCategory) {
            // only keep leaf node
            treeModel.filter {
               return it.userObject=='child21'
            }
            int count = treeModel.getNodeCount()
            // count = root, child2, child21
            assert count == 3 
        }
    }

    void testFilter4() {
        use(TreeModelCategory) {
            // filter everything but root
            treeModel.filter {
                return !it.parent
            }
            int count = treeModel.getNodeCount()
            assert count == 1
        }
    }

    void testFilter5() {
        use(TreeModelCategory) {
            // filter everything
            treeModel.filter {
                return false
            }
            int count = treeModel.getNodeCount()
            assert count == 0
        }
    }

}