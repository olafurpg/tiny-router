package tinyrouter

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.matching.Regex

import java.util.regex.Pattern

trait TinyRouter {
  val int = StringUnapply[Int](x => Try(x.toInt).toOption)
  val long = StringUnapply[Long](x => Try(x.toLong).toOption)
  val float = StringUnapply[Float](x => Try(x.toFloat).toOption)

  implicit class UrlExtractor(sc: StringContext) {
    def url: PathExtractor = PathExtractor.cached(sc.parts)
  }

  def static[T: ClassTag](t: T, url: String): Bijection[T] = {
    Bijection[T](_ => url, {
      case `url` => t
    })
  }
  def dynamic[T: ClassTag](toUrl: T => String)(
      fromUrl: PartialFunction[String, T]): Bijection[T] = {
    Bijection[T](toUrl, fromUrl)
  }
}

object TinyRouter extends TinyRouter

/** Wrapper for two functions, T => String and String => T */
case class Bijection[T: ClassTag](toUrl: T => String,
                                  fromUrl: PartialFunction[String, T]) {
  def key: String = implicitly[ClassTag[T]].runtimeClass.getName
}

case class Router[T](bijections: Bijection[_ <: T]*) {
  private val rulesByClasstag: Map[String, Bijection[_ <: T]] =
    bijections.map(x => x.key -> x).toMap
  def fromUrl(url: String): Option[T] = {
    // For a HUGE number of rules, this is inefficient. We could probably
    // optimize by ordering the rules smarter.
    bijections.collectFirst {
      case Bijection(_, fromUrl) if fromUrl.isDefinedAt(url) => fromUrl(url)
    }
  }
  def toUrl(p: T): Option[String] = {
    rulesByClasstag.get(p.getClass.getName).map { rule =>
      rule.asInstanceOf[Bijection[T]].toUrl(p)
    }
  }
}

/** Anything that can unapply on string values */
abstract class StringUnapply[T] {
  def unapply(arg: String): Option[T]
}
object StringUnapply {
  def apply[T](f: String => Option[T]) = new StringUnapply[T] {
    override def unapply(arg: String): Option[T] = f(arg)
  }
}

/**
  * Allows for extracting bits from urls in pattern matching.
  *
  * For example:
  *
  * {{{
  *   "edit/1" match {
  *     case url"edit/${int(n)}" => EditUser(n)
  *   }
  * }}}
  *
  * @param regex The regex that is used to extract the raw parts.
  * @param partDescriptors Descriptors saying whether each part should be decoded or not.
  */
private[tinyrouter] class PathExtractor(regex: Regex,
                                        partDescriptors: Seq[PathPart.Value]) {
  def unapplySeq(path: String): Option[List[String]] = extract(path)
  private def extract(path: String): Option[List[String]] = {
    regex.unapplySeq(path).map { parts =>
      parts.zip(partDescriptors).map {
        case (part, PathPart.Decoded) => part // TODO(olafur) uri decode.
        case (part, PathPart.Raw) => part
      }
    }
  }
}

private[tinyrouter] object PathExtractor {
  private val cache = mutable.Map.empty[Seq[String], PathExtractor]
  def cached(parts: Seq[String]): PathExtractor = {
    cache.getOrElseUpdate(parts, {
      val (regexParts, descs) = parts.tail.map {
        part =>
          if (part.startsWith("*")) {
            "(.*)" + Pattern.quote(part.drop(1)) -> PathPart.Raw
          } else if (part.startsWith("<") && part.contains(">")) {
            val splitted = part.split(">", 2)
            val regex = splitted(0).drop(1)
            "(" + regex + ")" + Pattern.quote(splitted(1)) -> PathPart.Raw

          } else {
            "([^/]*)" + Pattern.quote(part) -> PathPart.Decoded
          }
      }.unzip
      new PathExtractor(
        regexParts.mkString(Pattern.quote(parts.head), "", "/?").r,
        descs)
    })
  }
}

/**
  * A path part descriptor. Describes whether the path part should be decoded, or left as is.
  */
private object PathPart extends Enumeration {
  val Decoded, Raw = Value
}
