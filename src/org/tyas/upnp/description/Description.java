package org.tyas.upnp.description;

public class Description
{
	public interface ServiceElement
	{
		DeviceElement getParent();
		DeviceDescription getDeviceDescription();
		UpnpServiceType getType();
		UpnpServiceId getId();
		String getScpdUrl();
		String getControlUrl();
		String getEventSubUrl();
	}

	public interface DeviceElement
	{
		DeviceElement getParent();
		DeviceDescription getDeviceDescription();
		UpnpDeviceType getType();
		String getFriendlyName();
		String getManufacturer();
		String getManufacturerUrl();
		String getModelDescription();
		String getModelName();
		String getModelNumber();
		String getModelUrl();
		String getSerialNumber();
		UpnpUdn getUdn();
		String getUpc();
		String getPresentationUrl();

		Set<UpnpServiceId> getServiceSet();
		Set<UpnpUdn> getDeviceSet();

		ServiceElement getService(UpnpServiceId id);
		DeviceElement getDevice(UpnpUdn udn);
	}

	public interface DeviceDescription
	{
		int getConfigId();
		int getVersionMajor();
		int getVersionMinor();
		DeviceElement getRootDevice();
	}

	public interface ServiceDescription
	{
	}
}
