package org.laika.pixpdqadapter;

import java.io.*;
import javax.xml.transform.dom.*;
import javax.xml.validation.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author kananm
 */
public class AuditMessageValidator {

    public static void main(String[] args)
            throws SAXException, IOException, ParserConfigurationException {

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        File schemaLocation = new File("D:/CCHIT/Code/openpixpdq/trunk/OpenPIXPDQ/conf/atna.xsd");
        Schema schema = factory.newSchema(schemaLocation);
        Validator validator = schema.newValidator();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new File("D:/CCHIT/Code/openpixpdq/trunk/OpenPIXPDQ/conf/msg.txt"));

        DOMSource source = new DOMSource(doc);
        DOMResult result = new DOMResult();

        try {
            validator.validate(source, result);
            Document augmented = (Document) result.getNode();

            System.out.println("Message is valid");

            NodeList lst = doc.getElementsByTagName("AuditMessage");
            int totalSection = lst.getLength();

            for (int s = 0; s < totalSection; s++) {
                Node firstSectionNode = lst.item(s);
                Element firstSectionElement = (Element) firstSectionNode;
                // Result
                NodeList obrList = firstSectionElement.getElementsByTagName("EventIdentification");

                NodeList textFNList = null;
                Element firstNameElement = null;

                for (int s1 = 0; s1 < obrList.getLength(); s1++) {
                    firstNameElement = (Element) obrList.item(s1);
                    textFNList = firstNameElement.getChildNodes();

                    //if (((Node) textPLTList.item(0)).getNodeValue().trim().equalsIgnoreCase("RP")) {
                    //listPdfLink.add(((Node) textFNList.item(0)).getNodeValue().trim());


                    System.out.println(((Node) textFNList.item(0)).getNodeValue());
                }
            }

        } catch (SAXException ex) {
            System.out.println("Message is not valid because ");
            System.out.println(ex.getMessage());
        }

    }
}
