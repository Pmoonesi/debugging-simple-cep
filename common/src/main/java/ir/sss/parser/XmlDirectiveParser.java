package ir.sss.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class XmlDirectiveParser {

    public static String parse(String path) {

        // Load and clean the XML content
        String xmlContent = null;
        try {
            xmlContent = readFileToString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String cleanedXmlContent = cleanXmlContent(xmlContent);

        // Parse the cleaned XML content
        Document document = null;
        try {
            document = parseXmlString(cleanedXmlContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Start DFS from the root element
        Element root = document.getDocumentElement();

        if (!root.getNodeName().equals("directive")) {
            System.out.println("not a directive! exiting ...");
            return "";
        }

        bfs(root);
//
//        try {
//            System.out.println(convertTreeToXML(root));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        return dfs(root, root, 1);
    }

    public static void bfs(Node node) {

        if (node.getChildNodes().getLength() == 0) return;

        Queue<Node> nodesQueue = new LinkedList<>();

        nodesQueue.add(node);

        int nodeIdCounter = 0;

        while (!nodesQueue.isEmpty()) {
            Node current = nodesQueue.poll();


            if (current.getNodeName().equals("rule")) {
                ((Element) current).setAttribute("rid", String.valueOf(nodeIdCounter));
                nodeIdCounter += 1;
            }

            NodeList children = current.getChildNodes();

            if (children.getLength() == 1 && children.item(0).getNodeName().equals("rules")) {
                current = children.item(0);
                children = current.getChildNodes();
            }

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    nodesQueue.add(child);
                }
            }

        }
    }

    // DFS function
    public static String dfs(Node root, Node node, int level) {
        StringBuilder rulesString = new StringBuilder();

        Node parent = node;

        // Process child nodes
        NodeList children = parent.getChildNodes();

        if (children.getLength() == 1 && children.item(0).getNodeName().equals("rules")) {
            parent = children.item(0);
            children = parent.getChildNodes();
        }

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            // some function
            String temp;
            temp = createRule((Element) root, (Element) node, (Element) child, level);
            rulesString.append(temp);
            rulesString.append("\n");
            temp = dfs(root, child, level + 1);
            rulesString.append(temp);
            rulesString.append("\n");
        }

        return rulesString.toString();
    }

    private static String createRule(Element root, Element parent, Element child, int level) {
        if (parent.equals(root)) {

            ScenarioRule scenarioRuleInit;
            scenarioRuleInit =
                    new ScenarioRuleInit.ScenarioRuleInitBuilder(root.getAttribute("id"), root.getAttribute("name"), Integer.valueOf(root.getAttribute("priority")),
                            child.getAttribute("rid"), level, Integer.valueOf(child.getAttribute("reliability")), Integer.valueOf(child.getAttribute("occurrence")), 0,
                            child.getAttribute("from"), child.getAttribute("to"), child.getAttribute("port_from"), child.getAttribute("port_to"), child.getAttribute("plugin_id"), child.getAttribute("plugin_sid")).build();
            return scenarioRuleInit.getRule();

        } else {

            int prevRuleTimeout = 0;
            try {
                prevRuleTimeout = Integer.parseInt(parent.getAttribute("time_out"));
            } catch (NumberFormatException ignored) {}

            ScenarioRule scenarioRuleLevelUp;
            scenarioRuleLevelUp =
                    new ScenarioRuleLevelUp.ScenarioRuleLevelUpBuilder(root.getAttribute("id"), root.getAttribute("name"), Integer.valueOf(root.getAttribute("priority")),
                            child.getAttribute("rid"), level, Integer.valueOf(child.getAttribute("reliability")), Integer.valueOf(child.getAttribute("occurrence")), Integer.valueOf(child.getAttribute("time_out")),
                            child.getAttribute("from"), child.getAttribute("to"), child.getAttribute("port_from"), child.getAttribute("port_to"), child.getAttribute("plugin_id"), child.getAttribute("plugin_sid"))
                            .setPreviousRule(parent.getAttribute("rid"), level - 1, Integer.valueOf(parent.getAttribute("reliability")), Integer.valueOf(parent.getAttribute("occurrence")), prevRuleTimeout,
                                    parent.getAttribute("from"), parent.getAttribute("to"), parent.getAttribute("port_from"), parent.getAttribute("port_to"), parent.getAttribute("plugin_id"), parent.getAttribute("plugin_sid"))
                            .build();
            return scenarioRuleLevelUp.getRule();

        }
    }

    // Read file content into a String
    private static String readFileToString(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    // Clean up XML content: trim and normalize whitespace
    private static String cleanXmlContent(String xmlContent) {
        // Replace multiple whitespace characters with a single space
        String cleanedContent = xmlContent.replaceAll("\\s+", " ").trim();
        // Optional: Remove spaces before and after XML tags
        cleanedContent = cleanedContent.replaceAll(">\\s+<", "><");
        return cleanedContent;
    }

    // Parse cleaned XML content into a Document
    private static Document parseXmlString(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlContent));
        return builder.parse(is);
    }

    public static String convertTreeToXML(Node root) throws Exception {
        // Initialize a TransformerFactory
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Create a DOMSource from the root node
        DOMSource domSource = new DOMSource(root);

        // Prepare the output result
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        // Transform the DOM tree to XML
        transformer.transform(domSource, result);

        // The XML string is now available in the writer
        return writer.toString();
    }

}
