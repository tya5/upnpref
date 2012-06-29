package org.tyas.upnp.description;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class ValueRangeElement implements Description.ValueRangeElement
{
	private String mMinimum;
	private String mMaximum;
	private String mStep;

	public ValueRangeElement(String min, String max, String step) {
		mMinimum = min;
		mMaximum = max;
		mStep = step;
	}

	private ValueRangeElement() {
	}

	@Override public String getMinimum() { return mMinimum; }
	
	@Override public String getMaximum() { return mMaximum; }
	
	@Override public String getStep() { return mStep; }

	public static ValueRangeElement getByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		if (! "allowedValueRange".equals(((Element)node).getTagName())) return null;

		ValueRangeElement a = new ValueRangeElement();

		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("minimum")) {
				a.mMinimum = Description.getStringByNode(node);
			} else if (tag.equals("maximum")) {
				a.mMaximum = Description.getStringByNode(node);
			} else if (tag.equals("step")) {
				a.mStep = Description.getStringByNode(node);
			}
		}

		if (a.getMinimum() == null) return null;
		if (a.getMaximum() == null) return null;

		return a;
	}
}
