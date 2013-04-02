package org.tyas.upnp;

public class UpnpServiceId
{
	public static final String UPNP_ORG = "upnp-org";
		
	private final String mDomain;
	private final String mId;

	public UpnpServiceId(String domain, String id) {
		mDomain = domain;
		mId = id;
	}

	public UpnpServiceId(String id) {
		this(UPNP_ORG, id);
	}

	public String getDomain() {
		return mDomain;
	}

	public String getId() {
		return mId;
	}

	@Override public String toString() {
		return "urn:" + mDomain + ":serviceId:" + mId;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof UpnpServiceId) {
			return toString().equals(((UpnpServiceId)obj).toString());
		}
		return false;
	}

	public static UpnpServiceId getByString(String urn) {
		String [] ar = urn.split(":", 0);

		if (ar.length != 4) return null;

		if (! "urn".equals(ar[0])) return null;

		if (! "serviceId".equals(ar[2])) return null;

		return new UpnpServiceId(ar[1], ar[3]);
	}
}