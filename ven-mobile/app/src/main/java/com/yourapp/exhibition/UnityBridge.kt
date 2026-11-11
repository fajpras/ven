package com.yourapp.exhibition

import android.util.Log
import com.unity3d.player.UnityPlayer

/**
 * Bridge class matching Unity's PlayBridge default package and method signatures:
 * Unity calls:
 * - PlayBridge_RequestRoom -> com.yourapp.exhibition.UnityBridge.requestRoom()
 * - PlayBridge_OpenSchedule -> com.yourapp.exhibition.UnityBridge.openSchedule(roomId)
 */
object UnityBridge {

    private const val TAG = "UnityBridge"

    var currentRoomJson: String? = null
    var onScheduleRequested: ((roomId: String) -> Unit)? = null

    @JvmStatic
    fun requestRoom() {
        Log.d(TAG, "Unity requested room data")
        val json = currentRoomJson
        if (!json.isNullOrBlank()) {
            UnityPlayer.UnitySendMessage("PlayBridge", "OnRoomData", json)
        } else {
            Log.w(TAG, "No room JSON available to deliver to Unity PlayBridge")
        }
    }

    @JvmStatic
    fun openSchedule(roomId: String) {
        Log.d(TAG, "Unity requested open schedule for room: $roomId")
        onScheduleRequested?.invoke(roomId)
    }

    @JvmStatic
    fun sendRoomData(json: String) {
        currentRoomJson = json
        UnityPlayer.UnitySendMessage("PlayBridge", "OnRoomData", json)
    }

    @JvmStatic
    fun sendError(message: String) {
        UnityPlayer.UnitySendMessage("PlayBridge", "OnRoomError", message)
    }
}
