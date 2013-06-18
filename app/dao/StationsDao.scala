package dao

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

object StationDao

object StationItemDao extends MongoDao[StationItem] {

  def collectionName() = "stations_items"

  implicit val reader = Json.format[StationItem]
  implicit val writer = Json.writes[StationItem]

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

  def findRunning(): Future[List[StationItem]] = {
    Logger.debug("Search running items")
    collection
      .find(Json.obj("endAt" -> Json.obj("$exists" -> false)))
      .cursor[StationItem]
      .toList()
  }

  def save(stationDetails: StationItem) = {
    collection.insert(stationDetails)
  }

  def update(bsonId: BSONObjectID, attributes: JsObject) = {
    collection.update(
      Json.obj("_id" -> bsonId),
      Json.obj("$set" -> attributes))
  }

}