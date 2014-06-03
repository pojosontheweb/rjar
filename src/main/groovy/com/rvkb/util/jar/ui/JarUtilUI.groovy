package com.rvkb.util.jar.ui;

import java.awt.BorderLayout as BL
import javax.swing.WindowConstants as WC

import groovy.swing.SwingBuilder
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer
import javax.swing.JLabel
import java.awt.Component
import javax.swing.Icon
import javax.swing.ImageIcon
import com.rvkb.util.jar.ui.JUTreeModel
import java.util.jar.JarFile
import com.rvkb.util.jar.ui.JURootNode
import com.rvkb.util.jar.ui.JUTreeNode
import com.rvkb.util.swing.TreeModelCategory
import java.awt.Color

class JarUtilGUI {

    private Object lock = new Object()

    def swing = new SwingBuilder()

    def quit = swing.action(
        name: 'Quit',
        closure: { System.exit(0) },
        mnemonic: 'Q',
        accelerator: 'ctrl Q'
    )

    def open = swing.action(
        name: 'Open',
        mnemonic: 'O',
        accelerator: 'ctrl O',
        closure: this.&openFile
    )

    def close = swing.action(
        name: 'Close',
        mnemonic: 'C',
        accelerator: 'ctrl C',
        closure: this.&closeTab
    )

    def about = swing.action(
        name: 'About',
        mnemonic: 'A',
        accelerator: 'ctrl A',
        closure: this.&showAbout
    )

    def frame = swing.frame(
            title : "rjar - recursive jar manipulation tool",
            defaultCloseOperation: WC.EXIT_ON_CLOSE,
            size: [600,400]
            ) {
        menuBar {
            menu('File', mnemonic: 'F') {
                menuItem(action: open)
                menuItem(action: close)
                separator()
                menuItem(action: quit)
            }
            menu('Help', mnemonic: 'H') {
                menuItem(action: about)
            }
        }
        panel(layout: new BL()) {
            tabbedPane(constraints: BL.CENTER, id: 'tabs')
            label(id:'status',constraints: BL.SOUTH, text:'Welcome to rjar ! Use menu to open a java archive...')
        }
    }

    void openFile(event) {
        def fileChooser = swing.fileChooser(fileFilter: new JarFileChooserFilter())
        if (fileChooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile()
            openFileInTab f
        }
    }

    void openFileInTab(File f) {
        frame.enabled = false
        Thread.start {
            synchronized(lock) {
                swing.status.text = "Opening $f.name ..."
                def tab = swing.panel(layout: new BL()) {
                    panel(layout: new BL(), constraints: BL.NORTH) {
                        label(text: 'Filter entries (regexp) : ', constraints: BL.WEST)
                        def tf = textField(
                            id: "tf_$f.name",
                            constraints: BL.CENTER,
                            actionPerformed : { loadTree(f) }
                        )
                        panel(constraints: BL.EAST) {
                            button(
                                text: 'filter',
                                actionPerformed : { loadTree(f) }
                            )
                            button(
                                text: 'clear',
                                actionPerformed: { evt ->
                                    tf.text = ''
                                    loadTree(f)
                                }
                            )
                        }
                    }
                    panel(layout: new BL(), constraints: BL.CENTER) {
                        scrollPane() {
                            tree(id: "tree_$f.name", cellRenderer: new JarTreeRenderer(), model: loadModel(f))
                        }
                    }
                }
                swing.tabs.addTab(f.name, tab)
                swing.tabs.setSelectedComponent(tab)
                swing.status.text = "Archive opened successfully"
                frame.enabled = true
            }
        }        
    }

    def statusListenerClosure = { entry ->
        swing.status.text = entry.fullName
    }

    void loadTree(File f) {       
        def tf = swing."tf_$f.name"
        def tree = swing."tree_$f.name"
        // refresh tree model
        JUTreeModel treeModel = loadModel(f)
        // filter if needed
        if (tf.text) {
            use (TreeModelCategory) {
                treeModel.filter {
                    return it.name =~ tf.text
                }
            }
        }
        tree.model = treeModel
        swing.status.text = "Archive content reloaded"
    }

    JUTreeModel loadModel(File f) {
        def treeModel = new JUTreeModel(new JarFile(f), [statusListenerClosure])
        treeModel.loadFromJar()
        return treeModel
    }

    void closeTab(event) {
        def c = swing.tabs.getSelectedComponent()
        if (c) {
            swing.tabs.remove(c)
            swing.status.text = "Archive closed"
        }
    }

    void showAbout(event) {
        JOptionPane.showMessageDialog(frame,
'''rjar : recursive java archive manipulation tool
http://rjar.googlecode.com''')
    }

    public static void main(String[] args) {
        JarUtilGUI gui = new JarUtilGUI()
        gui.frame.show()        
    }


}

class JarFileChooserFilter extends FileFilter {

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true
        } else {
            return file.name =~ /.*\.[jwear]ar/            
        }
    }

    public String getDescription() {
        return 'Java Archive (jar, war, ear, rar)'
    }

}


class JarTreeRenderer extends JLabel implements TreeCellRenderer {

    static final Icon ICON_TREE_JAR     = new ImageIcon(JarUtilGUI.class.getResource('/com/rvkb/util/jar/ui/jar_obj.png'))
    static final Icon ICON_TREE_FILE    = new ImageIcon(JarUtilGUI.class.getResource('/com/rvkb/util/jar/ui/file_obj.png'))
    static final Icon ICON_TREE_FOLDER    = new ImageIcon(JarUtilGUI.class.getResource('/com/rvkb/util/jar/ui/folder_closed.gif'))

    static final Color COLOR_ODD = Color.WHITE;
    static final Color COLOR_EVEN = Color.LIGHT_GRAY;
    static final Color COLOR_SELECTED = Color.BLUE;

    public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (o instanceof JURootNode) {
            setText(o.name)
            setIcon ICON_TREE_JAR
        } else if (o instanceof JUTreeNode) {
            setText(o.name)
            if (o.entry.nestedJar) {
                setIcon ICON_TREE_JAR
            } else {
                setIcon ICON_TREE_FILE
            }
        }
        // TODO color doesn't work :-/
        if (selected) {
            setBackground COLOR_SELECTED
        } else {
            if (row % 2 == 0) {
                setBackground COLOR_EVEN
            } else {
                setBackground COLOR_ODD
            }
        }
        return this
    }

}