package com.telegram.hunter.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import cn.hutool.core.thread.ThreadUtil;

public class LogPrint {
	
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private static final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>(1);
	
	static {
		executor.execute(() -> {
			while (true) {
				try {
					String msg = blockingQueue.poll();
					if (StringUtils.isNotBlank(msg)) {
						System.out.println(msg);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				ThreadUtil.sleep(1000);
			}
		});
	}
	
	public static void log(String msg, Object... arguments) {
		if (msg == null) {
            return;
        }
        FormattingTuple ft = MessageFormatter.arrayFormat(msg, arguments);
        blockingQueue.offer(ft.getMessage());
	}
	
}
