package pregnaware.database

import akka.util.Timeout
import pregnaware.{DbTest, UnitSpec}

import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext}

class BabyNameTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val dbWrapper = new DatabaseWrapper {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "add baby names" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    val user2 = Await.result(dbWrapper.addUser("TEST_2", "TEST_EMAIL_2", "TEST_PASSWORD_2"), timeout)

    val baby1 = Await.result(dbWrapper.addName(user1.userId, user1.userId, "TEST_NAME_1", isBoy = true), timeout)
    val baby2 = Await.result(dbWrapper.addName(user1.userId, user2.userId, "TEST_NAME_2", isBoy = false), timeout)

    baby1.isBoy should be (true)
    baby1.name should be ("TEST_NAME_1")
    baby1.suggestedBy should be (user1.userId)
    baby1.suggestedByName should be ("TEST_1")
    baby1.userId should be (user1.userId)

    baby2.isBoy should be (false)
    baby2.name should be ("TEST_NAME_2")
    baby2.suggestedBy should be (user2.userId)
    baby2.suggestedByName should be ("TEST_2")
    baby2.userId should be (user1.userId)

    val user1PostName = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get

    user1PostName.babyNames should not be empty
    user1PostName.babyNames should have size 2
    user1PostName.babyNames.head.name should be ("TEST_NAME_1")
    user1PostName.babyNames.last.name should be ("TEST_NAME_2")
  }

  // This cleans up the users created in this test
  "Users" should "be deleted" taggedAs(DbTest) in {
    Seq("TEST_EMAIL_1", "TEST_EMAIL_2").foreach { email =>
      val deleteFut = dbWrapper.getUser(email).flatMap {
        case None => Future.successful(())
        case Some(user) => dbWrapper.deleteUser(user.userId)
      }
      Await.ready(deleteFut, timeout)
      Await.result(dbWrapper.getUser(email), timeout) should not be defined
    }
  }
}
