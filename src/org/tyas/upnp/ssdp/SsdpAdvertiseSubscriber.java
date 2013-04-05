package org.tyas.upnp.ssdp;

import java.io.*;
import java.net.*;
import java.util.*;

public class SsdpAdvertiseSubscriber implements SsdpConstant
{
	public interface OnAdvertisementListener
	{
		public void onAdvertisement(SsdpAdvertiseSubscriber subscriber, SsdpAdvertisement adv, InetAddress remoteHost);
	}

	private final MulticastSocket mSock;

	private final List<OnAdvertisementListener> mOnAdvertisementListeners =
		new ArrayList<OnAdvertisementListener>();
	
	private final Thread mThread = new Thread("") {
			public void run() {
				byte [] buf = new byte[ 2048 ];
				DatagramPacket pkt = new DatagramPacket(buf, buf.length);
				
				while (! isClosed()) {
					try {
						mSock.receive( pkt );
						
						InputStream in = new ByteArrayInputStream( pkt.getData() );
						SsdpAdvertisement adv = SsdpAdvertisement.read( in );
						
						if (adv != null) {
							performOnAdvertisement(adv, pkt.getAddress());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

	private SsdpAdvertiseSubscriber(MulticastSocket sock) {
		mSock = sock;
		
		mThread.start();
	}

	@Override protected void finalize() {
		close();
	}

	public static SsdpAdvertiseSubscriber newInstance(InetAddress localAddress) {
		MulticastSocket sock = null;

		try {
			sock = new MulticastSocket( null );
			sock.setReuseAddress( true );
			sock.bind(new InetSocketAddress(localAddress, MULTICAST_PORT));
			sock.joinGroup(InetAddress.getByName( MULTICAST_HOST ));
			
		} catch (Exception e) {
			e.printStackTrace();
			
			if (sock != null) {
				sock.close();
			}
			
			return null;
		}
		
		return new SsdpAdvertiseSubscriber( sock );
	}

	public void close() {
		mSock.close();
		mThread.interrupt();
	}

	public boolean isClosed() {
		return mSock.isClosed() || mThread.isInterrupted();
	}

	public void addOnAdvertisementListener(OnAdvertisementListener l) {
		synchronized (mOnAdvertisementListeners) {
			mOnAdvertisementListeners.add( l );
		}
	}

	public void removeOnAdvertisementListener(OnAdvertisementListener l) {
		synchronized (mOnAdvertisementListeners) {
			mOnAdvertisementListeners.remove( l );
		}
	}

	public void performOnAdvertisement(SsdpAdvertisement adv, InetAddress remoteHost) {
		OnAdvertisementListener [] listeners;

		synchronized (mOnAdvertisementListeners) {
			listeners = new OnAdvertisementListener[ mOnAdvertisementListeners.size() ];
			mOnAdvertisementListeners.toArray( listeners );
		}

		for (OnAdvertisementListener l: listeners) {

			if (isClosed()) break;

			try {
				l.onAdvertisement(this, adv, remoteHost);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}

