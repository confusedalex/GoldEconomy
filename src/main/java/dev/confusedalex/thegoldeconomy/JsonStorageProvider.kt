package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Logger

class JsonStorageProvider(private val dataFolder: File, private val logger: Logger) : StorageProvider {

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var playersFile: File
    private lateinit var fakeAccountsFile: File

    override fun initialize() {
        playersFile = File(dataFolder, "data/balance.json")
        fakeAccountsFile = File(dataFolder, "data/fakeAccounts.json")

        playersFile.parentFile.mkdirs()

        if (!playersFile.exists()) {
            playersFile.createNewFile()
            playersFile.writeText("{}")
        }
        if (!fakeAccountsFile.exists()) {
            fakeAccountsFile.createNewFile()
            fakeAccountsFile.writeText("{}")
        }

        logger.info("Using JSON file storage.")
    }

    override fun shutdown() {}

    override fun loadPlayerAccounts(): HashMap<String, Int> {
        return try {
            json.decodeFromString<HashMap<String, Int>>(playersFile.readText())
        } catch (e: Exception) {
            logger.warning("Failed to read balance.json, starting fresh: ${e.message}")
            HashMap()
        }
    }

    override fun loadFakeAccounts(): HashMap<String, Int> {
        return try {
            json.decodeFromString<HashMap<String, Int>>(fakeAccountsFile.readText())
        } catch (e: Exception) {
            logger.warning("Failed to read fakeAccounts.json, starting fresh: ${e.message}")
            HashMap()
        }
    }

    override fun savePlayerAccount(uuid: String, balance: Int) {}

    override fun saveFakeAccount(name: String, balance: Int) {}

    override fun savePlayerAccounts(accounts: Map<String, Int>) {
        playersFile.writeText(json.encodeToString(accounts))
    }

    override fun saveFakeAccounts(accounts: Map<String, Int>) {
        fakeAccountsFile.writeText(json.encodeToString(accounts))
    }
}
