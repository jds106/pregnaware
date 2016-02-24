package frontend.services

import akka.actor.{ActorRef, ActorRefFactory}
import akka.pattern.ask
import com.typesafe.scalalogging.StrictLogging
import frontend._
import frontend.entities.{AddFriendRequest, AddFriendResponse, FriendViewModel, UserViewModel}
import org.mindrot.jbcrypt.BCrypt
import spray.client.pipelining._
import spray.http._
import spray.routing._
import user.{LinkUsers, UserEntry}
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
  val routes: Route = login ~ logout ~ putUser ~ getUser ~ addFriend

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

  /** Adds this user to the friend list of another user so they can see this user's progress */
  def addFriend: Route = put {
    path("friend") {
      fetchUser { user =>
        entity(as[AddFriendRequest]) { friendRequest =>
          getService(userServiceName) { userUrl =>
            sendRequest(Get(s"$userUrl/findUser/${friendRequest.email}")) { response =>

              if (response.status.isSuccess) {
                val userFriend = response ~> unmarshal[UserEntry]
                val userLink = LinkUsers(userFriend.userId, user.userId)
                sendRequest(Put(s"$userUrl/friend", userLink)) { response =>
                  complete(StatusCodes.OK -> AddFriendResponse(userFriend.email))
                }
              } else {
                val tmpUUID = java.util.UUID.randomUUID().toString
                val tmpPassword = java.util.Base64.getUrlEncoder.encodeToString(tmpUUID.getBytes)

                val userFriend =
                  UserEntry(-1, "", friendRequest.email, BCrypt.hashpw(tmpPassword, salt), Seq.empty[Int])

                // Add the new user
                sendRequest(Put(s"$userUrl/user", userFriend)) { response =>
                  val persistedUserFriend = response ~> unmarshal[UserEntry]

                  // Add this user to the new user's list of friends
                  val userLink = LinkUsers(persistedUserFriend.userId, user.userId)
                  sendRequest(Put(s"$userUrl/friend", userLink)) { response =>
                    val session = sessionManager.getSession(persistedUserFriend.userId)
                    complete(StatusCodes.OK -> AddFriendResponse(userFriend.email, Some(session.sessionId)))
                  }
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

  /** Fetches the user entry for the specified session */
  def fetchUser(userHandler: UserEntry => Route): Route = {
    parameter('sessionId.as[String]) { sessionId =>
      parameter('userId.as[Option[Int]]) { requestedUserIdOpt =>
        sessionManager.getSession(sessionId) match {
          case None =>
            complete(StatusCodes.Unauthorized -> "User not logged in")

          case Some(session) =>
            getService(userServiceName) { userUrl =>
              sendRequest(Get(s"$userUrl/user/${session.userId}")) { response =>
                val user = response ~> unmarshal[UserEntry]
                val requestedUserId = requestedUserIdOpt.getOrElse(user.userId)

                if (user.userId == requestedUserId) {
                  userHandler(user)

                } else if (user.friends.contains(requestedUserId)) {
                  sendRequest(Get(s"$userUrl/user/$requestedUserId")) { friendResponse =>
                    userHandler(friendResponse ~> unmarshal[UserEntry])
                  }
                } else {
                  complete(StatusCodes.Forbidden -> s"User ${user.userId} not allowed to access user $requestedUserId")
                }
              }
            }
        }
      }
    }
  }
}
