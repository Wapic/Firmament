/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.features.world

import io.github.moulberry.repo.data.Coordinate
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.minecraft.util.math.Direction
import moe.nea.firmament.events.ServerChatLineReceivedEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.blockPos
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.render.RenderInWorldContext.Companion.renderInWorld
import moe.nea.firmament.util.unformattedString


object FairySouls : FirmamentFeature {


    @Serializable
    data class Data(
        val foundSouls: MutableMap<String, MutableSet<Int>> = mutableMapOf()
    )

    override val config: ManagedConfig
        get() = TConfig

    object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "found-fairysouls", ::Data)


    object TConfig : ManagedConfig("fairy-souls") {
        val displaySouls by toggle("show") { false }
        val resetSouls by button("reset") {
            DConfig.data?.foundSouls?.clear() != null
            updateMissingSouls()
        }
    }


    override val name: String get() = "Fairy Souls"
    override val identifier: String get() = "fairy-souls"

    val playerReach = 5
    val playerReachSquared = playerReach * playerReach

    var currentLocationName: String? = null
    var currentLocationSouls: List<Coordinate> = emptyList()
    var currentMissingSouls: List<Coordinate> = emptyList()

    fun updateMissingSouls() {
        currentMissingSouls = emptyList()
        val c = DConfig.data ?: return
        val fi = c.foundSouls[currentLocationName] ?: setOf()
        val cms = currentLocationSouls.toMutableList()
        fi.asSequence().sortedDescending().filter { it in cms.indices }.forEach { cms.removeAt(it) }
        currentMissingSouls = cms
    }

    fun updateWorldSouls() {
        currentLocationSouls = emptyList()
        val loc = currentLocationName ?: return
        currentLocationSouls = RepoManager.neuRepo.constants.fairySouls.soulLocations[loc] ?: return
    }

    fun findNearestClickableSoul(): Coordinate? {
        val player = MC.player ?: return null
        val pos = player.pos
        val location = SBData.skyblockLocation ?: return null
        val soulLocations: List<Coordinate> =
            RepoManager.neuRepo.constants.fairySouls.soulLocations[location] ?: return null
        return soulLocations
            .map { it to it.blockPos.getSquaredDistance(pos) }
            .filter { it.second < playerReachSquared }
            .minByOrNull { it.second }
            ?.first
    }

    private fun markNearestSoul() {
        val nearestSoul = findNearestClickableSoul() ?: return
        val c = DConfig.data ?: return
        val loc = currentLocationName ?: return
        val idx = currentLocationSouls.indexOf(nearestSoul)
        c.foundSouls.computeIfAbsent(loc) { mutableSetOf() }.add(idx)
        DConfig.markDirty()
        updateMissingSouls()
    }


    override fun onLoad() {
        SkyblockServerUpdateEvent.subscribe {
            currentLocationName = it.newLocraw?.skyblockLocation
            updateWorldSouls()
            updateMissingSouls()
        }
        ServerChatLineReceivedEvent.subscribe {
            when (it.text.unformattedString) {
                "You have already found that Fairy Soul!" -> {
                    markNearestSoul()
                }

                "SOUL! You found a Fairy Soul!" -> {
                    markNearestSoul()
                }
            }
        }
        WorldRenderLastEvent.subscribe {
            if (!TConfig.displaySouls) return@subscribe
            renderInWorld(it.matrices, it.camera) {
                color(1F, 1F, 0F, 0.8F)
                currentMissingSouls.forEach {
                    block(it.blockPos)
                }
                color(1f, 0f, 1f, 1f)
                currentLocationSouls.forEach {
                    wireframeCube(it.blockPos)
                }
            }
        }
    }
}
