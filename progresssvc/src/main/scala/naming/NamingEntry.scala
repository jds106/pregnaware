package naming

/** A holder for suggested name information */
case class NamingEntry(gender: String, name: String, source: String, userId: Int)

case class NamingEntries(userId: Int, entries: Seq[NamingEntry])