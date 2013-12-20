package fr.xebia.xke.akka.airport


class GameSpec extends GameSpecs with AirTrafficControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given an air traffic control` {
        control =>

          `Given a game`(control) {
            game =>

              `Given a probe watching`(game) {
                probe =>

                  `When target terminates`(control) {

                    `Then it should terminates`(probe, game)
                  }
              }
          }
      }
  }
}

