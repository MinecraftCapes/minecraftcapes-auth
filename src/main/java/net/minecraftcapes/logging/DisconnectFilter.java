package net.minecraftcapes.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class DisconnectFilter extends AbstractFilter {

	@Override
	public Result filter(LogEvent event) {
		Message message = event.getMessage();

		if (message == null) {
			return Result.NEUTRAL;
		}

		String formatted = message.getFormattedMessage();

		if (formatted.contains(" disconnected:") && formatted.contains("authorization")) {
			return Result.DENY;
		}

		return Result.NEUTRAL;
	}
}