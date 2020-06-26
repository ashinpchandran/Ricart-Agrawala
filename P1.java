import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.ArrayList;
import processqueue.*;
import processqueue.Process;

public class P1 {

    private static Clock clock;
	private static int pid;
	private static int port;
	private static ArrayList<TreeSet<Process>> pQueue;
	private static int resources[];
	private static int oks[];

    public static void main(String[] args) throws IOException {

        pid = 1;
		port = 60011;
		clock = new Clock(0, 1);
		resources = new int[5];
		oks = new int[5];

		pQueue = new ArrayList<TreeSet<Process>>();

		for(int i = 0; i < 5; i++) resources[i] = -2;
		for(int i = 0; i < 5; i++) oks[i] = 0;
		for(int i = 0; i < 5; i++){
			TreeSet<Process> ts = new TreeSet<Process>(new ProcessComparator());
			pQueue.add(ts);
		}
		
        server();
        try{ 
            Thread.sleep(10000); 
        }catch(Exception e){}
		
		// message sending  increment the clock
		clock.add();

        new Thread(() -> {
            
            try {
                
                while(true) {
                        synchronized(Thread.currentThread()) {
                            clock.add();

                            final int curTime = clock.getTime();
                            final int res = ((int)(Math.random()*10))%5;
                            resources[res] = curTime; // wants to access resource
                            
                            sendMessageMulticast(res, 0, curTime);	
                            int t = (int)(Math.random()*100000);
                            Thread.sleep(t);
                            }
                }
                } catch (InterruptedException e) {
                 e.printStackTrace();
                }
}).start();

}
    /**
	 * sendMessageMulticast()
	 * sends a multicast message to the group requesting access to the resource(0 to 4)
	 * @param resource The number of the resource you want to access
	 * @param type The type of the message (0 = access request, 1 = ok)
	 * @param time The time of the process clock at the time of sending the multicast
	 */
	public static void sendMessageMulticast(int resource, int type, int time) {
		System.out.println("Resource request " + resource + " by the process over time " + time);
		client(60012, resource, type, time);
		client(60013, resource, type, time);
	}
	
	/**
	 * client()
	 * Thread for sending messages
	 * @param destino the destination port of the message
	 * @param resource The number of the resource you want to access
	 * @param type The type of the message (0 = access request, 1 = ok)
	 * @param time The time of the process clock at the time of sending the multicast
	 */
	public synchronized static void client(final int destino, final int resource, final int type, final int time) 
        {
            new Thread(() -> {
                                try {
                                        String sendMessage;
					
					                    // Assemble the message to be sent, Containing the number of the resource to be accessed and the clock
					                    // time of the process at the time of the sending
					                    
					                    // resource request
					                    if(type == 0)
						                    sendMessage = "RESOURCE#" + resource + "#" + pid + "#" + time;
					                    
					                    // OK
					                    else
						                    sendMessage = "OK#" + resource + "#" + pid + "#" + time;

                                        // Send the request (or ok) to the process, 
					                    // The timestamp of the message is the current timestamp
					                    Socket s;
                                        s = new Socket("localhost", destino);
                                        BufferedWriter out = new BufferedWriter(
                                            new OutputStreamWriter(s.getOutputStream()));

                                        out.write(sendMessage);    
                                        out.newLine();
                                       	out.flush();
					
				
					                    if(type == 0)
						                    System.out.println("Resource Number " + resource + 
                                                                " is requested by the process number " + pid + 
                                                                    " over time " + time +  
                                                                        "to the process at th port" + destino);
					                    else 
					                     	System.out.println("Process " + pid + " sent OK " + resource + " to the destination " + destino);
					 

                                         s.close();

                	                } 
                                    catch (UnknownHostException e) 
                                    {
                    	                e.printStackTrace();
                	                } 
                                    catch (IOException e)
                                     {
                    	                e.printStackTrace();
                                     }
                               }
        ).start();
    }
	
	/**
	 * server()
	 * Thread server to receive messages
	 */
	public synchronized static void server() 
    {
        new Thread(() -> {
                            ServerSocket ss;
                            try {                
                                    ss = new ServerSocket(60011);

					                // Server listens
                                    while(true)
                                    { 
                                        Socket s = ss.accept();

						                // Put the thread to sleep to avoid competition
                                        try
                                        { 
							                int t = (int)(Math.random()*2000);
			                            	Thread.sleep(t); 
			                        	}
                                        catch(Exception e){}	

						                // Handles the request on an another thread while the server listens again
                                        algorithmProcessing(s);
						
                                    }
                             } 
                            catch (IOException e) 
                            {
                                e.printStackTrace();
                            }
                        }
        ).start();
    }

