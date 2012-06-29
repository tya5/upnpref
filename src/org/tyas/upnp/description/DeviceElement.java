package org.tyas.upnp.description;

import org.tyas.upnp.Upnp;
import org.tyas.upnp.UpnpUdn;
import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpServiceId;

import org.w3c.dom.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class DeviceElement implements Description.DeviceElement
{
	private Map<UpnpServiceId,ServiceElement> mServiceMap
		= new HashMap<UpnpServiceId,ServiceElement>();
	
	private Map<UpnpUdn,DeviceElement> mDeviceMap
		= new HashMap<UpnpUdn,DeviceElement>();

	private UpnpDeviceType mType;
	private String mFriendlyName;
	private String mManufacturer;
	private String mManufacturerUrl;
	private String mModelDescription;
	private String mModelName;
	private String mModelNumber;
	private String mModelUrl;
	private String mSerialNumber;
	private UpnpUdn mUdn;
	private String mUpc;
	private String mPresentationUrl;
	private DeviceElement mParent;
	private DeviceDescription mDeviceDescription;

	public DeviceElement(UpnpUdn udn) {
		mUdn = udn;
	}

	private DeviceElement() {
	}

	@Override public DeviceElement getParent() { return mParent; }

	@Override public DeviceDescription getDeviceDescription() { return mDeviceDescription; }

	@Override public UpnpDeviceType getType() { return mType; }

	@Override public String getFriendlyName() { return mFriendlyName; }

	@Override public String getManufacturer() { return mManufacturer; }

	@Override public String getManufacturerUrl() { return mManufacturerUrl; }

	@Override public String getModelDescription() { return mModelDescription; }

	@Override public String getModelName() { return mModelName; }

	@Override public String getModelNumber() { return mModelNumber; }

	@Override public String getModelUrl() { return mModelUrl; }

	@Override public String getSerialNumber() { return mSerialNumber; }

	@Override public UpnpUdn getUdn() { return mUdn; }

	@Override public String getUpc() { return mUpc; }

	@Override public String getPresentationUrl() { return mPresentationUrl; }

	@Override public Set<UpnpServiceId> getServiceSet() {
		return mServiceMap.keySet();
	}

	@Override public Set<UpnpUdn> getDeviceSet() {
		return mDeviceMap.keySet();
	}

	@Override public ServiceElement getService(UpnpServiceId id) {
		return mServiceMap.get(id);
	}

	@Override public DeviceElement getDevice(UpnpUdn udn) {
		return mDeviceMap.get(udn);
	}

	private DeviceElement setUdn(UpnpUdn udn) {
		mUdn = udn;
		return this;
	}

	public DeviceElement setType(UpnpDeviceType type) {
		mType = type;
		return this;
	}

	public DeviceElement setFriendlyName(String friendlyName) {
		mFriendlyName = friendlyName;
		return this;
	}

	public DeviceElement setManufacturer(String manufacturer) {
		mManufacturer = manufacturer;
		return this;
	}

	public DeviceElement setManufacturerUrl(String manufacturerUrl) {
		mManufacturerUrl = manufacturerUrl;
		return this;
	}

	public DeviceElement setModelDescription(String desc) {
		mModelDescription = desc;
		return this;
	}

	public DeviceElement setModelName(String name) {
		mModelName = name;
		return this;
	}

	public DeviceElement setModelNumber(String number) {
		mModelNumber = number;
		return this;
	}

	public DeviceElement setModelUrl(String url) {
		mModelUrl = url;
		return this;
	}

	public DeviceElement setSerialNumber(String serial) {
		mSerialNumber = serial;
		return this;
	}

	public DeviceElement setUpc(String upc) {
		mUpc = upc;
		return this;
	}

	public DeviceElement setPresentationUrl(String url) {
		mPresentationUrl = url;
		return this;
	}

	public DeviceElement putService(ServiceElement service) {
		service.setParent(this);
		service.setDeviceDescription(getDeviceDescription());
		mServiceMap.put(service.getId(), service);
		return this;
	}

	public DeviceElement putDevice(DeviceElement device) {
		device.setParent(this);
		device.setDeviceDescription(getDeviceDescription());
		mDeviceMap.put(device.getUdn(), device);
		return this;
	}

	public DeviceElement setParent(DeviceElement parent) {
		mParent = parent;
		return this;
	}

	public DeviceElement setDeviceDescription(DeviceDescription desc) {
		mDeviceDescription = desc;
		for (DeviceElement d: mDeviceMap.values()) {
			d.setDeviceDescription(desc);
		}
		for (ServiceElement s: mServiceMap.values()) {
			s.setDeviceDescription(desc);
		}
		return this;
	}

	public Element toXmlElement() {
		return null;
	}

	public static DeviceElement createDeviceElement(Element elmDevice) {
		if (! "device".equals(elmDevice.getTagName())) return null;

		Node node = elmDevice.getFirstChild();
		DeviceElement dev = new DeviceElement();

		for (; node != null; node = node.getNextSibling()) {
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			String tag = ((Element)node).getTagName();

			if (tag == null) {
				;
			} else if (tag.equals("deviceType")) {
				dev.setType(UpnpDeviceType.getByUrn(Description.getStringByTag(node)));
			} else if (tag.equals("friendlyName")) {
				dev.setFriendlyName(Description.getStringByTag(node));
			} else if (tag.equals("manufacturer")) {
				dev.setManufacturer(Description.getStringByTag(node));
			} else if (tag.equals("manufacturerURL")) {
				dev.setManufacturerUrl(Description.getStringByTag(node));
			} else if (tag.equals("modelDescription")) {
				dev.setModelDescription(Description.getStringByTag(node));
			} else if (tag.equals("modelName")) {
				dev.setModelName(Description.getStringByTag(node));
			} else if (tag.equals("modelNumber")) {
				dev.setModelNumber(Description.getStringByTag(node));
			} else if (tag.equals("modelURL")) {
				dev.setModelUrl(Description.getStringByTag(node));
			} else if (tag.equals("serialNumber")) {
				dev.setSerialNumber(Description.getStringByTag(node));
			} else if (tag.equals("UDN")) {
				dev.setUdn(UpnpUdn.getByUuid(Description.getStringByTag(node)));
			} else if (tag.equals("UPC")) {
				dev.setUpc(Description.getStringByTag(node));
			} else if (tag.equals("iconList")) {
				;
			} else if (tag.equals("serviceList")) {
				Node s = node.getFirstChild();
				for (; s != null; s = s.getNextSibling()) {
					if (s.getNodeType() != Node.ELEMENT_NODE) continue;
					ServiceElement se = ServiceElement.createServiceElement((Element)s);
					if (se != null) dev.putService(se);
				}
			} else if (tag.equals("deviceList")) {
				Node d = node.getFirstChild();
				for (; d != null; d = d.getNextSibling()) {
					if (d.getNodeType() == Node.ELEMENT_NODE) {
						DeviceElement de = DeviceElement.createDeviceElement((Element)d);
						if (de != null) dev.putDevice(de);
					}
				}
			} else if (tag.equals("presentationURL")) {
				dev.setPresentationUrl(Description.getStringByTag(node));
			}
		}
		
		if (dev.getUdn() == null) return null;

		return dev;
	}
}
