package org.tyas.upnp.action;

import org.tyas.http.HttpMessage;
import org.tyas.http.HttpRequest;
import org.tyas.http.HttpResponse;
import org.tyas.upnp.UpnpServiceType;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;

public abstract class ActionMessage
{
	public static final String SOAPACTION = "SOAPACTION";
	public static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	private UpnpServiceType mServiceType;
	private String mActionName;
	private List<Argument> mArgs;

	private ActionMessage(UpnpServiceType type, String name, List<Argument> args) {
		mServiceType = type;
		mActionName = name;
		mArgs = args;
	}

	private ActionMessage(UpnpServiceType type, String name) {
		this(type, name, new ArrayList<Argument>());
	}

	private ActionMessage() {}

	public UpnpServiceType getServiceType() { return mServiceType; }

	public String getActionName() { return mActionName; }

	public int getArgumentListLength() { return mArgs.size(); }

	public String getArgumentName(int idx) { return mArgs.get(idx).mName; }

	public String getArgumentValue(int idx) { return mArgs.get(idx).mValue; }

	public String getArgumentValue(String name) {
		if (name == null) return null;
		
		for (Argument arg: mArgs) {
			if (name.equals(arg.mName)) return arg.mValue;
		}

		return null;
	}

	/** @param doc should be namespace aware */
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
			DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
			factory.setNamespaceAware(true);
			doc = factory
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

	public void writeDocument(OutputStream out) throws TransformerException {
		Document doc = toDocument();
		
		TransformerFactory
			.newInstance()
			.newTransformer()
			.transform(new DOMSource(doc), new StreamResult(out));
	}
	
	public static String getStringByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element elm = ((Element)node);

		Node c = node.getFirstChild();

		if (c == null) return "";

		String s = c.getNodeValue();

		return s == null ? "": s;
	}

	public void sendByHttpRequest(String uri, OutputStream out)
		throws TransformerException, IOException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();

		writeDocument(array);
		
		new HttpRequest.Builder("POST", uri, HttpMessage.VERSION_1_1)
			.putFirst(SOAPACTION, "\"" + getServiceType() + "#" + getActionName() + "\"")
			.send(out, array.toByteArray());
	}

	public void sendByHttpRequest(String uri, Socket sock)
		throws TransformerException, IOException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();

		writeDocument(array);
		
		new HttpRequest.Builder("POST", uri, HttpMessage.VERSION_1_1)
			.setHost(sock.getInetAddress().getHostAddress(), sock.getPort())
			.putFirst(SOAPACTION, "\"" + getServiceType() + "#" + getActionName() + "\"")
			.send(sock.getOutputStream(), array.toByteArray());
	}

	public InputStream sendByHttpRequest(String host, int port, String uri)
		throws IOException, TransformerException
	{
		Socket sock = new Socket(host, port);

		sendByHttpRequest(uri, sock);

		return sock.getInputStream();
	}

	public void sendByHttpResponse(OutputStream out)
		throws TransformerException, IOException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();

		writeDocument(array);
		
		new HttpResponse.Builder(HttpMessage.VERSION_1_1, "200", "OK")
			.send(out, array.toByteArray());
	}

	private static class Argument
	{
		String mName;
		String mValue;

		Argument(String n, String v) {
			mName = n;
			mValue = v;
		}
	}

	public static class Const extends ActionMessage
	{
		private Const() { super(); }
		private Const(Const msg) { super(msg.getServiceType(), msg.getActionName(), ((ActionMessage)msg).mArgs); }
	}

	public static class Builder extends ActionMessage
	{
		public Builder(UpnpServiceType service, String action) {
			super(service, action);
		}

		public Builder add(String name, String value) {
			((ActionMessage)this).mArgs.add(new Argument(name, value));
			return this;
		}
	}

	private static ActionMessage.Const getByNode(Node node) {
		if (node == null) return null;

		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		ActionMessage.Const c = new ActionMessage.Const();

		((ActionMessage)c).mServiceType = UpnpServiceType.getByUrn(node.getNamespaceURI());

		if (((ActionMessage)c).mServiceType == null) return null;

		((ActionMessage)c).mActionName = ((Element)node).getTagName();

		node = node.getFirstChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			((ActionMessage)c).mArgs.add(new Argument(((Element)node).getTagName(), getStringByNode(node)));
		}

		return c;
	}

	/** @param doc should be namespace aware */
	public static ActionMessage.Const getByDocument(Document doc) {
		Node node = getChildByTagNameNS(doc, NS_SOAP, "Envelope");

		node = getChildByTagNameNS(node, NS_SOAP, "Body");

		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return getByNode(node);
			}
		}

		return null;
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

	public static ActionMessage.Const readDocument(InputStream in)
		throws ParserConfigurationException, SAXException, IOException
	{
		ByteArrayOutputStream arrayout = new ByteArrayOutputStream();
		int data;

		while ((data = in.read()) >= 0) arrayout.write(data);

		ByteArrayInputStream arrayin = new ByteArrayInputStream(arrayout.toByteArray());

		DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
		factory.setNamespaceAware(true);
		
		Document doc = factory
			.newDocumentBuilder()
			.parse(arrayin);
					
		return ActionMessage.getByDocument(doc);
	}
}
