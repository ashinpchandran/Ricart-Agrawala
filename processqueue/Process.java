package processqueue;

import java.lang.Comparable;



public class Process implements Comparable<Process>{
	private int pid;
	private int number;
	private int timeStamp;
	
	/**
	 * Construtor
	 */
	public Process(int pid, int number, int timeStamp) {
		this.pid = pid;
		this.number = number;
		this.timeStamp = timeStamp;
	}
	
	/**
	 * getProcessNumber()
	 * Return Process Number
	 */
	public int getProcessNumber() {
		return pid;
	}
	
	/**
	 * getTimeStamp()
	 * Return the time when the message was sent
	 */
	public int getTimeStamp() {
		return timeStamp;
	}

	public int compareTo(Process m2) {

        int d = Integer.compare(this.timeStamp,m2.timeStamp);
        if(d==0)
        return Integer.compare(this.pid,m2.pid);
        return d;
	}

//public int compareTo(Process m2) {
//		if(this.timeStamp < m2.timeStamp)
//			return -1;
//		
//		if(this.timeStamp > m2.timeStamp)
//			return 1;
//		
//		return 0;
//	}
}
