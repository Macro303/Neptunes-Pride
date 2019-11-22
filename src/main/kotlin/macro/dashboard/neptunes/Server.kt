package macro.dashboard.neptunes

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import macro.dashboard.neptunes.config.Config.Companion.CONFIG
import macro.dashboard.neptunes.cycle.CycleRouter
import macro.dashboard.neptunes.cycle.CycleTable
import macro.dashboard.neptunes.game.GameRouter
import macro.dashboard.neptunes.game.GameTable
import macro.dashboard.neptunes.player.PlayerRouter
import macro.dashboard.neptunes.player.PlayerTable
import macro.dashboard.neptunes.team.TeamRouter
import macro.dashboard.neptunes.team.TeamTable
import org.jetbrains.exposed.sql.exists
import org.slf4j.LoggerFactory

object Server {
	private val LOGGER = LoggerFactory.getLogger(Server::class.java)

	init {
		LOGGER.info("Initializing Neptune's Dashboard")
		checkLogLevels()
		checkDatabase()
		checkConfigGames()
	}

	private fun checkLogLevels() {
		LOGGER.trace("TRACE | is Visible")
		LOGGER.debug("DEBUG | is Visible")
		LOGGER.info("INFO  | is Visible")
		LOGGER.warn("WARN  | is Visible")
		LOGGER.error("ERROR | is Visible")
	}

	private fun checkDatabase() {
		Util.query(description = "Check All Tables Exist") {
			GameTable.exists()
			PlayerTable.exists()
			CycleTable.exists()
			TeamTable.exists()
		}
	}

	private fun checkConfigGames() {
		val game = GameTable.select(ID = CONFIG.game.id)
		if (game == null) {
			try {
				Proteus.getGame(gameID = CONFIG.game.id, code = CONFIG.game.code)
				LOGGER.info("New Game Loaded => ${CONFIG.game.id}")
			} catch (iser: InternalServerErrorResponse) {
				LOGGER.error("Failed Game Load: ${CONFIG.game.id} => ${CONFIG.game.code}")
			}
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		embeddedServer(
			Netty,
			port = CONFIG.server.port ?: 5505,
			host = CONFIG.server.hostName ?: "localhost",
			module = Application::module
		).apply { start(wait = true) }
	}
}

fun Application.module() {
	install(ContentNegotiation) {
		gson {
			setPrettyPrinting()
			disableHtmlEscaping()
			serializeNulls()
		}
	}
	install(DefaultHeaders) {
		header(name = "Developer", value = "Macro303")
		header(name = HttpHeaders.Server, value = "Neptunes-Dashboard")
		header(name = HttpHeaders.AcceptLanguage, value = "en-NZ")
		header(name = HttpHeaders.ContentLanguage, value = "en-NZ")
	}
	install(ConditionalHeaders)
	install(AutoHeadResponse)
	install(FreeMarker) {
		templateLoader = ClassTemplateLoader(this::class.java, "/templates")
	}
	install(StatusPages) {
		exception<HttpResponseException> {
			call.respond(error = it)
		}
		status(HttpStatusCode.NotFound) {
			call.respond(error = NotFoundResponse())
		}
	}
	install(Routing) {
		intercept(ApplicationCallPipeline.Setup) {
			application.log.debug("==> ${call.request.httpVersion} ${call.request.httpMethod.value} ${call.request.uri}, Accept: ${call.request.accept()}, Content-Type: ${call.request.contentType()}, User-Agent: ${call.request.userAgent()}")
		}
		trace {
			application.log.trace(it.buildText())
		}
		route(path = "/api") {
			route(path = "/game") {
				GameRouter.getGame(route = this)
				GameRouter.updateGame(route = this)
			}
			route(path = "/players") {
				PlayerRouter.getPlayers(route = this)
				route(path = "/{player-id}") {
					PlayerRouter.getPlayer(route = this)
					PlayerRouter.updatePlayer(route = this)
					route(path = "/cycles") {
						CycleRouter.getCycles(route = this)
						route(path = "/{cycle}") {
							CycleRouter.getCycle(route = this)
						}
					}
				}
			}
			route(path = "/teams") {
				TeamRouter.getTeams(route = this)
				TeamRouter.addTeam(route = this)
				route(path = "/{team-id}") {
					TeamRouter.getTeam(route = this)
					TeamRouter.updateTeam(route = this)
				}
			}
		}
		route(path = "/players/{alias}") {
			PlayerRouter.displayPlayer(route = this)
		}
		route(path = "/teams/{name}") {
			TeamRouter.displayTeam(route = this)
		}
		static {
			defaultResource(resource = "/static/index.html")
			resources(resourcePackage = "static/images")
			resources(resourcePackage = "static/css")
			resources(resourcePackage = "static/js")
			resource(remotePath = "/navbar.html", resource = "static/navbar.html")
			resource(remotePath = "/players", resource = "static/players.html")
			resource(remotePath = "/teams", resource = "static/teams.html")
			resource(remotePath = "/about", resource = "static/about.html")
			resource(remotePath = "/Neptunes-Dashboard.yaml", resource = "static/Neptunes-Dashboard.yaml")
		}
		intercept(ApplicationCallPipeline.Fallback) {
			if (call.response.status() != null)
				application.log.info("${call.request.httpMethod.value.padEnd(4)}: ${call.response.status()} - ${call.request.uri}")
		}
	}
}

suspend fun ApplicationCall.respond(error: HttpResponseException) {
	if (request.local.uri.startsWith(prefix = "/api")) {
		if (request.accept()?.contains(ContentType.Application.Json.toString()) == true)
			respond(
				message = mapOf(
					"message" to error.message,
					"status" to error.status
				),
				status = error.status
			)
		else
			respond(
				message = error.message ?: "",
				status = error.status
			)
	} else
		respond(
			message = FreeMarkerContent(
				template = "error.ftl",
				model = error.toMap()
			)
		)
	when {
		error.status.value < 100 -> application.log.error("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri} => ${error.message}")
		error.status.value in (100 until 200) -> application.log.info("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri} => ${error.message}")
		error.status.value in (200 until 300) -> application.log.info("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri}")
		error.status.value in (300 until 400) -> application.log.warn("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri} => ${error.message}")
		error.status.value in (400 until 500) -> application.log.warn("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri} => ${error.message}")
		error.status.value >= 500 -> application.log.error("${request.httpMethod.value.padEnd(4)}: ${error.status} - ${request.uri} => ${error.message}")
	}
}