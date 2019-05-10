package macro.dashboard.neptunes.player

import io.javalin.http.InternalServerErrorResponse
import macro.dashboard.neptunes.Util
import macro.dashboard.neptunes.backend.ProteusPlayer
import macro.dashboard.neptunes.game.GameTable
import macro.dashboard.neptunes.team.TeamTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.slf4j.LoggerFactory

/**
 * Created by Macro303 on 2019-Feb-11.
 */
object PlayerTable : IntIdTable(name = "Player") {
	private val gameCol = reference(
		name = "gameID",
		foreign = GameTable,
		onUpdate = ReferenceOption.CASCADE,
		onDelete = ReferenceOption.CASCADE
	)
	private val teamCol = reference(
		name = "teamID",
		foreign = TeamTable,
		onUpdate = ReferenceOption.CASCADE,
		onDelete = ReferenceOption.CASCADE
	)
	private val aliasCol = text(name = "alias")
	private val nameCol = text(name = "name").nullable()

	private val LOGGER = LoggerFactory.getLogger(this::class.java)

	init {
		Util.query(description = "Create Player table") {
			uniqueIndex(gameCol, aliasCol)
			SchemaUtils.create(this)
		}
	}

	fun select(ID: Int): Player? = Util.query(description = "Select Player by ID: $ID") {
		select {
			id eq ID
		}.orderBy(teamCol to SortOrder.ASC, aliasCol to SortOrder.ASC).limit(n = 1)
			.firstOrNull()?.parse()
	}

	fun select(alias: String): Player? =
		Util.query(description = "Select Player by Alias: $alias") {
			select {
				aliasCol like alias
			}.orderBy(teamCol to SortOrder.ASC, aliasCol to SortOrder.ASC).limit(n = 1)
				.firstOrNull()?.parse()
		}

	fun search(): List<Player> = Util.query(description = "Search for Players") {
		selectAll().orderBy(teamCol to SortOrder.ASC, aliasCol to SortOrder.ASC).map {
			it.parse()
		}
	}

	fun searchByTeam(teamID: Int): List<Player> = Util.query(description = "Search for Players in team: $teamID") {
		select {
			teamCol eq teamID
		}.orderBy(teamCol to SortOrder.ASC, aliasCol to SortOrder.ASC).map {
			it.parse()
		}
	}

	fun insert(gameID: Long, update: ProteusPlayer): Boolean = Util.query(description = "Insert Proteus Player") {
		val teamID = TeamTable.select(name = "Free For All")?.ID
			?: throw InternalServerErrorResponse("Unable to Find Team => Free For All")
		try {
			insert {
				it[gameCol] = EntityID(id = gameID, table = GameTable)
				it[aliasCol] = update.alias
				it[teamCol] = EntityID(id = teamID, table = TeamTable)
			}
			true
		} catch (esqle: ExposedSQLException) {
			false
		}
	}

	fun update(ID: Int, teamID: Int, name: String?) = Util.query(description = "Update Player") {
		try {
			update({ id eq ID }) {
				it[teamCol] = EntityID(id = teamID, table = TeamTable)
				it[nameCol] = name
			}
		} catch (esqle: ExposedSQLException) {
		}
	}

	private fun ResultRow.parse(): Player = Player(
		ID = this[id].value,
		gameID = this[gameCol].value,
		teamID = this[teamCol].value,
		name = this[nameCol],
		alias = this[aliasCol]
	)
}