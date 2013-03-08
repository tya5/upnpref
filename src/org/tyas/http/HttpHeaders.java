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

	public static void deepcopy(Map<String,List<String>> src, Map<String,List<String>> dst) {
		if (src != null && dst != null) {
			for (String key: src.keySet()) {
				dst.put(key, new ArrayList<String>(src.get(key)));
			}
		}
	}
}