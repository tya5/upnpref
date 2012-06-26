package org.tyas.upnp.ssdp;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Main
{
	public static void main(String [] args) {
		try {

			MulticastSocket sock = new MulticastSocket(Ssdp.DEFAULT_PORT);

			sock.joinGroup(InetAddress.getByName(Ssdp.MULTICAST_HOST));

			DatagramPacket pkt = new SsdpSearchRequest()
				.setHost(Ssdp.MULTICAST_HOST + ":" + Ssdp.DEFAULT_PORT)
				.setMaxWaitTime(5)
				.setSearchTarget("ssdp:all")
				.setMan(Ssdp.MAN_DISCOVER)
				.toDatagramPacket();

			pkt.setAddress(InetAddress.getByName(Ssdp.MULTICAST_HOST));
			pkt.setPort(Ssdp.DEFAULT_PORT);

			sock.send(pkt);

			for (int ii = 0; ; ii++) {
				new SsdpServer() {
					@Override protected void onAdvertisement(Ssdp.Advertisement adv) {
						System.out.println("onAdvertisement");
						System.out.println(" HOST:" + adv.getHost());
						System.out.println(" LOCATION:" + adv.getDescriptionUrl());
						System.out.println(" NT:" + adv.getNotificationType());
						System.out.println(" NTS:" + adv.getNotificationSubType());
						System.out.println(" USN:" + adv.getUniqueServiceName());
					}
					@Override protected void onSearchRequest(Ssdp.SearchRequest sreq) {
						System.out.println("onSearchRequest");
						System.out.println(" HOST:" + sreq.getHost());
						System.out.println(" MAN:" + sreq.getMan());
						System.out.println(" MX:" + sreq.getMaxWaitTime());
						System.out.println(" ST:" + sreq.getSearchTarget());
					}
					@Override protected void onSearchResponse(Ssdp.SearchResponse sresp) {
						System.out.println("onSearchResponse");
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
}
