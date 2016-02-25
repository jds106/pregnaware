package frontend.services

import akka.actor.{ActorRef, ActorRefFactory}
import akka.pattern.ask
import com.typesafe.scalalogging.StrictLogging
import frontend._
import frontend.entities._
import org.mindrot.jbcrypt.BCrypt
import spray.client.pipelining._
import spray.http._
import spray.routing._
import user.{ModifiedUser, LinkUsers, UserEntry}
import utils.Json4sSupport._

import scala.concurrent.{Await, ExecutionContext}

/** Provides user-based support for the front-end */
case class UserManagementHttpService(
  sessionManager: SessionManager,
  userServiceName: String,
  context: ActorRefFactory,
  implicit val ex: ExecutionContext,
  implicit val httpRef: ActorRef)
  extends HttpService with FrontEndFuncs with StrictLogging {

  override def actorRefFactory: ActorRefFactory = context

  // The password hashing salt
  private val salt = BCrypt.gensalt()

  /** The routes this service provides */
  val routes: Route = login ~ logout ~ putUser ~ getUser ~ editUser ~ addFriend ~ createFriend

  /** Adds a new user */
  def putUser: Route = put {
    path("user") {
      entity(as[NewUser]) { user =>
        getService(userServiceName) { userUrl =>
          sendRequest(Get(s"$userUrl/findUser/${user.email}")) { response =>
            logger.info(s"Received response to findUser: $response")

            if (response.status.isSuccess) {
              // The user was found - cannot create a new user with this e-mail address
              logger.info(s"User already exists")
              complete(StatusCodes.Conflict -> s"User ${user.email} already exists")

            } else {
              // The user was not found - create it
              val newUserEntry =
                UserEntry(-1, user.displayName, user.email, BCrypt.hashpw(user.password, salt), Seq.empty[Int])

              sendRequest(Put(s"$userUrl/user", newUserEntry)) { response =>
                val persistedUserEntry = response ~> unmarshal[UserEntry]
                val session = sessionManager.getSession(persistedUserEntry.userId)
                complete(StatusCodes.OK -> session.sessionId)
              }
            }
          }
        }
      }
    }
  }

  def editUser: Route = put {
    path("editUser") {
      entity(as[EditUserRequest]) { userEdit =>
        fetchSessionUser { sessionUser =>
          val displayName = userEdit.displayName.getOrElse(sessionUser.displayName).trim
          val email = userEdit.email.getOrElse(sessionUser.email).trim
          val passwordHash = userEdit.password match {
            case None => sessionUser.passwordHash
            case Some(p) => BCrypt.hashpw(p, salt)
          }

          val modifiedUser = ModifiedUser(sessionUser.userId, displayName, email, passwordHash)
          getService(userServiceName) { userUrl =>
            sendRequest(Put(s"$userUrl/editUser", modifiedUser)) { response =>
              val persistedUserEntry = response ~> unmarshal[UserEntry]
              complete(StatusCodes.OK -> persistedUserEntry)
            }
          }
        }
      }
    }
  }

  /** Adds this user to the friend list of another user so they can see this user's progress */
  def addFriend: Route = put {
    path("friend") {
      fetchSessionUser { user =>
        entity(as[UserEntry]) { userFriend =>
          getService(userServiceName) { userUrl =>
            val userLink = LinkUsers(userFriend.userId, user.userId)
            sendRequest(Put(s"$userUrl/friend", userLink)) { response =>
              complete(HttpResponse(status = response.status, entity = response.entity))
            }
          }
        }
      }
    }
  }

  /** Creates a new user with the current user added as a friend */
  def createFriend: Route = put {
    path("createFriend") {
      fetchSessionUser { user =>
        entity(as[AddFriendRequest]) { addFriendRequest =>
          getService(userServiceName) { userUrl =>
            sendRequest(Get(s"$userUrl/findUser/${addFriendRequest.email}")) { response =>

              if (response.status.isSuccess) {
                // The user was found - cannot create a new user with this e-mail address
                complete(StatusCodes.Conflict -> s"User ${addFriendRequest.email} already exists")

              } else {
                // The user was not found - create it
                val tmpPassword = java.util.UUID.randomUUID().toString
                val tmpPasswordHash = BCrypt.hashpw(tmpPassword, salt)

                val newUserEntry = UserEntry(
                  -1, s"Friend of ${user.displayName}", addFriendRequest.email, tmpPasswordHash, Seq(user.userId))

                sendRequest(Put(s"$userUrl/user", newUserEntry)) { response =>
                  val persistedUserEntry = response ~> unmarshal[UserEntry]
                  val session = sessionManager.getSession(persistedUserEntry.userId)
                  complete(StatusCodes.OK -> session.sessionId)
                }
              }
            }
          }
        }
      }
    }
  }

  def login: Route = post {
    path("login") {
      logRequestResponse("Login", akka.event.Logging.ErrorLevel) {
        entity(as[ReturningUser]) { user =>
          getService(userServiceName) { userUrl =>
            sendRequest(Get(s"$userUrl/findUser/${user.email}")) { response =>
              if (response.status.isFailure) {
                complete(StatusCodes.Unauthorized -> s"Unknown user: ${user.email}")

              } else {
                val userEntry = response ~> unmarshal[UserEntry]
                if (!BCrypt.checkpw(user.password, userEntry.passwordHash)) {
                  complete(StatusCodes.Unauthorized -> s"Invalid password for user: ${user.email}")

                } else {
                  val session = sessionManager.getSession(userEntry.userId)
                  complete(StatusCodes.OK -> session.sessionId)
                }
              }
            }
          }
        }
      }
    }
  }

  /** Note this is a "post" action to avoid browser pre-fetching */
  def logout: Route = post {
    path("logout") {
      parameters('sessionId.as[String]) { sessionId =>
        sessionManager.getSession(sessionId) match {
          case None =>
            logger.warn(s"Unknown user attempted logout: s$sessionId")
            complete(StatusCodes.OK)

          case Some(session) =>
            logger.info(s"User logged out: ${session.userId} / ${session.sessionId}")
            sessionManager.removeSession(session)
            complete(StatusCodes.OK)
        }
      }
    }
  }

  /** Fetches the user display model */
  def getUser: Route = get {
    path("user") {
      fetchUser { user =>
        getService(userServiceName) { userUrl =>

          val friendModels = user.friends.map { friendId =>
            val request = Get(s"$userUrl/user/$friendId")
            val response = Await.result(httpRef.ask(request).mapTo[HttpResponse], this.timeout.duration)
            val friendEntry = response ~> unmarshal[UserEntry]
            FriendViewModel(friendEntry.userId, friendEntry.displayName, friendEntry.email)
          }

          complete(StatusCodes.OK -> UserViewModel(user.userId, user.displayName, user.email, friendModels))
        }
      }
    }
  }
}
