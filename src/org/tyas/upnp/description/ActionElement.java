package org.tyas.upnp.description;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

public class ActionElement implements Description.ActionElement
{
	private String mName;
	private List<ArgumentElement> mArgs = new ArrayList<ArgumentElement>();

	public ActionElement(String name) {
		mName = name;
	}

	private ActionElement() {
	}

	@Override public String getName() { return mName; }

	@Override public int getArgumentsLength() { return mArgs.size(); }

	@Override public ArgumentElement getArgument(int idx) { return mArgs.get(idx); }

	private ActionElement setName(String name) {
		mName = name;
		return this;
	}

	public ActionElement addArgumentElement(ArgumentElement arg) {
		mArgs.add(arg);
		return this;
	}

	public static ActionElement getByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		if (! "action".equals(((Element)node).getTagName())) return null;

		ActionElement a = new ActionElement();

		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("name")) {
				a.setName(Description.getStringByNode(node));
			} else if (tag.equals("argument")) {
				ArgumentElement g = ArgumentElement.getByNode(node);
				if (g != null) a.addArgumentElement(g);
			}
		}

		if (a.getName() == null) return null;

		return a;
	}
}
