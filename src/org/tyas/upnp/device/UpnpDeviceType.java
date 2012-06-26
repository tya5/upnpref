package org.tyas.upnp.device;

public class UpnpDeviceType implements Upnp.DeviceType
{
	private String mDomain;
	private String mType;
	private String mVersion;

	public static final String ROOT_DEVICE = "upnp:rootdevice";
	public static final String SCHEMAS_UPNP_ORG = "schemas-upnp-org";

	public UpnpDeviceType(String domain, String type, String version) {
		mDomain = domain;
		mType = type;
		mVersion = version;
	}

	public UpnpDeviceType(String type, String version) {
		this(SCHEMAS_UPNP_ORG, type, version);
	}

	public String getDomain() {
		return mDomain;
	}

	public String getType() {
		return mType;
	}

	public String getVersion() {
		return mVersion;
	}

	public String toString() {
		return "urn:" + mDomain + ":device:" + mType + ":" + mVersion;
	}

	public static DeviceType getByUrn(String urn) {
		String [] ar = urn.split(":", 0);

		if (ar.length != 5) return null;

		if (! "urn".equals(ar[0])) return null;

		if (! "device".equals(ar[2])) return null;

		return new UpnpDeviceType(ar[1], ar[3], ar[4]);
	}
}