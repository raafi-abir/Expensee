package com.expensee.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class DriveApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val SYNC_FILE_NAME = "expensee_data_sync.json"
        const val DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
        const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"
        const val DRIVE_APP_DATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }

    suspend fun findSyncFileId(accessToken: String): String? = withContext(Dispatchers.IO) {
        val url = "$DRIVE_FILES_URL?spaces=appDataFolder&q=name='$SYNC_FILE_NAME'+and+trashed=false&fields=files(id,name)"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 401) {
                    throw DriveAuthException("Drive authentication token expired or invalid (HTTP 401)")
                }
                throw IOException("Failed to query Drive appDataFolder: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val filesArray = json.optJSONArray("files")
            if (filesArray != null && filesArray.length() > 0) {
                filesArray.getJSONObject(0).optString("id", null)
            } else {
                null
            }
        }
    }

    suspend fun downloadSyncFile(accessToken: String, fileId: String): String? = withContext(Dispatchers.IO) {
        val url = "$DRIVE_FILES_URL/$fileId?alt=media"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 401) {
                    throw DriveAuthException("Drive authentication token expired (HTTP 401)")
                }
                if (response.code == 404) {
                    return@withContext null
                }
                throw IOException("Failed to download sync file from Drive: HTTP ${response.code}")
            }
            response.body?.string()
        }
    }

    suspend fun uploadNewSyncFile(accessToken: String, content: String): String = withContext(Dispatchers.IO) {
        val metadataJson = JSONObject().apply {
            put("name", SYNC_FILE_NAME)
            put("parents", org.json.JSONArray().put("appDataFolder"))
        }.toString()

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType())
            )
            .addPart(
                content.toRequestBody("application/octet-stream".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$DRIVE_UPLOAD_URL?uploadType=multipart")
            .header("Authorization", "Bearer $accessToken")
            .post(multipartBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 401) {
                    throw DriveAuthException("Drive authentication token expired (HTTP 401)")
                }
                throw IOException("Failed to create sync file in Drive: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            json.optString("id", "")
        }
    }

    suspend fun updateSyncFile(accessToken: String, fileId: String, content: String) = withContext(Dispatchers.IO) {
        val mediaBody = content.toRequestBody("application/octet-stream".toMediaType())

        val request = Request.Builder()
            .url("$DRIVE_UPLOAD_URL/$fileId?uploadType=media")
            .header("Authorization", "Bearer $accessToken")
            .patch(mediaBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 401) {
                    throw DriveAuthException("Drive authentication token expired (HTTP 401)")
                }
                throw IOException("Failed to update sync file in Drive: HTTP ${response.code}")
            }
        }
    }
}

class DriveAuthException(message: String) : IOException(message)
