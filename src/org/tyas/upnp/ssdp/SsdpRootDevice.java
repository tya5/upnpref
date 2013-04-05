package org.tyas.upnp.ssdp;

import java.util.*;
import java.net.*;
import org.tyas.upnp.*;

public class SsdpRootDevice implements SsdpConstant
{
	private final InetAddress mLocalAddress;
	private final UpnpUdn mRootDeviceUdn;
	private final String mDescriptionUrl;
	private final Map<UpnpUdn,UpnpDeviceType> mDeviceMap = new HashMap<UpnpUdn,UpnpDeviceType>();
	private final Map<UpnpDeviceType,Set<UpnpUdn>> mDeviceTypeMap = new HashMap<UpnpDeviceType,Set<UpnpUdn>>();
	private final Map<UpnpServiceType,Set<UpnpUdn>> mServiceTypeMap = new HashMap<UpnpServiceType,Set<UpnpUdn>>();

	private final Timer mTimer = new Timer();
	private final Random mRandom = new Random();

	private final SsdpRootDeviceSpec mSpec = new SsdpRootDeviceSpec() {
			public int getBootId() {
				return 1;
			}

			public int getConfigId() {
				return 0;
			}

			public String getDeviceDescriptionLocation() {
				return mDescriptionUrl;
			}

			public int getUnicastSearchPort() {
				return SsdpRootDevice.this.getUnicastSearchPort();
			}
		};

	private SsdpSearchServer mSearchServer = null;
	private SsdpAdvertisePublisher mAdvertiser = null;

	public SsdpRootDevice(InetAddress localAddress, UpnpUdn rootDevice, UpnpDeviceType type, String descriptionPath) {
		mLocalAddress = localAddress;
		mRootDeviceUdn = rootDevice;
		mDescriptionUrl = descriptionPath;

		addDevice(rootDevice, type);
	}

	public InetAddress getAddress() {
		return mLocalAddress;
	}

	public int getUnicastSearchPort() {
		return (mSearchServer != null) ? mSearchServer.getUnicastSearchPort(): -1;
	}

	public boolean isListening() {
		return
			(mSearchServer != null) &&
			(! mSearchServer.isClosed()) &&
			(mAdvertiser != null);
	}

	public void startListening() {
		if (mSearchServer == null) {
			mSearchServer = SsdpSearchServer.newInstance(mLocalAddress);
			
			if (mSearchServer != null) {
				mSearchServer.addOnSearchRequestListener(mOnSearchRequest);
			}
		}
		
		if (mAdvertiser == null) {
			mAdvertiser = SsdpAdvertisePublisher.newInstance(mLocalAddress);
		}
		
		notifyAll(SsdpAdvertisement.Nts.ALIVE);
	}

	public void stopListening() {
		notifyAll(SsdpAdvertisement.Nts.BYEBYE);
		
		if (mSearchServer != null) {
			mSearchServer.close();
			mSearchServer.removeOnSearchRequestListener(mOnSearchRequest);
			mSearchServer = null;
		}
		
		if (mAdvertiser != null) {
			mAdvertiser.close();
			mAdvertiser = null;
		}
	}

	public Set<UpnpUdn> getUdnSet() {
		return mDeviceMap.keySet();
	}

	public UpnpDeviceType getDeviceType(UpnpUdn dev) {
		return mDeviceMap.get(dev);
	}

	public Set<UpnpDeviceType> getDeviceTypeSet() {
		return mDeviceTypeMap.keySet();
	}

	public Set<UpnpServiceType> getServiceTypeSet() {
		return mServiceTypeMap.keySet();
	}

	public Set<UpnpUdn> getUdnSet(UpnpDeviceType type) {
		return mDeviceTypeMap.get(type);
	}

	public Set<UpnpUdn> getUdnSet(UpnpServiceType type) {
		return mServiceTypeMap.get(type);
	}

	public UpnpUdn getUdn() {
		return mRootDeviceUdn;
	}

	public synchronized void addDevice(UpnpUdn device, UpnpDeviceType type) {
		mDeviceMap.put(device, type);
		
		Set<UpnpUdn> set;
		
		set = mDeviceTypeMap.get(type);

		if (set == null) {
			set = new HashSet<UpnpUdn>();
			mDeviceTypeMap.put(type, set);
		}

		set.add(device);
	}

