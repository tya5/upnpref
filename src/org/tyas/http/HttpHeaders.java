package org.tyas.http;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class HttpHeaders extends HashMap<String,List<String>>
{
	public int getCount(String key) {
		List<String> l = get(key);
		return (l == null) ? -1: l.size();
	}

	public String getAt(String key, int idx) {
		List<String> l = get(key);
		return (l != null && idx < l.size()) ? l.get(idx): null;
	}

	public String getFirst(String key) {
		return getAt(key, 0);
	}

	public void add(String name, String value) {
		List<String> list = get(name);

		if (list == null) {
			list = new ArrayList<String>();
			put(name, list);
		}

		list.add(value);
	}

	public void set(String name, int index, String value) {
		List<String> list = get(name);
		
		if (list == null) {
			list = new ArrayList<String>();
			put(name, list);
		}
		
		if (index < list.size()) {
			list.set(index, value);
		} else {
			while (index > list.size()) {
				list.add("");
			}
			list.add(value);
		}
	}

	public void setFirst(String name, String value) {
		set(name, 0, value);
	}

	public void setSecond(String name, String value) {
		set(name, 1, value);
	}

	public void setThird(String name, String value) {
		set(name, 2, value);
	}

	public int getInt(String name, int defaultValue) {
		try {
			return Integer.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public long getLong(String name, long defaultValue) {
		try {
			return Long.decode(getFirst(name));
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static void deepcopy(Map<String,List<String>> src, Map<String,List<String>> dst) {
		if (src != null && dst != null) {
			for (String key: src.keySet()) {
				dst.put(key, new ArrayList<String>(src.get(key)));
			}
		}
	}
}