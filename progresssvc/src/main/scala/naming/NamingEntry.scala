package naming

/** A holder for suggested name information */
case class NamingEntry(gender: String, name: String)

case class NamingEntries(entries: Seq[NamingEntry])