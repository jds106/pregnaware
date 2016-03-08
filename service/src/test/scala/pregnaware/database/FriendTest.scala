package pregnaware.database

import java.time.LocalDate

import akka.util.Timeout
import pregnaware.{DbTest, UnitSpec}
import pregnaware.user.entities.WrappedFriend

import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.util.{Failure, Success}

class FriendTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val dbWrapper = new DatabaseWrapper() {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "be added" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    user1.displayName should be("TEST_1")
    user1.email should be("TEST_EMAIL_1")
    user1.passwordHash should be("TEST_PASSWORD_1")
    user1.babyNames should be(empty)
    user1.friends should be(empty)
    user1.dueDate should not be defined

    val user2 = Await.result(dbWrapper.addUser("TEST_2", "TEST_EMAIL_2", "TEST_PASSWORD_2"), timeout)
    user2.displayName should be("TEST_2")
    user2.email should be("TEST_EMAIL_2")
    user2.passwordHash should be("TEST_PASSWORD_2")
    user2.babyNames should be(empty)
    user2.friends should be(empty)
    user2.dueDate should not be defined
  }

  "User" should "add friend" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get

    // Add due dates and names to the users, and ensure they cannot be accessed by another user
    // until the friendship is confirmed
    val user1DueDate = LocalDate.of(2016, 1, 1)
    val user2DueDate = LocalDate.of(2016, 2, 2)

    Await.result(dbWrapper.setDueDate(user1.userId, user1DueDate), timeout)
    Await.result(dbWrapper.setDueDate(user2.userId, user2DueDate), timeout)

    Await.result(dbWrapper.addName(user1.userId, user1.userId, "NAME1", true), timeout)
    Await.result(dbWrapper.addName(user2.userId, user2.userId, "NAME2", true), timeout)

    val friendToBe : WrappedFriend = Await.result(dbWrapper.addFriend(user1.userId, user2.userId), timeout)

    friendToBe.userId should be(user2.userId)
    friendToBe.displayName should be ("TEST_2")
    friendToBe.email should be ("TEST_EMAIL_2")

    friendToBe.dueDate should not be defined
    friendToBe.babyNames should be (empty)

    val user1PostFriend = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2PostFriend = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get

    user1PostFriend.friendRequestsReceived should be (empty)
    user1PostFriend.friendRequestsSent should not be empty
    user1PostFriend.friendRequestsSent should have size 1
    user1PostFriend.friendRequestsSent.head.userId should be (user2.userId)
    user1PostFriend.friendRequestsSent.head.displayName should be ("TEST_2")
    user1PostFriend.friendRequestsSent.head.email should be ("TEST_EMAIL_2")
    user1PostFriend.friendRequestsSent.head.dueDate should not be defined
    user1PostFriend.friendRequestsSent.head.babyNames should be (empty)

    user2PostFriend.friendRequestsSent should be (empty)
    user2PostFriend.friendRequestsReceived should not be empty
    user2PostFriend.friendRequestsReceived should have size 1
    user2PostFriend.friendRequestsReceived.head.userId should be (user1.userId)
    user2PostFriend.friendRequestsReceived.head.displayName should be ("TEST_1")
    user2PostFriend.friendRequestsReceived.head.email should be ("TEST_EMAIL_1")
    user2PostFriend.friendRequestsReceived.head.dueDate should not be defined
    user2PostFriend.friendRequestsReceived.head.babyNames should be (empty)

    // User2 now confirms User1 as a friend
    val confirmedFriend = Await.result(dbWrapper.confirmFriend(user2.userId, user1.userId), timeout)
    confirmedFriend.userId should be (user1.userId)
    confirmedFriend.displayName should be ("TEST_1")

    val user1PostFriendAccept = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2PostFriendAccept = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get

    user1PostFriendAccept.friends should not be empty
    user1PostFriendAccept.friends should have size 1
    user1PostFriendAccept.friends.head.userId should be (user2PostFriend.userId)
    user1PostFriendAccept.friends.head.displayName should be ("TEST_2")
    user1PostFriendAccept.friends.head.email should be ("TEST_EMAIL_2")
    user1PostFriendAccept.friends.head.dueDate should be (Some(user2DueDate))
    user1PostFriendAccept.friends.head.babyNames.head.name should be ("NAME2")
    user1PostFriendAccept.friendRequestsSent should be (empty)
    user1PostFriendAccept.friendRequestsReceived should be (empty)

    user2PostFriendAccept.friends should not be empty
    user2PostFriendAccept.friends should have size 1
    user2PostFriendAccept.friends.head.userId should be (user1PostFriend.userId)
    user2PostFriendAccept.friends.head.displayName should be ("TEST_1")
    user2PostFriendAccept.friends.head.email should be ("TEST_EMAIL_1")
    user2PostFriendAccept.friends.head.dueDate should be (Some(user1DueDate))
    user2PostFriendAccept.friends.head.babyNames.head.name should be ("NAME1")
    user2PostFriendAccept.friendRequestsSent should be (empty)
    user2PostFriendAccept.friendRequestsReceived should be (empty)
  }

  "User" should "delete friend" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get

    Await.ready(dbWrapper.deleteFriend(user1.userId, user2.userId), timeout)

    val user1PostFriendDelete = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2PostFriendDelete = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get
    user1PostFriendDelete.friends should be(empty)
    user2PostFriendDelete.friends should be(empty)
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
