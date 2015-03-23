import java.awt.Graphics;
import java.util.ArrayList;

public class BattleShipGame
{
	public int x, y, xSize, ySize;		//constraints of the visible window

	public Sea mySea;		//the sea on the right
	public Sea theirSea;	//the sea on the left
	
	public boolean myTurn = true, myTurnEditing = true;
	public int totalTurns = 0;
	
	public static int[] shipSizes = new int[]{2, 3, 3, 4, 5};
	
	public static Ship editingShip = new Ship();
	public static int editingPos = -1;
	
	public BattleShipGame(int x, int y, int width, int height, int gridSize)
	{
		boolean rightStarts = true;
		
		if(rightStarts)
		{
			mySea = new Sea(x+width/2, y, width/2, height, gridSize, gridSize);
			theirSea = new Sea(x, y, width/2, height, gridSize, gridSize);
		}
		else
		{
			mySea = new Sea(x, y, width/2, height, gridSize, gridSize);
			theirSea = new Sea(x+width/2, y, width/2, height, gridSize, gridSize);
		}
		theirSea.setEnemy(true);
	}
	
	public BattleShipGame(int x, int y, int width, int height)
	{
		this(x, y, width, height, 10);
	}
	
	public void destroy()
	{
		mySea.destroy();
		theirSea.destroy();
	}
	
	//return true if all the ships on at least one side are all destroyed
	public boolean gameFinished()
	{
		return (mySea.fleetDestroyed() || theirSea.fleetDestroyed());
	}
	
	//return true if not all ships are destroyed on my side
	public boolean isMyWin()
	{
		return (!mySea.fleetDestroyed() && theirSea.fleetDestroyed());
	}
	
	//return true if not all ships are destroyed on their side
	public boolean isTheirWin()
	{
		return (!theirSea.fleetDestroyed() && mySea.fleetDestroyed());
	}
	
	public void makeRightWin(boolean myWin)
	{
		Main.rightWins = myWin;
		if(myWin)
		{
			mySea.gameWon = true;
			theirSea.gameWon = true;
			
			mySea.winner = true;
			theirSea.winner = false;
			
			theirSea.destroyFleet();
		}
		else
		{
			mySea.gameWon = true;
			theirSea.gameWon = true;
			
			theirSea.winner = true;
			mySea.winner = false;
			
			mySea.destroyFleet();
		}
	}
	
	public void clearEditingShips()
	{
		if(myTurnEditing)
			mySea.clearShips();
		else
			theirSea.clearShips();
	}
	
	public void toggleEditingMode()
	{
		if(!Main.editingMode)
		{
			mySea.renderEditingShip = false;
			theirSea.renderEditingShip = false;
		}
		else
		{
			if(myTurnEditing)
				mySea.renderEditingShip = true;
			else
				theirSea.renderEditingShip = true;
		}
		
		if(!Main.editingMode)
		{
			mySea.reset();
			theirSea.reset();
		}
		else
		{
			mySea.clearShips();
			theirSea.clearShips();
		}
	}
	
	public void rotateEditingShip()
	{
		Ship testShip = new Ship(editingShip);
		testShip.rotate();
		
		if(myTurnEditing)
		{
			if(mySea.shipInBounds(testShip))
				editingShip.rotate();
		}
		else
		{
			if(theirSea.shipInBounds(testShip))
				editingShip.rotate();
		}	
	}
	
	public void addEditingShip()
	{
		if(myTurnEditing)
		{
			if(mySea.shipWorks(editingShip))
				mySea.addShip(new Ship(editingShip));
		}
		else
		{
			if(theirSea.shipWorks(editingShip))
				theirSea.addShip(new Ship(editingShip));
		}
	}
	
	public boolean editShipWorks(Ship ship)
	{
		if(myTurnEditing)
			return mySea.shipWorks(ship);
		else
			return theirSea.shipWorks(ship);
	}
	
	public boolean editShipInBounds(Ship ship)
	{
		if(myTurnEditing)
			return mySea.shipInBounds(ship);
		else
			return theirSea.shipInBounds(ship);
	}
	
	public void editNextShip()
	{
		if(editingPos < shipSizes.length-1)
		{
			editingPos++;
			if(myTurnEditing)
			{
				editingShip = new Ship(shipSizes[editingPos], mySea.sea.length, mySea.sea[0].length, mySea.fleet);
				mySea.renderEditingShip = true;
			}
			else
			{
				editingShip = new Ship(shipSizes[editingPos], theirSea.sea.length, theirSea.sea[0].length, theirSea.fleet);
				theirSea.renderEditingShip = true;
			}
		}
		else
		{
			editingShip = new Ship();
			editingShip.xSize = 0;
			editingShip.ySize = 0;
			
			endEditingTurn();
		}
	}
	
	public void endEditingTurn()
	{
		if(myTurnEditing)			//finish with player 1
		{
			if(Main.isTwoPlayers)
			{
				myTurnEditing = false;
				mySea.renderEditingShip = false;
				theirSea.renderEditingShip = true;
				editingPos = -1;
				editNextShip();
				Main.manualAdding = true;
				Main.editingMode = true;
				Main.swap();
				theirSea.clearShips();
			}
			else
			{
				Main.manualAdding = true;
				ArrayList<Ship> proposedAIFleet = BattleShipAI.getShips(theirSea.sea);
				
				if(theirSea.isValidFleet(proposedAIFleet))
				{
					theirSea.clearShips();
					
					for(Ship ship : proposedAIFleet)
						theirSea.fleet.add(ship);
					theirSea.updateNums();
//					endEditingTurn();		//try without it for now
					
					mySea.renderEditingShip = false;
					theirSea.renderEditingShip = false;
					Main.editingMode = false;
					Main.placingShips = false; 
				}
				else
				{
					System.err.println("BUG IN AI - ILLEGAL SHIP PLACEMENT");
				}
			}
		}
		else if(!myTurnEditing)		//finish with player 2
		{
			mySea.renderEditingShip = false;
			theirSea.renderEditingShip = false;
			Main.editingMode = false;
			Main.swap();
			
			Main.placingShips = false; 
		}
	}
	
	public void swapStates()
	{
		myTurn  = !myTurn;
		mySea.isEnemy = !mySea.isEnemy;
		theirSea.isEnemy = !theirSea.isEnemy;
	}
	
	public void tick()
	{
		mySea.tick();
		theirSea.tick();
	}
	
	public void render(Graphics g)
	{
		theirSea.render(g);
		mySea.render(g);
	}
}
