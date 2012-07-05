package org.tyas.upnp.action;

import org.tyas.http.*;
import org.tyas.upnp.UpnpServiceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class AbsActionMessage
{
	public static final String SOAPACTION = "SOAPACTION";

	public static final String NS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	public abstract UpnpServiceType getServiceType();

	public abstract String getActionName();

	public abstract int getArgumentListLength();

	public abstract String getArgumentName(int idx);

	public abstract String getArgumentValue(int idx);

	public abstract  String getArgumentValue(String name);

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
}
