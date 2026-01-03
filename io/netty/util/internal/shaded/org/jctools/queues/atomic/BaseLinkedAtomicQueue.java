/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$ExitCondition
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$WaitStrategy
 *  io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseLinkedAtomicQueuePad2;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.LinkedQueueAtomicNode;
import java.util.Iterator;

abstract class BaseLinkedAtomicQueue<E>
extends BaseLinkedAtomicQueuePad2<E> {
    BaseLinkedAtomicQueue() {
    }

    @Override
    public final Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    protected final LinkedQueueAtomicNode<E> newNode() {
        return new LinkedQueueAtomicNode();
    }

    protected final LinkedQueueAtomicNode<E> newNode(E e) {
        return new LinkedQueueAtomicNode(e);
    }

    @Override
    public final int size() {
        int size;
        LinkedQueueAtomicNode chaserNode = this.lvConsumerNode();
        LinkedQueueAtomicNode producerNode = this.lvProducerNode();
        for (size = 0; chaserNode != producerNode && chaserNode != null && size < Integer.MAX_VALUE; ++size) {
            LinkedQueueAtomicNode next = chaserNode.lvNext();
            if (next == chaserNode) {
                return size;
            }
            chaserNode = next;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        LinkedQueueAtomicNode producerNode;
        LinkedQueueAtomicNode consumerNode = this.lvConsumerNode();
        return consumerNode == (producerNode = this.lvProducerNode());
    }

    protected E getSingleConsumerNodeValue(LinkedQueueAtomicNode<E> currConsumerNode, LinkedQueueAtomicNode<E> nextNode) {
        Object nextValue = nextNode.getAndNullValue();
        currConsumerNode.soNext(currConsumerNode);
        this.spConsumerNode(nextNode);
        return (E)nextValue;
    }

    @Override
    public E poll() {
        LinkedQueueAtomicNode currConsumerNode = this.lpConsumerNode();
        LinkedQueueAtomicNode nextNode = currConsumerNode.lvNext();
        if (nextNode != null) {
            return this.getSingleConsumerNodeValue(currConsumerNode, nextNode);
        }
        if (currConsumerNode != this.lvProducerNode()) {
            nextNode = this.spinWaitForNextNode(currConsumerNode);
            return this.getSingleConsumerNodeValue(currConsumerNode, nextNode);
        }
        return null;
    }

    @Override
    public E peek() {
        LinkedQueueAtomicNode currConsumerNode = this.lpConsumerNode();
        LinkedQueueAtomicNode nextNode = currConsumerNode.lvNext();
        if (nextNode != null) {
            return (E)nextNode.lpValue();
        }
        if (currConsumerNode != this.lvProducerNode()) {
            nextNode = this.spinWaitForNextNode(currConsumerNode);
            return (E)nextNode.lpValue();
        }
        return null;
    }

    LinkedQueueAtomicNode<E> spinWaitForNextNode(LinkedQueueAtomicNode<E> currNode) {
        LinkedQueueAtomicNode nextNode;
        while ((nextNode = currNode.lvNext()) == null) {
        }
        return nextNode;
    }

    @Override
    public E relaxedPoll() {
        LinkedQueueAtomicNode currConsumerNode = this.lpConsumerNode();
        LinkedQueueAtomicNode nextNode = currConsumerNode.lvNext();
        if (nextNode != null) {
            return this.getSingleConsumerNodeValue(currConsumerNode, nextNode);
        }
        return null;
    }

    @Override
    public E relaxedPeek() {
        LinkedQueueAtomicNode nextNode = this.lpConsumerNode().lvNext();
        if (nextNode != null) {
            return (E)nextNode.lpValue();
        }
        return null;
    }

    @Override
    public boolean relaxedOffer(E e) {
        return this.offer(e);
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative: " + limit);
        }
        if (limit == 0) {
            return 0;
        }
        LinkedQueueAtomicNode chaserNode = this.lpConsumerNode();
        for (int i = 0; i < limit; ++i) {
            LinkedQueueAtomicNode nextNode = chaserNode.lvNext();
            if (nextNode == null) {
                return i;
            }
            Object nextValue = this.getSingleConsumerNodeValue(chaserNode, nextNode);
            chaserNode = nextNode;
            c.accept(nextValue);
        }
        return limit;
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c) {
        return MessagePassingQueueUtil.drain(this, c);
    }

    @Override
    public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, wait, exit);
    }

    @Override
    public int capacity() {
        return -1;
    }
}

