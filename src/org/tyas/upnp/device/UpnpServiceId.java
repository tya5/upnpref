package org.tyas.upnp.device;

public class UpnpServiceId implements Upnp.ServiceId
{
	public static final String UPNP_ORG = "upnp-org";
		
	private String mDomain;
	private String mId;

	public UpnpServiceId(String domain, String id) {
		mDomain = domain;
		mId = id;
	}

	public UpnpServiceId(String id) {
		this(UPNP_ORG, id);
	}

	@Override public String getDomain() {
		return mDomain;
	}

	@Override public String getId() {
		return mId;
	}

	@Override public String toString() {
		return "urn:" + mDomain + ":serviceId:" + mId;
	}

	public static UpnpServiceId getByUrn(String urn) {
		String [] ar = urn.split(":", 0);

		if (ar.length != 4) return null;

		if (! "urn".equals(ar[0])) return null;

		if (! "serviceId".equals(ar[2])) return null;

		return new UpnpServiceId(ar[1], ar[3]);
	}
}