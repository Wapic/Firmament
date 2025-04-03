
package moe.nea.firmament.features.events.carnival

import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig

object CarnivalFeatures : FirmamentFeature {
    object TConfig : ManagedConfig(identifier, Category.EVENTS) {
        val enableBombSolver by toggle("bombs-solver") { true }
        val displayTutorials by toggle("tutorials") { true }
		val enableZombieShootoutHelper by toggle("zombie-shootout-helper") { true }
    }

    override val config: ManagedConfig?
        get() = TConfig
    override val identifier: String
        get() = "carnival"
}
