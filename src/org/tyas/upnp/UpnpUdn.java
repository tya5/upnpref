package org.tyas.upnp;

public class UpnpUdn
{
	private final String mUuid;

	public UpnpUdn(String uuid) {
		mUuid = uuid;
	}

	public String getUuid() {
		return mUuid;
	}

	@Override public String toString() {
		return "uuid:" + mUuid;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof UpnpUdn) {
			return toString().equals(((UpnpUdn)obj).toString());
		}
		return false;
	}

	public static UpnpUdn getByString(String udn) {
		String [] ar = udn.split(":", 0);

		if (ar.length != 2) return null;

		if (! "uuid".equals(ar[0])) return null;

		return new UpnpUdn(ar[1]);
	}
}
