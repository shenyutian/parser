package org.syt.parser.entry

/*
 * zhulei 2024/8/8-下午2:21
 */
class IntentFilter {
    val icon: String? = null
    val label: String? = null
    val priority: Int? = null
    val actions: MutableList<String> = mutableListOf()
    val categories: MutableList<String> = mutableListOf()
    val dataList: MutableList<IntentData> = mutableListOf()

    fun addAction(action: String) {
        actions.add(action)
    }

    fun addCategory(category: String) {
        categories.add(category)
    }

    fun addData(data: IntentData) {
        dataList.add(data)
    }

    override fun toString(): String {
        return "IntentFilter{actions=" + this.actions + ", categories=" + this.categories + ", dataList=" + this.dataList + "}";
    }

    class IntentData {
        var scheme: String? = null
        var mimeType: String? = null
        var host: String? = null
        var pathPrefix: String? = null
        var type: String? = null

        override fun toString(): String {
            return "IntentData{scheme='" + this.scheme + '\'' + ", mimeType='" + this.mimeType + '\'' + ", host='" + this.host + '\'' + ", pathPrefix='" + this.pathPrefix + '\'' + ", type='" + this.type + '\'' + '}'
        }
    }
}