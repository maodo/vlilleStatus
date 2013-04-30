import org.specs2.mutable._

import java.util.concurrent.TimeUnit
import org.joda.time.DateTime

import concurrent.duration.Duration
import concurrent.{Await, Future}

import play.api.libs.json._

import models._
import dao._
import utils.DateImplicits._

import TestHelpers._

class StationsDaoSpec extends Specification {

  "The StationsDao class" should {

    "save all stations" in new FakeApp {
      val stations = List(
        new Station(_id = 1, name = "Station 1", lng = 1, lat = 2),
        new Station(_id = 2, "Station 2", lng = 2, lat = 3)
      )
      StationDao.save(stations)

      val futureStations: Future[List[Station]] = StationDao.find()
      Await.ready(futureStations, Duration(5, TimeUnit.SECONDS))

      val savedStations = seq(futureStations)
      savedStations.size must be equalTo (2)
    }

    "save one station" in new FakeApp {
      StationItemDao.save(new StationItem(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))
      val futureStations: Future[List[StationItem]] = StationItemDao.find()
      Await.ready(futureStations, Duration(5, TimeUnit.SECONDS))

      val savedStations = seq(futureStations)
      savedStations.size must be equalTo (1)

      val savedStation = savedStations.head
      savedStation.stationId must be equalTo(1)
      savedStation.down must be equalTo(true)
      savedStation.bikes must be equalTo(10)
      savedStation.attachs must be equalTo(20)
    }

    "find running items" in new FakeApp {
      StationItemDao.save(new StationItem(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))
      StationItemDao.save(new StationItem(stationId = 2, down = false, duration = 5, bikes = 0, attachs = 18))
      StationItemDao.save(new StationItem(stationId = 3, down = true, duration = 5, bikes = 0, attachs = 10))

      val futureItems = StationItemDao.findRunning()
      Await.ready(futureItems, Duration(5, TimeUnit.SECONDS))

      val items = seq(futureItems)
      items.size must be equalTo(3)
    }

    "update attributes by BSONId" in new FakeApp {
      StationItemDao.save(new StationItem(stationId = 1, down = true, duration = 5,  bikes = 10, attachs = 20))
      val futureItems = StationItemDao.find()
      Await.ready(futureItems, Duration(5, TimeUnit.SECONDS))

      val items = seq(futureItems)
      items must not be Nil

      StationItemDao.update(items.head.id.get, Json.obj("duration" -> 10))
      val futureItemsAfterUpdate = StationItemDao.find()
      Await.ready(futureItemsAfterUpdate, Duration(5, TimeUnit.SECONDS))

      val updatedItems = seq(futureItemsAfterUpdate)
      updatedItems.head.duration must be equalTo(10)
    }

  }


}
