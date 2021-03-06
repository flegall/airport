package controllers

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport.{GameEvent, Settings, Game}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Enumerator.TreatCont1
import play.api.libs.iteratee.{Input, Enumerator, Iteratee}
import play.api.mvc._
import scala.Some
import scala.concurrent.{ExecutionContext, Future}

object Application extends Controller {

  private val steps = Seq(
    Settings.EASY,
    Settings.MEDIUM,
    Settings.HARD
  )

  def newGame(level: Int) = Action {

    if (game != null) {
      system.stop(game)
      game = null
    }

    val settings = steps(level)

    game = system.actorOf(Props(classOf[Game], settings))

    if (listener != null) {
      system.eventStream.unsubscribe(listener)
      system.stop(listener)
    }

    listener = system.actorOf(Props[EventListener])

    system.eventStream.subscribe(listener, classOf[PlaneStatus])
    system.eventStream.subscribe(listener, classOf[GameEvent])

    Ok(views.html.index(settings)(level, if (level < steps.length - 1) Some(level + 1) else None))
  }

  def index = Action {
    Redirect(routes.Application.newGame(0))
  }

  def events = WebSocket.using[String] {
    request =>

    // Log events to the console
      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String](println).map {
        _ =>
          println("Disconnected")
      }

      val out = Enumerator2.infiniteUnfold(listener) {
        listener =>
          ask(listener, DequeueEvents)(Timeout(1 second))
            .mapTo[Option[String]]
            .map(replyOption => replyOption
            .map(reply => (listener, reply))
          )
      }

      (in, out)
  }

  import play.api.Play.current

  val system = Akka.system

  var listener: ActorRef = null
  var game: ActorRef = null
}

case object DequeueEvents

object Enumerator2 {
  /**
   * Like [[play.api.libs.iteratee.Enumerator.unfold]], but allows the unfolding to be done asynchronously.
   *
   * @param s The value to unfold
   * @param f The unfolding function. This will take the value, and return a future for some tuple of the next value
   *          to unfold and the next input, or none if the value is completely unfolded.
   *          $paramEcSingle
   */
  def infiniteUnfold[S, E](s: S)(f: S => Future[Option[(S, E)]])(implicit ec: ExecutionContext): Enumerator[E] = Enumerator.checkContinue1(s)(new TreatCont1[E, S] {
    val pec = ec.prepare()

    def apply[A](loop: (Iteratee[E, A], S) => Future[Iteratee[E, A]], s: S, k: Input[E] => Iteratee[E, A]): Future[Iteratee[E, A]] = {
      f(s).flatMap {
        case Some((newS, e)) => loop(k(Input.El(e)), newS)
        case None => Thread.sleep(50); loop(k(Input.Empty), s)
      }(ExecutionContext.global)
    }
  })

}