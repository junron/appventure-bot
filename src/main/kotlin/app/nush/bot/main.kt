package app.nush.bot


import app.nush.bot.Config.Companion.config
import app.nush.bot.commands.*
import app.nush.bot.server.startServer
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault

val helpText = """
    **Bot version**: 1.4.2
    **Commands**
    **`${config.botPrefix}ping`**
    Check if server is alive
    
    **`${config.botPrefix}help`**
    Displays this message
    
    **`${config.botPrefix}nick <nickname>`**
    Sends a rename request to the exco

    **`${config.botPrefix}sendverify`**
    Dms you the verification message
    
    
    **`${config.botPrefix}github verify`**
    DMs a GitHub verification message
    
    **`${config.botPrefix}github share <repo name> <mentions>`**
    Adds mentioned users to specified GitHub repository. Users must have a registered GitHub account.
    May not work if user is not already in AppVenture organization (admin only)
    
    **`${config.botPrefix}projects create <repo name> [channel-only]`**
    Creates new project and repo with same name (admin only)
    Append "channel-only" if repo already exists
    
    **`${config.botPrefix}projects linkrepo <repo name>`**
    Links current channel to existing project (admin only)
    
    **`${config.botPrefix}projects archive`**
    Moves project channel to archive (admin only)
    
    **`${config.botPrefix}members import`**
    Imports members from attached CSV file (admin only)
    
    **`${config.botPrefix}members export`**
    Exports members to CSV file (admin only)
    
    **`${config.botPrefix}members refresh`**
    Gives alumni role to graduating Y6s (admin only)
""".trimIndent()

const val url =
    "http://login.microsoftonline.com/d72a7172-d5f8-4889-9a85-d7424751592a/oauth2/authorize?client_id=9f1a352a-8217-4a32-a4d3-d3c06d7b8581&redirect_uri=https://verify.nush.app/&response_type=id_token&response_mode=form_post&nonce="
lateinit var botId: String
@ExperimentalStdlibApi
@UnstableDefault
suspend fun main() {
    GlobalScope.launch {
        startServer()
    }
    bot(config.discordToken) {
        started {
            botId = it.user.id
        }
        commands(config.botPrefix) {
            command("help") {
                reply(helpText)
            }
            command("ping") {
                reply("pong")
            }
            Verify.init(this@bot, this)
            Nick.init(this@bot, this)
        }
        commands("${config.botPrefix}members ") {
            MembersCommand.init(this@bot, this)
        }
        commands("${config.botPrefix}projects ") {
            Projects.init(this@bot, this)
        }
        commands("${config.botPrefix}github ") {
            Github.init(this@bot, this)
        }

        userJoinedGuild {
            val userId = it.user?.id ?: return@userJoinedGuild
            Verify.sendVerifyMessage(this, userId)
        }
    }
}
