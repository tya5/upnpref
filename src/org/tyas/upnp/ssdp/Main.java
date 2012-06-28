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

	public static void main1(String [] args) {
		try {

			MulticastSocket sock = new MulticastSocket(Ssdp.DEFAULT_PORT);

			sock.joinGroup(InetAddress.getByName(Ssdp.MULTICAST_HOST));

			DatagramPacket pkt = new SsdpSearchRequest()
				.setHost(Ssdp.MULTICAST_HOST + ":" + Ssdp.DEFAULT_PORT)
				.setMaxWaitTime(5)
				.setSearchTarget("urn:schemas-upnp-org:device:MediaServer:1")
				.setMan(Ssdp.MAN_DISCOVER)
				.toDatagramPacket();

			pkt.setAddress(InetAddress.getByName(Ssdp.MULTICAST_HOST));
			pkt.setPort(Ssdp.DEFAULT_PORT);

			sock.send(pkt);

			for (int ii = 0; ; ii++) {
				new SsdpServer() {
					@Override protected void onAdvertisement(Ssdp.Advertisement adv, SsdpServer.Context ctx) {
						System.out.println("onAdvertisement");
						System.out.println(" from:" + ctx.getPacket().getAddress());
						System.out.println(" HOST:" + adv.getHost());
						System.out.println(" LOCATION:" + adv.getDescriptionUrl());
						System.out.println(" NT:" + adv.getNotificationType());
						System.out.println(" NTS:" + adv.getNotificationSubType());
						System.out.println(" USN:" + adv.getUniqueServiceName());
					}
					@Override protected void onSearchRequest(Ssdp.SearchRequest sreq, SsdpServer.Context ctx) {
						System.out.println("onSearchRequest");
						System.out.println(" from:" + ctx.getPacket().getAddress());
						System.out.println(" HOST:" + sreq.getHost());
						System.out.println(" MAN:" + sreq.getMan());
						System.out.println(" MX:" + sreq.getMaxWaitTime());
						System.out.println(" ST:" + sreq.getSearchTarget());
					}
					@Override protected void onSearchResponse(Ssdp.SearchResponse sresp, SsdpServer.Context ctx) {
						System.out.println("onSearchResponse");
						System.out.println(" from:" + ctx.getPacket().getAddress());
						System.out.println(" LOCATION:" + sresp.getDescriptionUrl());
						System.out.println(" ST:" + sresp.getSearchTarget());
						System.out.println(" USN:" + sresp.getUniqueServiceName());
					}
				}.accept(sock).run();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main2(String [] args) {
		try {
			SsdpFilter [] filters = new SsdpFilter[] {
				new SsdpRootDeviceFilter() {
					@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("add " + ptr.getUniqueServiceName());
					}
					@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("rem " + ptr.getUniqueServiceName());
					}
				},
				new SsdpDeviceTypeFilter(new UpnpDeviceType(UpnpDeviceType.SCHEMAS_UPNP_ORG, "MediaServer", "1")) {
					@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("add " + ptr.getUniqueServiceName());
					}
					@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("rem " + ptr.getUniqueServiceName());
					}
				},
				new SsdpServiceTypeFilter(new UpnpServiceType(UpnpServiceType.SCHEMAS_UPNP_ORG, "Layer3Forwarding", "1")) {
					@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("add " + ptr.getUniqueServiceName());
					}
					@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("rem " + ptr.getUniqueServiceName());
					}
				},
				new SsdpDeviceFilter(new UpnpUdn("5F9EC1B3-ED59-79BB-4530-00E036ED7715")) {
					@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("add " + ptr.getUniqueServiceName());
					}
					@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("rem " + ptr.getUniqueServiceName());
					}
				},
				new SsdpDeviceFilter(new UpnpUdn("9a4bd800-1dd1-11b2-8000-003a9d6522f5")) {
					@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("add " + ptr.getUniqueServiceName());
					}
					@Override protected void onRemoved(Ssdp.RemoteDevicePointer ptr, InetAddress adr) {
						System.out.println("rem " + ptr.getUniqueServiceName());
					}
				},
			};

			SsdpServer server = new SsdpServer();
			MulticastSocket sock = new MulticastSocket(Ssdp.DEFAULT_PORT);
			SsdpSearchRequest req = new SsdpSearchRequest()
				.setHost(Ssdp.MULTICAST_HOST, Ssdp.DEFAULT_PORT)
				.setMaxWaitTime(3)
				.setMan(Ssdp.MAN_DISCOVER);

			sock.joinGroup(InetAddress.getByName(Ssdp.MULTICAST_HOST));

			for (SsdpFilter filter: filters) {
				server.addHandler(filter.getSsdpHandler());

				DatagramPacket pkt = req
					.setSearchTarget(filter.getSearchTarget())
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
