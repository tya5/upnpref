package org.tyas.upnp.device;

public class Udn
{
	private String mUuid;

	public Udn(String uuid) {
		mUuid = uuid;
	}

	public String getUuid() {
		return mUuid;
	}

	public String toString() {
		return "uuid:" + mUuid;
	}

	public static Udn getByUuid(String udn) {
		String [] ar = udn.split(":", 0);

		if (ar.length != 2) return null;

		if (! "uuid".equals(ar[0])) return null;

		return new Udn(ar[1]);
	}
}
