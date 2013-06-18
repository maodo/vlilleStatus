package dao

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json.{Writes, Json, Format}
import play.api.Logger

import models.Station

object StationListDao extends MongoDao[Station] {

  def collectionName() = "stations"

  def findAll(): Future[List[Station]] = {
    Cache.getOrElse[Future[List[Station]]]("stations") {
      implicit val format: Format[Station] = Json.format[Station]
      Logger.debug(s"Find all documents from $collectionName")

      collection
        .find(Json.obj())
        .sort(Json.obj("name" -> 1))
        .cursor[Station]
        .toList
    }
  }

  def save(stations: List[Station]) = {
    implicit val writes: Writes[Station] = Json.writes[Station]
    Logger.debug("Save stations")

    stations.foreach(station => collection.insert(station))
  }

  def removeAllAndSave(stations: List[Station]) = {
    Logger.debug("Remove all stations")
    collection.remove(Json.obj())
    save(stations)
  }

}
