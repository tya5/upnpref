package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpDeviceType;

public class SsdpDeviceTypeFilter extends SsdpFilter
{
	private UpnpDeviceType mType;

	public SsdpDeviceTypeFilter(UpnpDeviceType type) {
		mType = type;
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return mType.toString(); }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
				UpnpDeviceType type = UpnpDeviceType.getByUrn(adv.getNotificationType());
				if (mType.equals(type)) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(Ssdp.SearchRequest req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(Ssdp.SearchResponse resp, SsdpServer.Context ctx) {
				UpnpDeviceType type = UpnpDeviceType.getByUrn(resp.getSearchTarget());
				if (mType.equals(type)) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}