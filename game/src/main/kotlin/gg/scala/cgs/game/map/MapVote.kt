package gg.scala.cgs.game.map

import gg.scala.cgs.game.vote.VoteTopic
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.vote.Vote

class MapVote : Vote(ScGameFramework.instance.game!!.maps.values.map { VoteTopic(it.name) })