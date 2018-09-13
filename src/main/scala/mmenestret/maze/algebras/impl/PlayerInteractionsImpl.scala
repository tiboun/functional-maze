package mmenestret.maze.algebras.impl
import cats.MonadError
import cats.implicits._
import mmenestret.maze.ADT.{Azerty, Down, KeyboardLayout, Left, Move, Qwerty, Right, Up}
import mmenestret.maze.algebras.{PlayerInteractions, PrintAndRead}

object PlayerInteractionsImpl {

  def apply[F[+ _]: PrintAndRead: MonadError[?[_], Throwable]]: PlayerInteractions[F] =
    new PlayerInteractions[F] {

      val PR: PrintAndRead[F] = PrintAndRead[F]

      override def clearPlayerScreen(): F[Unit] = PR.clearScreen()

      override def displayEndMessage(msg: String): F[Unit] = PR.println(msg)

      override def displayMap(mapAsString: String): F[Unit] = PR.clearAndPrintln(mapAsString)

      override def askPlayerDirection(layout: KeyboardLayout): F[Move] = {
        val keys = KeyboardLayout.keys(layout)
        (for {
          input ← PR.readKeyStrokeAsChar
          move ← input match {
            case k if k == keys.up    ⇒ Up.pure[F]: F[Move]
            case k if k == keys.down  ⇒ Down.pure[F]
            case k if k == keys.left  ⇒ Left.pure[F]
            case k if k == keys.right ⇒ Right.pure[F]
            case _                    ⇒ askPlayerDirection(layout)
          }
        } yield move).recoverWith { case _ ⇒ askPlayerDirection(layout) }
      }

      override def afkForMapSize(): F[Int] =
        PR.println("What's the map side's size you want to play on, noob ?") *> PR.readInt

      override def afkForNumberOfTrap(): F[Int] = PR.println("How many traps ?") *> PR.readInt

      override def askForKeyboardLayout(): F[KeyboardLayout] =
        (PR.println("(A)zerty or (Q)werty ? (A or Q for the idiots who didn't get it...)") *> PR.readChar.map(
          _.toLower))
          .flatMap {
            case 'a' ⇒ Azerty.pure[F]
            case 'q' ⇒ Qwerty.pure[F]
            case _   ⇒ askForKeyboardLayout()
          }
    }

}
