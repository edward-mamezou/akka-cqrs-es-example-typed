package com.github.j5ik2o.adceet.api.write.use_case;/*
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

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.Message;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import wvlet.airframe.ulid.ULID;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AddMessageInteractor implements AddMessageUseCase {
    private final ActorSystem<Void> system;
    private final ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregateRef;
    private final Duration askTimeout;

    public AddMessageInteractor(ActorSystem<Void> system, ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregateRef, Duration askTimeout) {
        this.system = system;
        this.threadAggregateRef = threadAggregateRef;
        this.askTimeout = askTimeout;
    }

    public AddMessageInteractor(ActorSystem<Void> system, ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregateRef ) {
        this(system, threadAggregateRef, Duration.ofSeconds(30));
    }

    @Override
    public CompletionStage<ThreadId> execute(Message message) {
        return AskPattern.<ThreadAggregateProtocol.CommandRequest, ThreadAggregateProtocol.AddMessageReply>ask(
                threadAggregateRef,
                replyTo -> new ThreadAggregateProtocol.AddMessage(ULID.newULID(), message.threadId(), message, replyTo),
                askTimeout,
                system.scheduler()
        ).thenCompose(result -> switch (result) {
            case ThreadAggregateProtocol.AddMessageSucceeded c -> CompletableFuture.completedStage(c.threadId());
            case ThreadAggregateProtocol.AddMessageFailed f ->
                    CompletableFuture.failedFuture(new AddMessageException(f.error().message()));
        });
    }
}
