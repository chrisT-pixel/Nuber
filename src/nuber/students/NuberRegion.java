package nuber.students;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A single Nuber region that operates independently of other regions, other than getting 
 * drivers from bookings from the central dispatch.
 * 
 * A region has a maxSimultaneousJobs setting that defines the maximum number of bookings 
 * that can be active with a driver at any time. For passengers booked that exceed that 
 * active count, the booking is accepted, but must wait until a position is available, and 
 * a driver is available.
 * 
 * Bookings do NOT have to be completed in FIFO order.
 * 
 * @author james
 *
 */
//public class NuberRegion implements Runnable {
public class NuberRegion {
	
	NuberDispatch dispatch;
	String regionName;
	int maxSimultaneousJobs;
	ExecutorService regionService;
	LinkedBlockingQueue<Booking> idleBookings = new LinkedBlockingQueue<Booking>(); 
	
	
	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {

		this.dispatch = dispatch;
		this.regionName = regionName;
		this.maxSimultaneousJobs = maxSimultaneousJobs;
		this.regionService = Executors.newFixedThreadPool(maxSimultaneousJobs); 
		

	}
	
	/**
	 * Creates a booking for given passenger, and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available, and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shutdown, this function should return null, and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 * @throws InterruptedException 
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) throws InterruptedException {
		
		if(regionService.isShutdown()) {
			System.out.println("This region has been shutdown so the booking is rejected");
			return null;
		}
		
		Booking booking = new Booking(dispatch, waitingPassenger);
		
		dispatch.logEvent(booking, "Starting Booking, getting driver");
		
		idleBookings.put(booking);
		
		Future<BookingResult> futureBooking = regionService.submit(new Callable<BookingResult>(){
			
			public BookingResult call() throws Exception {
				
				Booking b = idleBookings.take();
		
				BookingResult br = b.call();
				
				return br;
			    
			}
		});
		
		return futureBooking;
	
	}
	
	
	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public void shutdown() {
		
		regionService.shutdown();
	
	}

		
}
