package org.tyas.upnp.description;

import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpServiceType;
import org.tyas.upnp.UpnpServiceId;
import org.tyas.upnp.UpnpUdn;

import org.w3c.dom.*;

import java.util.Set;

public class Description
{
	public interface ServiceElement
	{
		DeviceElement getParent();
		DeviceDescription getDeviceDescription();
		UpnpServiceType getType();
		UpnpServiceId getId();
		String getScpdUrl();
		String getControlUrl();
		String getEventSubUrl();
	}

	public interface DeviceElement
	{
		DeviceElement getParent();
		DeviceDescription getDeviceDescription();
		UpnpDeviceType getType();
		String getFriendlyName();
		String getManufacturer();
		String getManufacturerUrl();
		String getModelDescription();
		String getModelName();
		String getModelNumber();
		String getModelUrl();
		String getSerialNumber();
		UpnpUdn getUdn();
		String getUpc();
		String getPresentationUrl();

		Set<UpnpServiceId> getServiceSet();
		Set<UpnpUdn> getDeviceSet();

		ServiceElement getService(UpnpServiceId id);
		DeviceElement getDevice(UpnpUdn udn);
	}

	public interface DeviceDescription
	{
		int getConfigId();
		int getVersionMajor();
		int getVersionMinor();
		DeviceElement getRootDevice();
	}

	public interface ServiceDescription
	{
	}

	public static String getStringByTag(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element elm = ((Element)node);

		Node c = node.getFirstChild();

		if (c == null) return "";

		String s = c.getNodeValue();

		return s == null ? "": s;
	}

	public static int getIntByTag(Node node, int defaultValue) {
		String s = getStringByTag(node);

		try {
			return Integer.decode(s);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static String getStringAttrByTag(Node node, String attr) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element e = ((Element)node);

		String s = e.getAttribute(attr);

		return s == null ? "": s;
	}

	public static int getIntAttrByTag(Node node, String attr, int defaultValue) {
		String s = getStringAttrByTag(node, attr);

		try {
			return Integer.decode(s);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
