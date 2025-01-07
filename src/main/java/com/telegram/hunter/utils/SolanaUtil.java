package com.telegram.hunter.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bitcoinj.core.Base58;

public class SolanaUtil {
	
	/**
	 * 解析字符串中solana地址
	 * @param text
	 */
	public static String analyzeSolanaAddress(String text) {
		String solanaAddressPattern = "[1-9A-HJ-NP-Za-km-z]{32,44}";
		Pattern pattern = Pattern.compile(solanaAddressPattern);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String potentialAddress = matcher.group();
			if (isValidSolanaAddress(potentialAddress)) {
				return potentialAddress;
			}
		}
		return null;
	}
	
	/**
	 * 校验Solana地址是否合法
	 * @param address
	 * @return
	 */
	public static boolean isValidSolanaAddress(String address) {
		try {
			byte[] decoded = Base58.decode(address);
			return decoded.length == 32;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
}
