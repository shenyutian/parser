package org.apk.parser.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 已安装应用列表适配器：参考系统级应用管理工具的信息密度展示
 * 图标 + 名称 + 包名 + 版本/更新时间/目标 SDK，点击回调返回包名
 */
class AppListAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
    private var appList: List<AppInfo> = emptyList()

    fun submitList(list: List<AppInfo>) {
        appList = list
        notifyDataSetChanged()
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        val packageNameTextView: TextView = itemView.findViewById(R.id.packageNameTextView)
        val appIconImageView: ImageView = itemView.findViewById(R.id.appIconImageView)
        val versionTextView: TextView = itemView.findViewById(R.id.versionTextView)
        val metaTextView: TextView = itemView.findViewById(R.id.metaTextView)
        val issuerTextView: TextView = itemView.findViewById(R.id.issuerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appNameTextView.text = appInfo.appName
        holder.packageNameTextView.text = appInfo.packageName
        holder.appIconImageView.setImageDrawable(appInfo.icon)
        holder.versionTextView.text = appInfo.versionName
        holder.metaTextView.text = holder.itemView.context.getString(
            R.string.item_meta,
            dateFormat.format(appInfo.updateTime),
            daysAgo(appInfo.updateTime),
            appInfo.targetSdkVersion
        )
        if (appInfo.certIssuer != null) {
            holder.issuerTextView.visibility = View.VISIBLE
            holder.issuerTextView.text = holder.itemView.context.getString(
                R.string.cert_issuer,
                appInfo.certIssuer
            )
        } else {
            holder.issuerTextView.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onItemClick(appInfo.packageName) }
    }

    private fun daysAgo(time: Long): Long =
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time).coerceAtLeast(0)

    override fun getItemCount(): Int = appList.size
}
