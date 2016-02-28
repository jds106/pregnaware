package naming.entities

case class WrappedBabyName(
  id: Int, userId: Int, suggestedBy: Int, suggestedByName: String, name: String, isBoy: Boolean)
