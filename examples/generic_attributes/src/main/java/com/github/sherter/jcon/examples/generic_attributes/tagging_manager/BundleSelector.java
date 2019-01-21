package com.github.sherter.jcon.examples.generic_attributes.tagging_manager;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.BundleInstantiationException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Instantiates the selected Tagging Bundle from Configuration File.
 */
public class BundleSelector {
    private static final Logger logger = LoggerFactory.getLogger(BundleSelector.class);

    public static TaggingBundle instantiateBundle(TaggingManager manager, String selectedBundle) throws BundleInstantiationException {
        InputStream in = manager.getClass().getClassLoader()
                .getResourceAsStream("bundles.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        TaggingBundle taggingBundle = null;

        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document document = docBuilder.parse(in);

            Node root = document.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            NodeList bundles = null;

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeName().equals("bundles")) {
                    bundles = node.getChildNodes();
                }
            }

            if (selectedBundle == null || bundles == null) {
                throw new IllegalArgumentException("Configuration file is invalid or delivered Bundle name is incorrect.");
            }

            String bundleClassName = null;

            for (int i = 0; i < bundles.getLength(); i++) {
                Node node = bundles.item(i);
                if (node.getNodeName().equals("bundle")) {
                    NamedNodeMap attributes = node.getAttributes();

                    String name = attributes.getNamedItem("name").getNodeValue();
                    if (name.equals(selectedBundle)) {
                        logger.info("TaggingBundle=" + selectedBundle + " was selected.");
                        bundleClassName = attributes.getNamedItem("class").getNodeValue();

                    }
                }
            }

            Class<?> bundleClass = Class.forName(bundleClassName);
            Constructor<?> constructor = bundleClass.getConstructor(TaggingManager.class);
            taggingBundle = (TaggingBundle) constructor.newInstance(manager);



        } catch (ParserConfigurationException | SAXException | IOException | InstantiationException
                | InvocationTargetException | NoSuchMethodException | IllegalAccessException
                | ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        if (taggingBundle == null)
            throw new BundleInstantiationException();

        return taggingBundle;


    }


}
