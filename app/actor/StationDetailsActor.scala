package actor

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.Actor

import play.api.Logger

import models._
import parser._
import mongo._
import utils.DateImplicits._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.{JsNumber, Json}

class StationDetailsActor extends Actor {

  def saveAllDetails(stations: List[Station]) = stations.foreach(station => saveDetails(station.id))

  def saveDetails(stationId: Int) = StationDetailsDao.save(getDetails(stationId))

  def getDetails(stationId: Int) = VlilleParser.details(stationId)

  def checkPreviousState(runningItemsByStationId: Map[Int, StationDetails], stations: List[Station]) = {
    Logger.debug(s"Check stations previous state for ${runningItemsByStationId.size}")

    stations.foreach(station => {
      val mmaybeRunningItem = runningItemsByStationId.get(station.id)
      mmaybeRunningItem match {
        case None => {
          Logger.debug(s"No previous state for ${station.id} - ${station.name}")
          saveDetails(station.id)
        }
        case Some(runningItem: StationDetails) => {
          val currentBsonId = runningItem.id.get

          // Initialize attributes to update with duration.
          val durationInSeconds = millisToSeconds(DateTime.now().minus(runningItem.startAt))
          val currentDuration = Json.obj("duration" -> durationInSeconds)

          val currentDetails = getDetails(station.id)
          if (runningItem.down != currentDetails.down) {
            Logger.debug(s"Station ${station.id} - ${station.name} status has changed for down: ${currentDetails.down}")

            // State has changed, update the state and the end time and save new details.
            StationDetailsDao.update(currentBsonId, currentDuration ++ Json.obj("endAt" -> JsNumber(DateTime.now().getMillis())))
            StationDetailsDao.save(currentDetails)
          } else {
            StationDetailsDao.update(currentBsonId, currentDuration)
          }
        }
      }
    })
  }

  def millisToSeconds(millis: Long) = TimeUnit.MILLISECONDS.toSeconds(millis)

  def receive = {
    case _ => {
      Logger.debug("Find all stations and save each station details.")
      val futureStations = StationsDao.find()
      futureStations map {
        stations =>
          val futureRunningItems =StationDetailsDao.findRunningItems()
          futureRunningItems.map {
            runningItems =>
              val now = DateTime.now()

              if (runningItems.isEmpty) {
                Logger.debug("No previous state for all stations, create one.")
                saveAllDetails(stations)
              }
              else {
                val runningItemsByStationId = runningItems.map(details => (details.stationId, details)).toMap
                checkPreviousState(runningItemsByStationId, stations)
              }

              val duration = DateTime.now().minus(now)
              val durationInSeconds = millisToSeconds(duration)
              Logger.debug(s"Stations integration in $durationInSeconds secs")
          }
      }
    }
  }
}