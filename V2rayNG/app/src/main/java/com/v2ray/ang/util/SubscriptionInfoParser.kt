package com.v2ray.ang.util

import android.util.Base64
import com.v2ray.ang.dto.entities.SubscriptionItem
import okhttp3.Headers

/**
 * Reads the usual provider headers of a subscription response:
 *  - subscription-userinfo: upload=..; download=..; total=..; expire=..
 *  - profile-title: plain text, url-encoded or "base64:..."
 */
object SubscriptionInfoParser {
    private const val IMPORT_PLACEHOLDER = "import sub"

    fun apply(item: SubscriptionItem, headers: Headers?) {
        if (headers == null) return
        headers["subscription-userinfo"]?.let { parseUserInfo(item, it) }
        headers["profile-title"]?.let { raw ->
            val title = decodeTitle(raw)
            if (title.isNotBlank()) {
                // Rename only if the user has not renamed the subscription themselves.
                val untouched = item.remarks.isBlank() ||
                    item.remarks == IMPORT_PLACEHOLDER ||
                    item.remarks == item.profileTitle
                if (untouched) item.remarks = title
                item.profileTitle = title
            }
        }
    }

    private fun parseUserInfo(item: SubscriptionItem, raw: String) {
        val values = raw.split(';').mapNotNull {
            val parts = it.trim().split('=', limit = 2)
            if (parts.size == 2) parts[0].trim().lowercase() to parts[1].trim().toDoubleOrNull()?.toLong() else null
        }.toMap()
        val upload = values["upload"] ?: 0L
        val download = values["download"] ?: 0L
        if (values.containsKey("upload") || values.containsKey("download")) {
            item.trafficUsed = upload + download
        }
        values["total"]?.let { item.trafficTotal = it }
        values["expire"]?.let { item.expireAt = it }
    }

    private fun decodeTitle(raw: String): String {
        val value = raw.trim()
        if (value.startsWith("base64:", ignoreCase = true)) {
            return runCatching {
                String(Base64.decode(value.substring(7).trim(), Base64.DEFAULT), Charsets.UTF_8).trim()
            }.getOrDefault("")
        }
        return runCatching { Utils.decodeURIComponent(value) }.getOrDefault(value).trim()
    }
}
