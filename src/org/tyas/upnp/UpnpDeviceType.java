package org.tyas.upnp;

public class UpnpDeviceType
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

	@Override public String toString() {
		return "urn:" + mDomain + ":device:" + mType + ":" + mVersion;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof UpnpDeviceType) {
			return toString().equals(((UpnpDeviceType)obj).toString());
		}
		return false;
	}

	public static UpnpDeviceType getByUrn(String urn) {
		String [] ar = urn.split(":", 0);

		if (ar.length != 5) return null;

		if (! "urn".equals(ar[0])) return null;

		if (! "device".equals(ar[2])) return null;

		return new UpnpDeviceType(ar[1], ar[3], ar[4]);
	}

	public static UpnpDeviceType getByUsn(String usn) {
		String [] ar = usn.split("::", 0);

		if (ar.length > 1) return getByUrn(ar[1]);

		return null;
	}
}
