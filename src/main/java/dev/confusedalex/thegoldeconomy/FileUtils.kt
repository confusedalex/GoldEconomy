package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Deprecated("Use StorageProvider instead")
fun createPlayersFile(): File {
    val playersFile = File("plugins/TheGoldEconomy/data/balance.json")
    if (!playersFile.exists()) {
        playersFile.parentFile.mkdirs()
        playersFile.createNewFile()
        playersFile.writeText("{}")
    }
    return playersFile
}

@Deprecated("Use StorageProvider instead")
fun createFakeAccountsFile(): File {
    val fakeAccountsFile = File("plugins/TheGoldEconomy/data/fakeAccounts.json")
    if (!fakeAccountsFile.exists()) {
        fakeAccountsFile.parentFile.mkdirs()
        fakeAccountsFile.createNewFile()
        fakeAccountsFile.writeText("{}")
    }
    return fakeAccountsFile
}

@Deprecated("Use StorageProvider instead")
fun writeToFiles(playerAccounts: HashMap<String, Int>, fakeAccounts: HashMap<String, Int>) {
    createPlayersFile().writeText(Json.encodeToString(playerAccounts))
    createFakeAccountsFile().writeText(Json.encodeToString(fakeAccounts))
}
