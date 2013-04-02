package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpDeviceType;

public class SsdpDeviceTypeFilter extends SsdpFilter
{
	private final UpnpDeviceType mType;

	public SsdpDeviceTypeFilter(Listener listener, UpnpDeviceType type) {
		super(listener);
		mType = type;
	}

	public SsdpDeviceTypeFilter(UpnpDeviceType type) {
		super();
		mType = type;
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return mType.toString(); }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(SsdpAdvertisement adv, SsdpServer.Context ctx) {
				UpnpDeviceType type = UpnpDeviceType.getByUrn(adv.getNotificationType());
				if (mType.equals(type)) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(SsdpSearchRequest req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(SsdpSearchResponse resp, SsdpServer.Context ctx) {
				UpnpDeviceType type = UpnpDeviceType.getByUrn(resp.getSearchTarget());
				if (mType.equals(type)) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}