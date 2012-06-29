package org.tyas.upnp.description;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ServiceDescription implements Description.ServiceDescription
{
	private int mConfigId;
	private int mVersionMajor;
	private int mVersionMinor;
	private Map<String,ActionElement> mActionMap = new HashMap<String,ActionElement>();
	private Map<String,StateElement> mStateMap = new HashMap<String,StateElement>();

	public ServiceDescription(int configid, int major, int minor) {
		mConfigId = configid;
		mVersionMajor = major;
		mVersionMinor = minor;
	}

	private ServiceDescription() {}

	@Override public int getConfigId() { return mConfigId; }

	@Override public int getVersionMajor() { return mVersionMajor; }

	@Override public int getVersionMinor() { return mVersionMinor; }

	@Override public Set<String> getActionNameSet() {
		return mActionMap.keySet();
	}

	@Override public Set<String> getStateNameSet() {
		return mStateMap.keySet();
	}

	@Override public ActionElement getActionElement(String name) {
		return mActionMap.get(name);
	}

	@Override public StateElement getStateElement(String name) {
		return mStateMap.get(name);
	}

	private ServiceDescription setConfigId(int id) {
		mConfigId = id;
		return this;
	}

	private ServiceDescription setVersionMajor(int major) {
		mVersionMajor = major;
		return this;
	}

	private ServiceDescription setVersionMinor(int minor) {
		mVersionMinor = minor;
		return this;
	}

	public ServiceDescription putActionElement(ActionElement act) {
		mActionMap.put(act.getName(), act);
		return this;
	}

	public ServiceDescription putStateElement(StateElement stat) {
		mStateMap.put(stat.getName(), stat);
		return this;
	}

	public static ServiceDescription getByDocument(Document doc) {
		Node node;

		for (node = doc.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if ("scpd".equals(((Element)node).getTagName())) {
					break;
				}
			}
		}

		if (node == null) return null;

		ServiceDescription s = new ServiceDescription();
		s.setConfigId(Description.getIntAttrByNode(node, "configId", 0));

		node = node.getFirstChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String tag = ((Element)node).getTagName();

				if (tag == null) {
					;
				} else if (tag.equals("actionList")) {
					for (Node nn = node.getFirstChild(); nn != null; nn = nn.getNextSibling()) {
						ActionElement ac = ActionElement.getByNode(nn);
						if (ac != null) s.putActionElement(ac);
					}
				} else if (tag.equals("serviceStateTable")) {
					for (Node nn = node.getFirstChild(); nn != null; nn = nn.getNextSibling()) {
						StateElement st = StateElement.getByNode(nn);
						if (st != null) s.putStateElement(st);
					}
				} else if (tag.equals("specVersion")) {
					Node spec = node.getFirstChild();

					for (; spec != null; spec = spec.getNextSibling()) {
						if (spec.getNodeType() == Node.ELEMENT_NODE) {
							String tag2 = ((Element)spec).getTagName();

							if (tag2 == null) {
								;
							} else if (tag2.equals("major")) {
								s.setVersionMajor(Description.getIntByNode(spec, -1));
							} else if (tag2.equals("minor")) {
								s.setVersionMinor(Description.getIntByNode(spec, -1));
							}
						}
					}
				}
			}
		}

		if (s.getVersionMajor() < 0) return null;

		if (s.getVersionMinor() < 0) return null;

		return s;
	}
}
