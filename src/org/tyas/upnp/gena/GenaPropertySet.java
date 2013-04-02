package org.tyas.upnp.gena;

import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GenaPropertySet extends HashMap<String,String>
{
	private static final String NS_EVENT = "urn:schemas-upnp-org:event-1-0";

	public byte [] toByteArray() {
		return serialize(toDocument());
	}
	
	private byte [] serialize(Document document) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			
			Transformer transformer = TransformerFactory
				.newInstance()
				.newTransformer();
			
			transformer.transform(new DOMSource(document), new StreamResult(out));
			
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return out.toByteArray();
	}

	public Document toDocument() {
		Document doc = null;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			doc = factory
				.newDocumentBuilder()
				.getDOMImplementation()
				.createDocument(NS_EVENT, "e:propertyset", null);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		
		Element pset = doc.getDocumentElement();
		
		for (Map.Entry<String,String> ent: entrySet()) {
			Element prop = doc.createElement("e:property");
			pset.appendChild(prop);
			
			Element v = doc.createElement(ent.getKey());
			v.appendChild(doc.createTextNode(ent.getValue()));
			
			prop.appendChild(v);
		}
		
		return doc;
	}

	public static GenaPropertySet read(InputStream in) throws IOException, SAXException, ParserConfigurationException {
		Document doc;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setNamespaceAware(true);
		doc = factory
			.newDocumentBuilder()
			.parse(in);
		
		return getPropertySetByDocument(doc);
	}

	private static GenaPropertySet getPropertySetByDocument(Document doc) {
		GenaPropertySet props = new GenaPropertySet();
		Node node = getChildByTagNameNS(doc, NS_EVENT, "propertyset");
		
		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			
			if (! NS_EVENT.equals(node.getNamespaceURI())) continue;
			
			if (! "property".equals(node.getLocalName())) continue;
			
			Node n = node.getFirstChild();
			
			for (; n != null; n = n.getNextSibling()) {
				
				if (node.getNodeType() != Node.ELEMENT_NODE) continue;
				
				String name = ((Element)n).getTagName();
				Node child = n.getFirstChild();
				
				if (child != null) {
					String value = child.getNodeValue();
					props.put(name, value);
					break;
				}
			}
		}
		
		return props;
	}

	public static String getStringByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";
		
		Element elm = ((Element)node);
		
		Node c = node.getFirstChild();
		
		if (c == null) return "";
		
		String s = c.getNodeValue();
		
		return s == null ? "": s;
	}

	private static Node getChildByTagNameNS(Node node, String ns, String tag) {
		if (node == null) return null;
		
		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			
			if ((ns == null) && (node.getNamespaceURI() != null)) continue;
			
			if ((ns != null) && (! ns.equals(node.getNamespaceURI()))) continue;
			
			if (! tag.equals(node.getLocalName())) continue;
			
			return node;
		}
		
		return null;
	}

}
