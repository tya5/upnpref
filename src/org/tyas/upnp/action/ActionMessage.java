package org.tyas.upnp.action;

import org.tyas.upnp.UpnpServiceType;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class ActionMessage extends AbsActionMessage
{
	private static class Argument
	{
		String mName;
		String mValue;

		Argument(String n, String v) {
			mName = n;
			mValue = v;
		}
	}

	public static class Const extends AbsActionMessage
	{
		private UpnpServiceType mServiceType;
		private String mActionName;
		private List<Argument> mArgs = new ArrayList<Argument>();

		private Const() {}

		@Override public UpnpServiceType getServiceType() { return mServiceType; }

		@Override public String getActionName() { return mActionName; }

		@Override public int getArgumentListLength() { return mArgs.size(); }

		@Override public String getArgumentName(int idx) { return mArgs.get(idx).mName; }

		@Override public String getArgumentValue(int idx) { return mArgs.get(idx).mValue; }

		@Override public String getArgumentValue(String name) {
			if (name == null) return null;
		
			for (Argument arg: mArgs) {
				if (name.equals(arg.mName)) return arg.mValue;
			}

			return null;
		}
	}

	private UpnpServiceType mServiceType;
	private String mActionName;
	private List<Argument> mArgs = new ArrayList<Argument>();

	public ActionMessage(UpnpServiceType service, String action) {
		mServiceType = service;
		mActionName = action;
	}

	@Override public UpnpServiceType getServiceType() { return mServiceType; }

	@Override public String getActionName() { return mActionName; }

	@Override public int getArgumentListLength() { return mArgs.size(); }
	
	@Override public String getArgumentName(int idx) { return mArgs.get(idx).mName; }
	
	@Override public String getArgumentValue(int idx) { return mArgs.get(idx).mValue; }

	@Override public String getArgumentValue(String name) {
		if (name == null) return null;
		
		for (Argument arg: mArgs) {
			if (name.equals(arg.mName)) return arg.mValue;
		}

		return null;
	}

	public ActionMessage add(String name, String value) {
		mArgs.add(new Argument(name, value));
		return this;
	}

	private static ActionMessage.Const getByNode(Node node) {
		if (node == null) return null;

		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		ActionMessage.Const c = new ActionMessage.Const();

		c.mServiceType = UpnpServiceType.getByUrn(node.getNamespaceURI());

		if (c.mServiceType == null) return null;

		c.mActionName = ((Element)node).getTagName();

		node = node.getFirstChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			c.mArgs.add(new Argument(((Element)node).getTagName(), getStringByNode(node)));
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
		DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
		factory.setNamespaceAware(true);
		Document doc = factory
			.newDocumentBuilder()
			.parse(in);
					
		return ActionMessage.getByDocument(doc);
	}
}
