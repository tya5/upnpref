package org.tyas.upnp.description;

public class Description
{
	public interface Service
	{
		Device getParent();
		DeviceDescription getDeviceDescription();
		UpnpServiceType getType();
		UpnpServiceId getId();
		String getScpdUrl();
		String getControlUrl();
		String getEventSubUrl();
	}

	public interface Device
	{
		Device getParent();
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

		Service getService(UpnpServiceId id);
		Device getDevice(UpnpUdn udn);
	}

	public interface DeviceDescription
	{
		int getConfigId();
		int getVersionMajor();
		int getVersionMinor();
		Device getRootDevice();
	}

	public interface ServiceDescription
	{
	}
}
