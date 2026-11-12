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
            json = """{"roomId":"viewer_room","templateId":"template01","title":"Pameran 3D","description":"","wallColor":"#FFFFFF","isPublic":true,"ownerId":"owner","viewerId":"visitor","items":[
                {"prefabId":"hpanel_01","position":{"x":0,"y":1.8,"z":5.2},"rotation":{"x":0,"y":180,"z":0}},
                {"prefabId":"vpanel_01","position":{"x":-4.8,"y":1.8,"z":1.5},"rotation":{"x":0,"y":90,"z":0}},
                {"prefabId":"vpanel_02","position":{"x":4.8,"y":1.8,"z":1.5},"rotation":{"x":0,"y":-90,"z":0}},
                {"prefabId":"hpanel_02","position":{"x":-2.5,"y":1.8,"z":-4.8},"rotation":{"x":0,"y":0,"z":0}},
                {"prefabId":"sculpture_01","position":{"x":2,"y":0,"z":0},"rotation":{"x":0,"y":-45,"z":0}},
                {"prefabId":"bench_01","position":{"x":-2,"y":0,"z":0},"rotation":{"x":0,"y":90,"z":0}}
            ]}"""
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
