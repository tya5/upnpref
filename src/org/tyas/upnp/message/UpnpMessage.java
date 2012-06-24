package org.tyas.upnp.message;

public class UpnpMessage
{
	public static final String NTS = "NTS";
	public static final String NT = "NT";
	public static final String BOOTID = "BOOTID.UPNP.ORG";
	public static final String CONFIGID = "CONFIGID.UPNP.ORG";
	public static final String SEARCHPORT = "SEARCHPORT.UPNP.ORG";

	public static final String ROOTDEVICE = "upnp:rootdevice";

	public interface DeviceUuid
	{
		String getDeviceUuid();
	}

	public interface DeviceType
	{
		String getDeviceDomain();
		String getDeviceType();
		String getDeviceVersion();
	}

	public interface ServiceType
	{
		String getServiceDomain();
		String getServiceType();
		String getServiceVersion();
	}

	public interface Headers extends Http.Headers
	{
		boolean isNtRootDevice();
		DeviceUuid getNtDeviceUuid();
		DeviceType getNtDeviceType();
		ServiceType getNtServiceType();
		DeviceUuid getUsnDeviceUuid();
		DeviceType getUsnDeviceType();
		ServiceType getUsnServiceType();
		String getNts();
		int getBootId();
		int getConfigId();
		int getSearchPort();
	}

	public static String packDeviceUuid(DeviceUuid uuid) {
		return "uuid:" + uuid.getDeviceUuid();
	}

	public static DeviceUuid unpackDeviceUuid(String line) {
		String pfx = "uuid:";
		int idx = line.indexOf(pfx);
		if (idx < 0) return null;
		final String uuid = line.substring(idx + pfx.length());

		return new DeviceUuid() {
			@Override public String getDeviceUuid() { return uuid; }
		};
	}

	public static String packDeviceType(DeviceType dev) {
		return "urn:" + dev.getDeviceDomain() + ":device:" + dev.getDeviceType() + ":" + dev.getDeviceType();
	}

	public static DeviceType unpackDeviceType(String line) {
	}

	public static String packServiceType(ServiceType serv) {
		return "urn:" + serv.getServiceDomain() + ":service:" + serv.getServiceType() + ":" + serv.getServiceType();
	}

	public static ServiceType unpackServiceType(String line) {
	}
}
