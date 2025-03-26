package org.syt.parser.entry

import java.util.*

/*
 * zhulei 2024/8/8-下午2:12
 */
data class ApkMeta(
    var packageName: String? = null,
    var label: String? = null,
    var icon: String? = null,
    var versionName: String? = null,
    var versionCode: Long? = null,
    var revisionCode: Long? = null,
    var minSdkVersion: String? = null,
    var sharedUserId: String? = null,
    var sharedUserLabel: String? = null,
    var split: String? = null,
    var configForSplit: String? = null,
    var isFeatureSplit: Boolean? = null,
    var isSplitRequired: Boolean? = null,
    var isolatedSplits: Boolean? = null,
    var installLocation: String? = null,
    var targetSdkVersion: String? = null,
    var maxSdkVersion: String? = null,
    var compileSdkVersion: String? = null,
    var compileSdkVersionCodename: String? = null,
    var platformBuildVersionCode: String? = null,
    var platformBuildVersionName: String? = null,
    var glEsVersion: GlEsVersion ?= null,
    var anyDensity : Boolean = false,
    var smallScreens : Boolean = false,
    var normalScreens : Boolean = false,
    var largeScreens : Boolean = false,
    var application: Application = Application(),
    var usesPermissions: MutableList<String> = mutableListOf(),
    var permissions: MutableList<Permission> = mutableListOf(),
    var services: MutableList<Service> = mutableListOf(),
    var activities: MutableList<Activity> = mutableListOf(),
    var activityAliasList: MutableList<ActivityAlias> = mutableListOf(),
    var receivers: MutableList<Receiver> = mutableListOf(),
    var providers: MutableList<Provider> = mutableListOf(),
    var intentFilters: MutableList<IntentFilter> = mutableListOf(),
    var usesFeatures: MutableList<UseFeature> = mutableListOf(),
    var queries: Queries = Queries(),
) {

    fun addUsesFeature(usesFeature: UseFeature) {
        usesFeatures.add(usesFeature)
    }

    fun addPermissions(permission: Permission) {
        permissions.add(permission)
    }

    fun addService(service: Service) {
        services.add(service)
    }

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun addReceiver(receiver: Receiver) {
        receivers.add(receiver)
    }

    fun addProvider(provider: Provider) {
        providers.add(provider)
    }

    fun addActivityAlias(activityAlias: ActivityAlias) {
        activityAliasList.add(activityAlias)
    }

    fun addIntentFilter(intentFilter: IntentFilter) {
        intentFilters.add(intentFilter)
    }

    fun addUsesPermission(permission: String) {
        usesPermissions.add(permission)
    }

    fun getLauncher(): Activity? {
        // 有问题，需要找到第一个
        for (activity in activities) {
            val intentFiltersActivity = activity.intentFilters
            for (intentFilter in intentFiltersActivity) {
                val actions = intentFilter.actions
                val categories = intentFilter.categories
                if (actions.contains("android.intent.action.MAIN") && categories.contains("android.intent.category.LAUNCHER")) {
                    return activity
                }
            }
        }
        return null
    }

    fun getLauncherAll(): List<Activity> {
        val launcherAll: MutableList<Activity> = ArrayList()
        for (activity in activities) {
            val intentFiltersActivity = activity.intentFilters
            for (intentFilter in intentFiltersActivity) {
                val actions = intentFilter.actions
                val categories = intentFilter.categories
                if (actions.contains("android.intent.action.MAIN") && categories.contains("android.intent.category.LAUNCHER")) {
                    launcherAll.add(activity)
                }
            }
        }
        return launcherAll
    }

    override fun toString(): String {
        return ("packageName: \t" + packageName + "\n"
                + "label: \t" + label + "\n"
                + "icon: \t" + icon + "\n"
                + "versionName: \t" + versionName + "\n"
                + "versionCode: \t" + versionCode + "\n"
                + "minSdkVersion: \t" + minSdkVersion + "\n"
                + "targetSdkVersion: \t" + targetSdkVersion + "\n"
                + "maxSdkVersion: \t" + maxSdkVersion)
    }

}

data class UseFeature(
    val name: String? = null,
    val required: Boolean = false,
)
