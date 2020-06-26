package processqueue;

import java.util.Comparator;

import processqueue.Process;

public class ProcessComparator implements Comparator<Process> {
	

	public int compare(Process m1, Process m2) {
        
        
		return m1.compareTo(m2);
	}
}
