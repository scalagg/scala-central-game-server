package gg.scala.parties.receiver

import com.google.gson.Gson
import gg.scala.aware.AwareBuilder
import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.QuickAccess
import gg.scala.parties.PartySpigotPlugin
import gg.scala.parties.model.Party
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.config.serversync.ServerSyncConfig
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Service
object PartyReceiverHandler
{
    @Inject
    lateinit var plugin: PartySpigotPlugin

    val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("party:backbone")
            .codec(AwareMessageCodec)
            .logger(plugin.logger)
            .build()
    }

    @Configure
    fun configure()
    {
        aware.listen(this)
        aware.connect()
            .toCompletableFuture()
            .join()
    }

    @Close
    fun close()
    {
        aware.shutdown()
    }

    @Subscribe("network-disconnect")
    fun onNetworkDisconnect(message: AwareMessage)
    {
        // very temporary
        if (Lemon.instance.settings.id != "hub1")
        {
            return
        }

        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        val party = PartyService
            .loadPartyOfPlayerIfAbsent(uniqueId)
            .join() ?: return

        if (uniqueId == party.leader.uniqueId)
        {
            party.gracefullyForget().join()
        } else
        {
            PartyService
                .handlePartyLeave(uniqueId)
                .join()
        }
    }

    @Subscribe("setup-party")
    fun onSetupParty(message: AwareMessage) {
        println("[CGSBukkit - Party Transfer] Received a Party Request")

        val serverName = message.retrieve("server") as? String ?: return
        val partyJson = message.retrieve("party") as? String ?: return

        if (ServerSync.getLocalGameServer().id.equals(serverName, ignoreCase = true)) {
            println("[CGSBukkit - Party Transfer] We are the target server for the party...")

            val party = Gson().fromJson(partyJson, Party::class.java)
            println("[CGSBukkit - Party Transfer] ${QuickAccess.nameOrConsole(party.leader.uniqueId)}'s Party!")
        }
    }

    @Subscribe("party-warp")
    fun onPartyWarp(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        val party = PartyService
            .loadedParties.values
            .firstOrNull {
                it.uniqueId == uniqueId
            }
            ?: return

        val server = message
            .retrieve<String>("server")

        for (uuid in party.members.keys)
        {
            val bukkitPlayer = Bukkit
                .getPlayer(uuid)
                ?: continue

            bukkitPlayer.sendMessage("${CC.SEC}You're being warped to ${CC.PRI}$server${CC.SEC}!")

            VelocityRedirectSystem.redirect(
                bukkitPlayer, server
            )
        }
    }

    @Subscribe("party-update")
    fun onPartyUpdate(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        PartyService
            .reloadPartyByUniqueId(uniqueId)
    }

    @Subscribe("party-forget")
    fun onPartyForget(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        PartyService.loadedParties
            .remove(uniqueId)
    }
}
