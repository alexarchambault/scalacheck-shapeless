package org.scalacheck.util

import scala.language.experimental.macros
import scala.reflect.macros.Context

import shapeless._

/**
 * Needs update.
 *
 * Looking for an implicit `Lazy[Priority[H, L]]` will look up for an implicit `H`,
 * or, if it fails, for an implicit `L`, and wrap the result in a `Lazy[Priority[H, L]]`.
 *
 * It allows for definitions like
 *     implicit def mkTC[T]
 *      (implicit
 *        impl: Lazy[Implicit[
 *          TC[T],
 *          Fallback[T]
 *        ]]
 *      ): TC[T] = impl.value.fold(identity)(_.toTC)
 * which is looking for an implicit `TC[T]`, but at the same time providing one. Without `Priority`,
 * this would typically lead to stack overflows at runtime, as `mkTC` would find itself as a `TC[T]`,
 * and call itself without terminating. Thanks to `Priority`, the lookup for a `TC[T]` by `mkTC` will
 * ignore the implicit `TC[T]` it itself provides, hence only looking for `TC[T]` elsewhere. Note that
 * this does not prevent `mkTC` to provide `TC` instances the same way for other types during the search
 * for a `TC[T]` or `Fallback[T]`.
 */
case class LowPriority[+H, +L](value: L) extends AnyVal

object LowPriority extends LazyExtensionCompanion {
  def apply[H, L](implicit lkpPriority: Strict[LowPriority[H, L]]): LowPriority[H, L] =
    lkpPriority.value


  implicit def init[H, L]: LowPriority[H, L] = macro initImpl

  def instantiate(ctx0: DerivationContext): LazyExtension { type Ctx = ctx0.type } =
    new LowPriorityLookupExtension {
      type Ctx = ctx0.type
      val ctx: ctx0.type = ctx0
    }
}

trait LowPriorityTypes {
  type C <: Context
  val c: C

  import c.universe._


  def lowPriorityTpe: Type = typeOf[LowPriority[_, _]].typeConstructor

  object LowPriorityTpe {
    def unapply(tpe: Type): Option[(Type, Type)] =
      tpe.normalize match {
        case TypeRef(_, cpdTpe, List(highTpe, lowTpe))
          if cpdTpe.asType.toType.typeConstructor =:= lowPriorityTpe =>
          Some(highTpe, lowTpe)
        case _ =>
          None
      }
  }

  def maskTpe: Type = typeOf[Mask[_, _]].typeConstructor

  object MaskTpe {
    def unapply(tpe: Type): Option[(Type, Type)] =
      tpe.normalize match {
        case TypeRef(_, cpdTpe, List(mTpe, tTpe))
          if cpdTpe.asType.toType.typeConstructor =:= maskTpe =>
          Some(mTpe, tTpe)
        case _ =>
          None
      }
  }

}

trait LowPriorityLookupExtension extends LazyExtension with LowPriorityTypes {
  type C = ctx.c.type
  lazy val c: C = ctx.c

  import ctx._
  import c.universe._

  case class ThisState(
    priorityLookups: List[TypeWrapper]
  ) {
    def addPriorityLookup(tpe: Type): ThisState =
      copy(priorityLookups = TypeWrapper(tpe) :: priorityLookups)
    def removePriorityLookup(tpe: Type): ThisState =
      copy(priorityLookups = priorityLookups.filter(_ != TypeWrapper(tpe)))
  }

  def id = "low-priority"

  def initialState = ThisState(Nil)

  def derivePriority(
    state: State,
    extState: ThisState,
    update: (State, ThisState) => State )(
    priorityTpe: Type,
    highInstTpe: Type,
    lowInstTpe: Type,
    mask: String
  ): Option[(State, Instance)] = {
    val higherPriorityAvailable = {
      val extState1 = extState
        .addPriorityLookup(priorityTpe)
      val state1 = update(state, extState1)

      ctx.derive(state1)(highInstTpe)
        .right.toOption
        .flatMap{case (state2, inst) =>
          if (inst.inst.isEmpty)
            resolve0(state2)(highInstTpe)
              .map{case (_, tree, _) => tree }
          else
            Some(inst.inst.get)
        }
        .exists { actualTree =>
          mask.isEmpty || {
            actualTree match {
              case TypeApply(method, other) =>
                !method.toString().endsWith(mask)
              case _ =>
                true
            }
          }
        }
    }

    def low =
      ctx.derive(state)(lowInstTpe)
        .right.toOption
        .map{case (state1, inst) =>
          val highInstTpe0 =
            if (mask.isEmpty)
              highInstTpe
            else
              appliedType(maskTpe, List(ConstantType(Constant(mask)), highInstTpe))
          (state1, q"_root_.org.scalacheck.util.LowPriority[$highInstTpe0, ${inst.actualTpe}](${inst.ident})", appliedType(lowPriorityTpe, List(highInstTpe0, inst.actualTpe)))
        }

    if (higherPriorityAvailable)
      c.abort(c.enclosingPosition, s"Higher priority $highInstTpe available")
    else
      low.map {case (state1, extInst, actualTpe) =>
        state1.closeInst(priorityTpe, extInst, actualTpe)
      }
  }

  def derive(
    state0: State,
    extState: ThisState,
    update: (State, ThisState) => State )(
    instTpe0: Type
  ): Option[Either[String, (State, Instance)]] =
    instTpe0 match {
      case LowPriorityTpe(highTpe, lowTpe) =>
        Some {
          if (extState.priorityLookups.contains(TypeWrapper(instTpe0)))
            Left(s"Not deriving $instTpe0")
          else
            state0.lookup(instTpe0).left.flatMap { state =>
              val eitherHighTpeMask =
                highTpe match {
                  case MaskTpe(mTpe, tTpe) =>
                    mTpe match {
                      case ConstantType(Constant(mask: String)) if mask.nonEmpty =>
                        Right((tTpe, mask))
                      case _ =>
                        Left(s"Unsupported mask type: $mTpe")
                    }
                  case _ =>
                    Right((highTpe, ""))
                }

              eitherHighTpeMask.right.flatMap{case (highTpe, mask) =>
                derivePriority(state, extState, update)(instTpe0, highTpe, lowTpe, mask)
                  .toRight(s"Unable to derive $instTpe0")
              }
            }
        }

      case _ => None
    }
}

