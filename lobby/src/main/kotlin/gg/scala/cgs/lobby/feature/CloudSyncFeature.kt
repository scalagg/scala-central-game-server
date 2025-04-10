package gg.scala.cgs.lobby.feature

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan

/**
 * @author Topu
 * @date 11/16/2024
 */
@Service
@IgnoreAutoScan
@SoftDependency("cloudsync")
object CloudSyncFeature
{
    @Configure
    fun configure()
    {
        CloudSyncDiscoveryService
            .discoverable.assets
            .apply {
                add("gg.scala.cgs:lobby:cgs-lobby${
                    if ("dev" in ServerSync.getLocalGameServer().groups) ":gradle-dev" else ""
                }")
            }
    }
}