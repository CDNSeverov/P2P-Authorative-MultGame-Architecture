// TaskQueue.java
package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    public static final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
}