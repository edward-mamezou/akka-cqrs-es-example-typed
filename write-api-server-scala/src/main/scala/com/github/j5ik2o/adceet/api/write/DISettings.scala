package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ ActorRef, ActorSystem, Scheduler }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.SelfUp
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.github.j5ik2o.adceet.api.write.aggregate._
import com.github.j5ik2o.adceet.api.write.use.`case`.{
  AddMemberUseCase,
  AddMemberInteractor,
  AddMessageUseCase,
  AddMessageInteractor,
  CreateThreadUseCase,
  CreateThreadInteractor
}
import com.typesafe.config.{ Config, ConfigFactory }
import org.slf4j.{ Logger, LoggerFactory }
import wvlet.airframe._
import wvlet.log.io.StopWatch

object DISettings {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def di(args: Args, stopWatch: StopWatch): DesignWithContext[_] = newDesign
    .bind[Config].toInstance(ConfigFactory.load())
    .bind[ActorSystem[MainActor.Command]].toProvider[Session, Config] { (session, config) =>
      ActorSystem(new MainActor(session, stopWatch).create(args), "adceet", config)
    }
    .bind[Scheduler].toProvider[ActorSystem[MainActor.Command]] { system =>
      system.scheduler
    }

  def mainActor(ctx: ActorContext[MainActor.Command], roleNames: Seq[RoleNames.Value]): DesignWithContext[_] = {
    newDesign
      .bind[ClusterBootstrap].toInstance(
        ClusterBootstrap(ctx.system)
      )
      .bind[ActorRef[SelfUp]].toInstance(
        ctx.spawn(SelfUpReceiver.create(ctx.self), "self-up")
      )
      .bind[ClusterSharding].toInstance(ClusterSharding(ctx.system))
      .bind[ActorRef[ThreadAggregateProtocol.CommandRequest]].toEagerSingletonProvider[ClusterSharding] {
        clusterSharding =>
          val behavior = if (roleNames.contains(RoleNames.Backend)) {
            Some(ThreadAggregates.create {
              _.asString
            } { id =>
              ThreadAggregate.create(id) { (id, ref) =>
                ThreadPersist.persistBehavior(
                  id,
                  ref
                )
              }
            })
          } else None

          val _ = ShardedThreadAggregate.initClusterSharding(
            clusterSharding,
            behavior
          )
          logger.info(s"Starting Shard Region: $roleNames")
          ctx.spawn(ShardedThreadAggregate.ofProxy(clusterSharding), "sharded-thread")
      }
      .bind[CreateThreadUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new CreateThreadInteractor(ctx.system, actorRef)
      }
      .bind[AddMemberUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new AddMemberInteractor(ctx.system, actorRef)
      }
      .bind[AddMessageUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new AddMessageInteractor(ctx.system, actorRef)
      }
  }
}
