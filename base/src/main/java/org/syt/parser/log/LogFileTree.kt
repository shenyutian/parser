package org.syt.parser.log

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * timber 写入 log 中 需要默认给一个log路径
 */
class LogFileTree(
    val rootFile: File
) : Log.DebugTree() {

    private val MAX_BYTES = 5000 * 1024 // 5000K平均为每个文件40000行
    private val NEW_LINE = System.getProperty("line.separator")
    private val NEW_LINE_REPLACEMENT = " <br> "
    private val SEPARATOR = ","
    private val logNameFormat: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
    private var writeThread: WriteThread

    init {
        rootFile.listFiles()?.forEach {
            if (it.lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)) {
                it.delete()
            }
        }
        rootFile.mkdirs()
        writeThread = WriteThread(rootFile.absolutePath, MAX_BYTES).apply {
            Thread(this).start()
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "$tag ", message, t)
        val builder = StringBuilder()
        builder.append(logNameFormat.format(Date()))
            .append(SEPARATOR)
            .append(getLogName(priority))
            .append(SEPARATOR)
            .append(tag)
            .append(SEPARATOR)

        // message
        var msg = message
        if (message.contains(NEW_LINE)) {
            // 新行将破坏CSV格式，因此我们在此处替换它
            msg = msg.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
        }
        builder.append(msg)
        // new line
        builder.append(NEW_LINE)

        writeThread.sendMessage(builder.toString())
    }

    private fun getLogName(priority: Int) =
        when (priority) {
            Log.VERBOSE -> "verbose"
            Log.DEBUG -> "debug"
            Log.INFO -> "info"
            Log.WARN -> "warn"
            Log.ERROR -> "error"
            Log.ASSERT -> "assert"
            else -> "other $priority"
        }

    class WriteThread(
        folder: String?,
        maxFileSize: Int
    ) : Runnable {
        val queue: ArrayDeque<String> = ArrayDeque()

        private val folder: String = checkNotNull(folder)
        private val maxFileSize: Int = maxFileSize
        private val logNameFormat: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd", Locale.UK)

        override fun run() {
            while (true) {
                val msg = queue.pollFirst()
                if (msg.isNullOrEmpty()) {
                    Thread.sleep(50L)
                } else {
                    handleMessage(msg)
                }
            }
        }

        fun sendMessage(msg: String) {
            queue.push(msg)
        }

        private fun handleMessage(msg: String) {

            val logFile = getLogFile(folder, logNameFormat.format(Date()))

            var fileWriter: FileWriter? = null
            try {
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, msg)
                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) {

                    }
                }
            }
        }

        /**
         * 这总是在单个后台线程上调用。实现类只能写入fileWriter，
         * 仅此而已。抽象类负责其他一切，包括关闭流和捕获IOException
         *
         * @param fileWriter 的实例已初始化为正确的文件
         */
        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(folderName: String, fileName: String): File {
            val folder = File(folderName)
            if (!folder.exists()) {
                folder.mkdirs()
            } else {
                // 删除7天前的文件
                folder.listFiles()?.forEach {
                    if (it.lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)) {
                        it.delete()
                    }
                }
            }
            var newFileCount = 0
            var newFile: File
            var existingFile: File? = null
            newFile = File(folder, String.format("%s_%s.log", fileName, newFileCount))
            while (newFile.exists()) {
                existingFile = newFile
                newFileCount++
                newFile = File(folder, String.format("%s_%s.log", fileName, newFileCount))
            }
            return if (existingFile != null) {
                if (existingFile.length() >= maxFileSize) {
                    newFile
                } else {
                    existingFile
                }
            } else newFile
        }
    }

}