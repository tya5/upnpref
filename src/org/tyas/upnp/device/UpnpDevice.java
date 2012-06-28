package org.tyas.upnp.device;

import org.tyas.upnp.Upnp;
import org.tyas.upnp.UpnpUdn;
import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpServiceId;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class UpnpDevice implements Upnp.Device
{
	private Map<UpnpServiceId,UpnpService> mServiceMap
		= new HashMap<UpnpServiceId,UpnpService>();
	
	private Map<UpnpUdn,UpnpDevice> mDeviceMap
		= new HashMap<UpnpUdn,UpnpDevice>();

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

	public UpnpDevice(UpnpUdn udn) {
		mUdn = udn;
	}

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

	@Override public UpnpService getService(UpnpServiceId id) {
		return mServiceMap.get(id);
	}

	@Override public UpnpDevice getDevice(UpnpUdn udn) {
		return mDeviceMap.get(udn);
	}

	public UpnpDevice setType(UpnpDeviceType type) {
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