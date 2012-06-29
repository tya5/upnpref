package org.tyas.upnp.description;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

public class StateElement implements Description.StateElement
{
	private boolean mIsSendEvents;
	private boolean mIsMulticast;
	private String mName;
	private String mDataType;
	private String mDefaultValue;
	private ValueRangeElement mVaRange; // null if not exists
	private List<String> mVaList; // null if not exists

	public StateElement(String name, boolean sendEvents, boolean multicast) {
		mName = name;
		mIsSendEvents = sendEvents;
		mIsMulticast = multicast;
	}

	private StateElement() {}

	@Override public boolean isSendEvents() { return mIsSendEvents; }
	
	@Override public boolean isMulticast() { return mIsMulticast; }
	
	@Override public String getName() { return mName; }
	
	@Override public String getDataType() { return mDataType; }
	
	@Override public String getDefaultValue() { return mDefaultValue; }
	
	@Override public ValueRangeElement getValueRange() { return mVaRange; }

	@Override public List<String> getValueList() { return mVaList; }

	private void setSendEvents(boolean b) { mIsSendEvents = b; }

	private void setMulticast(boolean b) { mIsMulticast = b; }

	private void setName(String name) { mName = name; }

	public StateElement setDataType(String type) {
		mDataType = type;
		return this;
	}

	public StateElement setDefaultValue(String v) {
		mDefaultValue = v;
		return this;
	}

	public StateElement setValueRange(ValueRangeElement vr) {
		mVaRange = vr;
		return this;
	}

	public StateElement setValueList(List<String> vl) {
		mVaList = vl;
		return this;
	}

	public static StateElement getByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return null;

		if (! "stateVariable".equals(((Element)node).getTagName())) return null;

		StateElement a = new StateElement();
		a.setSendEvents("yes".equals(Description.getStringAttrByNode(node, "sendEvents")));
		a.setMulticast("yes".equals(Description.getStringAttrByNode(node, "multicast")));

		for (node = node.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("name")) {
				a.setName(Description.getStringByNode(node));
			} else if (tag.equals("dataType")) {
				a.setDataType(Description.getStringByNode(node)); // FIXME: need to check "type" attribute
			} else if (tag.equals("defaultValue")) {
				a.setDefaultValue(Description.getStringByNode(node));
			} else if (tag.equals("allowedValueRange")) {
				ValueRangeElement vr = ValueRangeElement.getByNode(node);
				if (vr != null) a.setValueRange(vr);
			} else if (tag.equals("allowedValueList")) {
				List<String> li = new ArrayList<String>();

				for (Node nn = node.getFirstChild(); nn != null; nn = nn.getNextSibling()) {
					if (nn.getNodeType() != Node.ELEMENT_NODE) continue;

					if (! "allowedValue".equals(((Element)nn).getTagName())) continue;
					
					li.add(Description.getStringByNode(nn));
				}

				a.setValueList(li);
			}
		}

		if (a.getName() == null) return null;

		if (a.getDataType() == null) return null;

		return a;
	}
}
