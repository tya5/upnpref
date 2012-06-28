package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpUdn;

public class SsdpDeviceFilter extends SsdpFilter
{
	private UpnpUdn mUdn;

	public SsdpDeviceFilter(UpnpUdn udn) {
		mUdn = udn;
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return mUdn.toString(); }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
				UpnpUdn udn = UpnpUdn.getByUuid(adv.getNotificationType());
				if (mUdn.equals(udn)) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(Ssdp.SearchRequest req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(Ssdp.SearchResponse resp, SsdpServer.Context ctx) {
				UpnpUdn udn = UpnpUdn.getByUuid(resp.getSearchTarget());
				if (mUdn.equals(udn)) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}