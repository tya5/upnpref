package org.tyas.upnp.device;

public class UpnpRemoteDeviceManager
{
	private Map<Upnp.Udn,Upnp.Device> mRemoteDeviceMap;

	public interface RemoteDeviceListener
	{
		void onRemoteDeviceRemoved();
		void onRemoteDeviceAdded();
		void onRemoteDeviceUpdated();
	}

	private final SsdpServer.Handler mSsdpHandler = new SsdpServer.Handler() {
			@Override public void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
				if (DeviceType.ROOT_DEVICE.equals( adv.getNotificationType() )) {
					String [] ar = adv.getUniqueServiceName().split("::", 0);
					Upnp.Udn udn = UpnpUdn.getByUuid(ar[0]);
					String nts = adv.getNotificationType();
					Upnp.Device dev = getRootDevice(udn);

					if (nts == null) {
						;
					} else if (nts.equals(Ssdp.NTS_ALIVE)) {
						if (dev == null) {
							;
						}
					} else if (nts.equals(Ssdp.NTS_BYEBYE)) {
						if (dev != null) {
							;
						}
					} else if (nts.equals(Ssdp.NTS_UPDATE)) {
						if (dev == null) {
							;
						} else {
							;
						}
					}
				}
			}
			@Override public void onSearchRequest(Ssdp.SearchRequest req, SsdpServer.Context ctx) {
			}
			@Override public void onSearchResponse(Ssdp.SearchResponse resp, SsdpServer.Context ctx) {
			}
		};

	public SsdpServer.Handler getSsdpHandler() {
		return mSsdpHandler;
	}

	public Set<Upnp.Udn> getRootDeviceSet() {
		return mRootDeviceMap.keySet();
	}

	public Upnp.Device getRootDevice(Upnp.Udn udn) {
		return mRootDeviceMap.get(udn);
	}
}
