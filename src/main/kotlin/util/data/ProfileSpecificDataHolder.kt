package moe.nea.firmament.util.data

import kotlinx.serialization.KSerializer

abstract class ProfileSpecificDataHolder<S>(
	dataSerializer: KSerializer<S>,
	configName: String,
	configDefault: () -> S
) : ProfileKeyedConfig<S>(configName, dataSerializer, configDefault)
