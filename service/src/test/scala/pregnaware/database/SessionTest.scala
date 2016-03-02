package pregnaware.database

import akka.util.Timeout
import pregnaware.UnitSpec
import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext, Await}

class SessionTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val dbWrapper = new DatabaseWrapper() {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "add a session" in {
    val user1 = Await.result(dbWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    val sessionId = Await.result(dbWrapper.getSession(user1.userId), timeout)
    val userId = Await.result(dbWrapper.getUserIdFromSession(sessionId), timeout).get

    userId should be (user1.userId)
  }

  // This cleans up the users created in this test
  "Users" should "be deleted" in {
    Seq("TEST_EMAIL_1").foreach { email =>
      val deleteFut = dbWrapper.getUser(email).flatMap {
        case None => Future.successful(())
        case Some(user) => dbWrapper.deleteUser(user.userId)
      }
      Await.ready(deleteFut, timeout)
      Await.result(dbWrapper.getUser(email), timeout) should not be defined
    }
  }
}
