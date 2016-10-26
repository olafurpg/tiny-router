package tinyrouter

sealed trait Page
case object Dashboard extends Page
case class Edit(id: Int) extends Page
case class Update(from: Int, to: Double) extends Page
case class View(user: User) extends Page
case class User(name: String)

object PageRouter {
  import TinyRouter._
  val router = Router[Page](
    dynamic[View](x => s"view/${x.user.name}") {
      case url"view/$name" => View(User(name))
    },
    dynamic[Edit](x => s"edit/${x.id}") {
      case url"edit/${ int(i) }" => Edit(i)
    },
    dynamic[Update](x => s"update/${x.from}/${x.to}") {
      case url"update/${ int(from) }/${ double(to) }" => Update(from, to)
    },
    static(Dashboard, "dashboard")
  )
}
