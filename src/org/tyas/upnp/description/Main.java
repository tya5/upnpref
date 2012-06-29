package org.tyas.upnp.description;

import org.tyas.upnp.ssdp.Ssdp;
import org.tyas.upnp.ssdp.SsdpFilter;
import org.tyas.upnp.ssdp.SsdpRootDeviceFilter;
import org.tyas.upnp.ssdp.SsdpDeviceTypeFilter;
import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpUdn;
import org.tyas.upnp.UpnpServiceId;

import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

public class Main
{
	public static void main(String [] args) {

		final Map<UpnpUdn,DeviceDescription> mDescriptionMap = new HashMap<UpnpUdn,DeviceDescription>();

		SsdpFilter.Listener listener = new SsdpFilter.Listener() {
				@Override protected void onAdded(Ssdp.RemoteDevicePointer ptr, InetAddress addr) {
					System.out.println("add " + ptr.getUniqueServiceName());

					UpnpUdn udn = ptr.getUniqueServiceName().getUdn();
					DeviceDescription desc = mDescriptionMap.get(udn);

					if (desc == null) {
						try {
							Document doc = null;

							try {
								URL url = ptr.getDescriptionUrl();
								DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
								System.out.println("DeviceDescription URL: " + url);
								URLConnection con = url.openConnection();
								con.setConnectTimeout(1000);
								con.setReadTimeout(3000);
								InputStream in = con.getInputStream();
								doc = b.parse(in);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								return;
							}
							desc = DeviceDescription.getByDocument(doc);
							mDescriptionMap.put(udn, desc);
							System.out.println("RootDevice            FriendlyName: " + desc.getRootDevice().getModelName());
							System.out.println("RootDevice               ModelName: " + desc.getRootDevice().getFriendlyName());
							System.out.println("RootDevice              DeviceType: " + desc.getRootDevice().getType());
							System.out.println("RootDevice  Num of Embeded Devices: " + desc.getRootDevice().getDeviceSet().size());
							System.out.println("RootDevice Num of Embeded Services: " + desc.getRootDevice().getServiceSet().size());

							DeviceElement root = desc.getRootDevice();
							for (UpnpServiceId id: root.getServiceSet()) {
								ServiceElement se = root.getService(id);

								try {
									URL url = new URL(ptr.getDescriptionUrl(), se.getScpdUrl());
									DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
									System.out.println("ServiceDescription URL: " + url);
									URLConnection con = url.openConnection();
									con.setConnectTimeout(1000);
									con.setReadTimeout(3000);
									InputStream in = con.getInputStream();
									doc = b.parse(in);
								} catch (Exception e) {
									System.out.println(e.getMessage());
								}
								
								ServiceDescription sd = ServiceDescription.getByDocument(doc);

								System.out.println(" Actions: " + sd.getActionNameSet().size());
								System.out.println("  States: " + sd.getStateNameSet().size());

								for (String n: sd.getActionNameSet()) {
									System.out.println("    Action["+n+"]");
								}
								for (String n: sd.getStateNameSet()) {
									System.out.println("     State["+n+"]");
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Description is cached");
					}
						
				}
			};

		SsdpFilter [] filters = new SsdpFilter[] {
			new SsdpRootDeviceFilter(listener),
			new SsdpDeviceTypeFilter(listener, new UpnpDeviceType("MediaRenderer", "1")),
		};

		org.tyas.upnp.ssdp.Main.listen(filters, 3, true);
	}
}
