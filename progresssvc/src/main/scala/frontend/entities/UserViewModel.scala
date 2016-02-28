package frontend.entities

/** Add a user */
case class AddUserRequest(displayName: String, email: String, password: String)

/** The user view model */
case class UserViewModel(userId: Int, displayName: String, email: String, friends: Seq[FriendViewModel])

/** modify an existing user */
case class EditUserRequest(displayName: Option[String], email: Option[String], password: Option[String])

/** The friend view model */
case class FriendViewModel(userId: Int, displayName: String, email: String)

/** Support sharing of the pregnancy data */
case class AddFriendRequest(email: String)
