package com.mno.ethermom.utils.messaging;

import com.mno.ethermom.utils.ConfigUtil;

public class MessagingUtil {

	public static void sendMessage(String message) throws Exception {
		
		if (ConfigUtil.isTelegramEnabled()) {
			TelegramUtil.sendMessage(message);
		}
		
		if (ConfigUtil.isIFTTTEnabled()) {
			IFTTTUtil.sendMessage(message);
		}
	}

}
