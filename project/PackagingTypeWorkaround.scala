/*
This is a workaround to fix the inability of sbt to resolve ${packaging.type}

[warn] 	::::::::::::::::::::::::::::::::::::::::::::::
[warn] 	::              FAILED DOWNLOADS            ::
[warn] 	:: ^ see resolution messages for details  ^ ::
[warn] 	::::::::::::::::::::::::::::::::::::::::::::::
[warn] 	:: javax.ws.rs#javax.ws.rs-api;2.1.1!javax.ws.rs-api.${packaging.type}
[warn] 	::::::::::::::::::::::::::::::::::::::::::::::
*/
import sbt._

object PackagingTypeWorkaround extends AutoPlugin {
  override val buildSettings = {
    sys.props += "packaging.type" -> "jar"
    Nil
  }
}