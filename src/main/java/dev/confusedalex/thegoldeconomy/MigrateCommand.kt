package dev.confusedalex.thegoldeconomy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import org.bukkit.command.CommandSender

@CommandAlias("bank")
class MigrateCommand(
    private val plugin: TheGoldEconomy,
    private val util: Util,
    private val jsonProvider: JsonStorageProvider,
    private val targetStorage: StorageProvider
) : BaseCommand() {

    @Subcommand("migrate")
    @CommandPermission("thegoldeconomy.admin")
    @Description("Migrate balance data from JSON files to the active database backend.")
    fun migrate(sender: CommandSender) {
        sender.sendMessage(util.formatMessage("Starting migration from JSON to database..."))

        val playerData = jsonProvider.loadPlayerAccounts()
        val fakeData = jsonProvider.loadFakeAccounts()

        if (playerData.isEmpty() && fakeData.isEmpty()) {
            sender.sendMessage(util.formatMessage("Nothing to migrate — both JSON files are empty."))
            return
        }

        targetStorage.savePlayerAccounts(playerData)
        targetStorage.saveFakeAccounts(fakeData)

        sender.sendMessage(util.formatMessage(
            "Migration complete! Moved ${playerData.size} player account(s) and ${fakeData.size} fake account(s) to the database."
        ))

        plugin.logger.info("Migration finished: ${playerData.size} player accounts, ${fakeData.size} fake accounts.")
    }
}
