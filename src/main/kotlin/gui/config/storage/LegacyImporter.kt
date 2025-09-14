package moe.nea.firmament.gui.config.storage

import java.nio.file.Path
import javax.xml.namespace.QName
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.writeText
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.configFolder
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.configVersionFile
import moe.nea.firmament.gui.config.storage.FirmamentConfigLoader.storageFolder

object LegacyImporter {
	val legacyConfigVersion = 995
	val backupPath = configFolder.resolveSibling("firmament-legacy-config-${System.currentTimeMillis()}")

	fun copyIf(from: Path, to: Path) {
		if (from.exists()) {
			to.createParentDirectories()
			from.copyTo(to)
		}
	}

	fun importFromLegacy() {
		configFolder.moveTo(backupPath)
		configFolder.createDirectories()

		copyIf(
			backupPath.resolve("inventory-buttons.json"),
			storageFolder.resolve("inventory-buttons.json")
		)

		backupPath.listDirectoryEntries("*.json")
			.forEach { path ->
				val name = path.name
				if (name == "inventory-buttons.json")
					return@forEach
				path.copyTo(configFolder.resolve(name))
			}

		backupPath.resolve("profiles")
			.forEachDirectoryEntry { category ->
				category.forEachDirectoryEntry { profile ->
					copyIf(
						profile,
						FirmamentConfigLoader.profilePath
							.resolve(profile.nameWithoutExtension)
							.resolve(category.name + ".json")
					)
				}
			}

		configVersionFile.writeText(legacyConfigVersion.toString())
	}
}
