package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpUdn;

public class SsdpDeviceFilter extends SsdpFilter
{
	private UpnpUdn mUdn;

	public SsdpDeviceFilter(Listener listener, UpnpUdn udn) {
		super(listener);
		mUdn = udn;
	}

	public SsdpDeviceFilter(UpnpUdn udn) {
		super();
		mUdn = udn;
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return mUdn.toString(); }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(SsdpAdvertisement adv, SsdpServer.Context ctx) {
				UpnpUdn udn = UpnpUdn.getByUuid(adv.getNotificationType());
				if (mUdn.equals(udn)) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(SsdpSearchRequest req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(SsdpSearchResponse resp, SsdpServer.Context ctx) {
				UpnpUdn udn = UpnpUdn.getByUuid(resp.getSearchTarget());
				if (mUdn.equals(udn)) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}