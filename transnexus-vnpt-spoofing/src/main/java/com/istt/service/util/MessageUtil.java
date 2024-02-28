package com.istt.service.util;

import java.util.HashSet;
import java.util.Set;

public class MessageUtil {
	/**
	 * Helper function
	 *
	 * @param value
	 * @return
	 */
	public static Set<Long> toPrefixSet(String value) {
		Set<Long> result = new HashSet<>();
		if (value == null)
			return result;
		for (int i = 1; i <= value.length(); i++) {
			result.add(Long.parseLong(value.substring(0, i)));
		}
		return result;
	}
}
