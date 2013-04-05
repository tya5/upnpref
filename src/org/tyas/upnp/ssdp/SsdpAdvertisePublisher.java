package org.tyas.upnp.ssdp;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.tyas.upnp.*;

public class SsdpAdvertisePublisher implements SsdpConstant
{
	private final DatagramSocket mSock;

	private SsdpAdvertisePublisher(DatagramSocket sock) {
		mSock = sock;
	}

	public static SsdpAdvertisePublisher newInstance(InetAddress localAddress) {
		SsdpAdvertisePublisher pub = null;
		int retryMax = 10;
		int retry = 0;
		Random r = new Random();
		
		while ((pub == null) && ((retry++) < retryMax)) {
			try {
				DatagramSocket sock = new DatagramSocket( null );
				int port = 49152 + r.nextInt(65535 - 49152);
				
				sock.setReuseAddress( true );
				sock.bind(new InetSocketAddress(localAddress, port));
				pub = new SsdpAdvertisePublisher(sock);
				
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		
		return pub;
	}

	public void close() {
		mSock.close();
	}

	public void notify(SsdpRootDeviceSpec spec, UpnpUsn usn, String nt, SsdpAdvertisement.Nts nts) {
		SsdpAdvertisement.Builder b = new SsdpAdvertisement.Builder(MULTICAST_HOST, MULTICAST_PORT);
		
		b.setDescriptionUrl( spec.getDeviceDescriptionLocation() );
		b.setUsn( usn.toString() );
		b.setSearchPort( spec.getUnicastSearchPort() );
		b.setBootId( spec.getBootId() );
		b.setConfigId( spec.getConfigId() );
		b.setNotificationType( nt );
		b.setNotificationSubType( nts );

		try {
			DatagramPacket pkt = b.build().toDatagramPacket();
			
			pkt.setAddress(InetAddress.getByName(MULTICAST_HOST));
			pkt.setPort(MULTICAST_PORT);
			
			mSock.send( pkt );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
