package models

import scala.Option

import org.joda.time.DateTime

import reactivemongo.bson._

trait MongoModel[T] {
  def _id: T
  def id = _id
}

case class Station(_id: Int, name: String, lng: Float, lat: Float) extends MongoModel[Int]

case class StationItem(_id: Option[BSONObjectID], stationId: Int, down: Boolean,
                          startAt: DateTime, endAt: Option[DateTime], duration: Long,
                          bikes: Int, attachs: Int) extends MongoModel[Option[BSONObjectID]] {
  def this(stationId: Int, down: Boolean, duration: Int,  bikes: Int, attachs: Int) {
    this(Some(BSONObjectID.generate), stationId, down, DateTime.now(), None, duration, bikes, attachs)
  }

}

//object StationItemBSON {
//
//  implicit object Reader extends BSONDocumentReader[StationDetails] {
//    def read(document: BSONDocument): StationItem = {
//      implicit val doc = document
//      val station = new StationItem(
//        Some(doc.getAs[BSONObjectID]("_id")),
//        doc.getAs[BSONInteger]("stationId").get.value,
//        doc.getAs[BSONBoolean]("down").get.value,
//        doc.getAs[BSONBoolean]("startAt").get.value,
//        doc.getAs[BSONBoolean]("endAt").get.value,
//        doc.getAs[BSONInteger]("bikes").get.value,
//        doc.getAs[BSONInteger]("attachs").get.value
//      station
//    }
//  }
//
//}
