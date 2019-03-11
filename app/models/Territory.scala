package models

class Territory(val id: Int, val name: String, val parent: String) {
  var armies: Int = 0
  var owner: Option[Player] = None
}

object Territory {
  def adjacencies(id: Int): List[Int] = territoryData(id)._3
  val territoryData: Map[Int, (String, String, List[Int])] = List(
    ("alaska", "northamerica", List(1, 5, 31)),
    ("alberta", "northamerica", List(0, 5, 6, 8)),
    ("centralamerica", "northamerica", List(12, 8, 3)),
    ("easternunitedstates", "northamerica", List(8, 6, 7, 2)),
    ("greenland", "northamerica", List(14, 7, 6, 5)),
    ("northwestterritory", "northamerica", List(0, 1, 6, 4)),
    ("ontario", "northamerica", List(7, 4, 5, 1, 8, 3)),
    ("quebec", "northamerica", List(6, 3, 4)),
    ("westernunitedstates", "northamerica", List(1, 6, 3, 2)),
    ("argentina", "southamerica", List(10, 11)),
    ("brazil", "southamerica", List(24, 9, 11, 12)),
    ("peru", "southamerica", List(9, 10, 12)),
    ("venezuela", "southamerica", List(11, 10, 2)),
    ("greatbritain", "europe", List(14, 19, 15, 16)),
    ("iceland", "europe", List(4, 13, 16)),
    ("northerneurope", "europe", List(13, 16, 19, 17, 18)),
    ("scandinavia", "europe", List(14, 18, 13, 15)),
    ("southerneurope", "europe", List(32, 18, 15, 19, 22, 24)),
    ("ukraine", "europe", List(16, 15, 17, 26, 32, 36)),
    ("westerneurope", "europe", List(13, 15, 17, 24)),
    ("congo", "africa", List(21, 25, 24)),
    ("eastafrica", "africa", List(32, 22, 23, 20, 24, 25)),
    ("egypt", "africa", List(17, 24, 21, 32)),
    ("madagascar", "africa", List(21, 25)),
    ("northafrica", "africa", List(10, 20, 19, 17, 22, 21, 20)),
    ("southafrica", "africa", List(20, 23, 21)),
    ("afghanistan", "asia", List(18, 36, 27, 28, 32)),
    ("china", "asia", List(26, 28, 34, 36, 35, 33)),
    ("india", "asia", List(26, 27, 34, 32)),
    ("irkutsk", "asia", List(35, 37, 31, 33)),
    ("japan", "asia", List(31, 33)),
    ("kamchatka", "asia", List(0, 37, 29, 33, 30)),
    ("middleeast", "asia", List(18, 17, 22, 21, 26, 28)),
    ("mongolia", "asia", List(35, 29, 31, 30, 27)),
    ("siam", "asia", List(39, 27, 28)),
    ("siberia", "asia", List(36, 26, 27, 33, 29, 37)),
    ("ural", "asia", List(18, 26, 35)),
    ("yakutsk", "asia", List(35, 29, 31)),
    ("easternaustralia", "australia", List(40, 41)),
    ("indonesia", "australia", List(34, 41, 40)),
    ("newguinea", "australia", List(39, 38, 41)),
    ("westernaustralia", "australia", List(39, 38, 40))
  ).zipWithIndex.map(_.swap)(collection.breakOut)
}
