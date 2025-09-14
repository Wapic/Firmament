package moe.nea.firmament.gui.config.storage

import java.io.PrintWriter
import java.nio.file.Path
import org.apache.commons.io.output.StringBuilderWriter
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText
import moe.nea.firmament.Firmament

data class ConfigLoadContext(
	val loadId: String,
) : AutoCloseable {
	val logFile = Path("logs")
		.resolve(Firmament.MOD_ID)
		.resolve("config-$loadId.log")
		.toAbsolutePath()
	val logBuffer = StringBuilder()

	var shouldSaveLogBuffer = false
	fun markShouldSaveLogBuffer() {
		shouldSaveLogBuffer = true
	}

	fun logDebug(message: String) {
		logBuffer.append("[DEBUG] ").append(message).appendLine()
	}

	fun logInfo(message: String) {
		Firmament.logger.info("[ConfigUpgrade] $message")
		logBuffer.append("[INFO] ").append(message).appendLine()
	}

	fun logError(message: String, exception: Throwable) {
		markShouldSaveLogBuffer()
		Firmament.logger.error("[ConfigUpgrade] $message", exception)
		logBuffer.append("[ERROR] ").append(message).appendLine()
		PrintWriter(StringBuilderWriter(logBuffer)).use {
			exception.printStackTrace(it)
		}
		logBuffer.appendLine()
	}

	fun logError(message: String) {
		markShouldSaveLogBuffer()
		Firmament.logger.error("[ConfigUpgrade] $message")
		logBuffer.append("[ERROR] ").append(message).appendLine()
	}

	fun ensureWritable(path: Path) {
		path.createParentDirectories()
	}

	override fun close() {
		logInfo("Closing out config load.")
		if (shouldSaveLogBuffer) {
			try {
				ensureWritable(logFile)
				logFile.writeText(logBuffer.toString())
			} catch (ex: Exception) {
				logError("Could not save config load log", ex)
			}
		}
	}
}
