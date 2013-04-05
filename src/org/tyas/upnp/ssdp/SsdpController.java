package org.tyas.upnp.ssdp;

import java.util.*;
import java.net.*;
import org.tyas.upnp.*;

public class SsdpController implements SsdpConstant
{
	public final class RemoteService
	{
		private final URL description;
		private final UpnpUsn usn;
		private final int bootId;
		private final int configId;
		private final int searchPort;
		private final InetAddress remoteAddress;
		private final long maxAge;
		
		private final TimerTask expireTask = new TimerTask() {
				public void run() {
					expireRemoteService( usn );
				}
			};
		
		private RemoteService(SsdpAdvertisement msg, InetAddress remoteAddress) {
			description = msg.getDescriptionUrl();
			usn = msg.getUniqueServiceName();
			bootId = msg.getBootId();
			configId = msg.getConfigId();
			searchPort = msg.getSearchPort();
			this.remoteAddress = remoteAddress;
			maxAge = msg.getMaxAge();
		}

		private RemoteService(SsdpSearchResponse msg, InetAddress remoteAddress) {
			description = msg.getDescriptionUrl();
			usn = msg.getUniqueServiceName();
			bootId = msg.getBootId();
			configId = msg.getConfigId();
			searchPort = msg.getSearchPort();
			this.remoteAddress = remoteAddress;
			maxAge = msg.getMaxAge();
		}

		public URL getDescriptionUrl() { return description; }
		public UpnpUsn getUsn() { return usn; }
		public int getBootId() { return bootId; }
		public int getConfigId() { return configId; }
		public int getSearchPort() { return searchPort; }
		public InetAddress getRemoteAddress() { return remoteAddress; }
	}

	public interface OnRemoteServiceChangeListener
	{
		public void onServiceFound(SsdpController controller, RemoteService service);
		public void onServiceByeBye(SsdpController controller, RemoteService service, UpnpUsn usn);
		public void onServiceExpired(SsdpController controller, RemoteService service, UpnpUsn usn);
		public void onServiceUpdated(SsdpController controller, RemoteService beforeUpdated, RemoteService afterUpdated);
	}

	private final Timer mTimer = new Timer();
	private final InetAddress mLocalAddress;
	private SsdpSearchClient mSearchClient = null;
	private SsdpAdvertiseSubscriber mAdvertiseSubscriber = null;
	
	private final List<OnRemoteServiceChangeListener> mListeners =
		new ArrayList<OnRemoteServiceChangeListener>();

	public SsdpController(InetAddress localAddress) {
		mLocalAddress = localAddress;
	}

	public InetAddress getLocalAddress() {
		return mLocalAddress;
	}

	public void addOnRemoteServiceChangeListener(OnRemoteServiceChangeListener l) {
		synchronized (mListeners) {
			mListeners.add( l );
		}
	}

	public void removeOnRemoteServiceChangeListener(OnRemoteServiceChangeListener l) {
		synchronized (mListeners) {
			mListeners.remove( l );
		}
	}

	public void searchAllByMulticast(int maxWaitTimeSeconds) {
		searchByMulticast(ST_ALL, maxWaitTimeSeconds);
	}

	public void searchAllByUnicast(String host, int port) {
		searchByUnicast(ST_ALL, host, port);
	}

	public void searchRootDeviceByMulticast(int maxWaitTimeSeconds) {
		searchByMulticast(ROOT_DEVICE, maxWaitTimeSeconds);
	}

	public void searchRootDeviceByUnicast(String host, int port) {
		searchByUnicast(ROOT_DEVICE, host, port);
	}

	public void searchByMulticast(UpnpUdn device, int maxWaitTimeSeconds) {
		searchByMulticast(device.toString(), maxWaitTimeSeconds);
	}

	public void searchByUnicast(UpnpUdn device, String host, int port) {
		searchByUnicast(device.toString(), host, port);
	}

	public void searchByMulticast(UpnpDeviceType type, int maxWaitTimeSeconds) {
		searchByMulticast(type.toString(), maxWaitTimeSeconds);
	}

	public void searchByUnicast(UpnpDeviceType type, String host, int port) {
		searchByUnicast(type.toString(), host, port);
	}

	public void searchByMulticast(UpnpServiceType type, int maxWaitTimeSeconds) {
		searchByMulticast(type.toString(), maxWaitTimeSeconds);
	}

	public void searchByUnicast(UpnpServiceType type, String host, int port) {
		searchByUnicast(type.toString(), host, port);
	}

	private void searchByMulticast(String searchTarget, int maxWaitTimeSeconds) {
		if (! isListening()) {
			startListening();
		}
		
		if (mSearchClient != null) {
			mSearchClient.searchByMulticast(searchTarget, maxWaitTimeSeconds);
		}
	}

	private void searchByUnicast(String searchTarget, String remoteHost, int remotePort) {
		if (! isListening()) {
			startListening();
		}
		
		if (mSearchClient != null) {
			mSearchClient.searchByUnicast(searchTarget, remoteHost, remotePort);
		}
	}

