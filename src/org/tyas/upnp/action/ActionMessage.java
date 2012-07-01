package org.tyas.upnp.action;

public abstract class ActionMessage
{
	public static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	public static Document toDocument(ActionMessage act) {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		Element env = doc.createElementNS(NS_SOAP, "Envelope");
		env.setAttributeNS(NS_SOAP, "encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");

		Element body = doc.createElementNS(NS_SOAP, "Body");

		body.appendChild(act.toElement(doc));
		env.appendChild(body);
		doc.appendChild(env);

		return doc;
	}

	public abstract Element toElement(Document doc);

	public Document toDocument() {
		return toDocument(this);
	}
}
