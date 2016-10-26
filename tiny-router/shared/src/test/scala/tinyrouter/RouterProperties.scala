package tinyrouter
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalacheck.Shapeless._

object RouterProperties extends Properties("Router") {
  property("router is comprehensive") = forAll { page: Page =>
    PageRouter.router.toUrl(page).isDefined
  }
  property("routes are bijective") = forAll { page: Page =>
    val url = PageRouter.router.toUrl(page).get
    val page2 = PageRouter.router.fromUrl(url).get
    page == page2
  }
}
