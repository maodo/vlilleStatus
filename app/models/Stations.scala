package models

import scala.Option

import org.joda.time.DateTime

import reactivemongo.bson.BSONObjectID

case class Station(_id: Int, name: String, lng: Float, lat: Float) {
  def id = _id
}
case class StationDetails(_id: Option[BSONObjectID], stationId: Int, down: Boolean, bikes: Int, attachs: Int, createdAt: Option[DateTime]) {
  def this(stationId: Int, down: Boolean, bikes: Int, attachs: Int) {
    this(Some(BSONObjectID.generate), stationId, down, bikes, attachs, Some(DateTime.now()))
  }

  def id = _id
}
