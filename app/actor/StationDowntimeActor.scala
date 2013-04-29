package actor

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.Actor

import play.api.Logger
import play.api.libs.json._

import reactivemongo.bson.BSONObjectID

import models._
import parser._
import dao._
import utils.DateImplicits._

class StationDownTimeActor extends Actor {

  def receive = {
    case _ => {
      Logger.debug("Find all stations and save each station item details")
      val futureStations = StationDao.find()
      futureStations map {
        stations =>
          val futureRunningItems = StationItemDao.findRunning()
          futureRunningItems.map {
            runningItems =>
              val now = DateTime.now()

              if (runningItems.isEmpty) {
                Logger.debug("No previous state for all stations, create one")
                saveAllItems(stations)
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

  def saveAllItems(stations: List[Station]) = stations.foreach(station => saveItem(station.id))

  def saveItem(stationId: Int) = StationItemDao.save(getItemDetails(stationId))

  def getItemDetails(stationId: Int) = VlilleParser.details(stationId)

  def checkPreviousState(runningItemsByStationId: Map[Int, StationItem], stations: List[Station]) = {
    Logger.debug(s"Check stations previous state for ${runningItemsByStationId.size}")

    stations.foreach(station => {
      val mmaybeRunningItem = runningItemsByStationId.get(station.id)
      mmaybeRunningItem match {
        case None => {
          Logger.debug(s"No previous state for ${station.id} - ${station.name}")
          saveItem(station.id)
        }
        case Some(runningItem: StationItem) => updateOrAddNewItem(station, runningItem)
      }
    })
  }

  def updateOrAddNewItem(station: Station, runningItem: StationItem) = {
    val currentBsonId = runningItem.id.get

    // Initialize attributes to update with duration.
    val durationInSeconds = millisToSeconds(DateTime.now().minus(runningItem.startAt))
    val currentDuration = Json.obj("duration" -> durationInSeconds)

    val currentDetails = getItemDetails(station.id)
    if (runningItem.down != currentDetails.down) {
      Logger.debug(s"Station ${station.id} - ${station.name} status has changed for down: ${currentDetails.down}")

      // State has changed, update the state and the end time and save new details.
      StationItemDao.update(currentBsonId, currentDuration ++ Json.obj("endAt" -> JsNumber(DateTime.now().getMillis())))
      StationItemDao.save(currentDetails)
    } else {
      StationItemDao.update(currentBsonId, currentDuration)
    }
  }

  def millisToSeconds(millis: Long) = TimeUnit.MILLISECONDS.toSeconds(millis)
}