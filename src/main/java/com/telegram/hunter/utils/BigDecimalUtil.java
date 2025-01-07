package com.telegram.hunter.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
	
	/**
	 * 保留小数点后非零外N位数字
	 * @param decimal
	 * @param scale
	 * @return
	 */
	public static String format(BigDecimal decimal, int scale) {
		if (scale <= 0) {
			return decimal.toBigInteger().toString();
		}
		
		String plainStr = decimal.toPlainString();
		int i = plainStr.indexOf(".");
		if (i == -1) {
			return plainStr;
		}
		
		int newScale = scale;
		boolean allzero = true;
		while (++i < plainStr.length()) {
			if (plainStr.charAt(i) != '0') {
				allzero = false;
				break;
			}
			newScale++;
		}
		if (allzero) {
			newScale = scale;
		}
		return decimal.setScale(newScale, RoundingMode.HALF_UP).toPlainString();
	}
	
}
