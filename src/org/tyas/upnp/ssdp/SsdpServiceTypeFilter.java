package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpServiceType;

public class SsdpServiceTypeFilter extends SsdpFilter
{
	private UpnpServiceType mType;

	public SsdpServiceTypeFilter(Listener listener, UpnpServiceType type) {
		super(listener);
		mType = type;
	}

	public SsdpServiceTypeFilter(UpnpServiceType type) {
		super();
		mType = type;
	}

	@Override public SsdpServer.Handler getSsdpHandler() { return mHandler; }

	@Override public String getSearchTarget() { return mType.toString(); }

	private final SsdpServer.Handler mHandler = new SsdpServer.Handler() {

			@Override public void onAdvertisement(SsdpAdvertisement.Const adv, SsdpServer.Context ctx) {
				UpnpServiceType type = UpnpServiceType.getByUrn(adv.getNotificationType());
				if (mType.equals(type)) {
					performOnAdvertisement(adv, ctx.getPacket().getAddress());
				}
			}

			@Override public void onSearchRequest(SsdpSearchRequest.Const req, SsdpServer.Context ctx) {
			}

			@Override public void onSearchResponse(SsdpSearchResponse.Const resp, SsdpServer.Context ctx) {
				UpnpServiceType type = UpnpServiceType.getByUrn(resp.getSearchTarget());
				if (mType.equals(type)) {
					performOnFound(resp, ctx.getPacket().getAddress());
				}
			}

		};
}
