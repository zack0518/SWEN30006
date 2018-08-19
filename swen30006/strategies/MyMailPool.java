package strategies;

import java.util.ArrayList;
import java.util.Stack;

import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.StorageTube;
import exceptions.TubeFullException;

import java.util.List;
import java.lang.Object;

public class MyMailPool implements IMailPool{

//	the most important change is I created two new stack to store Mail_itme which are too heavy( weight > 2000)
	private Stack<MailItem> nonPriorityPool;
	private Stack<MailItem> priorityPool;
	private Stack<MailItem> nonPriorityPoolStrong;
	private Stack<MailItem> priorityPoolStrong;
	
	
	private static final int MAX_TAKE = 4;
	private Robot robot1, robot2, robot3;
//	private List<Robot> robots;

	public MyMailPool(){
		// Start empty
		nonPriorityPool = new Stack<MailItem>();
		priorityPool = new Stack<MailItem>();
		nonPriorityPoolStrong = new Stack<MailItem>();
		priorityPoolStrong = new Stack<MailItem>();
//		robots = new ArrayList<>();
		
	}

	
//	this method to sort the mail_items in a stack, according to their arrival_time - destination_floor
//	the basic idea is to use an assistant stack	
	public static void sortStack(Stack<MailItem> stack) {
		Stack<MailItem> help = new Stack<MailItem>();
		while (!stack.isEmpty()) {
			MailItem cur = stack.pop();		
			double test = 2.9;
			double time = cur.getArrivalTime() + cur.getDestFloor()*test;
			while (!help.isEmpty() && (help.peek().getArrivalTime() + help.peek().getDestFloor()*test) > time) {
				
				stack.push(help.pop());
			}
			help.push(cur);
		}
		
		while (!help.isEmpty()) {
			stack.push(help.pop());
		}		
	}

	
//	this method to sort the mail_items in the tube, according to their destination_floor
	private void sortTube(StorageTube tube) {
		StorageTube help = new StorageTube();
		while (!tube.isEmpty()) {
			MailItem cur = tube.pop();
			
			int time = cur.getDestFloor();
			while (!help.isEmpty() && help.peek().getDestFloor() > time) {
			
			
				try {
					tube.addItem(help.pop());
				} catch (TubeFullException e) {
					e.printStackTrace();
				}

			}
			try {
				help.addItem(cur);
			} catch (TubeFullException e) {
				e.printStackTrace();
			}
		}
		
		while (!help.isEmpty()) {
			try {
				tube.addItem(help.pop());
			} catch (TubeFullException e) {
				e.printStackTrace();
			}
		}
	}
	
	

	public void addToPool(MailItem mailItem) {
		// Check whether it has a priority or not
		if(mailItem instanceof PriorityMailItem){
			
//			then check if it is an heavy mail
			if (mailItem.getWeight() > 2000) {
				priorityPoolStrong.push(mailItem);
			}
			else {
				priorityPool.push(mailItem);
			}
			
//			after add item into pool, sort the pool
			sortStack(priorityPool);
			sortStack(priorityPoolStrong);
		}
		else{
			if (mailItem.getWeight() > 2000) {
				nonPriorityPoolStrong.push(mailItem);
			}
			else {
				nonPriorityPool.add(mailItem);
			}
			sortStack(nonPriorityPool);
			sortStack(nonPriorityPoolStrong);
		}
	}
	
//	get the size of the pool
	private int getStackSize(Stack<MailItem> stack) {
		return stack.size();
	}	
	
	@Override
	public void step() {
		
//		for (Robot robot : robots) {
//			fillTube(robot);
//		}
		if (robot1 != null) fillTube(robot1);
		if (robot2 != null) fillTube(robot2);
		if (robot3 != null) fillTube(robot3);
		
	}
	
	

	
//	the idea is the weak robot only pick item from normal pool,
//	the strong robot first pick item from heavy pool, then pick from normal pool
	private void fillTube(Robot robot) {
		boolean strong = robot.isStrong();	
		if (strong) {
			fillStrongRobot(robot);
			fillWeakRobot(robot);
		}
		else {
			fillWeakRobot(robot);
		}

	}
	
	

//	weak robot only first pick priority pool, and then check if it can bring some non_poriority items
	private void fillWeakRobot(Robot robot) {
		StorageTube tube = robot.getTube();
		try {
			while (getStackSize(priorityPool) > 0 && tube.getSize() < MAX_TAKE) {
				tube.addItem(priorityPool.pop());
			}
			while (tube.getSize() < MAX_TAKE && getStackSize(nonPriorityPool) > 0) {
				tube.addItem(nonPriorityPool.pop());
			}
			if (tube.getSize() > 0 ) {
				
				sortTube(tube);
				robot.dispatch();
			}
		} catch (TubeFullException e) {
			e.printStackTrace();
		}
		
	}
	
	
//	strong robot first pick item from strong_pool
	private void fillStrongRobot(Robot robot) {
		
		StorageTube tube = robot.getTube();
		try {
			while (getStackSize(priorityPoolStrong) > 0 && tube.getSize() < MAX_TAKE) {
				tube.addItem(priorityPoolStrong.pop());
			}
			while (tube.getSize() < MAX_TAKE && getStackSize(nonPriorityPoolStrong) > 0) {
				tube.addItem(nonPriorityPoolStrong.pop());
			}
			if (tube.getSize() > 0) {
				
				sortTube(tube);
				robot.dispatch();
			}
		} catch (TubeFullException e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public void registerWaiting(Robot robot) {
		
//		robots.add(robot);
		if (robot1 == null) {
			robot1 = robot;
		} else if (robot2 == null) {
			robot2 = robot;
		} else if (robot3 == null) {
			robot3 = robot;
		} else {
		}
	}

	@Override
	public void deregisterWaiting(Robot robot) {
		
//		robots.add(robot);
		if (robot1 == robot) {
			robot1 = null;
		} else if (robot2 == robot) {
			robot2 = null;
		} else if (robot3 == robot) {
			robot3 = null;
		} else {
		}
		
	}

}
