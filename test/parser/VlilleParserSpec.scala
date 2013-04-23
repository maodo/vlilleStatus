package parser.specs

import org.specs2.mutable._

import java.util.concurrent.TimeUnit

import parser._
import models._

class VlilleParserSpec extends Specification {

  "The VlilleParserSpec class" should {

    "read all sttaions" in {
      val stations = VlilleParser.list()
      stations.size must be >(100)
    }

    "read one station details " in {
      val stationDetails = VlilleParser.details(1)
      stationDetails.stationId must be equalTo(1)
      stationDetails.bikes must be >=(0)
      stationDetails.attachs must be >=(0)
    }
  }

}