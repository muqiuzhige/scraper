package com.telegram.hunter.utils;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;

public class HttpUtils {
	
	public static <M, N> N request(Method method, String url, M body, TypeReference<N> type) {
		return request(method, url, null, null, body, type);
	}
	
	public static <M, N> N request(Method method, String url, Map<String, String> headerMap, Map<String, Object> paramMap, M body, TypeReference<N> type) {
		try {
			HttpRequest httpRequest = HttpUtil.createRequest(method, url);
			httpRequest.addHeaders(headerMap);
			httpRequest.contentType("application/json");
			httpRequest.body(JSON.toJSONString(body));
			httpRequest.form(paramMap);
			HttpResponse httpResponse = httpRequest.execute();
			if (httpResponse.isOk()) {
				return JSON.parseObject(httpResponse.body(), type);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
}
