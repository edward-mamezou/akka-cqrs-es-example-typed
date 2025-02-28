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
package com.github.j5ik2o.adceet.api.write;

import akka.actor.typed.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import java.util.concurrent.ExecutionException;

public class Main {
  public static void main(String[] args) {
    try {
      var injector = Guice.createInjector(new MainModule());
      var key = Key.get(new TypeLiteral<ActorSystem<MainProtocol.Command>>() {});
      var system = injector.getInstance(key);
      system.getWhenTerminated().toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
