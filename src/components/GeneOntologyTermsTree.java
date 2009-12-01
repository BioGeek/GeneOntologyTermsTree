/**
 * @author: Jeroen Van Goey
 *
 * A java program that uses an XML file with GO-terms (downloadable on
 * http://archive.geneontology.org/latest-full/go_200911-termdb.obo-xml.gz),
 * and presents the information in this file as a JTree, that consists of
 * - level 1: all terms (<term>) in the file, to be visualized by their name (<name>)
 * - level 2: shows below each term the corresponding <is_a> references as subnodes,
 * visualized by their GO-identifiers. On double-click on a <is_a> reference the
 * corresponding <term> in the JTree is put in focus.
 *
 */
package components;

/**
 * This application requires the following additional file:
 *   src/components/go_200911-termdb.obo-xml
 */
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GeneOntologyTermsTree extends JPanel
        implements TreeSelectionListener {

    private JTree tree;
    private static boolean DEBUG = false;
    private static String lineStyle = "Horizontal";
    private static boolean useSystemLookAndFeel = false;
    private static Document dom;
    private static Map myGOTerms = new HashMap();
    private static final long serialVersionUID = 7526472295622776147L;

    public static class GOTerm {

        private String name;
        private String id;
        private LinkedList<String> multipleIs_aReferences = new LinkedList<String>();

        public GOTerm(String id, String name, LinkedList<String> multipleIs_aReferences) {
            this.id = id;
            this.name = name;
            for (String is_a: multipleIs_aReferences) {
                this.multipleIs_aReferences.add(is_a);

            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public LinkedList<String> getMultipleIs_aReferences() {
            return multipleIs_aReferences;
        }

        public void setMultipleIs_aReferences(LinkedList<String> multipleIs_aReferences) {
            this.multipleIs_aReferences = multipleIs_aReferences;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("GO term Details:");
            sb.append("\nName: " + getName());
            for (String is_a: multipleIs_aReferences) {
                sb.append("\nIs a: " + is_a);
            }
            return sb.toString();

        }
    }

    public GeneOntologyTermsTree() {
        super(new GridLayout(1, 0));

        //Create the nodes.
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("Gene Ontology terms");
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", lineStyle);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);


        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);


        Dimension minimumSize = new Dimension(200, 100);
        treeView.setMinimumSize(minimumSize);
        add(treeView);
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {

            // put in focus
            String go_id = (String) nodeInfo;
            LinkedList ll = (LinkedList) myGOTerms.get(go_id);
            searchTree(tree, tree.getPathForRow(0), (String) ll.getFirst());
        }

        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }

    private static void searchTree(JTree tree, TreePath path, String q) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (node == null) {
            return;
        }
        if (node.toString().equals(q)) {
            tree.addSelectionPath(path);
        }
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            java.util.Enumeration e = node.children();
            while (e.hasMoreElements()) {
                searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
            }
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode nameNode = null;
        DefaultMutableTreeNode is_aNode = null;

        Iterator it = myGOTerms.values().iterator();
        while (it.hasNext()) {
            LinkedList ll = (LinkedList) it.next();
            nameNode = new DefaultMutableTreeNode(ll.getFirst());
            top.add(nameNode);
            LinkedList multipleIs_aReferences = (LinkedList) ll.getLast();
            for (Object is_a: multipleIs_aReferences) {
                is_aNode = new DefaultMutableTreeNode(is_a);
                nameNode.add(is_aNode);
            }

        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame("Gene Ontology terms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new GeneOntologyTermsTree());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    private static LinkedList<String> getTextValues(Element ele, String tagName) {
        LinkedList<String> textVals = new LinkedList<String>();
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String is_a = el.getFirstChild().getNodeValue();
                textVals.add(is_a);

            }

        }
        return textVals;
    }

    private static GOTerm getGOTerm(Element goTermEl) {

        //for each <term> element get text values of
        //id, name and is_a's.
        String id = getTextValue(goTermEl, "id");
        String name = getTextValue(goTermEl, "name");
        LinkedList<String> multipleIs_aReferences = getTextValues(goTermEl, "is_a");

        //Create a new GOTerm with the value read from the xml nodes
        GOTerm t = new GOTerm(id, name, multipleIs_aReferences);

        return t;
    }

    private static void parseDocument() {
        //get the root elememt
        Element docEle = dom.getDocumentElement();

        //get a nodelist of <term> elements
        NodeList nl = docEle.getElementsByTagName("term");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {

                //get the term element
                Element el = (Element) nl.item(i);

                //get the GOTerm object
                GOTerm t = getGOTerm(el);

                LinkedList value = new LinkedList();
                value.add(t.getName());
                value.add(t.getMultipleIs_aReferences());
                myGOTerms.put(t.getId(), value);
            }
        }

    }

    private static void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse("src/components/go_200911-termdb.obo-xml");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private static void printData() {

        System.out.println("No of GO Terms '" + myGOTerms.size() + "'.");

        Iterator it = myGOTerms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());

        }
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //parse the xml file and get the dom object
                parseXmlFile();

                //get each term element and create a GOTerm object
                parseDocument();

                if (DEBUG) {
                    printData();
                }

                createAndShowGUI();
            }
        });
    }
}
