package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Actor}
import fr.xebia.xke.akka.airport.plane.{WaitingForGate, Taxiing, Parking, WaitingForTaxiway, Flying}
import languageFeature.postfixOps

case class Plane(airControl: ActorRef, game: ActorRef, settings: Settings) extends Actor with Flying with WaitingForTaxiway with Taxiing with WaitingForGate with Parking {

  def receive: Receive = flying

}