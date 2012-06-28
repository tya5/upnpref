package org.tyas.upnp.device;

import org.tyas.upnp.Upnp;
import org.tyas.upnp.UpnpServiceType;
import org.tyas.upnp.UpnpServiceId;

public class UpnpService implements Upnp.Service
{
	private UpnpServiceType mType;
	private UpnpServiceId mId;
	private String mScpdUrl;
	private String mControlUrl;
	private String mEventSubUrl;

	public UpnpService(UpnpServiceId id) {
		mId = id;
	}

	@Override public UpnpServiceType getType() { return mType; }

	@Override public UpnpServiceId getId() { return mId; }

	@Override public String getScpdUrl() { return mScpdUrl; }

	@Override public String getControlUrl() { return mControlUrl; }

	@Override public String getEventSubUrl() { return mEventSubUrl; }

	public UpnpService setType(UpnpServiceType type) {
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
