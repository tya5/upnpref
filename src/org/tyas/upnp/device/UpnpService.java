package org.tyas.upnp.device;

public class UpnpService implements Upnp.Service
{
	private Upnp.ServiceType mType;
	private Upnp.ServiceId mId;
	private String mScpdUrl;
	private String mControlUrl;
	private String mEventSubUrl;

	public UpnpService(Upnp.ServiceId id) {
		mId = id;
	}

	@Override public ServiceType getType() { return mType; }

	@Override public ServiceId getId() { return mId; }

	@Override public String getScpdUrl() { return mScpdUrl; }

	@Override public String getControlUrl() { return mControlUrl; }

	@Override public String getEventSubUrl() { return mEventSubUrl; }

	public UpnpService setType(Upnp.ServiceType type) {
		mType = type;
		return this;
	}

	public UpnpService setScpdUrl(String url) {
		mScpdUrl = url;
		return this;
	}

	public UpnpService setControlUrl(String url) {
		mControlUrl = url;
		return this;
	}

	public UpnpService setEventSubUrl(String url) {
		mEventSubUrl = url;
		return this;
	}
}
