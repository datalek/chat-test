package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import models._
import models.chat._
import models.users._

import akka.actor._
import scala.concurrent.duration._

object Application extends Controller {
  
  /**
   * Just display the home page.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(None))
  }
  
  def rooms = TODO
  
  def about = TODO
  
  /*
   /**
   * Display the chat room page.
   */
  def chatRoom(username: Option[String]) = Action { implicit request =>
    System.out.println("(chatRoom) Username" + username);
    username.filterNot(_.isEmpty).map { username =>
      Ok(views.html.chatRoom(username))
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Please choose a valid username."
      )
    }
  }
  
  /**
   * Handles the chat websocket.
   */
  def chat(username: String) = WebSocket.async[JsValue] { request  =>

    ChatRoom.join(username)
    
  }
 */
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
