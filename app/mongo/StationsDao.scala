package mongo

import concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.cache.Cache
import play.api.Logger

import play.modules.reactivemongo.ReactiveMongoPlugin

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.bson.DefaultBSONHandlers._

import models._

trait MongoDao {
  def db = ReactiveMongoPlugin.db
  def collection: BSONCollection = db.collection(collectionName())
  def collectionName(): String
}

object StationsDao extends MongoDao {

  def collectionName() = "stations"

  implicit val writer = StationBSON.Writer
  implicit val reader = StationBSON.Reader

  def find(): Future[List[Station]] = {
    Cache.getOrElse[Future[List[Station]]]("stations") {
      Logger.debug("Find all documents from " + collectionName())

      val query = BSONDocument(
        "$orderby" -> BSONDocument(
          "name" -> BSONInteger(1)
        ),
        "$query" -> BSONDocument()
      )
      collection.find(query).cursor.toList
    }
  }

  def save(stations: List[Station]) = {
    Logger.debug("save all stations")

    stations.foreach(station => collection.insert(station))
  }

}

object StationDetailsDao extends MongoDao {

  def collectionName() = "stations_details"

  implicit val writer = StationDetailsBSON.Writer
  implicit val reader = StationDetailsBSON.Reader

  def find(): Future[List[StationDetails]] = {
    val q = BSONDocument()
    collection.find(q).cursor.toList
  }

  def save(stationDetails: StationDetails) = {
    collection.insert(stationDetails)
  }

}