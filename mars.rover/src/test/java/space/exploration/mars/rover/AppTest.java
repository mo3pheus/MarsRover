package space.exploration.mars.rover;

import java.util.Queue;
import java.util.PriorityQueue;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    public static class QueueHolder{
        Queue<String> q = new PriorityQueue<String>();

        public Queue<String> getQ() {
            return q;
        }

        public void setQ(Queue<String> q) {
            this.q = q;
        }
    }

    public static void main(String[] args){
        System.out.println("Priority Queue test");

        QueueHolder queueHolder = new QueueHolder();
        queueHolder.getQ().add("Sanket");

        System.out.println(" Queue contents = " + queueHolder.getQ().poll());
        System.out.println(" Queue contents = " + queueHolder.getQ().poll());
    }
}
