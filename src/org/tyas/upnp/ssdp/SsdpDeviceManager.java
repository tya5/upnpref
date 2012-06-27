package org.tyas.upnp.ssdp;

public class SsdpDeviceManager
{
	private Map<String,RemoteDevice> mRootDeviceMap;

	public class RemoteDevice
	{
		public Ssdp.RemoteDevicePointer getRemoteDevicePointer();
		public InetAddress getAddress();
	}

	public interface RemoteDeviceListener
	{
		void onRemoteDeviceAlive(RemoteDevice dev);
		void onRemoteDeviceFound(RemoteDevice dev);
		void onRemoteDeviceUpdate(RemoteDevice dev);
		void onRemoteDeviceByebye(RemoteDevice dev);
	}

	private final SsdpServer.Handler mSsdpServerHandler = new SsdpServer.Handler() {
			@Override public void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
				String nt = adv.getNotificationType();

				if (nt.equals(UpnpDeviceType.ROOT_DEVICE)) {
					String nts = adv.getNotificationSubType();
					String usn = adv.getUniqueServiceName();

					if (nts.equals(Ssdp.NTS_ALIVE)) {
						RemoteDevice remo = new RemoteDevice(adv, ctx.getDatagramPacket().getAddress());
						mRootDeviceMap.put(usn, remo);
						performOnRemoteDeviceAlive(remo);
					}
					if (nts.equals(Ssdp.NTS_UPDATE)) {
						RemoteDevice remo = new RemoteDevice(adv, ctx.getDatagramPacket().getAddress());
						mRootDeviceMap.put(usn, remo);
						performOnRemoteDeviceUpdate(remo);
					}
					if (nts.equals(Ssdp.NTS_BYEBYE)) {
						RemoteDevice remo = new RemoteDevice(adv, ctx.getDatagramPacket().getAddress());
						mRootDeviceMap.remove(usn);
						performOnRemoteDeviceByebye(usn, remo);
					}
				}
			}
			@Override public void onSearchRequest(Ssdp.SearchRequest req, SsdpServer.Context ctx) {
			}
			@Override public void onSearchResponse(Ssdp.SearchResponse resp, SsdpServer.Context ctx) {
				String st = resp.getSearchTarget();

				if (st.equals(Ssdp.ROOT_DEVICE)) {
					String usn = resp.getUniqueServiceName();
					RemoteDevice remo = new RemoteDevice(resp, ctx.getDatagramPacket().getAddress());
					mRootDeviceMap.put(usn, remo);
					performOnRemoteDeviceFound(usn, remo);
				}
			}
		};

	public SsdpServer.Handler getSsdpHandler() {
		return mSsdpServerHandler;
	}

	public Set<Upnp.Udn> getRemoteRootDeviceSet() {
	}

	public Set<Upnp.Udn> getRemoteDeviceSet() {
	}

	public Set<Upnp.ServiceType> getRemoteServiceTypeSet() {
	}

	public RemoteDevice getRemoteDevice(Upnp.Udn udn) {
		return mDeviceMap.get(udn);
	}
}
