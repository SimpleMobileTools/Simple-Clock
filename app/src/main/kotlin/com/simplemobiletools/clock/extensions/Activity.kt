package com.simplemobiletools.clock.extensions

import com.simplemobiletools.clock.BuildConfig
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.PermissionRequiredDialog
import com.simplemobiletools.commons.extensions.canUseFullScreenIntent
import com.simplemobiletools.commons.extensions.openFullScreenIntentSettings
import com.simplemobiletools.commons.extensions.openNotificationSettings

fun BaseSimpleActivity.handleFullScreenNotificationsPermission(
    notificationsCallback: (granted: Boolean) -> Unit,
) {
    handleNotificationPermission { granted ->
        if (granted) {
            if (canUseFullScreenIntent()) {
                notificationsCallback(true)
            } else {
                PermissionRequiredDialog(
                    activity = this,
                    textId = com.simplemobiletools.commons.R.string.allow_full_screen_notifications_reminders,
                    positiveActionCallback = {
                        openFullScreenIntentSettings(BuildConfig.APPLICATION_ID)
                    },
                    negativeActionCallback = {
                        notificationsCallback(false)
                    }
                )
            }
        } else {
            PermissionRequiredDialog(
                activity = this,
                textId = com.simplemobiletools.commons.R.string.allow_notifications_reminders,
                positiveActionCallback = {
                    openNotificationSettings()
                },
                negativeActionCallback = {
                    notificationsCallback(false)
                }
            )
        }
    }
}
