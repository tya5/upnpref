package org.tyas.upnp.ssdp;

import java.io.*;
import java.net.*;
import java.util.*;

import org.tyas.upnp.*;

public class SsdpSearchClient implements SsdpConstant
{
	public interface OnSearchResponseListener
	{
		// Called on single private thread, but not the thread on which search method is invoked.
		public void onSearchResponse(SsdpSearchClient client, SsdpSearchResponse searchResponse, InetAddress remoteHost);
	}

	public interface OnListeningStateChangeListener
	{
		public void onListeningStart(SsdpSearchClient client);
		public void onListeningStop(SsdpSearchClient client);
	}

	private final DatagramSocket  mMSearchTx;
	private final DatagramSocket  mUSearchTx;
	private final DatagramSocket  mUSearchRx;

	private final List<OnSearchResponseListener> mOnSearchResponseListeners =
		new ArrayList<OnSearchResponseListener>();

	private final List<OnListeningStateChangeListener> mOnListeningStateChangeListeners =
		new ArrayList<OnListeningStateChangeListener>();

	private final Thread mThread = new Thread("SsdpSearchClientThread") {
			public void run() {
				doRun();
			}

			private void doRun() {
				performOnListeningStart();

				byte [] buf = new byte[ 2048 ];
				DatagramPacket pkt = new DatagramPacket(buf, buf.length);

				while (! isClosed()) {
					try {
						mUSearchRx.receive( pkt );

						InputStream in = new ByteArrayInputStream( pkt.getData() );
						SsdpSearchResponse resp = SsdpSearchResponse.read( in );

						if (resp != null) {
							performOnSearchResponse(resp, pkt.getAddress());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				performOnListeningStop();
			}
		};

	private SsdpSearchClient(DatagramSocket sock) {
		mMSearchTx = sock;
		mUSearchTx = sock;
		mUSearchRx = sock;
		
		mThread.start();

		log("client wait on "+mUSearchRx.getLocalAddress()+":"+mUSearchRx.getLocalPort());
	}

	@Override protected void finalize() {
		close();
	}

	public static SsdpSearchClient newInstance(InetAddress localAddress) {
		int retryMax = 10;
		int retry = 0;
		Random r = new Random();
		SsdpSearchClient client = null;
		
		while ((client == null) && ((retry++) < retryMax)) {
			int port = 49152 + r.nextInt(65535 - 49152);
			
			client = newInstance(localAddress, port);
		}
		
		return client;
	}

	public static SsdpSearchClient newInstance(InetAddress localAddress, int port) {
		DatagramSocket sock = null;
		
		try {
			sock = new DatagramSocket( null );
			sock.setReuseAddress( true );
			sock.bind(new InetSocketAddress(localAddress, port));
			
		} catch (Exception e) {
			e.printStackTrace();
			
			if (sock != null) {
				sock.close();
			}
			
			return null;
		}
		
		return new SsdpSearchClient(sock);
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

	public void searchByMulticast(String searchTarget, int maxWaitTimeSeconds) {
		try {
			SsdpSearchRequest.Builder reqBuilder = new SsdpSearchRequest.Builder();
		
			reqBuilder.setSearchTarget( searchTarget );
			reqBuilder.setMaxWaitTime( maxWaitTimeSeconds );
		
			DatagramPacket pkt = reqBuilder.build().toDatagramPacket();
		
			pkt.setAddress(InetAddress.getByName(reqBuilder.getHost()));
			pkt.setPort(reqBuilder.getPort());

			//System.out.println("SearchClient send");
			//System.out.println(new String(pkt.getData(), pkt.getOffset(), pkt.getLength()));
		
			mMSearchTx.send( pkt );
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	public void searchByUnicast(String searchTarget, String remoteHost, int remotePort) {
		try {
			SsdpSearchRequest.Builder reqBuilder = new SsdpSearchRequest.Builder(remoteHost, remotePort);
		
			reqBuilder.setSearchTarget( searchTarget );
		
			DatagramPacket pkt = reqBuilder.build().toDatagramPacket();
		
			pkt.setAddress(InetAddress.getByName(reqBuilder.getHost()));
			pkt.setPort(reqBuilder.getPort());
		
			mUSearchTx.send( pkt );
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	public void close() {
		mMSearchTx.close();
		mUSearchTx.close();
		mThread.interrupt();
	}

	public boolean isClosed() {
		return mMSearchTx.isClosed() || mUSearchTx.isClosed() || mThread.isInterrupted();
	}

	public void addOnSearchResponseListener(OnSearchResponseListener l) {
		synchronized (mOnSearchResponseListeners) {
			mOnSearchResponseListeners.add( l );
		}
	}

	public void removeOnSearchResponseListener(OnSearchResponseListener l) {
		synchronized (mOnSearchResponseListeners) {
			mOnSearchResponseListeners.remove( l );
		}
	}

	private void performOnSearchResponse(SsdpSearchResponse searchResponse, InetAddress remoteHost) {
		OnSearchResponseListener [] listeners;
		
		synchronized (mOnSearchResponseListeners) {
			listeners = new OnSearchResponseListener[ mOnSearchResponseListeners.size() ];
			mOnSearchResponseListeners.toArray(listeners);
		}
		
		for (OnSearchResponseListener l: listeners) {
			
			if (isClosed()) break;
			
			try {
				l.onSearchResponse(this, searchResponse, remoteHost);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void addOnListeningStateChangeListener(OnListeningStateChangeListener l) {
		synchronized (mOnListeningStateChangeListeners) {
			mOnListeningStateChangeListeners.add( l );
		}
	}

	public void removeOnListeningStateChangeListener(OnListeningStateChangeListener l) {
		synchronized (mOnListeningStateChangeListeners) {
			mOnListeningStateChangeListeners.remove( l );
		}
	}

	private void performOnListeningStart() {
		for (OnListeningStateChangeListener l: getTempOnListeningStateChangeListeners()) {

			try {
				l.onListeningStart( this );
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void performOnListeningStop() {
		for (OnListeningStateChangeListener l: getTempOnListeningStateChangeListeners()) {

			try {
				l.onListeningStop( this );
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private OnListeningStateChangeListener [] getTempOnListeningStateChangeListeners() {
		synchronized (mOnListeningStateChangeListeners) {
			OnListeningStateChangeListener [] listeners = new OnListeningStateChangeListener[ mOnListeningStateChangeListeners.size() ];
			mOnListeningStateChangeListeners.toArray( listeners );
			return listeners;
		}
	}

	private void log(String text) {
	}

	public static void main(String [] args) {
		try {
			UpnpUdn device = new UpnpUdn("6eb518e5-f8b9-4318-bf49-3483b0d8c716");
			UpnpDeviceType type = new UpnpDeviceType("org.tyas", "Foo", "1");
			//SsdpRootDevice rootDev = new SsdpRootDevice(device, type, "");
			SsdpSearchServer server = SsdpSearchServer.newInstance(InetAddress.getLocalHost());
			SsdpSearchClient client = SsdpSearchClient.newInstance(InetAddress.getLocalHost());
			
			client.addOnSearchResponseListener(new OnSearchResponseListener() {
					public void onSearchResponse(SsdpSearchClient c, SsdpSearchResponse resp, InetAddress remoteHost) {
						System.out.println("SearchResponse from "+remoteHost);
					}
				});
			
			client.searchRootDeviceByMulticast(5);
			client.searchByMulticast(device, 5);
			
			Thread.sleep(10000);
			
			client.close();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
