package pregnaware.database

import java.time.LocalDate

import akka.util.Timeout
import pregnaware.UnitSpec
import pregnaware.database.wrappers.UserWrapper

import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext}

class DueDateTest extends UnitSpec {
  self =>

  private implicit val timeout = 10.seconds
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val dbWrapper = new DatabaseWrapper() {
    override implicit def executor: ExecutionContext = self.executor
    override implicit def timeout: Timeout = self.timeout
  }

  "User" should "add due date" in {
    val user1 = Await.result(dbWrapper.addUser("TEST_1", "TEST_EMAIL_1", "TEST_PASSWORD_1"), timeout)
    user1.dueDate should not be defined

    val dueDate = LocalDate.of(2016, 7, 21)
    val dueDateResponse = Await.result(dbWrapper.setDueDate(user1.userId, dueDate), timeout)

    dueDateResponse should be (dueDate)

    val user1PostDueDate = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    user1PostDueDate.dueDate should be (Some(dueDate))

    Await.ready(dbWrapper.deleteDueDate(user1.userId), timeout)
    val user1PostDueDateDeleted = Await.result(dbWrapper.getUser("TEST_EMAIL_1"), timeout).get
    user1PostDueDateDeleted.dueDate should not be defined
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
