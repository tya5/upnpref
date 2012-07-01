package org.tyas.upnp.action;

public class ActionRequest extends ActionMessage implements Action.Request
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

	public static class Const implements Action.Request
	{
		private UpnpServiceType mServiceType;
		private String mActionName;
		private List<Argument> mArgs = new ArrayList<Argument>();

		private Const() {}

		@Override public UpnpServiceType getServiceType() { return mServiceType; }

		@Override public String getActionName() { return mActionName; }

		@Override public int getLength() { return mArgs.size(); }

		@Override public String get(int idx) { return mArgs.get(idx); }

		@Override public Element toElement(Document doc) { return toElement(this, doc); }

		@Override public String get(String name) {
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

	public ActionRequest(UpnpServiceType service, String action) {
		mServiceType = service;
		mActionName = action;
	}

	@Override public UpnpServiceType getServiceType() { return mServiceType; }

	@Override public String getActionName() { return mActionName; }

	@Override public int getLength() { return mArgs.size(); }

	@Override public String get(int idx) { return mArgs.get(idx); }

	@Override public String get(String name) {
		if (name == null) return null;
		
		for (Argument arg: mArgs) {
			if (name.equals(arg.mName)) return arg.mValue;
		}

		return null;
	}

	@Override public Element toElement(Document doc) { return toElement(this, doc); }

	public void add(String name, String value) {
		mArgs.add(new Argument(name, value));
	}

	public static Element toElement(Action.Request req, Document doc) {
		Element elm = doc.createElementNS(req.getServiceType().toString(), req.getActionName());

		for (Argument arg: mArgs) {
			Element a = doc.createElement(arg.mName);
			a.appendChild(doc.createTextNode(arg.mValue));
			elm.appendChild(a);
		}

		return elm;
	}

	public static ActionRequest.Const getByNode(Node node) {
		if (node == null) return null;
		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		ActionRequest.Const c = new ActionRequest.Const();
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

	public static String getStringByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element elm = ((Element)node);

		Node c = node.getFirstChild();

		if (c == null) return "";

		String s = c.getNodeValue();

		return s == null ? "": s;
	}
}
