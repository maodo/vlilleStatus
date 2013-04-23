package models

import java.net.URL
import java.io.InputStreamReader

import scala.Option
import scala.xml.Elem
import scala.xml.XML
import scala.xml.NodeSeq

import org.joda.time.DateTime

import play.api.Logger

import reactivemongo.bson._
import handlers._
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONInteger

case class Station(id: Int, name: String, longitude: Float, latitude: Float)
case class StationDetails(id: Option[BSONObjectID], stationId: Int, status: Boolean, bikes: Int, attachs: Int, createdAt: Option[DateTime]) {
  def this(stationId: Int, status: Boolean, bikes: Int, attachs: Int) {
    this(Some(BSONObjectID.generate), stationId, status, bikes, attachs, Some(DateTime.now()))
  }
}

object StationBSON {

  implicit object Reader extends BSONReader[Station] {

    def fromBSON(document: BSONDocument): Station = {
      implicit val doc = document.toTraversable
      val station = new Station(
        doc.getAs[BSONInteger]("_id").get.value,
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONDouble]("lng").get.value.toFloat,
        doc.getAs[BSONDouble]("lat").get.value.toFloat)
      station
    }
  }

  implicit object Writer extends BSONWriter[Station] {

    def toBSON(station: Station) = {
      BSONDocument(
        "_id" -> BSONInteger(station.id),
        "name" -> BSONString(station.name),
        "lng" -> BSONDouble(station.longitude),
        "lat" -> BSONDouble(station.latitude)
      )
    }
  }

}

object StationDetailsBSON {

  implicit object Reader extends BSONReader[StationDetails] {
    def fromBSON(document: BSONDocument): StationDetails = {
      implicit val doc = document.toTraversable
      val station = new StationDetails(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONInteger]("stationId").get.value,
        doc.getAs[BSONBoolean]("down").get.value,
        doc.getAs[BSONInteger]("bikes").get.value,
        doc.getAs[BSONInteger]("attachs").get.value,
        doc.getAs[BSONDateTime]("when").map(dt => new DateTime(dt.value)))
      station
    }
  }

  implicit object Writer extends BSONWriter[StationDetails] {
    def toBSON(station: StationDetails) = {
      BSONDocument(
        "_id" -> station.id.getOrElse(BSONObjectID.generate),
        "stationId" -> BSONInteger(station.stationId),
        "down" -> BSONBoolean(station.status),
        "bikes" -> BSONInteger(station.bikes),
        "attachs" -> BSONInteger(station.attachs),
        "when" ->  station.createdAt.map(date => BSONDateTime(date.getMillis))
      )
    }
  }
}