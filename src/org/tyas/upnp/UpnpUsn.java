package org.tyas.upnp;

public class UpnpUsn
{
	private String mPrefix;
	private String mSuffix;

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

	public String toString() {
		if (mPrefix == null) return "";

		if (mSuffix == null) return mPrefix;

		return mPrefix + "::" + mSuffix;
	}

	public static UpnpUsn getByString(String usn) {
		String [] ar = usn.split("::");

		return new UpnpUsn(ar > 0 ? ar[0]:null, ar > 1 ? ar[1]:null);
	}
}
