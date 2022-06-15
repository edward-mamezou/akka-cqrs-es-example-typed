package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, Message, MessageId, ThreadId }
import wvlet.airframe.ulid.ULID

abstract class AbstractThreadAggregateTestBase(testKit: ActorTestKit) {
  val inMemoryMode: Boolean = false

  def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest]

  private def actorRef(id: ThreadId, inMemoryMode: Boolean): ActorRef[ThreadAggregateProtocol.CommandRequest] = {
    testKit.spawn(behavior(id, inMemoryMode))
  }

  def shouldCreateThread(): Unit = {
    val id        = ThreadId()
    val threadRef = actorRef(id, inMemoryMode)
    val accountId = AccountId()

    val createThreadReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.CreateThreadReply]()
    threadRef ! ThreadAggregateProtocol.CreateThread(
      ULID.newULID,
      id,
      accountId,
      createThreadReplyProbe.ref
    )
    val createThreadReply = createThreadReplyProbe.expectMessageType[ThreadAggregateProtocol.CreateThreadSucceeded]
    assert(id == createThreadReply.threadId)
  }

  def shouldAddMember(): Unit = {
    val id         = ThreadId()
    val threadRef  = actorRef(id, inMemoryMode)
    val accountId1 = AccountId()
    val accountId2 = AccountId()

    val createThreadReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.CreateThreadReply]()
    threadRef ! ThreadAggregateProtocol.CreateThread(
      ULID.newULID,
      id,
      accountId1,
      createThreadReplyProbe.ref
    )
    val createThreadReply = createThreadReplyProbe.expectMessageType[ThreadAggregateProtocol.CreateThreadSucceeded]
    assert(id == createThreadReply.threadId)

    val addMemberReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.AddMemberReply]()
    threadRef ! ThreadAggregateProtocol.AddMember(ULID.newULID, id, accountId2, addMemberReplyProbe.ref)
    val addMemberReply =
      addMemberReplyProbe.expectMessageType[ThreadAggregateProtocol.AddMemberSucceeded]
    assert(id == addMemberReply.threadId)
  }

  def shouldAddMessage(): Unit = {
    val id         = ThreadId()
    val threadRef  = actorRef(id, inMemoryMode)
    val accountId1 = AccountId()
    val accountId2 = AccountId()
    val messageId  = MessageId()
    val body       = "ABC"

    val createThreadReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.CreateThreadReply]()
    threadRef ! ThreadAggregateProtocol.CreateThread(
      ULID.newULID,
      id,
      accountId1,
      createThreadReplyProbe.ref
    )
    val createThreadReply =
      createThreadReplyProbe.expectMessageType[ThreadAggregateProtocol.CreateThreadSucceeded]
    assert(id == createThreadReply.threadId)

    val addMemberReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.AddMemberReply]()
    threadRef ! ThreadAggregateProtocol.AddMember(ULID.newULID, id, accountId2, addMemberReplyProbe.ref)
    val addMemberReply =
      addMemberReplyProbe.expectMessageType[ThreadAggregateProtocol.AddMemberSucceeded]
    assert(id == addMemberReply.threadId)

    val addMessageReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.AddMessageReply]()
    threadRef ! ThreadAggregateProtocol.AddMessage(
      ULID.newULID,
      id,
      Message(
        messageId,
        id,
        accountId2,
        body
      ),
      addMessageReplyProbe.ref
    )
    val addMessageReply =
      addMessageReplyProbe.expectMessageType[ThreadAggregateProtocol.AddMessageSucceeded]
    assert(id == addMessageReply.threadId)
  }
}
