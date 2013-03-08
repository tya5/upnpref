package org.tyas.upnp.ssdp;

import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpServiceType;
import org.tyas.upnp.UpnpUdn;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Main
{
	public static void main(String [] args) {
		main2(args);
	}

	public static void main2(String [] args) {
		SsdpFilter.Listener listener = new SsdpFilter.Listener()
			{
				@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
					System.out.println("add " + ptr.getUniqueServiceName());
				}
				@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
					System.out.println("rem " + ptr.getUniqueServiceName());
				}
			};

		SsdpFilter [] filters = new SsdpFilter[] {
			new SsdpRootDeviceFilter(listener),
			new SsdpDeviceTypeFilter(listener, new UpnpDeviceType("MediaServer", "1")),
			new SsdpServiceTypeFilter(listener, new UpnpServiceType("Layer3Forwarding", "1")),
			new SsdpDeviceFilter(listener, new UpnpUdn("5F9EC1B3-ED59-79BB-4530-00E036ED7715")),
			new SsdpDeviceFilter(listener, new UpnpUdn("9a4bd800-1dd1-11b2-8000-003a9d6522f5")),
		};

		listen(filters, 3, false);
	}

	public static void listen(SsdpFilter [] filters, int mx, boolean all) {
		SsdpServer server = new SsdpServer();
		SsdpSearchRequest.Builder req = new SsdpSearchRequest.Builder()
			.setMaxWaitTime(mx)
			.setMan(Ssdp.MAN_DISCOVER);
		req.mHttpMessageBuilder.setHost(Ssdp.MULTICAST_HOST, Ssdp.DEFAULT_PORT);

		try {
			MulticastSocket sock = new MulticastSocket(Ssdp.DEFAULT_PORT);

			sock.joinGroup(InetAddress.getByName(Ssdp.MULTICAST_HOST));
			
			for (SsdpFilter filter: filters) {
				server.addHandler(filter.getSsdpHandler());

				if (! all) {
					DatagramPacket pkt = req
						.setSearchTarget(filter.getSearchTarget())
						.build()
						.toDatagramPacket();

					pkt.setAddress(InetAddress.getByName(Ssdp.MULTICAST_HOST));
					pkt.setPort(Ssdp.DEFAULT_PORT);
					sock.send(pkt);
				}
			}

			if (all) {
				DatagramPacket pkt = req
					.setSearchTarget(Ssdp.ST_ALL)
					.build()
					.toDatagramPacket();

				pkt.setAddress(InetAddress.getByName(Ssdp.MULTICAST_HOST));
				pkt.setPort(Ssdp.DEFAULT_PORT);
				sock.send(pkt);
			}

			for (int ii = 0; ; ii++) {
				server.accept(sock).run();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
