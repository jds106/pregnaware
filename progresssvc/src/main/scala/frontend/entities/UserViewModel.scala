package frontend.entities

/** The user view model */
case class UserViewModel(userId: Int, displayName: String, email: String, friends: Seq[FriendViewModel])

/** The friend view model */
case class FriendViewModel(userId: Int, displayName: String, email: String)

/** Support sharing of the pregnancy data */
case class AddFriendRequest(email: String)

case class AddFriendResponse(email: String, sessionId: Option[String] = None)