	public synchronized void removeDevice(UpnpUdn device) {
		UpnpDeviceType type = mDeviceMap.get(device);

		mDeviceMap.remove(type);

		Set<UpnpUdn> set = mDeviceTypeMap.get(type);

		if (set != null) {
			set.remove(device);
		}

		for (UpnpServiceType stype: mServiceTypeMap.keySet()) {
			Set<UpnpUdn> set2 = mServiceTypeMap.get(stype);

			if (set2 != null) {
				set2.remove(device);
			}
		}
	}

	public synchronized void addServiceType(UpnpUdn device, UpnpServiceType serviceType) {
		Set<UpnpUdn> set = mServiceTypeMap.get(serviceType);
		
		if (set == null) {
			set = new HashSet<UpnpUdn>();
			mServiceTypeMap.put(serviceType, set);
		}
		
		set.add(device);
	}

	public synchronized void removeServiceType(UpnpUdn device, UpnpServiceType serviceType) {
		Set<UpnpUdn> set = mServiceTypeMap.get(serviceType);
		
		if (set != null) {
			set.remove(device);
		}
	}

	private synchronized void notifyAll(SsdpAdvertisement.Nts nts) {
		notifyRootDevice(nts);

		for (UpnpUdn udn: getUdnSet()) {
			notifyDevice(udn, nts);
		}

		for (UpnpDeviceType devType: getDeviceTypeSet()) {
			notifyDeviceType(devType, nts);
		}

		for (UpnpServiceType srvType: getServiceTypeSet()) {
			notifyServiceType(srvType, nts);
		}
	}

	private void notifyRootDevice(SsdpAdvertisement.Nts nts) {
		notify(new UpnpUsn(getUdn(), ROOT_DEVICE), ROOT_DEVICE, nts);
	}

	private void notifyDevice(UpnpUdn udn, SsdpAdvertisement.Nts nts) {
		notify(new UpnpUsn(udn, null), udn.toString(), nts);
	}

	private synchronized void notifyDeviceType(UpnpDeviceType type, SsdpAdvertisement.Nts nts) {
		String suffix = type.toString();
		Set<UpnpUdn> set = getUdnSet(type);
		
		if (set != null) {
			for (UpnpUdn udn: set) {
				notify(new UpnpUsn(udn, suffix), type.toString(), nts);
			}
		}
	}

	private synchronized void notifyServiceType(UpnpServiceType type, SsdpAdvertisement.Nts nts) {
		String suffix = type.toString();
		Set<UpnpUdn> set = getUdnSet(type);
		
		if (set != null) {
			for (UpnpUdn udn: set) {
				notify(new UpnpUsn(udn, suffix), type.toString(), nts);
			}
		}
	}

	private void notify(UpnpUsn usn, String nt, SsdpAdvertisement.Nts nts) {
		if (! isListening()) {
			return;
		}

		if (mAdvertiser == null) {
			return;
		}

		mAdvertiser.notify(mSpec, usn, nt, nts);
	}

	private synchronized void responseAll(SsdpSearchRequest req, InetAddress host, int port, boolean delayByMx) {
		responseRootDevice(req, host, port, delayByMx);

		for (UpnpUdn udn: getUdnSet()) {
			responseDevice(req, udn, host, port, delayByMx);
		}

		for (UpnpDeviceType devType: getDeviceTypeSet()) {
			responseDeviceType(req, devType, host, port, delayByMx);
		}

		for (UpnpServiceType srvType: getServiceTypeSet()) {
			responseServiceType(req, srvType, host, port, delayByMx);
		}
	}

	private void responseRootDevice(SsdpSearchRequest req, InetAddress host, int port, boolean delayByMx) {
		response(req, new UpnpUsn(getUdn(), ROOT_DEVICE), host, port, delayByMx);
	}

	private void responseDevice(SsdpSearchRequest req, UpnpUdn udn, InetAddress host, int port, boolean delayByMx) {
		response(req, new UpnpUsn(udn, null), host, port, delayByMx);
	}

	private synchronized void responseDeviceType(SsdpSearchRequest req, UpnpDeviceType type, InetAddress host, int port, boolean delayByMx) {
		String suffix = type.toString();
		Set<UpnpUdn> set = getUdnSet(type);
		
		if (set != null) {
			for (UpnpUdn udn: set) {
				response(req, new UpnpUsn(udn, suffix), host, port, delayByMx);
			}
		}
	}

