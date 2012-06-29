package org.tyas.upnp.description;

import org.tyas.upnp.Upnp;
import org.tyas.upnp.UpnpServiceType;
import org.tyas.upnp.UpnpServiceId;

import org.w3c.dom.*;

public class ServiceElement implements Description.ServiceElement
{
	private UpnpServiceType mType;
	private UpnpServiceId mId;
	private String mScpdUrl;
	private String mControlUrl;
	private String mEventSubUrl;
	private DeviceElement mParent;
	private DeviceDescription mDeviceDescription;

	public ServiceElement(UpnpServiceId id) {
		mId = id;
	}

	private ServiceElement() {
	}

	@Override public DeviceElement getParent() { return mParent; }

	@Override public DeviceDescription getDeviceDescription() { return mDeviceDescription; }

	@Override public UpnpServiceType getType() { return mType; }

	@Override public UpnpServiceId getId() { return mId; }

	@Override public String getScpdUrl() { return mScpdUrl; }

	@Override public String getControlUrl() { return mControlUrl; }

	@Override public String getEventSubUrl() { return mEventSubUrl; }

	private ServiceElement setId(UpnpServiceId id) {
		mId = id;
		return this;
	}

	public ServiceElement setType(UpnpServiceType type) {
		mType = type;
		return this;
	}

	public ServiceElement setScpdUrl(String url) {
		mScpdUrl = url;
		return this;
	}

	public ServiceElement setControlUrl(String url) {
		mControlUrl = url;
		return this;
	}

	public ServiceElement setEventSubUrl(String url) {
		mEventSubUrl = url;
		return this;
	}

	public ServiceElement setParent(DeviceElement parent) {
		mParent = parent;
		return this;
	}

	public ServiceElement setDeviceDescription(DeviceDescription desc) {
		mDeviceDescription = desc;
		return this;
	}

	public Element toXmlElement() {
		return null;
	}

	public static ServiceElement createServiceElement(Element elmService) {
		if (! "service".equals(elmService.getTagName())) return null;

		Node node = elmService.getFirstChild();
		ServiceElement serv = new ServiceElement();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("serviceType")) {
				serv.setType(UpnpServiceType.getByUrn(Description.getStringByTag(node)));
			} else if (tag.equals("serviceId")) {
				serv.setId(UpnpServiceId.getByUrn(Description.getStringByTag(node)));
			} else if (tag.equals("SCPDURL")) {
				serv.setScpdUrl(Description.getStringByTag(node));
			} else if (tag.equals("controlURL")) {
				serv.setControlUrl(Description.getStringByTag(node));
			} else if (tag.equals("eventSubURL")) {
				serv.setEventSubUrl(Description.getStringByTag(node));
			}
		}

		if (serv.getId() == null) return null;

		return serv;
	}
}
