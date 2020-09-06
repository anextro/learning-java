import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Implements a simple Communication Manager
 * with two apis: read and write.
 *
 */
public class SimpleCommManager {

	private final Random RAND = new Random();
	private final int BOUND = 50;
	public int read() {
		return RAND.nextInt(BOUND);
	}
	public void write(int value) {
		System.out.println("write "+value);
	}
	
	public static class ReadThread extends Thread {
		volatile boolean run = true;
		SimpleCommManager mgr;
		LinkedBlockingQueue<Integer> sendqueue;
		LinkedBlockingQueue<Integer> recvqueue;
		public ReadThread(SimpleCommManager mgr, LinkedBlockingQueue<Integer> sendqueue,LinkedBlockingQueue<Integer> recvqueue) {
			this.mgr=mgr;
			this.sendqueue=sendqueue;
			this.recvqueue=recvqueue;
		}
		
		public void run() {
			
			while(run) {
				int value = mgr.read();
				if(value >= 10 && value <= 20)
					sendqueue.offer(value);
				else recvqueue.offer(value);
				
			}
		}
	}
	
	public static class WriteThread extends Thread {
		public volatile boolean run = true;
		SimpleCommManager mgr;
		LinkedBlockingQueue<Integer> sendqueue;
		
		public WriteThread(SimpleCommManager mgr, LinkedBlockingQueue<Integer> sendqueue) {
			this.mgr=mgr;
			this.sendqueue=sendqueue;
			
		}
		
		public void run() {
			
			while(run) {
				Integer value=null;
				try {
					value = sendqueue.poll(10, TimeUnit.MILLISECONDS);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(value!=null)
					mgr.write(value);
			}
		}
	}
	
	public static class RunnerThread extends Thread {
		volatile boolean run = true;
		SimpleCommManager mgr;
		LinkedBlockingQueue<Integer> sendqueue;
		LinkedBlockingQueue<Integer> recvqueue;
		public RunnerThread(SimpleCommManager mgr, LinkedBlockingQueue<Integer> sendqueue,LinkedBlockingQueue<Integer> recvqueue) {
			this.mgr=mgr;
			this.sendqueue=sendqueue;
			this.recvqueue=recvqueue;
		}
		
		public void run() {
			
			while(run) {
				Integer value = null;
				try {
					value = recvqueue.poll(10, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(value == null) {
				}else {
					if(value < 10)
						sendqueue.offer(value);
				}
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		SimpleCommManager mgr = new SimpleCommManager();
		
		
		LinkedBlockingQueue<Integer> sendqueue = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<Integer> recvqueue = new LinkedBlockingQueue<>();
		
		ReadThread rt = new ReadThread(mgr,sendqueue,recvqueue);
		WriteThread wt = new WriteThread(mgr,sendqueue);
		Thread WR = new Thread( rt );
		Thread WS = new Thread( wt );
		
		RunnerThread r = new RunnerThread(mgr,sendqueue,recvqueue);
		Thread P = new Thread(r);
		
		Thread GC = new Thread() {
			volatile boolean run = true;
			public void run() {
				while(run) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("GC awake");
					run=false;
				}
				rt.run=false;
				r.run=false;
				wt.run=false;
				
				sendqueue.clear();
				recvqueue.clear();
			}
		};
		
		WR.start();
		P.start();
		WS.start();
		GC.start();
		GC.join();
		
		System.out.println("size of recvqueue "+recvqueue.size()+": "+sendqueue.size());
	}
}