	/**
	 * algorithmProcessing()
	 * Handles the request for receiving and sending message
	 * @param s the socket that received the request
	 */
    public synchronized static void algorithmProcessing(final Socket s) 
    {   
         new Thread(() -> {
                    try 
                        {
                            // Read the message
                            BufferedReader in = new BufferedReader(
                                new InputStreamReader(s.getInputStream()));
								
                            String line = null;
					        
					        line = in.readLine();
						
					        // Break the message
					        String linebreak[] = line.split("#");
						
					        // Event (received a message), ajust the internal clock
					        int resourceNumber = Integer.parseInt(linebreak[1]);
					        int messageTiming = Integer.parseInt(linebreak[3]);
					        int sender = Integer.parseInt(linebreak[2]);
					
					        clock.adjust(messageTiming);
						
					        // If Requested and Treated
					        if(linebreak[0].equals("RESOURCE"))
                            {	
						        Process prc = new Process(sender, resourceNumber, messageTiming);
						
						        //free resource
						        if (resources[resourceNumber] == -2)
                                 {
							        // Event (message sending), increments the clock
							        clock.add();
						
							        switch (sender) 
                                    {
								        case 2:
									        client(60012, resourceNumber, 1, clock.getTime());
									        System.out.println("Resource " + resourceNumber + " free / sent OK to " + sender);
									        break;
								        case 3:
									        client(60013, resourceNumber, 1, clock.getTime());
									        System.out.println("Resource " + resourceNumber + " free / sent OK to " + sender);
									        break;
							        }
						        }
						
						        // resource is being used by the process
						        else if ( resources[resourceNumber] == -1) 
                                {
							        System.out.println("Resource " + resourceNumber + " being used / queued " + sender);
							        
							        // Event (add to Queue), increment clock
							        clock.add();
							        
							        if(!pQueue.get(resourceNumber).add(prc)) 
								        System.out.println("There was an error adding to the Queue");
							        
						        }
						
						        // wants to access resource
						        else {
							            // if the message time is longer when the request was made
							            if (messageTiming > resources[resourceNumber]) 
                                        {
								            // Event (add to queue), increment clock
								            clock.add();

								            if(!pQueue.get(resourceNumber).add(prc)) 
									            System.out.println("There was an error adding to the Queue");
								
						                    System.out.println("Resource " + resourceNumber + 
                                                                " wants to be accessed and has clocked / queued " + 
                                                                    sender + " (" + resources[resourceNumber] + ")");
							            } 
							
							            // if not, send ok
							            else 
                                        {
								            // Event (message sending), increment clock
								            clock.add();							
							
								            switch (sender) 
                                                {
									                case 2:
										            client(60012, resourceNumber, 1, clock.getTime());
										            System.out.println("Resource " + resourceNumber + 
                                                                        " wants to be accessed and lost clock/ sent OK " + 
                                                                            sender + " (" + resources[resourceNumber] + 
                                                                                " - " + messageTiming + ")");
									                break;
									                case 3:
										            client(60013, resourceNumber, 1, clock.getTime());
										            System.out.println("Resource " + resourceNumber + 
                                                                        " wants to be accessed and lost clock/ sent OK " + 
                                                                            sender + " (" + resources[resourceNumber] + 
                                                                                " - " + messageTiming + ")");
										            break;
								                }
							            }
						            }							
					            }
						
					            // if its OK
					            else if (linebreak[0].equals("OK")) 
                                {							
						            // Adds one to received OKs
						            // Internal Event (received OK and changed the counter), add one to the clock
						            clock.add();	
						
						            oks[resourceNumber]++;
						
						            System.out.println("Received Ok for resource " + resourceNumber + " from " + sender);

						            // if the process has received the second Ok for the desired resource
						            if(oks[resourceNumber] == 2) 
                                    {							
							            //uses resources for a while
							            
							            // Event (resource usage), increment clock
							            clock.add();
							
							            resources[resourceNumber] = -1;
							            Thread.sleep(3000); 

							            clock.add();

							            System.out.println("Resource " + resourceNumber + " was used and released in time " + clock.getTime());

							            // releases resources
							            resources[resourceNumber] = -2;
							            oks[resourceNumber] = 0;

							            // send OKs 
							            while ( !pQueue.get(resourceNumber).isEmpty() ) 
                                        {

								            // Internal event (delivered the message to the application), add one to the clock
								            clock.add();
									
								            switch (pQueue.get(resourceNumber).first().getProcessNumber()) 
                                            {
									            case 2:
										            clock.add();
										            client(60012, resourceNumber, 1, clock.getTime());
										            System.out.println("Resource " + resourceNumber + " finished access by "+ pid + "/sent OK to" + sender);
										            break;
									            case 3:
										            clock.add();
										            client(60013, resourceNumber, 1, clock.getTime());
										            System.out.println("Resource " + resourceNumber + " finished access by "+ pid + "/sent OK to" + sender);
										            break;
								            }
								            pQueue.get(resourceNumber).remove(pQueue.get(resourceNumber).first());
							            }
						            }
					            }

                            }
                            catch (UnknownHostException e) 
                            {
                                	e.printStackTrace();
                            } 
                            catch (IOException e) 
                            {
                                   	e.printStackTrace();
                            }
                            catch(Exception e){}
            }
        ).start();
    }
}
