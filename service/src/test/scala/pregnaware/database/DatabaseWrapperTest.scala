package pregnaware.database

import java.time.LocalDate

import akka.util.Timeout
import pregnaware.UnitSpec
import pregnaware.database.wrappers.{BabyNameWrapper, ProgressWrapper, SessionWrapper, UserWrapper}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

class DatabaseWrapperTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val userWrapper = new UserWrapper() {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  private val babyNameWrapper = new BabyNameWrapper {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  private val progressWrapper = new ProgressWrapper {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  private val sessionWrapper = new SessionWrapper {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "be added" in {
    val user1 = Await.result(userWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    user1.displayName should be("TEST_1")
    user1.email should be("TEST_EMAIL_1")
    user1.passwordHash should be("TEST_PASSWORD_1")
    user1.babyNames should be(empty)
    user1.friends should be(empty)
    user1.dueDate should not be defined

    val user2 = Await.result(userWrapper.addUser("TEST_2", "TEST_EMAIL_2", "TEST_PASSWORD_2"), timeout)
    user2.displayName should be("TEST_2")
    user2.email should be("TEST_EMAIL_2")
    user2.passwordHash should be("TEST_PASSWORD_2")
    user2.babyNames should be(empty)
    user2.friends should be(empty)
    user2.dueDate should not be defined
  }

  "User" should "add friend" in {
    val user1 = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get

    val friend1 = Await.result(userWrapper.addFriend(user1.userId, user2.userId), timeout)

    friend1.userId should be(user2.userId)
    friend1.babyNames should be (empty)
    friend1.displayName should be ("TEST_2")
    friend1.dueDate should not be defined
    friend1.email should be ("TEST_EMAIL_2")

    val user1PostFriend = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2PostFriend = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get

    user1PostFriend.friends should not be empty
    user1PostFriend.friends should have size 1
    user1PostFriend.friends.head.userId should be (user2PostFriend.userId)
    user1PostFriend.friends.head.displayName should be ("TEST_2")
    user1PostFriend.friends.head.email should be ("TEST_EMAIL_2")

    user2PostFriend.friends should not be empty
    user2PostFriend.friends should have size 1
    user2PostFriend.friends.head.userId should be (user1PostFriend.userId)
    user2PostFriend.friends.head.displayName should be ("TEST_1")
    user2PostFriend.friends.head.email should be ("TEST_EMAIL_1")
  }

  "User" should "add baby names" in {
    val user1 = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get

    val baby1 = Await.result(babyNameWrapper.addName(user1.userId, user1.userId, "TEST_NAME_1", isBoy = true), timeout)
    val baby2 = Await.result(babyNameWrapper.addName(user1.userId, user2.userId, "TEST_NAME_2", isBoy = false), timeout)

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

    val user1PostName = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get

    user1PostName.babyNames should not be empty
    user1PostName.babyNames should have size 2
    user1PostName.babyNames.head.name should be ("TEST_NAME_1")
    user1PostName.babyNames.last.name should be ("TEST_NAME_2")
  }

  "User" should "add due date" in {
    val user1 = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    user1.dueDate should not be defined

    val dueDate = LocalDate.of(2016, 7, 21)
    val dueDateResponse = Await.result(progressWrapper.setDueDate(user1.userId, dueDate), timeout)

    dueDateResponse should be (dueDate)

    val user1PostDueDate = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    user1PostDueDate.dueDate should be (Some(dueDate))

    val user2PostDueDate = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get
    user2PostDueDate.friends.head.dueDate should be (Some(dueDate))

    Await.ready(progressWrapper.deleteDueDate(user1.userId), timeout)
    val user1PostDueDateDeleted = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    user1PostDueDateDeleted.dueDate should not be defined
  }

  "User" should "add a session" in {
    val user1 = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val sessionId = Await.result(sessionWrapper.getSession(user1.userId), timeout)
    val userId = Await.result(sessionWrapper.getUserIdFromSession(sessionId), timeout).get

    userId should be (user1.userId)
  }

  "User" should "delete friend" in {
    val user1 = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get

    Await.ready(userWrapper.deleteFriend(user1.userId, user2.userId), timeout)

    val user1PostFriendDelete = Await.result(userWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2PostFriendDelete = Await.result(userWrapper.getUser("TEST_EMAIL_2"), timeout).get
    user1PostFriendDelete.friends should be(empty)
    user2PostFriendDelete.friends should be(empty)
  }

  "Users" should "be deleted" in {
    Seq("TEST_EMAIL_1", "TEST_EMAIL_2").foreach { email =>
      val user = Await.result(userWrapper.getUser(email), timeout).get

      Await.ready(userWrapper.deleteUser(user.userId), timeout).onComplete{
        case Failure(e) => fail(e)
        case Success(_) => /* Success */
      }
    }
  }
}
