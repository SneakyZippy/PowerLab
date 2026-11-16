package com.sneakyzippy.powerlab.core.engine

import android.content.SharedPreferences
import android.util.Log
import com.sneakyzippy.powerlab.core.model.ChargeTelemetrySample
import com.sneakyzippy.powerlab.core.model.ChargeTestSession
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stages a completed charging session in SharedPreferences while waiting for the user
 * to tap "Tag & Save", so process death between unplugging and tapping the notification
 * does not lose the session, while swiping/dismissing discards it without touching Room.
 */
class PendingSessionStore(private val prefs: SharedPreferences) {

    fun save(session: ChargeTestSession, samples: List<ChargeTelemetrySample>) {
        val root = JSONObject().apply {
            put("session", serializeSession(session))
            put("samples", serializeSamples(samples))
        }
        prefs.edit().putString(KEY_PENDING_SESSION_JSON, root.toString()).apply()
    }

    fun restore(): Pair<ChargeTestSession, List<ChargeTelemetrySample>>? {
        val raw = prefs.getString(KEY_PENDING_SESSION_JSON, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val session = deserializeSession(root.getJSONObject("session"))
            val samples = deserializeSamples(root.optJSONArray("samples"))
            Pair(session, samples)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to restore pending session, clearing stale state", e)
            clear()
            null
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_PENDING_SESSION_JSON).apply()
    }

    private fun serializeSession(s: ChargeTestSession): JSONObject = JSONObject().apply {
        put("id", s.id)
        put("brickId", s.brickId ?: JSONObject.NULL)
        put("cableId", s.cableId ?: JSONObject.NULL)
        put("brickName", s.brickName)
        put("cableName", s.cableName)
        put("startTimeMs", s.startTimeMs)
        put("endTimeMs", s.endTimeMs)
        put("startBatteryLevel", s.startBatteryLevel)
        put("endBatteryLevel", s.endBatteryLevel)
        put("peakWattage", s.peakWattage.toDouble())
        put("avgWattage", s.avgWattage.toDouble())
        put("minVoltageMv", s.minVoltageMv)
        put("maxVoltageMv", s.maxVoltageMv)
        put("peakCurrentMa", s.peakCurrentMa)
        put("startTempDeciC", s.startTempDeciC)
        put("maxTempDeciC", s.maxTempDeciC)
        put("isThrottled", s.isThrottled)
        put("notes", s.notes)
    }

    private fun deserializeSession(obj: JSONObject): ChargeTestSession = ChargeTestSession(
        id = obj.getString("id"),
        brickId = obj.optString("brickId").takeIf { !obj.isNull("brickId") && it.isNotEmpty() },
        cableId = obj.optString("cableId").takeIf { !obj.isNull("cableId") && it.isNotEmpty() },
        brickName = obj.optString("brickName", "Unspecified Charger"),
        cableName = obj.optString("cableName", "Unspecified Cable"),
        startTimeMs = obj.getLong("startTimeMs"),
        endTimeMs = obj.getLong("endTimeMs"),
        startBatteryLevel = obj.getInt("startBatteryLevel"),
        endBatteryLevel = obj.getInt("endBatteryLevel"),
        peakWattage = obj.getDouble("peakWattage").toFloat(),
        avgWattage = obj.getDouble("avgWattage").toFloat(),
        minVoltageMv = obj.getInt("minVoltageMv"),
        maxVoltageMv = obj.getInt("maxVoltageMv"),
        peakCurrentMa = obj.getInt("peakCurrentMa"),
        startTempDeciC = obj.getInt("startTempDeciC"),
        maxTempDeciC = obj.getInt("maxTempDeciC"),
        isThrottled = obj.optBoolean("isThrottled", false),
        notes = obj.optString("notes", "")
    )

    private fun serializeSamples(samples: List<ChargeTelemetrySample>): JSONArray = JSONArray().apply {
        samples.forEach { sample ->
            put(JSONObject().apply {
                put("sessionId", sample.sessionId)
                put("timestampMs", sample.timestampMs)
                put("batteryLevel", sample.batteryLevel)
                put("voltageMv", sample.voltageMv)
                put("currentMa", sample.currentMa)
                put("wattage", sample.wattage.toDouble())
                put("tempDeciC", sample.tempDeciC)
            })
        }
    }

    private fun deserializeSamples(array: JSONArray?): List<ChargeTelemetrySample> {
        if (array == null) return emptyList()
        return List(array.length()) { idx ->
            val obj = array.getJSONObject(idx)
            ChargeTelemetrySample(
                sessionId = obj.getString("sessionId"),
                timestampMs = obj.getLong("timestampMs"),
                batteryLevel = obj.getInt("batteryLevel"),
                voltageMv = obj.getInt("voltageMv"),
                currentMa = obj.getInt("currentMa"),
                wattage = obj.getDouble("wattage").toFloat(),
                tempDeciC = obj.getInt("tempDeciC")
            )
        }
    }

    companion object {
        private const val TAG = "PendingSessionStore"
        private const val KEY_PENDING_SESSION_JSON = "pending_completed_session_json"
    }
}
