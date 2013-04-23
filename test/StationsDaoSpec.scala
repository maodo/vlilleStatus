import org.specs2.mutable._

import concurrent.duration.Duration
import concurrent.{Await, Future}

import java.util.concurrent.TimeUnit

import models._
import mongo._
import parser._

import TestHelpers._

class StationsDaoSpec extends Specification {

   "The StationsDao class" should {

     "save all stations" in new FakeApp {
       val stations = VlilleParser.list()
       StationsDao.save(stations)

       val futureStations: Future[List[Station]] = StationsDao.find()
       Await.ready(futureStations, Duration(5, TimeUnit.SECONDS))

       val savedStations = seq(futureStations)
       savedStations.size must be >(180)
     }

     "save one station" in new FakeApp {
       val stationDetails = VlilleParser.details(1)
       StationDetailsDao.save(stationDetails)

       val futureStations: Future[List[StationDetails]] = StationDetailsDao.find()
       Await.ready(futureStations, Duration(5, TimeUnit.SECONDS))

       val savedStations = seq(futureStations)
       savedStations.size must be equalTo(1)

     }
   }



 }
