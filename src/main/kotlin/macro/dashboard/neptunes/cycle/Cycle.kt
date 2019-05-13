package macro.dashboard.neptunes.cycle

import macro.dashboard.neptunes.Config.Companion.CONFIG
import org.slf4j.LoggerFactory

/**
 * Created by Macro303 on 2019-Mar-04.
 */
data class Cycle(
	val ID: Int,
	val playerID: Int,
	val cycle: Int,
	var economy: Int,
	var industry: Int,
	var science: Int,
	var stars: Int,
	var fleet: Int,
	var ships: Int,
	var isActive: Boolean,
	var scanning: Int,
	var hyperspace: Int,
	var experimentation: Int,
	var weapons: Int,
	var banking: Int,
	var manufacturing: Int
) {
	val economyPerCycle = economy * banking * CONFIG.gameCycle
	val industryPerCycle = industry * manufacturing * CONFIG.gameCycle
	val sciencePerCycle = science * experimentation * CONFIG.gameCycle

	fun toMap(): Map<String, Any?> {
		return mapOf(
			"stars" to stars,
			"ships" to ships,
			"economy" to economy,
			"economyPerCycle" to economyPerCycle,
			"industry" to industry,
			"industryPerCycle" to industryPerCycle,
			"science" to science,
			"sciencePerCycle" to sciencePerCycle,
			"tech" to mapOf(
				"scanning" to scanning,
				"hyperspace" to hyperspace,
				"experimentation" to experimentation,
				"weapons" to weapons,
				"banking" to banking,
				"manufacturing" to manufacturing
			).toSortedMap()
		).toSortedMap()
	}

	companion object {
		private val LOGGER = LoggerFactory.getLogger(this::class.java)
	}
}