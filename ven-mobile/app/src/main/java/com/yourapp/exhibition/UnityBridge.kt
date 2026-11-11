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
        var json = currentRoomJson
        if (json.isNullOrBlank()) {
            // Fallback: provide valid RoomDto so Unity PlayScene is never stuck on black screen
            json = """{"roomId":"viewer_room","templateId":"template01","title":"Pameran 3D","description":"","wallColor":"#FFFFFF","isPublic":true,"ownerId":"owner","viewerId":"visitor","items":[]}"""
            Log.i(TAG, "Delivering fallback RoomDto to Unity PlayBridge")
        }
        UnityPlayer.UnitySendMessage("PlayBridge", "OnRoomData", json)
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
