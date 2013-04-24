package parser

import java.io.InputStreamReader
import java.net.URL

import xml.{NodeSeq, XML, Elem}

import play.api.Logger

import models._

class VlilleParser(httpUrl: String) {

  def xml(): Elem = {
    XML.load(getInputStreamReader(httpUrl))
  }

  def getInputStreamReader(httpUrl: String): InputStreamReader = {
    val url = new URL(httpUrl)
    val conn = url.openConnection()

    new InputStreamReader(conn.getInputStream(), "UTF-8")
  }

}

object VlilleParser {

  val VlilleListUrl = "http://vlille.fr/stations/xml-stations.aspx"
  val VlilleDetailsUrl = "http://www.vlille.fr/stations/xml-station.aspx?borne="

  def xml(httpUrl: String): Elem = {
    return new VlilleParser(httpUrl).xml()
  }

  def list(): List[Station] = {
    Logger.debug("Get vlille stations")
    val elem = xml(VlilleListUrl)
    var stations: List[Station] = List()

    elem \ "marker" foreach { (entry) =>
      stations ::= fromListXML(entry)
    }

    Logger.debug(s"${stations.size} stations found")

    stations
  }

  def fromListXML(node: NodeSeq): Station = {
    new Station(
      id = (node \ "@id").toString().toInt,
      name = (node \ "@name").toString(),
      longitude = (node \ "@lng").toString().toFloat,
      latitude = (node \ "@lat").toString().toFloat)
  }

  def details(stationId: Int): StationDetails = {
    val elem = xml(VlilleDetailsUrl + stationId)

    new StationDetails(
      stationId,
      if (((elem \ "status") text).toString().toInt == 1) true else false,
      ((elem \ "bikes") text).toString().toInt,
      ((elem \ "attachs") text).toString().toInt)
  }

}