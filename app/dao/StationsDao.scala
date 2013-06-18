package dao

import concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import play.api.Logger
import play.api.libs.json._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.modules.reactivemongo.json.BSONFormats._

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.bson._
import reactivemongo.core.commands._

import models._

object StationDao

object StationItemDao extends MongoDao[StationItem] {

  def collectionName() = "stations_items"

  implicit val reader = Json.format[StationItem]
  implicit val writer = Json.writes[StationItem]

  def topUp() = uptime(false)
  def topDown() = uptime()

  def uptime(down: Boolean = true): Future[Stream[BSONDocument]] = {
    Logger.debug(s"Aggreating duration time by status $down")

    val command = Aggregate(collectionName(),
      Seq(
        Match(BSONDocument("down" -> true)),
        GroupField("stationId")(("count", SumField("duration"))),
        Sort(List(Descending("count"))),
        Limit(10)
      )
    )
    db.command(command)
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