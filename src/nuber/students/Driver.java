package nuber.students;

public class Driver extends Person {

	private Passenger currentPassenger;
	
	public Driver(String driverName, int maxSleep) {
	
		super(driverName, maxSleep);
		this.currentPassenger = null;
		
	}
	
	
	public Passenger getCurrentPassenger() {
		return currentPassenger;
	}


	public void setCurrentPassenger(Passenger currentPassenger) {
		this.currentPassenger = currentPassenger;
	}
	
	private void delay() {
		long actualDelay;
		
		try {
		// thread to sleep for random milliseconds
		actualDelay = randomWithRange(0,this.maxSleep);
		Thread.sleep(actualDelay);
		} catch (Exception e) {
		
			System.out.println(e);
			
			}
		}

		private long randomWithRange(long min, long max){
			
			long range = (max - min) + 1;
			return (long)(Math.random() * range) + min;
		}

	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
		
		this.setCurrentPassenger(newPassenger);
		this.delay();
		
		
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() {
		
		long travelTime = this.getCurrentPassenger().getTravelTime();
		
		try {
		Thread.currentThread();
		
		Thread.sleep(travelTime);
		} catch(Exception e) {
			System.out.print(e);
		}
		
	
	}


	@Override
	public String toString() {
		return "Driver [name=" + name + "]";
	}
	
	
	
}
