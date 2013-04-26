package mongo

import concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.cache.Cache
import play.api.Logger

import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.modules.reactivemongo.json.BSONFormats._

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import models._

trait MongoDao {
  def db = ReactiveMongoPlugin.db
  def collection: JSONCollection = db.collection(collectionName())
  def collectionName(): String
}

object StationsDao extends MongoDao {

  def collectionName() = "stations"

  def find(): Future[List[Station]] = {
    implicit val format: Format[Station] = Json.format[Station]
    Cache.getOrElse[Future[List[Station]]]("stations") {
      Logger.debug("Find all documents from " + collectionName())

      collection
        .find(Json.obj())
        .sort(Json.obj("name" -> -1))
        .cursor[Station]
        .toList
    }
  }
  def save(stations: List[Station]) = {
    implicit val writes: Writes[Station] = Json.writes[Station]
    Logger.debug("save all stations")

    stations.foreach(station => collection.insert(station))
  }

}

object StationDetailsDao extends MongoDao {

  def collectionName() = "stations_details"

  implicit val writer = Json.format[StationDetails]
  implicit val reader = Json.writes[StationDetails]

  def find(): Future[List[StationDetails]] = {
    collection
      .find(Json.obj())
      .cursor[StationDetails]
      .toList
  }

  def save(stationDetails: StationDetails) = {
    collection.insert(stationDetails)
  }

}