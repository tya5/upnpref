package org.tyas.upnp.description;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

public class ArgumentElement implements Description.ArgumentElement
{
	private String mName;
	private boolean mIsDirectionIn;
	private boolean mIsRetval;
	private String mStateVariableName;

	public ArgumentElement(String name, boolean in, boolean retval, String variable) {
		mName = name;
		mIsDirectionIn = in;
		mIsRetval = retval;
		mStateVariableName = variable;
	}

	private ArgumentElement() {}

	@Override public String getName() { return mName; }
	
	@Override public boolean isDirectionIn() { return mIsDirectionIn; }
	
	@Override public boolean isRetval() { return mIsRetval; }
	
	@Override public String getStateVariableName() { return mStateVariableName; }

	private void setName(String name) { mName = name; }

	private void setDirectionIn(boolean b) { mIsDirectionIn = b; }

	private void setRetval(boolean b) { mIsRetval = b; }

	private void setStateVariableName(String var) { mStateVariableName = var; }

	public static ArgumentElement getByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		if (! "argument".equals(((Element)node).getTagName())) return null;

		ArgumentElement a = new ArgumentElement();
		a.setDirectionIn(true);
		a.setRetval(false);

		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("name")) {
				a.setName(Description.getStringByNode(node));
			} else if (tag.equals("direction")) {
				a.setDirectionIn("in".equals(Description.getStringByNode(node)));
			} else if (tag.equals("retval")) {
				a.setRetval(true);
			} else if (tag.equals("relatedStateVariable")) {
				a.setStateVariableName(Description.getStringByNode(node));
			}
		}

		if (a.getName() == null) return null;

		if (a.getStateVariableName() == null) return null;

		return a;
	}
}
