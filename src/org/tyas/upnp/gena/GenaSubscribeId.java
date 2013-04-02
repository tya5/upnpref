package org.tyas.upnp.gena;

public class GenaSubscribeId
{
	private final String mUuid;

	public GenaSubscribeId(String uuid) {
		mUuid = uuid;
	}

	public String getUuid() { return mUuid; }

	@Override public String toString() { return "uuid:" + mUuid; }

	public static GenaSubscribeId getBySid(String sid) {
		String pfx = "uuid:";
		int idx = sid.indexOf(pfx);
		return new GenaSubscribeId(sid.substring(pfx.length()));
	}
}
