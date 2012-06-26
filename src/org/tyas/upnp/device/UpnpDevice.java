package org.tyas.upnp.device;

public class UpnpDevice implements Upnp.Device
{
	private Map<Upnp.ServiceId,UpnpService> mServiceMap
		= new HashMap<Upnp.ServiceId,UpnpService>();
	
	private Map<Upnp.Udn,UpnpDevice> mDeviceMap
		= new HashMap<Upnp.Udn,UpnpDevice>();

	private Upnp.DeviceType mType;
	private String mFriendlyName;
	private String mManufacturer;
	private String mManufacturerUrl;
	private String mModelDescription;
	private String mModelName;
	private String mModelNumber;
	private String mModelUrl;
	private String mSerialNumber;
	private Upnp.Udn mUdn;
	private String mUpc;
	private String mPresentationUrl;

	public UpnpDevice(Upnp.Udn udn) {
		mUdn = udn;
	}

	@Override public DeviceType getType() { return mType; }

	@Override public String getFriendlyName() { return mFriendlyName; }

	@Override public String getManufacturer() { return mManufacturer; }

	@Override public String getManufacturerUrl() { return mManufacturerUrl}

	@Override public String getModelDescription() { return mModelDescription; }

	@Override public String getModelName() { return mModelName; }

	@Override public String getModelNumber() { return mModelNumber; }

	@Override public String getModelUrl() { return mModelUrl; }

	@Override public String getSerialNumber() { return mSerialNumber; }

	@Override public Udn getUdn() { return mUdn; }

	@Override public String getUpc() { return mUpc; }

	@Override public String getPresentationUrl() { return mPresentationUrl; }

	@Override public Set<Upnp.ServiceId> getServiceSet() {
		return mServiceMap.keySet();
	}

	@Override public Set<Upnp.Udn> getDeviceSet() {
		return mDeviceMap.keySet();
	}

	@Override public UpnpService getService(Upnp.ServiceId id) {
		return mServiceMap.get(id);
	}

	@Override public UpnpDevice getDevice(Upnp.Udn udn) {
		return mDeviceMap.get(udn);
	}

	public UpnpDevice setType(Upnp.DeviceType type) {
		mType = type;
		return this;
	}

	public UpnpDevice setFriendlyName(String friendlyName) {
		mFriendlyName = friendlyName;
		return this;
	}

	public UpnpDevice setManufacturer(String manufacturer) {
		mManufacturer = manufacturer;
		return this;
	}

	public UpnpDevice setManufacturerUrl(String manufacturerUrl) {
		mManufacturerUrl = manufacturerUrl;
		return this;
	}

	public UpnpDevice setModelDescription(String desc) {
		mModelDescription = desc;
		return this;
	}

	public UpnpDevice setModelName(String name) {
		mModelName = name;
		return this;
	}

	public UpnpDevice setModelNumber(String number) {
		mModelNumber = number;
		return this;
	}

	public UpnpDevice setModelUrl(String url) {
		mModelUrl = url;
		return this;
	}

	public UpnpDevice setSerialNumber(String serial) {
		mSerialNumber = serial;
		return this;
	}

	public UpnpDevice setUpc(String upc) {
		mUpc = upc;
		return this;
	}

	public UpnpDevice setPresentationUrl(String url) {
		mPresentationUrl = url;
		return this;
	}

	public UpnpDevice putService(UpnpService service) {
		mServiceMap.put(service.getId(), service);
		return this;
	}

	public UpnpDevice putDevice(UpnpDevice device) {
		mDeviceMap.put(device.getUdn(), device);
		return this;
	}
}