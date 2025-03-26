package org.syt.parser.aab


/*
 * zhulei 2024/8/9-下午6:22
 */
object FirebaseKey {

    // gcm_defaultSenderId google_app_id google_api_key google_crash_reporting_api_key google_storage_bucket project_id

    val stringKeys = mutableListOf(
        "gcm_defaultSenderId",
        "google_app_id",
        "google_api_key",
        "google_crash_reporting_api_key",
        "google_storage_bucket",
        "project_id"
    )

    const val GOOGLE_SERVICE_FILE_NAME = "google-services.json"

}