package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import java.io.File
import java.util.*

private val dataDir = File("plugins/TheGoldEconomy/data")

fun createPlayersFile(dir: File = dataDir): File {
    val playersFile = File(dir, "balance.json")

    if (!playersFile.exists()) {
        // Creates the "data/" directory in the plugin directory
        // only needs to be called once, therefore only in this method
        playersFile.parentFile.mkdirs()
        playersFile.createNewFile()
        playersFile.writeText("{}")
    }
    return playersFile
}

@JvmOverloads
fun migrateFakeAccounts(
    dir: File = dataDir,
    nameToUuid: (String) -> UUID = { Bukkit.getOfflinePlayer(it).uniqueId },
) {
    val fakeAccountsFile = File(dir, "fakeAccounts.json")

    if (!fakeAccountsFile.exists()) return

    val playersFile = createPlayersFile(dir)
    val playerAccounts: HashMap<String, Int> = Json.decodeFromString(playersFile.readText())
    val fakeAccounts: HashMap<String, Int> = Json.decodeFromString(fakeAccountsFile.readText())

    fakeAccounts.forEach { (name, balance) ->
        playerAccounts.merge(nameToUuid(name).toString(), balance, Int::plus)
    }

    playersFile.writeText(Json.encodeToString(playerAccounts))
    fakeAccountsFile.delete()
}

@Synchronized
fun writeToFiles(playerAccounts: Map<String, Int>) {
    createPlayersFile().writeText(Json.encodeToString(playerAccounts))
}
