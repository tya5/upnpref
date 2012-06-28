package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpUsn;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

public abstract class SsdpFilter
{
	private Map<UpnpUsn,Ssdp.RemoteDevicePointer> mMap = new HashMap<UpnpUsn,Ssdp.RemoteDevicePointer>();

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

	protected void performOnAdvertisement(Ssdp.Advertisement adv, InetAddress addr) {
		String nts = adv.getNotificationSubType();
		UpnpUsn usn = adv.getUniqueServiceName();

		if (nts == null) {
		} else if (nts.equals(Ssdp.NTS_ALIVE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.put(usn, adv);
				onAlive(adv, addr);
			} else {
				mMap.put(usn, adv);
				onAdded(adv, addr);
				onAlive(adv, addr);
			}
		} else if (nts.equals(Ssdp.NTS_UPDATE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.put(usn, adv);
				onUpdate(adv, addr);
			} else {
				mMap.put(usn, adv);
				onAdded(adv, addr);
				onUpdate(adv, addr);
			}
		} else if (nts.equals(Ssdp.NTS_BYEBYE)) {
			if (mMap.keySet().contains(usn)) {
				mMap.remove(usn);
				onByebye(adv, addr);
				onRemoved(adv, addr);
			}
		}
	}

	protected void performOnFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
		UpnpUsn usn = ptr.getUniqueServiceName();

		if (mMap.keySet().contains(usn)) {
			mMap.put(usn, ptr);
			onFound(ptr, addr);
		} else {
			mMap.put(usn, ptr);
			onAdded(ptr, addr);
			onFound(ptr, addr);
		}
	}

	protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onAlive(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onUpdate(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onByebye(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
	protected void onFound(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {}
}
