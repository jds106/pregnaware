package utils

import java.lang.management.ManagementFactory

/** Wraps access to the current process idenitifier */
object PID {
  lazy val pid = ManagementFactory.getRuntimeMXBean.getName.split('@')(0)
}
