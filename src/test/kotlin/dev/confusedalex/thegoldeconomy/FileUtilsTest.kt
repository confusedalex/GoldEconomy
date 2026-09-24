package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

class FileUtilsTest {
    @TempDir
    lateinit var dir: File

    private val balanceFile get() = File(dir, "balance.json")
    private val fakeAccountsFile get() = File(dir, "fakeAccounts.json")

    private fun uuid(name: String): UUID = UUID.nameUUIDFromBytes(name.toByteArray())
    private fun key(name: String) = uuid(name).toString()

    private fun migrate() = migrateFakeAccounts(dir, ::uuid)
    private fun readBalances(): Map<String, Int> = Json.decodeFromString(balanceFile.readText())

    @Test
    fun migrateFakeAccounts_shouldAddAllEntriesToBalancesByUuid() {
        balanceFile.writeText("""{"${key("Steve")}": 100}""")
        fakeAccountsFile.writeText("""{"Town_A": 50, "Nation_B": 25}""")

        migrate()

        assertEquals(mapOf(key("Steve") to 100, key("Town_A") to 50, key("Nation_B") to 25), readBalances())
    }

    @Test
    fun migrateFakeAccounts_shouldAddToExistingBalanceOnCollision() {
        balanceFile.writeText("""{"${key("Steve")}": 100}""")
        fakeAccountsFile.writeText("""{"Steve": 50}""")

        migrate()

        assertEquals(mapOf(key("Steve") to 150), readBalances())
    }

    @Test
    fun migrateFakeAccounts_shouldDeleteFakeAccountsFile() {
        balanceFile.writeText("{}")
        fakeAccountsFile.writeText("""{"Town_A": 50}""")

        migrate()

        assertFalse(fakeAccountsFile.exists())
    }

    @Test
    fun migrateFakeAccounts_shouldCreateBalanceFileIfMissing() {
        fakeAccountsFile.writeText("""{"Town_A": 50}""")

        migrate()

        assertEquals(mapOf(key("Town_A") to 50), readBalances())
    }

    @Test
    fun migrateFakeAccounts_shouldDoNothingWithoutFakeAccountsFile() {
        balanceFile.writeText("""{"${key("Steve")}": 100}""")

        migrate()

        assertEquals(mapOf(key("Steve") to 100), readBalances())
    }
}
