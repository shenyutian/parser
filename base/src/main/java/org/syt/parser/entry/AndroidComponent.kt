package org.syt.parser.entry

/*
 * zhulei 2024/8/8-下午2:20
 */
abstract class AndroidComponent(
    var enabled: String? = null,
    var name: String? = null,
    var exported: Boolean = false,
    var process: String? = null,
    var label: String? = null,
    var icon: String? = null,
    var permission: String? = null,
    val intentFilters: MutableList<IntentFilter> = mutableListOf(),
    val metaDatas: MutableList<MetaData> = mutableListOf(),
    val propertys: MutableList<Property> = mutableListOf(),
) : BaseBean {

    fun addMetaData(metaData: MetaData) {
        metaDatas.add(metaData)
    }

    fun addIntentFilter(intentFilter: IntentFilter) {
        intentFilters.add(intentFilter)
    }

    fun addProperty(property: Property) {
        propertys.add(property)
    }

    override fun getFileName(): String? {
        return name
    }

}

data class MetaData(
    val name: String? = null,
    val resource: String? = null,
    val value: String? = null,
)