	private synchronized void responseServiceType(SsdpSearchRequest req, UpnpServiceType type, InetAddress host, int port, boolean delayByMx) {
		String suffix = type.toString();
		Set<UpnpUdn> set = getUdnSet(type);
		
		if (set != null) {
			for (UpnpUdn udn: set) {
				response(req, new UpnpUsn(udn, suffix), host, port, delayByMx);
			}
		}
	}

	private void response(final SsdpSearchRequest req, final UpnpUsn usn, final InetAddress host, final int port, boolean delayByMx) {
		
		if (! delayByMx) {
			doResponse(req.getSearchTarget(), usn, host, port);
			return;
		}

		long delay = ((long) req.getMaxWaitTime()) * mRandom.nextInt(1000);
		
		mTimer.schedule(new TimerTask() {
				public void run() {
					doResponse(req.getSearchTarget(), usn, host, port);
				}
			}, delay);
	}

	private void doResponse(String searchTarget, UpnpUsn usn, InetAddress host, int port) {
		if (! isListening()) {
			return;
		}
					
		if (mSearchServer == null) {
			return;
		}
					
		mSearchServer.response(host, port, mSpec, searchTarget, usn);
	}

	private final SsdpSearchServer.OnSearchRequestListener mOnSearchRequest =
		new SsdpSearchServer.OnSearchRequestListener()
		{
			public void onSearchRequest(SsdpSearchServer server, SsdpSearchRequest req, InetAddress host, int port, boolean isMulticast) {
				String st = req.getSearchTarget();
				
				if (ST_ALL.equals( st )) {
					responseAll(req, host, port, isMulticast);
					return;
				}
				
				if (ROOT_DEVICE.equals( st )) {
					responseRootDevice(req, host, port, isMulticast);
					return;
				}
				
				UpnpDeviceType dev = UpnpDeviceType.getByString( st );
				if (dev != null) {
					responseDeviceType(req, dev, host, port, isMulticast);
					return;
				}
				
				UpnpServiceType srv =  UpnpServiceType.getByString( st );
				if (srv != null) {
					responseServiceType(req, srv, host, port, isMulticast);
					return;
				}
				
				UpnpUdn udn = UpnpUdn.getByString( st );
				if (udn != null) {
					responseDevice(req, udn, host, port, isMulticast);
					return;
				}
			}
		};

	public static void main(String [] args) {
		try {
			UpnpUdn udn = new UpnpUdn("6eb518e5-f8b9-4318-bf49-3483b0d8c716");
			UpnpDeviceType type = new UpnpDeviceType("org.tyas", "Foo", "1");
			SsdpRootDevice rootDev = new SsdpRootDevice(InetAddress.getLocalHost(), udn, type, "");
			
			SsdpController ssdp = new SsdpController(InetAddress.getLocalHost());

			ssdp.addOnRemoteServiceChangeListener(new SsdpController.OnRemoteServiceChangeListener() {
					public void onServiceFound(SsdpController controller, SsdpController.RemoteService service) {
						System.out.println("*** found   "+service.getRemoteAddress()+" "+service.getUsn());
					}
					public void onServiceByeBye(SsdpController controller, SsdpController.RemoteService service, UpnpUsn usn) {
						System.out.println("*** byebye  "+(service != null ? service.getRemoteAddress():"none")+" "+usn);
					}
					public void onServiceExpired(SsdpController controller, SsdpController.RemoteService service, UpnpUsn usn) {
						System.out.println("*** expired "+(service != null ? service.getRemoteAddress():"none")+" "+usn);
					}
					public void onServiceUpdated(SsdpController controller, SsdpController.RemoteService beforeUpdated, SsdpController.RemoteService afterUpdated) {
						SsdpController.RemoteService service = afterUpdated;
						//System.out.println("updated "+service.getRemoteAddress()+" "+service.getUsn());
					}
				});
			
			rootDev.startListening();
			
			ssdp.startListening();
			
			//ssdp.searchAllByUnicast(rootDev.getAddress().getHostAddress(), rootDev.getUnicastSearchPort());
			ssdp.searchAllByMulticast(3);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
