package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, ActorSystem}
import org.scalatest.FreeSpec

class RunwaySpec extends RunwaySpecs with ActorSpecs with PlaneSpecs with AirTrafficControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        airControl =>

          `Given a runway`(airControl.ref) {
            runway =>

              `Given a probe watching`(runway) {
                probe =>

                  `When a plane lands at`(runway) {

                    `Then nothing should happen`(probe, runway)
                  }
              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        airControl =>

          `Given a runway`(airControl.ref) {
            runway =>

              `Given a probe watching`(runway) {
                probe =>

                  `Given a plane has already landed`(runway) {

                    `When a plane lands at`(runway) {

                      `Then it should terminates`(probe, runway)

                    }
                  }
              }
          }
      }
  }

  /*`Given an actor system` {
    implicit system =>

      `Given a probe` {
        airControl =>

          `Given a runway`(airControl.ref) {
            runway =>

              `Given a probe watching`(runway) {
                probe =>

                  `Given a probe watching`(runway) {
                    plane =>

                      `When a plane lands at`(runway) {

                        `Then air traffic control is notified of the landing`(airControl,)
                      }
                  }
              }
          }
      }
  }  */

}

trait RunwaySpecs extends FreeSpec {


  def `Given a runway`(airControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a runway" - {
      fun {
        system.actorOf(Props(classOf[Runway], airControl), "runway")
      }
    }
  }
}