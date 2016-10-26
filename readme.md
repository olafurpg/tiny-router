tiny-router
===========
A small library (100 LOC, zero dependencies) to map an ADT to urls and urls back to ADTs.
Works on Scala, Scala.js, 2.10 and 2.11.

```scala
libraryDependencies += "com.geirsson" %%% "tiny-router" % "latest.integration"
```

## Example

```scala
sealed abstract class Page
case object Dashboard extends Page
case class Edit(id: Int) extends Page
case class Update(from: Int, to: Float) extends Page

val router = {
  import tinyrouter.TinyRouter._
  val router = tinyrouter.Router[Page](
    dynamic[Edit](x => s"edit/${x.id}") {
      case url"edit/${int(i)}" => Edit(i)
    },
    dynamic[Update](x => s"update/${x.from}/${x.to}") {
      case url"update/${int(from)}/${float(to)}" => Update(from, to)
    },
    static(Dashboard, "dashboard")
  )
  // NOTE. You should only define one route per class of the ADT. The following will not work.
  val brokenRouter = tinyrouter.Router[Page](
    dynamic[Edit](x => s"edit/${x.id}") {
      case url"edit/${int(i)}" => Edit(i)
    },
    dynamic[Edit](x => s"banana/${x.id}") {
      case url"banana/${int(i)}" => Edit(i)
    }
  )
}
val url  = router.toUrl(Edit(2))    // Some("edit/2")
val edit = router.fromUrl("edit/2") // Some(Edit(2))
```

Sure, it requires a bit of boilerplate to provide the implementation for each direction.

## Alternatives

* [scalajs-router](https://github.com/japgolly/scalajs-react/blob/master/doc/ROUTER.md): zero boilerplate, really cool.

## Credits
The `url` extractor implementation is mostly borrowed and adapted from the awesome
Playframework [String Interpolating Routing DSL](https://www.playframework.com/documentation/2.5.x/ScalaSirdRouter).

### Testing

Use [scalacheck](https://scalacheck.org/) to test that your router is
well-behaved.  Optionally, use
[scalacheck-shapeless](https://github.com/alexarchambault/scalacheck-shapeless)
to automatically generate arbitrary instances of your page ADT. Example,

```scala
// in build.sbt: libraryDependencies += "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % "VERSION" % "test"
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalacheck.Shapeless._

object RouterProperties extends Properties("Router") {
  property("router is comprehensive") = forAll { page: Page =>
    router.toUrl(page).isDefined
  }
  property("routes are bijective") = forAll { page: Page =>
    val url = router.toUrl(page).get
    val page2 = router.fromUrl(url).get
    page == page2
  }
}
```
