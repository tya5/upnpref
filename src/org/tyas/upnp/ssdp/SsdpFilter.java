package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpUsn;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

public abstract class SsdpFilter
{
	public static class Listener
	{
		protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
		protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
		protected void onAlive(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
		protected void onUpdate(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
		protected void onByebye(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
		protected void onFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	}

	private Map<UpnpUsn,Ssdp.RemoteDevicePointer> mMap = new HashMap<UpnpUsn,Ssdp.RemoteDevicePointer>();
	private Listener mListener = new Listener() {
			@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onAdded(ptr, addr);
			}
			@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onRemoved(ptr, addr);
			}
			@Override protected void onAlive(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onAlive(ptr, addr);
			}
			@Override protected void onUpdate(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onUpdate(ptr, addr);
			}
			@Override protected void onByebye(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onByebye(ptr, addr);
			}
			@Override protected void onFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
				SsdpFilter.this.onFound(ptr, addr);
			}
		};

	protected SsdpFilter(Listener listener) {
		if (listener != null) {
			mListener = listener;
		}
	}

	protected SsdpFilter() {
	}

	public abstract SsdpServer.Handler getSsdpHandler();

	public abstract String getSearchTarget();

	public Set<UpnpUsn> getUsnSet() {
		return mMap.keySet();
	}

	public Ssdp.RemoteDevicePointer get(UpnpUsn usn) {
		return mMap.get(usn);
	}

	public void clear() {
		mMap.clear();
	}

	protected void performOnAdvertisement(SsdpAdvertisement.Const adv, InetAddress addr) {
		String nts = adv.getNotificationSubType();
		UpnpUsn usn = adv.getUniqueServiceName();

		if (nts == null) {
		} else if (nts.equals(Ssdp.NTS_ALIVE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.put(usn, adv);
				mListener.onAlive(adv, addr);
			} else {
				mMap.put(usn, adv);
				mListener.onAdded(adv, addr);
				mListener.onAlive(adv, addr);
			}
		} else if (nts.equals(Ssdp.NTS_UPDATE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.put(usn, adv);
				mListener.onUpdate(adv, addr);
			} else {
				mMap.put(usn, adv);
				mListener.onAdded(adv, addr);
				mListener.onUpdate(adv, addr);
			}
		} else if (nts.equals(Ssdp.NTS_BYEBYE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.remove(usn);
				mListener.onByebye(adv, addr);
				mListener.onRemoved(adv, addr);
			}
		}
	}

	protected void performOnFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
		UpnpUsn usn = ptr.getUniqueServiceName();

		if (mMap.keySet().contains(usn)) {
			mMap.put(usn, ptr);
			mListener.onFound(ptr, addr);
		} else {
			mMap.put(usn, ptr);
			mListener.onAdded(ptr, addr);
			mListener.onFound(ptr, addr);
		}
	}

	protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onAlive(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onUpdate(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onByebye(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
}
