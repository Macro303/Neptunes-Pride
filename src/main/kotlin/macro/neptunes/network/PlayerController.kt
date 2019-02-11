package macro.neptunes.network

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import macro.neptunes.core.Util
import macro.neptunes.core.Player
import macro.neptunes.data.PlayerTable
import org.apache.logging.log4j.LogManager

/**
 * Created by Macro303 on 2018-Nov-16.
 */
object PlayerController {
	private val LOGGER = LogManager.getLogger(PlayerController::class.java)

	fun getPlayers(): List<Player> {
		val players = PlayerTable.search().sorted()
		return players
	}

	suspend fun ApplicationCall.parsePlayer(useJson: Boolean = true): Player? {
		val alias = parameters["Alias"]
		val player = PlayerTable.search(alias = alias ?: "").sorted().firstOrNull()
		if (alias == null || player == null) {
			val message = Util.notFoundMessage(request = request, type = "Player", field = "Alias", value = alias)
			when (useJson) {
				true -> respond(
					message = message,
					status = HttpStatusCode.NotFound
				)
				false -> respond(
					message = FreeMarkerContent(template = "Exception.ftl", model = message),
					status = HttpStatusCode.NotFound
				)
			}
			return null
		}
		return player
	}

	fun Route.playerRoutes() {
		route(path = "/players") {
			contentType(contentType = ContentType.Application.Json) {
				get {
					call.respond(
						message = getPlayers(),
						status = HttpStatusCode.OK
					)
				}
				post {
					call.respond(
						message = Util.notImplementedMessage(request = call.request),
						status = HttpStatusCode.NotImplemented
					)
				}
				route(path = "/{Alias}") {
					get {
						val player = call.parsePlayer() ?: return@get
						call.respond(
							message = player,
							status = HttpStatusCode.OK
						)
					}
					put {
						val player = call.parsePlayer() ?: return@put
						call.respond(
							message = Util.notImplementedMessage(request = call.request),
							status = HttpStatusCode.NotImplemented
						)
					}
					delete {
						val player = call.parsePlayer() ?: return@delete
						call.respond(
							message = Util.notImplementedMessage(request = call.request),
							status = HttpStatusCode.NotImplemented
						)
					}
				}
			}
		}
	}
}