	public synchronized void startListening() {
		if (mSearchClient == null) {
			mSearchClient = SsdpSearchClient.newInstance( mLocalAddress );
			
			if (mSearchClient != null) {
				mSearchClient.addOnSearchResponseListener(mOnSearchResponse);
			}
		}
		if (mAdvertiseSubscriber == null) {
			mAdvertiseSubscriber = SsdpAdvertiseSubscriber.newInstance( mLocalAddress );
			
			if (mAdvertiseSubscriber != null) {
				mAdvertiseSubscriber.addOnAdvertisementListener( mOnAdvertisement );
			}
		}
	}

	public synchronized void stopListening() {
		if (mSearchClient != null) {
			mSearchClient.close();
			mSearchClient = null;
		}
		if (mAdvertiseSubscriber != null) {
			mAdvertiseSubscriber.close();
			mAdvertiseSubscriber = null;
		}
	}

	public synchronized boolean isListening() {
		return
			(mSearchClient != null) &&
			(! mSearchClient.isClosed()) &&
			(mAdvertiseSubscriber != null) &&
			(! mAdvertiseSubscriber.isClosed());
	}

	private final Map<UpnpUsn,RemoteService> mMap = new HashMap<UpnpUsn,RemoteService>();

	private synchronized void addRemoteService(RemoteService service) {
		RemoteService before = mMap.get(service.getUsn());
		
		mMap.put(service.getUsn(), service);

		long maxAge = service.maxAge;
		
		if (before == null) {
			mTimer.schedule(service.expireTask, maxAge * 1000L);
			performOnFound(service);
			
		} else {
			before.expireTask.cancel();
			mTimer.schedule(service.expireTask, maxAge * 1000L);
			performOnUpdated(before, service);
		}
	}

	private synchronized void byebyeRemoteService(UpnpUsn usn) {
		RemoteService before = mMap.get( usn );
		
		mMap.remove( usn );
		
		performOnByeBye( before, usn );
	}

	private synchronized void expireRemoteService(UpnpUsn usn) {
		RemoteService before = mMap.get( usn );
		
		mMap.remove( usn );
		
		performOnExpired( before, usn );
	}

	private void performOnFound(RemoteService s) {
		for (OnRemoteServiceChangeListener l: getTempListeners()) {
			try {
				l.onServiceFound(this, s);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void performOnByeBye(RemoteService s, UpnpUsn usn) {
		for (OnRemoteServiceChangeListener l: getTempListeners()) {
			try {
				l.onServiceByeBye(this, s, usn);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void performOnUpdated(RemoteService s0, RemoteService s1) {
		for (OnRemoteServiceChangeListener l: getTempListeners()) {
			try {
				l.onServiceUpdated(this, s0, s1);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void performOnExpired(RemoteService s, UpnpUsn usn) {
		for (OnRemoteServiceChangeListener l: getTempListeners()) {
			try {
				l.onServiceExpired(this, s, usn);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private OnRemoteServiceChangeListener [] getTempListeners() {
		synchronized (mListeners) {
			OnRemoteServiceChangeListener [] listeners = new OnRemoteServiceChangeListener[ mListeners.size() ];
			mListeners.toArray( listeners );
			return listeners;
		}
	}

	private final SsdpSearchClient.OnSearchResponseListener mOnSearchResponse =
		new SsdpSearchClient.OnSearchResponseListener() {
			public void onSearchResponse(SsdpSearchClient client, SsdpSearchResponse searchResponse, InetAddress remoteHost) {
				RemoteService s = new RemoteService( searchResponse, remoteHost );
				
				addRemoteService( s );
			}
		};

	private final SsdpAdvertiseSubscriber.OnAdvertisementListener mOnAdvertisement =
		new SsdpAdvertiseSubscriber.OnAdvertisementListener() {
			public void onAdvertisement(SsdpAdvertiseSubscriber subscriber, SsdpAdvertisement adv, InetAddress remoteHost) {
				SsdpAdvertisement.Nts nts = adv.getNotificationSubType();
				
				if (nts == SsdpAdvertisement.Nts.BYEBYE) {
					byebyeRemoteService( adv.getUniqueServiceName() );
				} else if (nts == SsdpAdvertisement.Nts.ALIVE) {
					addRemoteService(new RemoteService( adv, remoteHost ));
				}
			}
		};

	public static void main(String [] args) {
		try {
			SsdpController ssdp = new SsdpController(InetAddress.getLocalHost());

			ssdp.addOnRemoteServiceChangeListener(new OnRemoteServiceChangeListener() {
					public void onServiceFound(SsdpController controller, RemoteService service) {
						System.out.println("found   "+service.getRemoteAddress()+" "+service.getUsn());
					}
					public void onServiceByeBye(SsdpController controller, RemoteService service, UpnpUsn usn) {
						System.out.println("byebye  "+(service != null ? service.getRemoteAddress():"none")+" "+usn);
					}
					public void onServiceExpired(SsdpController controller, RemoteService service, UpnpUsn usn) {
						System.out.println("expired "+(service != null ? service.getRemoteAddress():"none")+" "+usn);
					}
					public void onServiceUpdated(SsdpController controller, RemoteService beforeUpdated, RemoteService afterUpdated) {
						RemoteService service = afterUpdated;
						//System.out.println("updated "+service.getRemoteAddress()+" "+service.getUsn());
					}
				});
			
			ssdp.startListening();
			
			ssdp.searchByMulticast(new UpnpDeviceType("org.tyas", "Foo", "1"), 5);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}