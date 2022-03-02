package choreo.api

import choreo.syntax.*
import caos.common.Example
import choreo.common.Simplify

object Examples:

  def simple(e:Choreo): Choreo = Simplify(e)

  val m: Agent = Agent("m")
  val w1: Agent = Agent("w1")
  val w2: Agent = Agent("w2")
  val w3: Agent = Agent("w3")
  val b: Agent = Agent("b")
  val s: Agent = Agent("s")
  val price: Msg = Msg(List("Price"))
  val descr: Msg = Msg(List("Descr"))
  val acc: Msg = Msg(List("Acc"))
  val rej: Msg = Msg(List("Rej"))
  val ack: Msg = Msg(List("Ack"))
  val work: Msg = Msg(List("Work"))
  val done: Msg = Msg(List("Done"))

  // Example 3 in paper
  val buyerSeller:Choreo =
    ((s->b|descr) || (s->b|price)) > ((b->s|acc) + (b->s|rej))

  // Example 4 in paper
  val masterWorker2:Choreo =
    (m->w1|work) > (m->w2|work) > ((w1->m|done) || (w2->m|done))

  // Example 12 in paper
  val masterWorker3:Choreo =
    (m->w1|work) > (m->w2|work) > (m->w3|work) >
      ((w1->m|done) || (w2->m|done) || (w3->m|done))


  val examples =
    Example(
      "// Buyer-Seller, Basic\n" +
        "s->b:Descr .\ns->b:Price .\n(s->b:Acc+s->b:Rej)",
      "Buyer-Seller, Basic",
      "Some description"
    ):: Example(
      s"""// 1 Master - 2 Workers, Basic\n""" +
        "m->w1:Work . m->w2:Work .\nw1->m:Done . w2->m:Done",
      "1Master-2Workers, Basic",
      "Some description"
    ):: Example(
      s"""// Buyer-Seller, Relaxed\n""" +
        "(s->b:Descr || s->b:Price) .\n(b->s:Acc + b->s:Rej)",
      "Buyer-Seller, Relaxed",
      ""
    ):: Example(
      s"""// 1 Master - 2 Workers, Relaxed\n""" +
        "m->w1:Work . m->w2:Work .\n(w1->m:Done || w2->m:Done)",
      "1Master-2Workers, Relaxed" ,
      ""
    ):: Example(
      s"""// 1 Master - 3 Workers, Relaxed\n""" +
        "m->w1:Work . m->w2:Work . m->w3:Work .\n(w1->m:Done || w2->m:Done || w3->m:Done)",
      "1Master-3Workers, Relaxed" ,
      ""
    ):: Example(
      "// ex1",
      "Ex.1" ,
      ""
    ):: Example(
      "// ex2",
      "Ex.2" ,
      ""
    ):: Example(
      "// ex3",
      "Ex.3" ,
      ""
    ):: Example(
      "// ex4",
      "Ex.4" ,
      ""
    )::Nil

