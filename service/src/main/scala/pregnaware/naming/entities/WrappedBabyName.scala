package pregnaware.naming.entities

case class WrappedBabyName(
  nameId: Int, userId: Int, suggestedBy: Int, suggestedByName: String, name: String, isBoy: Boolean)
