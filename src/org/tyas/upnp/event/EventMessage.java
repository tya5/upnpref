package org.tyas.upnp.event;

import org.tyas.http.HttpRequest;
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

public abstract class EventMessage
{
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";
	public static final String NS_EVENT = "urn:schemas-upnp-org:event-1-0";

	private PropertySet mPropertySet = new PropertySet();
	private SubscribeId mSid;
	private int mEventKey;

	public Set<String> getVariableNameSet() { return mPropertySet.keySet(); }

	public String getProperty(String variableName) { return mPropertySet.get(variableName); }

	public SubscribeId getSid() { return mSid; }

	public int getEventKey() { return mEventKey; }

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

	public static EventMessage.Const getByHttpRequest(HttpRequest.Input req)
		throws IOException, ParserConfigurationException, SAXException
	{
		EventMessage.Const e = new EventMessage.Const();

		((EventMessage)e).mSid = SubscribeId.getBySid(req.getFirst(SID));
		((EventMessage)e).mEventKey = req.getInt(SEQ, 0);

		PropertySet map = ((EventMessage)e).mPropertySet;
		Document doc;

		{
			ByteArrayOutputStream arrayout = new ByteArrayOutputStream();
			int data;
			while ((data = req.getInputStream().read()) >= 0) arrayout.write(data);

			ByteArrayInputStream arrayin = new ByteArrayInputStream(arrayout.toByteArray());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			doc = factory
				.newDocumentBuilder()
				.parse(arrayin);
		}

		getPropertySetByDocument(doc, ((EventMessage)e).mPropertySet);

		return e;
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

	public interface Base
	{
		SubscribeId getSid();
		int getEventKey();
		Set<String> getVariableNameSet();
		String getProperty(String variableName);
	}

	private static class PropertySet extends HashMap<String,String> {}

	public static class Const extends EventMessage implements Base
	{
	}

	public static class Builder extends EventMessage implements Base
	{
		public EventMessage.Builder putProperty(String variableName, String value) {
			((EventMessage)this).mPropertySet.put(variableName, value); return this;
		}

		public EventMessage.Builder setSid(SubscribeId sid) {
			((EventMessage)this).mSid = sid; return this;
		}

		public EventMessage.Builder setEventKey(int key) {
			((EventMessage)this).mEventKey = key; return this;
		}

		public EventMessage.Builder incrementEventKey() {
			((EventMessage)this).mEventKey++; return this;
		}
	}
}
