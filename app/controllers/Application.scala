package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.iteratee._
import models._
import models.chat._
import models.users._
import akka.actor._
import scala.concurrent.duration._


object Application extends Controller {
  
  var discussionForm = Form(
		  "name_discussion" -> nonEmptyText
  )
  
  /**
   * Just display the home page.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(None))
  }
  
  def rooms = TODO
  
  def about = TODO
  
  def room = Action { implicit request =>
    Ok(views.html.room(Option(""),List("uno", "due"), discussionForm))
  }
  
  def newDiscussion = Action{ implicit request =>
    discussionForm.bindFromRequest.fold(
    		errors => BadRequest(views.html.room(Option(""),List("uno", "due"), errors)),
    		name_discussion => {
    		  Ok(name_discussion)
    		}
    )
    
  }
  
  def chatRoomAkka(username: Option[String]) = Action { implicit request =>
    System.out.println("(chatRoomAkka) Username" + username);
    username.filterNot(_.isEmpty()).map { username =>
      Ok(views.html.chatRoomAkka(username))
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Plean choose a valid username!")
    }
  }
  
  def chatAkka(username: String) = WebSocket.async[JsValue] { request =>
    System.out.println("chatAkka socket");
    ChatRoomAkka.join(username);
  }
  
  def chatRoom(conversation: String, username: String) = Action { implicit request =>
    Ok(views.html.chatRoom(username, conversation))
  }
  
  def chat(conversation: String, username: String) = WebSocket.async[JsValue] { request =>
    Place.request(new User(username), conversation)
  } 
  
}
