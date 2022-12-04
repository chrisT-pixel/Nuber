package nuber.students;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	
	private boolean logEvents = false;
	HashMap<String, Integer> regionInfo;
	private LinkedBlockingQueue<Driver> idleDrivers;
	ArrayList<NuberRegion> regions = new ArrayList<NuberRegion>();
	private int bookingsAwaitingDriver = 0;
	
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
	
		this.logEvents = logEvents;
		this.regionInfo = regionInfo;
		idleDrivers = new LinkedBlockingQueue<Driver>(MAX_DRIVERS);
		
		for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) {
		    
			String regionName = entry.getKey();
		    Integer maxSimultaneousJobs = entry.getValue();
		    NuberRegion region = new NuberRegion(this, regionName, maxSimultaneousJobs);
		    
		    regions.add(region);
		    
		}
	}
	
	
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public synchronized boolean addDriver(Driver newDriver) {
		
		if(idleDrivers.offer(newDriver)) {
			
			return true;
			
		}
	
		else {
			System.out.println("driver NOT added");
			return false;
		}
	
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 * @throws InterruptedException 
	 */
	public Driver getDriver() throws InterruptedException{
		
		Driver d = idleDrivers.take();
		
		if(d == null) {
			System.out.println("There are no idle drivers available");
		}
		
		return d;
		
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The thread that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking.toString() + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) throws InterruptedException, ExecutionException {
		

		NuberRegion currentRegion = null;
		
		for (int i = 0; i < regions.size(); i++) {
		      
				currentRegion = regions.get(i);
				
				if(currentRegion.regionName.equals(region)) {
					
					if(currentRegion.regionService.isShutdown()) {
						System.out.println("This region has been shutdown - booking rejected");
						return null;
					}
					
					else {
						
						int currentBookingsAwaiting = this.getBookingsAwaitingDriver();
						this.setBookingsAwaitingDriver(currentBookingsAwaiting + 1);
						
						Future<BookingResult> FBR = currentRegion.bookPassenger(passenger);
						
						return FBR;
						
					}
					
				}
		   }
		
		return null;
	
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver(){
	
		return this.bookingsAwaitingDriver;
		
	}
	
	
	
	public void setBookingsAwaitingDriver(int bookingsAwaitingDriver) {
		this.bookingsAwaitingDriver = bookingsAwaitingDriver;
	}



	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
	
		for (int i = 0; i < regions.size(); i++) {
		      NuberRegion currentRegion = regions.get(i);
		      currentRegion.shutdown();
		    }
	
	}

}
