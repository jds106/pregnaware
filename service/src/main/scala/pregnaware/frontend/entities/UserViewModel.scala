package pregnaware.frontend.entities

case class LoginRequest(email: String, password: String)

case class AddUserRequest(displayName: String, email: String, password: String)

case class EditUserRequest(displayName: Option[String], email: Option[String], password: Option[String])

case class AddFriendRequest(email: String)

case class AddNameRequest(name: String, isBoy: Boolean)