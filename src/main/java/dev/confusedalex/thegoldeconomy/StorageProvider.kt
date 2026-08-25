package dev.confusedalex.thegoldeconomy

interface StorageProvider {
    fun initialize()
    fun shutdown()
    fun loadPlayerAccounts(): Map<String, Int>
    fun loadFakeAccounts(): Map<String, Int>
    fun savePlayerAccount(uuid: String, balance: Int)
    fun saveFakeAccount(name: String, balance: Int)
    fun savePlayerAccounts(accounts: Map<String, Int>)
    fun saveFakeAccounts(accounts: Map<String, Int>)
}
