package models

import scala.Option

import org.joda.time.DateTime

import reactivemongo.bson.BSONObjectID

case class Station(_id: Int, name: String, longitude: Float, latitude: Float) {
  def id = _id
}
case class StationDetails(id: Option[BSONObjectID], stationId: Int, status: Boolean, bikes: Int, attachs: Int, createdAt: Option[DateTime]) {
  def this(stationId: Int, status: Boolean, bikes: Int, attachs: Int) {
    this(Some(BSONObjectID.generate), stationId, status, bikes, attachs, Some(DateTime.now()))
  }
}
