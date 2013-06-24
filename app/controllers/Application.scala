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
    request.session.get("username").map { user =>
     Ok(views.html.index(Some(user)))
    }.getOrElse {
    	Ok(views.html.index(None))
    }
  }
  
  def login(username: Option[String]) = Action { implicit request =>
    username.filterNot(_.isEmpty()).map { username =>
      Redirect(routes.Application.index).withSession("username" -> username)
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Plean choose a valid username!")
    }
  }
  
  def logout = Action{ implicit request =>
    Redirect(routes.Application.index).withSession(session - "username")
    
  }
  
  def rooms = TODO
  
  def about = TODO
  
  def room = Action { implicit request =>
    request.session.get("username").map { user =>
     Ok(views.html.room(Option(user),Place.conversations.toList, discussionForm))
    }.getOrElse {
    	Redirect(routes.Application.index).flashing("error" -> "Plean choose a username!")
    }
  }
  
  def newDiscussion = Action{ implicit request =>
    request.session.get("username").map { user =>
     	discussionForm.bindFromRequest.fold(
    		errors => BadRequest(views.html.room(Option(user),Place.conversations.toList, errors)),
    		name_discussion => {
    		  Redirect(routes.Application.chatRoom(name_discussion, user))
    		}
     	)
    }.getOrElse {
    	Redirect(routes.Application.index).flashing("error" -> "Plean choose a username!")
    }  
  }
  
  def chatRoomAkka(username: Option[String]) = Action { implicit request =>
    username.filterNot(_.isEmpty()).map { username =>
      Ok(views.html.chatRoomAkka(username))
    }.getOrElse {
      Redirect(routes.Application.index).flashing("error" -> "Plean choose a valid username!")
    }
  }
  
  def chatAkka(username: String) = WebSocket.async[JsValue] { request =>
    ChatRoomAkka.join(username);
  }
  
  def chatRoom(conversation: String, username: String) = Action { implicit request =>
    Ok(views.html.chatRoom(username, conversation))
  }
  
  def chat(conversation: String, username: String) = WebSocket.async[JsValue] { request =>
    Place.request(new User(username), conversation)
  } 
  
}
