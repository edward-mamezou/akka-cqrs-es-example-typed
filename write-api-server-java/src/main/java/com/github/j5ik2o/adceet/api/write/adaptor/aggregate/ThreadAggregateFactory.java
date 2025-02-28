/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.adceet.api.write.adaptor.aggregate;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;

import java.util.function.BiFunction;

public final class ThreadAggregateFactory {

  public static Behavior<ThreadAggregateProtocol.CommandRequest> create(
      ThreadId id,
      BiFunction<
              ThreadId,
              ActorRef<ThreadAggregateProtocol.CommandRequest>,
              Behavior<ThreadPersist.Persist>>
          persistBehaviorF) {
    return Behaviors.setup(
        ctx ->
            Behaviors.withStash(
                255, stashBuffer -> new ThreadAggregate(ctx, stashBuffer, id, persistBehaviorF)));
  }

  public Behavior<ThreadAggregateProtocol.CommandRequest> create(ThreadId id) {
    return Behaviors.setup(
        ctx ->
            Behaviors.withStash(
                255,
                stashBuffer ->
                    new ThreadAggregate(
                        ctx,
                        stashBuffer,
                        id,
                        (_id, self) -> ThreadPersistFactory.persistBehavior(id, self))));
  }
}
