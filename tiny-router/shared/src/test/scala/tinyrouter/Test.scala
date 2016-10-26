package tinyrouter

object Test {
  import TinyRouter._
  sealed abstract class Page
  case object Dashboard extends Page
  case class Edit(id: Int) extends Page
  case class Update(from: Int, to: Float) extends Page
  val router = Router[Page](
    dynamic[Edit](x => s"edit/${x.id}") {
      case url"edit/${ int(i) }" => Edit(i)
    },
    dynamic[Update](x => s"update/${x.from}/${x.to}") {
      case url"update/${ int(from) }/${ float(to) }" => Update(from, to)
    },
    static(Dashboard, "dashboard")
  )
  def main(args: Array[String]): Unit = {
    val pages = Seq[Page](Dashboard, Edit(2), Update(3, 5.2f))
    pages.foreach { page =>
      val url = router.toUrl(page).get
      val page2 = router.fromUrl(url).get
      assert(page == page2)
    }
    println("All good!")
  }
}
