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
import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

//import ExecutonContext.Implicit.global



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
  
  def example0 = Action {
    val responseFuture: Future[play.api.libs.ws.Response] = WS.url("http://example.com").get()
    Logger.info("Before map")
    val resultFuture: Future[Result] = responseFuture.map { resp =>
      Logger.info("Within map")
      // Create a Result that uses the http status, body, and content-type 
      // from the example.com Response
      Status(resp.status)(resp.body).as(resp.ahcResponse.getContentType)
    }
    Logger.info("After map")    
    
    Async(resultFuture)
  }
  
  def example1 = Action {
     // Make 3 sequential async calls
    Logger.info("-- (1) Before All --")
    val responseFuture1 = {WS.url("http://google.com").get()}
    val responseFuture2 = {WS.url("http://yahoo.com").get()}
    val responseFuture3 = {WS.url("http://merda.com").get()}
    Logger.info("-- After call to sites --")
    val resultFuture = for {
 		foo <- {var r = responseFuture1;Logger.info("GOOGLE FINISH");r}
 		bar <- {var r = responseFuture2;Logger.info("YAHOO FINISH"); r}
 		baz <- {var r = responseFuture3;Logger.info("MERDA FINISH"); r}
 	} yield {
   	// Build a Result using foo, bar, and baz
 		Logger.info("< Start of yield >")
 		Status(foo.status)(foo.body).as(foo.ahcResponse.getContentType)
 	}
 	Logger.info("-- After yield --")
 	Async(resultFuture)
  }
  
  def example2 = Action {
     // Make 3 sequential async calls
    Logger.info("-- (2) Before All --")
    val responseFuture1 = WS.url("http://google.com")
    val responseFuture2 = WS.url("http://yahoo.com")
    val responseFuture3 = WS.url("http://merda.com")
    Logger.info("-- After call to sites --")
    val resultFuture = for {
 		foo <- {val a = responseFuture1.get();Logger.info("GOOGLE FINISH");a}
 		bar <- {val a = responseFuture2.get();Logger.info("YAHOO FINISH");a}
 		baz <- {val a = responseFuture3.get();Logger.info("MERDA FINISH");a}
 	} yield {
   	// Build a Result using foo, bar, and baz
 		Logger.info("< Start of yield >")
 		Status(foo.status)(foo.body).as(foo.ahcResponse.getContentType)
 	}
 	Logger.info("-- After yield --")
 	Async(resultFuture)
  }
  
  def example3 = Action {
    Logger.info("-- BEFORE ALL EXAMPLE 3--")
    val responseFuture1 = scala.concurrent.Future { Logger.info("(1)Start");Thread.sleep(1000); Logger.info("(1)Done");"done1" }
    val responseFuture2 = scala.concurrent.Future { Logger.info("(2)Start");Thread.sleep(500); Logger.info("(2)Done");"done2"}
    Logger.info("AFTER THREADS")
    val resultFuture = for {
      result1 <- responseFuture1
      result2 <- responseFuture2
    } yield {
      Logger.info("INTO YIELD")
      Ok("Result: " + result1 + ", " + result2)
    }
    Logger.info("-- AFTER ALL --")
    Async{
      resultFuture
    }
  }
  
  def example4 = Action {
    Logger.info("-- BEFORE ALL EXAMPLE 4--")
    //val responseFuture1 = scala.concurrent.Future { Logger.info("(1)Start");Thread.sleep(3000); Logger.info("(1)Done");"done1" }
    //val responseFuture2 = scala.concurrent.Future { Logger.info("(2)Start");Thread.sleep(5000); Logger.info("(2)Done");"done2"}
    //Logger.info("AFTER THREADS")
    val resultFuture = for {
      result1 <- scala.concurrent.Future { Logger.info("(1)Start");Thread.sleep(1000); Logger.info("(1)Done");"done1" }
      result2 <- scala.concurrent.Future { Logger.info("(2)Start");Thread.sleep(500); Logger.info("(2)Done");"done2" }
    } yield {
      Logger.info("INTO YIELD")
      Ok("Result: " + result1 + ", " + result2)
    }
    Logger.info("-- AFTER ALL --")
    Async{
      resultFuture
    }
  }

  def example5 = Action {
    //Logger.info("-- BEFORE ALL EXAMPLE 5--")
    val resultFuture = models.example.Akka.doHeavyWork
    //Logger.info("AFTER AKKA")
    val result = resultFuture.map{res =>
      Ok(res(0) + res(1))
    }
    /*
    val resultFuture = for {
      result1 <- scala.concurrent.Future { Logger.info("(1)Start");Thread.sleep(8000); Logger.info("(1)Done");"done1" }
      result2 <- scala.concurrent.Future { Logger.info("(2)Start");Thread.sleep(3000); Logger.info("(2)Done");"done2" }
    } yield {
      Logger.info("INTO YIELD")
      Ok("Result: " + result1 + ", " + result2)
    }
    */
    //Logger.info("-- AFTER ALL --")
    Async{
    	result
    }
    /*
    Async{
      resultFuture
    }
    */
  }
  
  /*
   * // Make 3 sequential async calls
   *	WS.url("http://foo.com").get().flatMap { foo =>
   * 		WS.url("http://bar.com").get().flatMap { bar =>
   *  			WS.url("http://baz.com").get().map { baz =>
   *   			// Build a Result using foo, bar, and baz
   *  				Ok(...)
   *			}
   *		}
   *	}
   */
  
  // Make 3 sequential async calls
/*	for {
 *		foo <- WS.url("http://foo.com").get()
 *		bar <- WS.url("http://bar.com").get()
 *		baz <- WS.url("http://baz.com").get()
 *	} yield {
 *  	// Build a Result using foo, bar, and baz
 *  	Ok(...)
 *	}
*/
  
}
