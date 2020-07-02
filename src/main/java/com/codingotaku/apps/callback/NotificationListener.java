package com.codingotaku.apps.callback;

import com.codingotaku.apps.custom.NotificationController;

public interface NotificationListener {
	void created(NotificationController.Type type, String message);
	void closed(NotificationController notification);
}
