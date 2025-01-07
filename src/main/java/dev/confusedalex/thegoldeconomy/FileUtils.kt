package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun createPlayersFile(): File {
    val playersFile = File("plugins/TheGoldEconomy/data/balance.json")

    if (!playersFile.exists()) {
        // Creates the "data/" directory in the plugin directory
        // only needs to be called once, therefore only in this method
        playersFile.parentFile.mkdirs()
        playersFile.createNewFile()
        playersFile.writeText("{}")
    }
    return playersFile
}

fun createFakeAccountsFile(): File {
    val fakeAccountsFile = File("plugins/TheGoldEconomy/data/fakeAccounts.json")

    if (!fakeAccountsFile.exists()) {
        fakeAccountsFile.createNewFile()
        fakeAccountsFile.writeText("{}")
    }
    return fakeAccountsFile
}

fun writeToFiles(playerAccounts: HashMap<String, Int>, fakeAccounts: HashMap<String, Int>) {
    createPlayersFile().writeText(Json.encodeToString(playerAccounts))
    createFakeAccountsFile().writeText(Json.encodeToString(fakeAccounts))
}