import java.io.File
import sbt._
import Keys._

object I18n {

  // path handling details are inelegant/repetitive, should be cleaned up - ST 5/30/12

  val resourceGeneratorTask =
    (cacheDirectory, baseDirectory, resourceManaged, streams) map {
      (cacheDir, baseDir, resourceDir, s) => {
        val names: Set[String] =
          IO.listFiles(baseDir / "dist" / "i18n").map(_.getName).filter(_.endsWith(".txt")).map(_.dropRight(4)).toSet
        val cache =
          FileFunction.cached(cacheDir / "native2ascii", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
            in: Set[File] =>
              names.map(name => native2ascii(s.log.info(_), baseDir, resourceDir, name))
          }
        cache(names.map(name => baseDir / "dist" / "i18n" / (name + ".txt"))).toSeq
      }}

  def native2ascii(log: String => Unit, base: File, dir: File, name: String): File = {
    log("native2ascii: " + name)
    val in = base / "dist" / "i18n" / (name + ".txt")
    val result = dir / (name + ".properties")
    IO.createDirectory(dir)
    (new sun.tools.native2ascii.Main).convert(
      Array("-encoding", "UTF-8", in.getPath, result.getPath))
    result
  }

}
