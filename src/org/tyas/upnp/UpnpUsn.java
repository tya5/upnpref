package org.tyas.upnp;

public class UpnpUsn
{
	private final String mPrefix;
	private final String mSuffix;

	public UpnpUsn(String pfx, String sfx) {
		mPrefix = pfx;
		mSuffix = sfx;
	}

	public String getPrefix() {
		return mPrefix;
	}

	public String getSuffix() {
		return mSuffix;
	}

	public UpnpUdn getUdn() {
		return UpnpUdn.getByUuid(mPrefix);
	}

	@Override public String toString() {
		if (mPrefix == null) return "";

		if (mSuffix == null) return mPrefix;

		return mPrefix + "::" + mSuffix;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof UpnpUsn) {
			return toString().equals(((UpnpUsn)obj).toString());
		}
		return false;
	}

	public static UpnpUsn getByString(String usn) {
		String [] ar = usn.split("::");

		return new UpnpUsn(ar.length > 0 ? ar[0]:null, ar.length > 1 ? ar[1]:null);
	}
}
