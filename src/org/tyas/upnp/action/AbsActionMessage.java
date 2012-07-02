package org.tyas.upnp.action;

import org.tyas.upnp.UpnpServiceType;

import org.w3c.dom.*;
import javax.xml.parsers.*;

public abstract class AbsActionMessage
{
	public static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	public abstract UpnpServiceType getServiceType();

	public abstract String getActionName();

	public abstract int getArgumentListLength();

	public abstract String getArgumentName(int idx);

	public abstract String getArgumentValue(int idx);

	public abstract  String getArgumentValue(String name);

	public Element toElement(Document doc) {
		Element elm = doc.createElementNS(getServiceType().toString(), getActionName());

		for (int ii = 0; ii < getArgumentListLength(); ii++) {
			String name = getArgumentName(ii);
			String value = getArgumentValue(ii);
			Element a = doc.createElement(name);
			a.appendChild(doc.createTextNode(value));
			elm.appendChild(a);
		}

		return elm;
	}

	public Document toDocument() {
		Document doc;
		try {
			doc = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.getDOMImplementation()
				.createDocument(NS_SOAP, "s:Envelope", null);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		doc.setDocumentURI(NS_SOAP);

		Element env = doc.getDocumentElement();
		env.setAttributeNS(NS_SOAP, "s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
		
		Element body = doc.createElement("s:Body");
		
		body.appendChild(toElement(doc));
		env.appendChild(body);

		return doc;
	}
	
	public static String getStringByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element elm = ((Element)node);

		Node c = node.getFirstChild();

		if (c == null) return "";

		String s = c.getNodeValue();

		return s == null ? "": s;
	}
}
