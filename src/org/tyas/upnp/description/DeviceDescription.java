package org.tyas.upnp.description;

public class DeviceDescription implements Description.DeviceDescription
{
	private int mConfigId;
	private int mVersionMajor;
	private int mVersionMinor;
	private DeviceElement mRootDevice;
	
	public DeviceDescription(int configid, int major, int minor, Device root) {
		mConfigId = configid;
		mVersionMajor = major;
		mVersionMinor = minor;
		mRootDevice = root;

		root.setParent(null);
		root.setDeviceDescription(this);
	}

	@Override public int getConfigId() { return mConfigId; }

	@Override public int getVersionMajor() { return mVersionMajor; }

	@Override public int getVersionMinor() { return mVersionMinor; }

	@Override public DeviceElement getRootDevice() { return mRootDevice; }

	public Element toXmlElement() {
		return null;
	}

	public static DeviceDescription createDeviceDescription(Document doc) {
		Node node;

		node = doc.getFirshChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == ELEMENT_TYPE) {
				if ("root".equals(((Element)node).getTagName())) {
					break;
				}
			}
		}

		if (node == null) return null;

		Element root = (Element)node;
		int configid = Integer.decode(root.getAttribute("configId"));
		int major = -1;
		int minor = -1;
		Device dev = null;

		node = root.getFirstChild();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() == ELEMENT_TYPE) {
				String tag = ((Element)node).getTagName();

				if (tag == null) {
					;
				} else if (tag.equals("device")) {
					dev = DeviceElement.createDeviceElement((Element)node);
				} else if (tag.equals("specVersion")) {
					Node spec = node.getFirstChild();

					for (; spec != null; spec = spec.getNextSibling()) {
						if (spec.getNodeType() == ELEMENT_TYPE) {
							String tag2 = ((Element)spec).getTagName();

							if (tag2 == null) {
								;
							} else if (tag2.equals("major")) {
								major = spec.getFirstChild().getNodeValue();
							} else if (tag2.equals("minor")) {
								minor = spec.getFirstChild().getNodeValue();
							}
						}
					}
				}
			}
		}

		if (dev == null) return null;

		if (major < 0) return null;

		if (minor < 0) return null;

		return new DeviceDescription(configid, major, minor, dev);
	}
}
