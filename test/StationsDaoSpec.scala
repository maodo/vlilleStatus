import org.specs2.mutable._

import org.joda.time.DateTime

import play.api.libs.json._

import models._
import dao._
import reactivemongo.bson.BSONObjectID
import utils.DateImplicits._

import TestAwait._

class StationsDaoSpec extends Specification {

  "The StationDao class" should {

    "save all stations and find them in ascending order" in new FakeApp {
      StationDao.save(List(
        new Station(_id = 1, name = "Station A", lng = 1, lat = 2),
        new Station(_id = 2, name = "Station Z", lng = 2, lat = 3)
      ))

      val stations = result(StationDao.find())
      stations.size must be equalTo (2)
      stations(0).name must be equalTo("Station A")
      stations(1).name must be equalTo("Station Z")
    }

  }

  "The StationIdemDao class" should {

    def saveDefaultStationItem() = StationItemDao.save(new StationItem(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))

    "save one station item and find it" in new FakeApp {
      saveDefaultStationItem()

      val stations = result(StationItemDao.find())
      stations.size must be equalTo (1)

      val firstStation = stations.head
      firstStation.stationId must be equalTo(1)
      firstStation.down must be equalTo(true)
      firstStation.bikes must be equalTo(10)
      firstStation.attachs must be equalTo(20)
    }

    "find running items" in new FakeApp {
      StationItemDao.save(new StationItem(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))
      StationItemDao.save(new StationItem(stationId = 2, down = false, duration = 5, bikes = 0, attachs = 18))
      StationItemDao.save(new StationItem(stationId = 3, down = true, duration = 5, bikes = 0, attachs = 10))
      // One finished item.
      StationItemDao.save(new StationItem(Some(BSONObjectID.generate), stationId = 4, down = true,
                            startAt = DateTime.now(), endAt = Some(DateTime.now().plusMinutes(2)), duration = 5,
                          bikes = 0, attachs = 10))

      val items = result(StationItemDao.findRunning())
      items.size must be equalTo(3)
    }

    "update attributes by BSONId" in new FakeApp {
      saveDefaultStationItem()

      val items = result(StationItemDao.find())
      items must not be Nil
      StationItemDao.update(items.head.id.get, Json.obj("duration" -> 10))

      val updatedItems = result(StationItemDao.find())
      updatedItems.head.duration must be equalTo(10)
    }

  }


}
