package org.tyas.upnp;

public class UpnpUsn
{
	private final UpnpUdn mPrefix;
	private final String mSuffix;

	public UpnpUsn(UpnpUdn pfx, String sfx) {
		mPrefix = pfx;
		mSuffix = sfx;
	}

	public UpnpUdn getPrefix() {
		return mPrefix;
	}

	public String getSuffix() {
		return mSuffix;
	}

	public UpnpUdn getUdn() {
		return mPrefix;
	}

	@Override public String toString() {
		if (mPrefix == null) return "";

		if (mSuffix == null) return mPrefix.toString();

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
		UpnpUdn udn = UpnpUdn.getByString(ar.length > 0 ? ar[0]:null);

		return new UpnpUsn(udn, ar.length > 1 ? ar[1]:null);
	}
}
