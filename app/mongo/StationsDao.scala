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
import reactivemongo.core.commands.{Project, Match, Aggregate}
import reactivemongo.bson.{BSONObjectID, BSONDocument}

trait MongoDao {
  def db = ReactiveMongoPlugin.db
  def collection: JSONCollection = db.collection(collectionName())
  def collectionName(): String
}

object StationsDao extends MongoDao {

  def collectionName() = "stations"

  def find(): Future[List[Station]] = {
    Cache.getOrElse[Future[List[Station]]]("stations") {
      implicit val format: Format[Station] = Json.format[Station]
      Logger.debug(s"Find all documents from $collectionName")

      collection
        .find(Json.obj())
        .sort(Json.obj("name" -> -1))
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

  def sampleAggregate() = {
    val command = Aggregate(collectionName(),
      Seq(
        Match(BSONDocument("stationId" -> BSONDocument("$in" -> List(1, 2)))),
        Project(("_id", 1), ("stationId", 1), ("down", 1), ("startAt", 1))
      )
    )
    val res = db.command(command)
    res.map {
      value => {
        value.foreach(v => {
          println(v.get("stationId"))
        })
      }
    }
  }

  def findRunningItems(): Future[List[StationDetails]] = {
    Logger.debug("Search running items")
    collection
      .find(Json.obj("endAt" -> Json.obj("$exists" -> false)))
      .cursor[StationDetails]
      .toList()
  }

  def save(stationDetails: StationDetails) = {
    collection.insert(stationDetails)
  }

  def update(bsonId: BSONObjectID, attributes: JsObject) = {
    collection.update(
      Json.obj("_id" -> bsonId),
      Json.obj("$set" -> attributes))
  }

}