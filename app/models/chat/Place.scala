package models.chat

import models.users._
import akka.actor._
import scala.concurrent.duration._
import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import akka.util.Timeout
import akka.pattern.ask
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.immutable.HashMap

/*
import play.api.libs.json._
import play.api.libs.functional.syntax._
*/

object Place {
  implicit val timeout = Timeout(1 second)
  val system = ActorSystem("PlaceSystem")
  val loggerIteratee = Iteratee.foreach[JsValue](event => Logger("Place").info(event.toString))
  var conversations = HashMap.empty[String, Int]
  
  def rooms = "";
  
  def request(user: User, conversationName: String):scala.concurrent.Future[(Iteratee[JsValue,_], Enumerator[JsValue])] = {
    var conversation = system.actorFor(system.child(conversationName));
    if(conversation.isTerminated){
      conversation = system.actorOf(Props[Conversation], conversationName)
      conversations += conversationName -> 1
    }
    
    (conversation ? Join(user)).map {
      case Connection(enumerator) => {
        //bind a logger
        enumerator |>> loggerIteratee
        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          conversation ! Talk(user, (event \ "text").as[String])
        }.mapDone { _ =>
          conversation ! Leave(user)
        }
        (iteratee,enumerator)
      }
      case ConnectionRefused(error) => {
        // Connection error
        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
      }
      case MissedTarget =>{ 
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString("")))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
      }
    }
  }
  
}

class Conversation extends Actor{
  var members = Set.empty[User]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
  
  def receive = {
    case Talk(user: User, message: String) => {
      notifyAll("talk", user, message)
    }
    case Invite => {
      
    }
    case Join(user: User, conversation: Option[Conversation]) => {
      if(this != conversation.getOrElse(this)){
    	  sender ! MissedTarget
      }
      else if(members.contains(user)){
        sender ! ConnectionRefused("User already into Conversation")
      }
      else{
    	  members = members + user;
    	  sender ! Connection(chatEnumerator)
    	  self ! NotifyJoin(user)
      }
    }
    case Leave(user: User) => {
      members = members - user
      notifyAll("leave", user, "has leaved the conversation")
    }
    case NotifyJoin(user: User) => {
      notifyAll("join", user, "has entered the conversation")
    }
  }
  
  def notifyAll(kind: String, user: User, text: String) {
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> JsString(user.username),
        "message" -> JsString(text),
        "members" -> JsArray(
          members.toList.map(f => JsString(f.username))
        )
      )
    )
    chatChannel.push(msg)
  }
  
}

/*Messages*/
case class Talk(who: User, message: String)
case class Invite(applicant: User, invited: User)
object Join{def apply(who: User) = new Join(who, None)}
case class Join(who: User, conversation: Option[Conversation])
case class Leave(who: User)

case class NotifyJoin(username: User)

case class Connection(enumerator: Enumerator[JsValue])
case class ConnectionRefused(message: String)
case object MissedTarget