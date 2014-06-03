package com.rvkb.util.swing

import javax.swing.tree.DefaultTreeModel

class TreeModelBuilderTest extends GroovyTestCase {

    void testBuilder() {
        def model = buildSampleTreeModel()
        assert model instanceof DefaultTreeModel
        use (TreeModelCategory) {
            int count = 0
            boolean foundChild4 = false
            model.eachNodeRecursive {
                count++
                if (!foundChild4 && it.userObject == 'child4') {
                    foundChild4 = true
                }
                return true
            }
            assert count == 10
            assert foundChild4
        }
    }

    def buildSampleTreeModel(builder=null) {
        if (!builder) {
            builder = new TreeModelBuilder(debug: true)
        }
        return builder.model('root') {
            node('child1')
            node('child2') {
                node('child21')
                node('child22')
                node('child23')
            }
            node('child3') {
                node('child31')
                node('child32')
            }
            child4()
        }
    }

}