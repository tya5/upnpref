package org.tyas.upnp.action;

import org.tyas.upnp.UpnpServiceType;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;
import java.util.ArrayList;

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

	public static ActionMessage.Const getByNode(Node node) {
		System.out.println("step 1");

		if (node == null) return null;

		System.out.println("step 2");

		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		System.out.println("step 3");

		ActionMessage.Const c = new ActionMessage.Const();
		c.mServiceType = UpnpServiceType.getByUrn(node.getNamespaceURI());

		System.out.println("step 4");

		if (c.mServiceType == null) return null;

		System.out.println("step 5");

		c.mActionName = ((Element)node).getTagName();

		node = node.getFirstChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			c.mArgs.add(new Argument(((Element)node).getTagName(), getStringByNode(node)));
		}

		return c;
	}

	public static ActionMessage.Const getByDocument(Document doc) {
		Node node = doc.getFirstChild();

		node = getChildByTagNameNS(node, NS_SOAP, "Envelope");
		node = getChildByTagNameNS(node, NS_SOAP, "Body");

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return getByNode(node);
			}
		}

		return null;
	}

	public static Node getChildByTagNameNS(Node node, String ns, String tag) {
		for (; node != null; node = node.getNextSibling()) {

			System.out.println("step 11");
			
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			System.out.println("step 12: " + ((Element)node).getTagName());

			if ((ns == null) && (node.getNamespaceURI() != null)) continue;
			System.out.println("step 13: " + node.getNamespaceURI());

			if ((ns != null) && (! ns.equals(node.getNamespaceURI()))) continue;
			System.out.println("step 14");

			if (! tag.equals(((Element)node).getTagName())) continue;
			System.out.println("step 15");

			return node;
		}

		System.out.println("step 16");

		return null;
	}
}
