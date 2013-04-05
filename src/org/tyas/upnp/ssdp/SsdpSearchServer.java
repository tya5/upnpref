package org.tyas.upnp.ssdp;

import java.io.*;
import java.net.*;
import java.util.*;

import org.tyas.upnp.*;

public class SsdpSearchServer implements SsdpConstant
{
	public interface OnSearchRequestListener
	{
		public void onSearchRequest(SsdpSearchServer server, SsdpSearchRequest req, InetAddress remoteHost, int remotePort, boolean isMulticast);
	}

	private final MulticastSocket mMSearchRx;
	private final DatagramSocket mUSearchRx;
	private final DatagramSocket mUSearchTx;
	private final Thread mMSearchThread;
	private final Thread mUSearchThread;

	private SsdpSearchServer(MulticastSocket msr, DatagramSocket usr) {
		mMSearchRx = msr;
		mUSearchRx = usr;
		mUSearchTx = usr;

		mMSearchThread = new SearchThread( msr );
		mUSearchThread = new SearchThread( usr );

		mUSearchThread.start();
		mMSearchThread.start();
	}

	@Override protected void finalize() {
		close();
	}

	public static SsdpSearchServer newInstance(InetAddress localAddress) {
		int retryMax = 10;
		int retry = 0;
		Random r = new Random();
		SsdpSearchServer server = null;
		
		while ((server == null) && ((retry++) < retryMax)) {
			int port = 49152 + r.nextInt(65535 - 49152);
			
			server = newInstance(localAddress, port);
		}
		
		return server;
	}

	public static SsdpSearchServer newInstance(InetAddress localAddress, int port) {
		DatagramSocket usock = null;
		MulticastSocket msock = null;
		
		try {
			msock = new MulticastSocket( null );
			msock.setReuseAddress( true );
			msock.bind(new InetSocketAddress( MULTICAST_PORT ));
			
			InetAddress groupAddress = InetAddress.getByName( MULTICAST_HOST );
			SocketAddress mcastGroup = new InetSocketAddress(groupAddress, MULTICAST_PORT);
			NetworkInterface incomingIface = NetworkInterface.getByInetAddress(localAddress);
			
			msock.joinGroup(mcastGroup, incomingIface);
			
			usock = new DatagramSocket( null );
			usock.setReuseAddress( true );
			usock.bind(new InetSocketAddress(localAddress, port));
		} catch (Exception e) {
			e.printStackTrace();
			
			if (msock != null) {
				msock.close();
			}
			
			if (usock != null) {
				usock.close();
			}
			
			return null;
		}
		
		return new SsdpSearchServer(msock, usock);
	}

	public int getUnicastSearchPort() {
		return mUSearchRx.getLocalPort();
	}

	public void close() {
		mMSearchRx.close();
		mUSearchRx.close();
		mMSearchThread.interrupt();
		mUSearchThread.interrupt();
	}

	public boolean isClosed() {
		return mMSearchRx.isClosed() ||
			mUSearchRx.isClosed() ||
			mMSearchThread.isInterrupted() ||
			mUSearchThread.isInterrupted();
	}

	private final List<OnSearchRequestListener> mOnSearchRequestListeners =
		new ArrayList<OnSearchRequestListener>();

	public void addOnSearchRequestListener(OnSearchRequestListener l) {
		synchronized (mOnSearchRequestListeners) {
			mOnSearchRequestListeners.add( l );
		}
	}

	public void removeOnSearchRequestListener(OnSearchRequestListener l) {
		synchronized (mOnSearchRequestListeners) {
			mOnSearchRequestListeners.remove( l );
		}
	}

	public void performOnSearchRequest(SsdpSearchRequest req, InetAddress host, int port, boolean isMulticast) {
		OnSearchRequestListener [] listeners;

		synchronized (mOnSearchRequestListeners) {
			listeners = new OnSearchRequestListener[ mOnSearchRequestListeners.size() ];
			mOnSearchRequestListeners.toArray(listeners);
		}

		for (OnSearchRequestListener l: listeners) {
			if (isClosed()) {
				return;
			}

			try {
				l.onSearchRequest(this, req, host, port, isMulticast);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void response(InetAddress host, int port, SsdpRootDeviceSpec spec, String searchTarget, UpnpUsn usn) {
		if (isClosed()) {
			return;
		}
		
		SsdpSearchResponse.Builder b = new SsdpSearchResponse.Builder();
		
		b.setDescriptionUrl( spec.getDeviceDescriptionLocation() );
		b.setSearchTarget( searchTarget );
		b.setUniqueServiceName( usn.toString() );
		b.setBootId( spec.getBootId() );
		b.setConfigId( spec.getConfigId() );

		int sport = spec.getUnicastSearchPort();
		b.setSearchPort(sport >= 0 ? sport: getUnicastSearchPort());
		
		try {
			DatagramPacket pkt = b.build().toDatagramPacket();
			
			pkt.setAddress(host);
			pkt.setPort(port);
			
			//System.out.println("SearchServer send");
			//System.out.println(new String(pkt.getData(), pkt.getOffset(), pkt.getLength()));
			
			mUSearchTx.send( pkt );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final class SearchThread extends Thread
	{
		private final DatagramSocket sock;

		public SearchThread(DatagramSocket sock) {
			super("");
			this.sock = sock;
		}

		protected void onStart() {
		}

		public void run() {
			onStart();
			
			byte [] buf = new byte[ 2048 ];
			DatagramPacket pkt = new DatagramPacket(buf, buf.length);
			
			while (! isClosed()) {
				try {
					sock.receive( pkt );
					
					//System.out.println("SearchServer recv");
					//System.out.println(new String(pkt.getData(), pkt.getOffset(), pkt.getLength()));
					
					InputStream in = new ByteArrayInputStream( pkt.getData() );
					SsdpSearchRequest req = SsdpSearchRequest.read( in );

					if (req == null) {
						continue;
					}
					
					performOnSearchRequest(req, pkt.getAddress(), pkt.getPort(), (sock instanceof MulticastSocket));
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
			onStop();
		}
		
		protected void onStop() {
		}
	}
}

