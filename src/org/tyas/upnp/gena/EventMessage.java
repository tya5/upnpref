package org.tyas.upnp.gena;

import org.tyas.http.HttpMessage;

import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class EventMessage
{
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";
	public static final String NS_EVENT = "urn:schemas-upnp-org:event-1-0";

	private final PropertySet mPropertySet;
	private final EventMessageHeader mHeader;

	private EventMessage(EventMessageHeader header, PropertySet props) {
		mPropertySet = props;
		mHeader = header;
	}

	public Set<String> getVariableNameSet() { return mPropertySet.keySet(); }

	public String getProperty(String variableName) { return mPropertySet.get(variableName); }

	public EventMessageHeader getHeader() { return mHeader; }

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

		for (Map.Entry<String,String> ent: mPropertySet.entrySet()) {
			Element prop = doc.createElement("e:property");
			pset.appendChild(prop);

			Element v = doc.createElement(ent.getKey());
			v.appendChild(doc.createTextNode(ent.getValue()));

			prop.appendChild(v);
		}

		return doc;
	}

	/*
	public DatagramPacket toDatagramPacket(HttpRequest.Builder req)
		throws IOException, TransformerException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();

		OutputStream out = req
			.putFirst(SID, getSid().toString())
			.putFirst(SEQ, "" + getEventKey())
			.send(array);

		TransformerFactory
			.newInstance()
			.newTransformer()
			.transform(new DOMSource(toDocument()), new StreamResult(out));

		byte[] data = array.toByteArray();

		return new DatagramPacket(data, data.length);
	}
	*/

	public static EventMessage getByHttpRequest(HttpMessage.Input<EventMessageHeader> req, SubscribeId sid)
		throws IOException, ParserConfigurationException, SAXException
	{
		EventMessageHeader e = req.getMessage();

		if ((sid != null) && (! sid.equals(e.getSid()))) return null;


		PropertySet props = new PropertySet();
		{
			Document doc;
			ByteArrayOutputStream arrayout = new ByteArrayOutputStream();
			int data;
			InputStream in = req.getInputStream();
			while ((data = in.read()) >= 0) arrayout.write(data);

			ByteArrayInputStream arrayin = new ByteArrayInputStream(arrayout.toByteArray());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			doc = factory
				.newDocumentBuilder()
				.parse(arrayin);

			getPropertySetByDocument(doc, props);
		}

		return new EventMessage(e, props);
	}

	private static void getPropertySetByDocument(Document doc, PropertySet set) {
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
					set.put(name, value);
					break;
				}
			}
		}
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

	private static class PropertySet extends HashMap<String,String> {}

	public static class Builder
	{
		private final PropertySet mPropertySet = new PropertySet();
		public final EventMessageHeader.Builder mHeaderBuilder;

		public Builder(String deliveryPath) {
			mHeaderBuilder = new EventMessageHeader.Builder(deliveryPath);
		}

		public Builder putProperty(String variableName, String value) {
			mPropertySet.put(variableName, value); return this;
		}

		public EventMessage.Builder setSid(SubscribeId sid) {
			mHeaderBuilder.setSid(sid); return this;
		}

		public EventMessage.Builder setEventKey(int key) {
			mHeaderBuilder.setEventKey(key); return this;
		}
	}
}
