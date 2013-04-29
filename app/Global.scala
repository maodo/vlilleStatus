import play.api.mvc._

import scala.concurrent.duration.DurationInt

import akka.actor.Props

import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka

import actor._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    play.api.Play.mode(app) match {
      case play.api.Mode.Test => // do not schedule anything for Test
      case _ => reminderDaemon(app)
    }
  }

  def reminderDaemon(app: Application) = {
    Logger.info("Scheduling the stations details actor")
    val reminderActor = Akka.system(app).actorOf(Props(new StationDetailsActor()))
    Akka.system(app).scheduler.schedule(0 seconds, 2 minutes, reminderActor, "vlilleStationDetailsActor")
  }

}
