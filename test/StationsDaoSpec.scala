import org.specs2.mutable._

import java.util.concurrent.TimeUnit
import org.joda.time.DateTime

import concurrent.duration.Duration
import concurrent.{Await, Future}

import play.api.libs.json._

import models._
import mongo._
import utils.DateImplicits._

import TestHelpers._

class StationsDaoSpec extends Specification {

  "The StationsDao class" should {

    "save all stations" in new FakeApp {
      val stations = List(
        new Station(_id = 1, name = "Station 1", lng = 1, lat = 2),
        new Station(_id = 2, "Station 2", lng = 2, lat = 3)
      )
      StationsDao.save(stations)

      val futureStations: Future[List[Station]] = StationsDao.find()
      Await.ready(futureStations, Duration(5, TimeUnit.SECONDS))

      val savedStations = seq(futureStations)
      savedStations.size must be equalTo (2)
    }

    "save one station" in new FakeApp {
      StationDetailsDao.save(new StationDetails(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))

      val futureStations: Future[List[StationDetails]] = StationDetailsDao.find()
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
      StationDetailsDao.save(new StationDetails(stationId = 1, down = true, duration = 5, bikes = 10, attachs = 20))
      StationDetailsDao.save(new StationDetails(stationId = 2, down = false, duration = 5, bikes = 0, attachs = 18))
      StationDetailsDao.save(new StationDetails(stationId = 3, down = true, duration = 5, bikes = 0, attachs = 10))

      val now = DateTime.now()
      val futureStationDetails = StationDetailsDao.findRunningItems()
      Await.ready(futureStationDetails, Duration(5, TimeUnit.SECONDS))

      val foundStations = seq(futureStationDetails)
      foundStations.size must be equalTo(3)
    }

    "update attributes by BSONId" in new FakeApp {
      StationDetailsDao.save(new StationDetails(stationId = 1, down = true, duration = 5,  bikes = 10, attachs = 20))
      val futureStationDetails = StationDetailsDao.find()
      Await.ready(futureStationDetails, Duration(5, TimeUnit.SECONDS))

      val stations = seq(futureStationDetails)
      stations must not be Nil

      StationDetailsDao.update(stations.head.id.get, Json.obj("duration" -> 10))
      val futureStationDetailsAfterUpdate = StationDetailsDao.find()
      Await.ready(futureStationDetailsAfterUpdate, Duration(5, TimeUnit.SECONDS))

      val updatedStations = seq(futureStationDetailsAfterUpdate)
      updatedStations.head.duration must be equalTo(10)
    }

  }


}
