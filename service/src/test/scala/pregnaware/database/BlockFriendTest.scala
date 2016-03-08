package pregnaware.database

import akka.util.Timeout
import pregnaware.{DbTest, UnitSpec}
import pregnaware.user.entities.WrappedFriend

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class BlockFriendTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val dbWrapper = new DatabaseWrapper() {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "be created" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    val user2 = Await.result(dbWrapper.addUser("TEST_2", "TEST_EMAIL_2", "TEST_PASSWORD_2"), timeout)
    val user3 = Await.result(dbWrapper.addUser("TEST_3", "TEST_EMAIL_3", "TEST_PASSWORD_3"), timeout)
  }

  "User" should "not block an unknown user" in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get

    // Cannot block a friendship before it is requested
    intercept[Exception] {
      Await.result(dbWrapper.blockFriend(user1.userId, user2.userId), timeout)
    }
  }

  "User" should "make and receive friend requests" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get
    val user3 = Await.result(dbWrapper.getUser("TEST_EMAIL_3"), timeout).get

    val friendToBe1_2: WrappedFriend = Await.result(dbWrapper.addFriend(user1.userId, user2.userId), timeout)
    val friendToBe1_3: WrappedFriend = Await.result(dbWrapper.addFriend(user1.userId, user3.userId), timeout)
    val friendToBe2_3: WrappedFriend = Await.result(dbWrapper.addFriend(user2.userId, user3.userId), timeout)

    friendToBe1_2.userId should be(user2.userId)
    friendToBe1_3.userId should be(user3.userId)
    friendToBe2_3.userId should be(user3.userId)

    val user1PostRequest = Await.result(dbWrapper.getUser(user1.userId), timeout).get
    val user2PostRequest = Await.result(dbWrapper.getUser(user2.userId), timeout).get
    val user3PostRequest = Await.result(dbWrapper.getUser(user3.userId), timeout).get

    user1PostRequest.friendRequestsSent should not be empty
    user1PostRequest.friendRequestsSent should have size 2
    user1PostRequest.friendRequestsReceived should be(empty)

    user2PostRequest.friendRequestsSent should not be empty
    user2PostRequest.friendRequestsSent should have size 1
    user2PostRequest.friendRequestsReceived should not be empty
    user2PostRequest.friendRequestsReceived should have size 1

    user3PostRequest.friendRequestsReceived should not be empty
    user3PostRequest.friendRequestsReceived should have size 2
    user3PostRequest.friendRequestsSent should be(empty)

    // We now have 1 -> 2, 1 -> 3, 2 -> 3
  }

  "User" should "block a pending request" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get
    val user3 = Await.result(dbWrapper.getUser("TEST_EMAIL_3"), timeout).get

    // Block 2 -> 1 (i.e. user 2 rejects the request from user 1)
    Await.result(dbWrapper.blockFriend(user2.userId, user1.userId), timeout)
    val user1PostBlock = Await.result(dbWrapper.getUser(user1.userId), timeout).get
    val user2PostBlock = Await.result(dbWrapper.getUser(user2.userId), timeout).get

    user1PostBlock.friendRequestsSent should not be empty
    user1PostBlock.friendRequestsSent should have size 1 // Only one outstanding request - to 3
    user1PostBlock.friendRequestsSent.head.userId should be(user3.userId)
    user1PostBlock.friendRequestsReceived should be(empty)

    user2PostBlock.friendRequestsSent should not be empty
    user2PostBlock.friendRequestsSent should have size 1
    user2PostBlock.friendRequestsReceived should be(empty)
  }

  "User" should "block a confirmed friend" taggedAs(DbTest) in {
    val user1 = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    val user2 = Await.result(dbWrapper.getUser("TEST_EMAIL_2"), timeout).get
    val user3 = Await.result(dbWrapper.getUser("TEST_EMAIL_3"), timeout).get

    // Confirm 1 -> 3 and and 2 -> 3, then have 1 block 3, and 3 block 2 (to make sure the block works both ways)
    Await.result(dbWrapper.confirmFriend(user3.userId, user1.userId), timeout)
    Await.result(dbWrapper.confirmFriend(user3.userId, user2.userId), timeout)

    val user1PostConfirm = Await.result(dbWrapper.getUser(user1.userId), timeout).get
    val user2PostConfirm = Await.result(dbWrapper.getUser(user2.userId), timeout).get
    val user3PostConfirm = Await.result(dbWrapper.getUser(user3.userId), timeout).get

    user1PostConfirm.friendRequestsReceived should be (empty)
    user1PostConfirm.friendRequestsSent should be (empty)
    user1PostConfirm.friends should not be empty
    user1PostConfirm.friends.head.userId should be (user3.userId)

    user2PostConfirm.friendRequestsReceived should be (empty)
    user2PostConfirm.friendRequestsSent should be (empty)
    user2PostConfirm.friends should not be empty
    user2PostConfirm.friends.head.userId should be (user3.userId)

    user3PostConfirm.friendRequestsReceived should be (empty)
    user3PostConfirm.friendRequestsSent should be (empty)
    user3PostConfirm.friends should not be empty
    user3PostConfirm.friends.head.userId should be (user1.userId)
    user3PostConfirm.friends.last.userId should be (user2.userId)

    // 1 blocks 3 and 3 blocks 2
    Await.result(dbWrapper.blockFriend(user1.userId, user3.userId), timeout)
    Await.result(dbWrapper.blockFriend(user3.userId, user2.userId), timeout)

    val user1PostBlock = Await.result(dbWrapper.getUser(user1.userId), timeout).get
    val user2PostBlock = Await.result(dbWrapper.getUser(user2.userId), timeout).get
    val user3PostBlock = Await.result(dbWrapper.getUser(user3.userId), timeout).get

    user1PostBlock.friendRequestsReceived should be (empty)
    user1PostBlock.friendRequestsSent should be (empty)
    user1PostBlock.friends should be (empty)

    user2PostBlock.friendRequestsReceived should be (empty)
    user2PostBlock.friendRequestsSent should be (empty)
    user2PostBlock.friends should be (empty)

    user3PostBlock.friendRequestsReceived should be (empty)
    user3PostBlock.friendRequestsSent should be (empty)
    user3PostBlock.friends should be (empty)
  }

  // This cleans up the users created in this test
  "Users" should "be deleted" taggedAs(DbTest) in {
    Seq("TEST_EMAIL_1", "TEST_EMAIL_2", "TEST_EMAIL_3").foreach { email =>
      val deleteFut = dbWrapper.getUser(email).flatMap {
        case None => Future.successful(())
        case Some(user) => dbWrapper.deleteUser(user.userId)
      }
      Await.ready(deleteFut, timeout)
      Await.result(dbWrapper.getUser(email), timeout) should not be defined
    }
  }
}
