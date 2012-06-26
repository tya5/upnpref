package org.tyas.upnp.device;

import org.tyas.upnp.ssdp.SsdpServer;

public class Main
{
	public static void main(String [] args) {

		UpnpRemoteDeviceManager mgr = new UpnpRemoteDeviceManager();
		SsdpServer server = new SsdpServer(mgr.getSsdpHandler());
		MulticastSocket sock = new MulticastSocket(Ssdp.DEFAULT_PORT);
		DatagramPacket pkt = new SsdpSearchRequest()
			.setHost(Ssdp.MULTICAST_HOST + ":" + Ssdp.DEFAULT_PORT)
			.setMaxWaitTime(5)
			.setSearchTarget("ssdp:all")
			.setMan(Ssdp.MAN_DISCOVER)
			.toDatagramPacket();

		pkt.setAddress(InetAddress.getByName(Ssdp.MULTICAST_HOST));
		pkt.setPort(Ssdp.DEFAULT_PORT);

		sock.joinGroup(InetAddress.getByName(Ssdp.MULTICAST_HOST));
		new DatagramSocket().send(pkt);

		for (int ii = 0; ii < 10; ii++) {

			server.accept(sock).run();

		}

		Set<Upnp.Udn> keys = mgr.getRootDeviceSet();

		for (Upnp.Udn udn: keys) {

			Upnp.Device dev = mgr.getDevice(udn);

			System.out.println(dev.toString());

		}
	}
}
