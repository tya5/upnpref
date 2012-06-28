package org.tyas.upnp;

public class UpnpServiceType
{
	private String mDomain;
	private String mType;
	private String mVersion;

	public static final String SCHEMAS_UPNP_ORG = "schemas-upnp-org";

	public UpnpServiceType(String domain, String type, String version) {
		mDomain = domain;
		mType = type;
		mVersion = version;
	}

	public UpnpServiceType(String type, String version) {
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
		return "urn:" + mDomain + ":service:" + mType + ":" + mVersion;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof UpnpServiceType) {
			return toString().equals(((UpnpServiceType)obj).toString());
		}
		return false;
	}

	public static UpnpServiceType getByUrn(String urn) {
		String [] ar = urn.split(":", 0);

		if (ar.length != 5) return null;

		if (! "urn".equals(ar[0])) return null;

		if (! "service".equals(ar[2])) return null;

		return new UpnpServiceType(ar[1], ar[3], ar[4]);
	}

	public static UpnpServiceType getByUsn(String usn) {
		String [] ar = usn.split("::", 0);

		if (ar.length > 1) return getByUrn(ar[1]);

		return null;
	}
}