package macro303.neptunes_pride

import com.google.gson.GsonBuilder
import tornadofx.*
import java.io.*
import java.util.*

/**
 * Created by Macro303 on 2018-04-18.
 */
internal data class Config(val gameID: Long) {
	val players = HashMap<String, String>()
	val proxyHostname: String? = null
	val proxyPort: Int? = null
	val headerSize = 40.px
	val buttonSize = 22.px
	val informationSize = 22.px
	val contentSize = 16.px
	val regularButtonFontName: String = "OverlockSC"
	val regularContentFontName: String = "LifeSavers"
	val boldContentFontName: String = "LifeSavers"

	companion object {
		private val gson = GsonBuilder()
			.serializeNulls()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create()
		private val DATA: File by lazy {
			val temp = File("bin")
			if (!temp.exists())
				temp.mkdirs()
			temp
		}
		private val configFile = File(DATA, "config.json")

		@JvmStatic
		fun loadConfig(): Config? {
			var config: Config? = null
			try {
				FileReader(configFile).use { fr -> config = gson.fromJson(fr, Config::class.java) }
			} catch (ignored: FileNotFoundException) {
			} catch (ioe: IOException) {
				ioe.printStackTrace()
			} finally {
				if (config == null) {
					saveConfig(Config(1))
					config = loadConfig()
				}
			}
			return config
		}

		@JvmStatic
		fun saveConfig(config: Config) {
			try {
				FileWriter(configFile).use { fw -> gson.toJson(config, fw) }
			} catch (ioe: IOException) {
				ioe.printStackTrace()
			}

		}
	}

	override fun toString(): String {
		return "Config(gameID=$gameID, players=$players, proxyHostname=$proxyHostname, proxyPort=$proxyPort, headerSize=$headerSize, buttonSize=$buttonSize, informationSize=$informationSize, contentSize=$contentSize, regularButtonFontName='$regularButtonFontName', regularContentFontName='$regularContentFontName', boldContentFontName='$boldContentFontName')"
	}
}