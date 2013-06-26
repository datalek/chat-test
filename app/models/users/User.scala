package models.users

class User(usernamec: String){
  val username = usernamec
}

/*
object test{
  
  def nonBlockingComposition: Future[Profile] = {
    // NOTE: We declare these up here so they start in parallel. This is required
    // because of how for expressions get expanded to maps and flatMaps.
    val futureName = name()
    val futureScore = score()
    val futureFriends = friends()
 
    for {
        nameResult <- futureName
        scoreResult <- futureScore
        friendsResult <- futureFriends
    } yield {
        Profile(nameResult, scoreResult, friendsResult)
    }
}
  
futuresSetup.scalaScala
1
2
3
def name(): Future[String] = Future { "david" }
def score(): Future[Double] = Future { 50.0 }
def friends(): Future[List[Int]] = Future { List(1, 2, 3) }
  
}
*/



