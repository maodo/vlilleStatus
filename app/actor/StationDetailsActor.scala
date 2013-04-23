package actor

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.Actor

import play.api.Logger

import models._
import parser._
import mongo._
import java.util.concurrent.TimeUnit

class StationDetailsActor extends Actor {

  def saveDetails(stations: List[Station]) = {
    val start = System.currentTimeMillis()

    stations.foreach({
      station =>
        val details = VlilleParser.details(station.id)
        StationDetailsDao.save(details)
    })

    val end = System.currentTimeMillis() - start
    val durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(end)
    Logger.debug(s"Stations integration in $durationInSeconds secs")

  }

  def receive = {
    case _ => {
      Logger.debug("Find all stations and save each station details.")
      val futureStations = StationsDao.find()
      futureStations map {
        stations => saveDetails(stations)
      }
    }
  }
}