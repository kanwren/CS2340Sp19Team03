package models

class Territory(val id: Int, val name: String, val parent: String) {
  var armies: Int = 0
  var owner: Option[Player] = None
}

object Territory {
  val territoryData: Map[Int, (String, String, List[Int])] = Map(
    0 -> ("alaska", "northamerica", List(1, 5, 31)),
    1 -> ("alberta", "northamerica", List(0, 5, 6, 8)),
    2 -> ("centralamerica", "northamerica", List(12, 8, 3)),
    3 -> ("easternunitedstates", "northamerica", List(8, 6, 7, 2)),
    4 -> ("greenland", "northamerica", List(14, 7, 6, 5)),
    5 -> ("northwestterritory", "northamerica", List(0, 1, 6, 4)),
    6 -> ("ontario", "northamerica", List(7, 4, 5, 1, 8, 3)),
    7 -> ("quebec", "northamerica", List(6, 3, 4)),
    8 -> ("westernunitedstates", "northamerica", List(1, 6, 3, 2)),
    9 -> ("argentina", "southamerica", List(10, 11)),
    10 -> ("brazil", "southamerica", List(24, 9, 11, 12)),
    11 -> ("peru", "southamerica", List(9, 10, 12)),
    12 -> ("venezuela", "southamerica", List(11, 10, 2)),
    13 -> ("greatbritain", "europe", List(14, 19, 15, 16)),
    14 -> ("iceland", "europe", List(4, 13, 16)),
    15 -> ("northerneurope", "europe", List(13, 16, 19, 17, 18)),
    16 -> ("scandinavia", "europe", List(14, 18, 13, 15)),
    17 -> ("southerneurope", "europe", List(32, 18, 15, 19, 22, 24)),
    18 -> ("ukraine", "europe", List(16, 15, 17, 26, 32, 36)),
    19 -> ("westerneurope", "europe", List(13, 15, 17, 24)),
    20 -> ("congo", "africa", List(21, 25, 24)),
    21 -> ("eastafrica", "africa", List(32, 22, 23, 20, 24, 25)),
    22 -> ("egypt", "africa", List(17, 24, 21, 32)),
    23 -> ("madagascar", "africa", List(21, 25)),
    24 -> ("northafrica", "africa", List(10, 20, 19, 17, 22, 21, 20)),
    25 -> ("southafrica", "africa", List(20, 23, 21)),
    26 -> ("afghanistan", "asia", List(18, 36, 27, 28, 32)),
    27 -> ("china", "asia", List(26, 28, 34, 36, 35, 33)),
    28 -> ("india", "asia", List(26, 27, 34, 32)),
    29 -> ("irkutsk", "asia", List(35, 37, 31, 33)),
    30 -> ("japan", "asia", List(31, 33)),
    31 -> ("kamchatka", "asia", List(0, 37, 29, 33, 30)),
    32 -> ("middleeast", "asia", List(18, 17, 22, 21, 26, 28)),
    33 -> ("mongolia", "asia", List(35, 29, 31, 30, 27)),
    34 -> ("siam", "asia", List(39, 27, 28)),
    35 -> ("siberia", "asia", List(36, 26, 27, 33, 29, 37)),
    36 -> ("ural", "asia", List(18, 26, 35)),
    37 -> ("yakutsk", "asia", List(35, 29, 31)),
    38 -> ("easternaustralia", "australia", List(40, 41)),
    39 -> ("indonesia", "australia", List(34, 41, 40)),
    40 -> ("newguinea", "australia", List(39, 38, 41)),
    41 -> ("westernaustralia", "australia", List(39, 38, 40))
  )
}
