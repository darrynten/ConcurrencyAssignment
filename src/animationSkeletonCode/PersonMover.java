package animationSkeletonCode;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class PersonMover extends Thread {
	public static int IDcounter=0;
	private Person thisPerson;
	public static RoomGrid grid; //shared grid
	private Random rand;
	private PeopleCounter counter;
	private boolean hasLock;
	private boolean entered;

	public volatile static boolean done=false; //add stop button
	public volatile static Lock lk = new ReentrantLock();
	public volatile static boolean isPaused = false;
	
	private int ID; //thread ID for debugging
	
	PersonMover( Person janeDoe) {
		thisPerson = janeDoe;
		rand = new Random();	
		hasLock = false;
		entered = false;

		synchronized (PersonMover.class) {
			ID=IDcounter;
			PersonMover.IDcounter++;
			}
	}
	
	PersonMover( Person creature, PeopleCounter score) {
		this(creature);
		this.counter=score;
	}
	
	
	
	private void headForRefreshments() {  //next move towards bar
		//no need to change this function
		int x_mv=-1;
		int y_mv=-1;
		GridBlock nextBlock =null;
		while (nextBlock==null) { // repeat until hit on empty cell
			
			x_mv= rand.nextInt(3)-1+thisPerson.getX();
			if (x_mv<0) x_mv=0;
			
			y_mv=thisPerson.getY()+1;
			if (y_mv<0) y_mv=0;		
			else if (y_mv>=grid.getMaxY()) y_mv=grid.getMaxY()-1;	
			
			if (!((x_mv==thisPerson.getX())&&(y_mv==thisPerson.getY()))) {
				nextBlock=grid.getBlock(x_mv,y_mv);
			} 
		}	
		thisPerson.moveToBlock(nextBlock);
	}
	
	private void mingle() { //random next move
		//no need to change this function
		int x_mv=-1;
		int y_mv=-1;
		GridBlock nextBlock =null;
		while (nextBlock==null) { // repeat until hit on empty cell
			x_mv= rand.nextInt(3)-1+thisPerson.getX();
			if (x_mv<0) x_mv=0;
			y_mv=rand.nextInt(3)-1+thisPerson.getY();;
			if (y_mv<0) y_mv=0;		
			if (!((x_mv==thisPerson.getX())&&(y_mv==thisPerson.getY()))) {
					//System.out.println("moving from x="+x+" y="+y); //debug
					//System.out.println("moving to x="+x_mv+" y="+y_mv); //debug
				nextBlock=grid.getBlock(x_mv,y_mv);
			} 
		}	
		thisPerson.moveToBlock(nextBlock);
		
	}
	
	public void run() {
		boolean arrived = false;
		
		//TODO add code to allow for pausing
		
		while(!entered){
			try {
				if(isPaused){
					synchronized(this){
						Thread.interrupted();
						wait();
					}
				}
				if(!arrived){
					sleep(rand.nextInt(10000)); //time till arriving at party
					counter.personArrived(); //add to counter
					arrived = true;
				}
				
				synchronized(PartyApp.limit){
					while(PartyApp.limit.get()>=0 && PartyApp.limit.get() <= PartyApp.counterD.score.getInside()){
						sleep(500);
					}
					GridBlock firstBlock =grid.getEntranceBlock(); //enter through entrance
					
					assert(firstBlock!=null);
					if(!hasLock){
						lk.lockInterruptibly();
						hasLock=true;
						//System.out.println("Lock: " + this.ID);
					}
					firstBlock.waitBlock();
				
					thisPerson.initBlock(firstBlock);
					counter.personEntered(); //add to counter
					entered = true;
					sleep(thisPerson.getSpeed());
					
				}
				
			} catch (InterruptedException e1) {			
				
				//System.out.println("Thread "+this.ID + " interrrupted." + hasLock);
				if(!isPaused){
					done=true;
					System.out.println("Done1" + ": " + this.ID);
				}
				continue;
			}
		}
		
		while ((!done)&&(thisPerson.inRoom())){				
			try {
				if(isPaused){
					synchronized(this){
						Thread.interrupted();
						this.wait();
					}
				}	
				sleep(thisPerson.getSpeed());
				if (thisPerson.thirsty()) {
					if (thisPerson.atRefreshmentStation()) {
						sleep(thisPerson.getSpeed()*4);//drinking for a while
						thisPerson.drink();
					}
					else headForRefreshments();
				}
				else if (thisPerson.atExit()) {
					thisPerson.leave();
				} 
				else {
					mingle();
				}
				if(hasLock){
					lk.unlock();
					hasLock = false;
				}
				//sleep(thisPerson.getSpeed());
				
			}			
			
			catch (InterruptedException e) {
				//System.out.println("Thread "+this.ID + "interrrupted.");
				if(!isPaused){
					done=true;
					System.out.println("Done2");
				}
				Thread.interrupted();
				continue;
			} 
		}
		counter.personLeft(); //add to counter		
		
	}	
}

