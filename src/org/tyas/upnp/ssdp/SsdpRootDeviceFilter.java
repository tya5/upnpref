package org.tyas.upnp.ssdp;

public class SsdpRootDeviceFilter extends SsdpFilter
{
	public SsdpRootDeviceFilter(Listener listener) {
		super(listener);
	}

	public SsdpRootDeviceFilter() {
		super();
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return Ssdp.ROOT_DEVICE; }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
				if (Ssdp.ROOT_DEVICE.equals(adv.getNotificationType())) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(Ssdp.SearchRequest req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(Ssdp.SearchResponse resp, SsdpServer.Context ctx) {
				if (Ssdp.ROOT_DEVICE.equals(resp.getSearchTarget())) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}
