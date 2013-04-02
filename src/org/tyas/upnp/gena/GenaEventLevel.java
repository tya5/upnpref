package org.tyas.upnp.gena;

public class GenaEventLevel
{
	public static final String DOMAIN_UPNP = "upnp";
	public static final GenaEventLevel GENERAL = new GenaEventLevel(DOMAIN_UPNP, "general");
	public static final GenaEventLevel DEBUG   = new GenaEventLevel(DOMAIN_UPNP, "debug");
	public static final GenaEventLevel INFO    = new GenaEventLevel(DOMAIN_UPNP, "info");
	public static final GenaEventLevel WARNING = new GenaEventLevel(DOMAIN_UPNP, "warning");
	public static final GenaEventLevel FAULT   = new GenaEventLevel(DOMAIN_UPNP, "fault");
	public static final GenaEventLevel EMERGENCY = new GenaEventLevel(DOMAIN_UPNP, "emergency");

	private final String mDomain;
	private final String mLevel;

	public GenaEventLevel(String domain, String level) {
		mDomain = domain;
		mLevel = level;
	}

	public String getDomain() {
		return mDomain;
	}

	public String getLevel() {
		return mLevel;
	}

	@Override public String toString() {
		return mDomain + ":/" + mLevel;
	}

	@Override public int hashCode() {
		return toString().hashCode();
	}

	@Override public boolean equals(Object obj) {
		if (obj instanceof GenaEventLevel) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	public static GenaEventLevel getByString(String level) {
		String [] ar = level.split(":/");

		return new GenaEventLevel(ar[0], ar[1]);
	}
}
