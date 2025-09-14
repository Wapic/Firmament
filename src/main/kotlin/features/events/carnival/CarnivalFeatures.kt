
package moe.nea.firmament.features.events.carnival

import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig

object CarnivalFeatures : FirmamentFeature {
	@Config
	object TConfig : ManagedConfig(identifier, Category.EVENTS) {
        val enableBombSolver by toggle("bombs-solver") { true }
        val displayTutorials by toggle("tutorials") { true }
    }

    override val config: ManagedConfig?
        get() = TConfig
    override val identifier: String
        get() = "carnival"
}
