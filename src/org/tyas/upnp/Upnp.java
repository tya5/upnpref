package org.tyas.upnp;

public class Upnp
{
	public interface ServiceId
	{
		String getDomain();
		String getId();
	}

	public interface Service
	{
		ServiceType getType();
		ServiceId getId();
		String getScpdUrl();
		String getControlUrl();
		String getEventSubUrl();
	}

	public interface Device
	{
		DeviceType getType();
		String getFriendlyName();
		String getManufacturer();
		String getManufacturerUrl();
		String getModelDescription();
		String getModelName();
		String getModelNumber();
		String getModelUrl();
		String getSerialNumber();
		Udn getUdn();
		String getUpc();
		String getPresentationUrl();

		Set<ServiceId> getServiceSet();
		Set<Udn> getDeviceSet();

		Service getService(ServiceId id);
		Device getDevice(Udn udn);
	}
}
