package org.tyas.upnp.description;

import org.tyas.upnp.UpnpDeviceType;
import org.tyas.upnp.UpnpServiceType;
import org.tyas.upnp.UpnpServiceId;
import org.tyas.upnp.UpnpUdn;

import org.w3c.dom.*;

import java.util.Set;
import java.util.List;

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

	public interface ArgumentElement
	{
		String getName();
		boolean isDirectionIn();
		boolean isRetval();
		String getStateVariableName();
	}

	public interface ActionElement
	{
		String getName();
		int getArgumentsLength();
		ArgumentElement getArgument(int idx);
	}

	public interface ValueRangeElement
	{
		String getMinimum();
		String getMaximum();
		String getStep();
	}

	public interface StateElement
	{
		boolean isSendEvents();
		boolean isMulticast();
		String getName();
		String getDataType();
		String getDefaultValue();
		ValueRangeElement getValueRange();
		List<String> getValueList();
	}

	public interface ServiceDescription
	{
		int getConfigId();
		int getVersionMajor();
		int getVersionMinor();
		Set<String> getActionNameSet();
		Set<String> getStateNameSet();
		ActionElement getActionElement(String name);
		StateElement getStateElement(String name);
	}

	public static String getStringByNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element elm = ((Element)node);

		Node c = node.getFirstChild();

		if (c == null) return "";

		String s = c.getNodeValue();

		return s == null ? "": s;
	}

	public static int getIntByNode(Node node, int defaultValue) {
		String s = getStringByNode(node);

		try {
			return Integer.decode(s);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static String getStringAttrByNode(Node node, String attr) {
		if (node.getNodeType() != Node.ELEMENT_NODE) return "";

		Element e = ((Element)node);

		String s = e.getAttribute(attr);

		return s == null ? "": s;
	}

	public static int getIntAttrByNode(Node node, String attr, int defaultValue) {
		String s = getStringAttrByNode(node, attr);

		try {
			return Integer.decode(s);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
