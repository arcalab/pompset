package choreo.pomsets

import choreo.pomsets._
import choreo.pomsets.Pomset._
import choreo.pomsets.Label._
import caos.sos.SOS
import choreo.syntax.Choreo.Action

/**
 * Created by   on 02/02/2021
 * 
 * Global semantics for pomsets that keeps executed events, 
 * relabelling them to the empty pomset.
 */
object PomKeepSOS extends SOS[Action,Pomset]:
  type PTrans = Set[(Action,Pomset)]

//  given globalPom as LTS[Pomset]:
//    extension (p:Pomset)
  override def next(p:Pomset): PTrans = nextPom(p)
  override def accepting(p:Pomset):Boolean = isTerminating(p)
  
  def nextPom(p:Pomset):PTrans =
    val minAlive = min(p)
    minAlive.flatMap(e=>nextEvent(e,p.reduce))

  def nextPomPP(p:Pomset):String = SOS.nextPP(PomKeepSOS,p)

  def nextEvent(e:Event,p:Pomset):PTrans = p.labels(e) match
    case LPoms(pomsets) =>
      val nextsInChoice:Set[(Pomset,PTrans)] = pomsets.map(pom => (pom,nextPom(pom)))
      val steps = nextsInChoice.flatMap(p1=>
        p1._2.map(t => (t._1,updateWithChoice(e,p,t._2,pomsets-p1._1))))
      var nexts = steps.map(s => (s._1,expand(s._2)))
      if pomsets.exists(pomset=>isTerminating(pomset))
        then nexts ++= nextPom(updateWithChoice(e,p,identity,pomsets)) 
      nexts
    case LAct(act) =>
      val np = Pomset(p.events,p.labels.updated(e,LPoms(Set(Pomset.identity))),p.order,p.loop)
      Set((act,expand(np)))

  def updateWithChoice(e:Event, from:Pomset, sel:Pomset,others:Set[Pomset]):Pomset =
    val terminate = others.flatMap(_.events) + e
    val termLabel = terminate.map(e => (e,LPoms(Set(Pomset.identity)))).toMap
    val newLabels = from.labels ++ termLabel ++ sel.labels
    Pomset(from.events,newLabels,from.order,from.loop)
  
  def isTerminating(p:Pomset):Boolean = 
    p == Pomset.identity ||
      p.uniqueEvents.forall(e=> p.labels(e) match {
            case LPoms(pomsets) => pomsets.exists(p=>isTerminating(p))
            case LAct(act) => false
          })

  def isFinal(p:Pomset):Boolean =
    p == Pomset.identity || 
      p.labels.forall(l=>l._2.isFinal)

  def min(p:Pomset):Set[Event] =
    val r = p.reduce
    val finale = r.uniqueEvents.filter(e=>r.labels(e).isFinal)
    (r.uniqueEvents -- r.uniqueOrders.filterNot(o=> finale contains o.left).map(o=>o.right)) -- finale
  
  def expand(p:Pomset):Pomset =
    val nextNest = findNextNested(p)
    var pom = p
    //var max:Int = 0
    for ((e,np) <- nextNest; pl <- np ; if pl.loop) do {
      //max = if pom.events.nonEmpty then pom.events.max+1 else 0
      pom = expandLoop(pom,pl,e)
    }
    pom

  def findNextNested(p:Pomset):Set[(Event,Set[Pomset])] =
    min(p).collect(e=>p.labels(e) match {case LPoms(ps) => (e,ps)})

  //def expandEvent(pomsets:Set[Pomset],p:Pomset):Set[Pomset] =
  //  pomsets.flatMap(pl=>expandLoop(p,pl))
  
  /**
   * Transform a loop pomset into Set(identity, (p >> p*)))
   * @param global
   * @param p
   * @return expanded pomset
   */
  def expandLoop(global:Pomset, p:Pomset,e:Event):Pomset =
    if !p.loop then p
    else // custom + and >> to avoide renaming 
      //val ep = p.freshEvents(global).encapsulate
      val (pl,rename) = replicateLoop(global,p)
      val ep = pl.encapsulate
      val seq = for a <- p.agents
                    in <- p.eventsOf(a)
                    inOther <- ep.eventsOf(a)
        yield Order(in,inOther)
      val outgoingOrders = remapOutgoingOrders(global,rename)
      val oneAndLoop = Pomset(p.events++ep.events
        , p.labels++ep.labels
        , p.order++ep.order++seq)
      Pomset(global.events++oneAndLoop.events
        , global.labels++oneAndLoop.labels + (e->LPoms(Set(oneAndLoop,Pomset.identity)))
        , global.order++oneAndLoop.order++outgoingOrders
        , global.loop)

  def replicateLoop(global:Pomset,pl:Pomset):(Pomset,Map[Event,Event]) =
    val max = (global.events ++ pl.events).max
    val fresh:Map[Event,Event] = pl.events.zip(LazyList from (max+1)).toMap
    (pl.renameEvents(fresh),fresh)

  def remapOutgoingOrders(global:Pomset,mapping:Map[Event,Event]):Set[Order] =
    var orders = global.order
    for (o <-global.order)
      if mapping.keySet contains o.left then
        orders -= o
        orders += Order(mapping(o.left),mapping.getOrElse(o.right,o.right))
      end if
    end for
    orders

  def terminate(p:Pomset):Pomset =
    Pomset(p.events,p.labels.map(l=>l._1->LPoms(Set(Pomset.identity))),p.order)
  
      

