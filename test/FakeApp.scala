import concurrent.duration.Duration
import concurrent.Await
import concurrent.{ExecutionContext}
import ExecutionContext.Implicits.global

import java.util.concurrent.TimeUnit

import org.specs2.execute.AsResult
import org.specs2.mutable.Around

import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.Play.current

import play.modules.reactivemongo.ReactiveMongoPlugin

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.DefaultBSONHandlers._

import dao._

trait FakeApp extends Around with org.specs2.specification.Scope {

  val TestMongoDbName = "test_vlille_status"

  val appTestMongoDatabase =
    Map(("mongodb.uri" -> s"mongodb://127.0.0.1:27017/$TestMongoDbName"))

  object FakeApp extends FakeApplication(
    additionalConfiguration = appTestMongoDatabase
  ) {
  }

  def around[T: AsResult](t: => T) = running(FakeApp) {
    Logger.debug("Running test ==================================")
    Logger.debug("Clear test database ===========================")
    val daos = List(StationListDao, StationItemDao)
    daos.foreach(dao => {
      Logger.debug(s"\tClear collection $dao")
      val futureRemove = ReactiveMongoPlugin.db.collection[BSONCollection](dao.collectionName()).remove(BSONDocument())
      Await.ready(futureRemove, Duration(60, TimeUnit.SECONDS))
    })

    // Run tests inside a fake application
    AsResult(t)
  }
}