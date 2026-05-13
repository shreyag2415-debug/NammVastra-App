package com.nammavastra

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

enum class UserRole {
    WEAVER, BUYER, GUEST
}

data class SareeData(
    val name: String = "",
    val material: String = "",
    val price: Int = 0,
    val weaverWhatsApp: String = "",
    val imageUrl: String = "",
    val id: String = ""
)

data class HistoryData(
    val title: String = "",
    val content: String = "",
    val imageUrl: String = ""
)

data class TrendData(
    val title: String = "",
    val desc: String = "",
    val colorHex: String = "#FFFFFF"
)

object FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchSarees(): List<SareeData> {
        return try {
            db.collection("sarees").get().await().toObjects(SareeData::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchHistory(): List<HistoryData> {
        return try {
            db.collection("history").get().await().toObjects(HistoryData::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchTrends(): List<TrendData> {
        return try {
            db.collection("trends").get().await().toObjects(TrendData::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSaree(saree: SareeData): Boolean {
        return try {
            db.collection("sarees").add(saree).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
