package processqueue;

public class Clock {
	private int time;
	private int increment;
	
	/**
	 * Construtor
	 * @param init_clock  initial clock time
	 * @param increment increment rate
	 */
	public Clock(int init_clock, int increment) {
		this.time = init_clock;
		this.increment = increment;
	}
	
	/**
	 * add()
	 * Increments 1 to the clock
	 */
	public void add() {
		time += increment;
	}
	
	/**
	 * getTime()
	 * Return the time
	 */
	public int getTime() {
		return time;
	}
	
	/**
	 * adjust()
	 * Ajust clock time max(current_time & given_time)
	 * given_time as parameter
	 * @param given_time used to adjust the time
	 */
	public void adjust(int given_time) {
		if(given_time > time)
			time = given_time;
		time++;
	}
}	
