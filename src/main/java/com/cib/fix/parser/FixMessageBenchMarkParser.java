package com.cib.fix.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FixMessageBenchMarkParser {
	private static final String DELIMITER = "\\|";
	private static final String EQUALS = "=";

	public static Map<Integer, String> parse(byte[] message) {
		

		
		if (message == null || message.length <= 0){
			return Collections.EMPTY_MAP;
		}
		
		HashMap<Integer, String> fixMap = new HashMap<>();
		String msg = new String(message);
		String[] tags = msg.split(DELIMITER);
		
		for (String tag : tags){
			int index = tag.indexOf(EQUALS);
			String key = tag.substring(0, index);
			String value = tag.substring(index + 1, tag.length() );
			fixMap.put(Integer.valueOf(key), value);
		}
		
		return fixMap;
	}
}