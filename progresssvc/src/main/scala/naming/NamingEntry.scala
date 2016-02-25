package naming

/** A holder for suggested name information */
case class NamingEntry(nameId: Int, gender: String, name: String, suggestedByUserId: Int)

case class NamingEntries(entries: Seq[NamingEntry])