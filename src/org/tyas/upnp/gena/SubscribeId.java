package org.tyas.upnp.gena;

public class SubscribeId
{
	private final String mUuid;

	public SubscribeId(String uuid) {
		mUuid = uuid;
	}

	public String getUuid() { return mUuid; }

	@Override public String toString() { return "uuid:" + mUuid; }

	public static SubscribeId getBySid(String sid) {
		String pfx = "uuid:";
		int idx = sid.indexOf(pfx);
		return new SubscribeId(sid.substring(pfx.length()));
	}
}
