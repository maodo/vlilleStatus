package controllers

import concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._
import mongo._

object StationsController extends Controller {
  
  def list = Action {
    implicit val writeTweetAsJson = Json.writes[Station]
    Async {
       StationsDao.find() map {
         stations => Ok(Json.toJson(stations)).as("application/json")
       }
    }
  }
  
}