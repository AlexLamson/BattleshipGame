import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Listening implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		switch(key)
		{
		case KeyEvent.VK_P:
			Main.reset();
			// System.out.println("Main.game.mySea.isEnemy: "+Main.game.mySea.isEnemy+", Main.game.myTurn: "+Main.game.myTurn+", Main.isTwoPlayers: "+Main.isTwoPlayers+", Main.game.mySea.renderEditingShip: "+Main.game.mySea.renderEditingShip+", Main.game.mySea.gameWon: "+Main.game.mySea.gameWon+", Main.editingMode: "+Main.editingMode+", BattleShipAI.sunkShips.size(): "+BattleShipAI.sunkShips.size());
			break;
		case KeyEvent.VK_Z:		//make left win the game!
			Main.game.makeRightWin(false);
			// Main.gameOver = foo;
			break;
		case KeyEvent.VK_X:		//make right win the game!
			Main.game.makeRightWin(true);
			// Main.gameOver = foo;
			break;
		case KeyEvent.VK_E:
			if(!Main.gameOver)
			{
				if(Main.placingShips)
				{
					if(!Main.manualAdding)
					{
						BattleShipGame.editingPos = -1;
						Main.game.editNextShip();
						Main.game.clearEditingShips();
					}
					Main.manualAdding = !Main.manualAdding;
					Main.editingMode = !Main.editingMode;
					Main.game.toggleEditingMode();
				}
			}
			break;
		case KeyEvent.VK_R:
			if(!Main.gameOver)
			{
				if(Main.placingShips)
				{
					Main.manualAdding = false;
					Main.editingMode = false;
					Main.game.toggleEditingMode();
					Main.game.mySea.reset();
				}
			}
			else
			{
				if(Main.gameOver)
					System.out.println("cannot randomize ships, game is over");
				if(!Main.placingShips)
					System.out.println("cannot randomize ships, not placing ships");
			}
			break;
		case KeyEvent.VK_CONTROL:
			if(!Main.gameOver)
			{
				if(Main.placingShips && Main.editingMode)
					Main.game.rotateEditingShip();
			}
			break;
		case KeyEvent.VK_SPACE:
			if(!Main.swappingNow)		//if there isn't a black screen already
			{
				if(!Main.gameOver)
				{
					if(Main.placingShips)
					{
						if(!Main.manualAdding)
						{
							Main.game.endEditingTurn();
						}
						else
						{
							if(!Main.editingMode)
							{
								if(Main.readyToSwap)
									Main.swap();
							}
							else
							{
								if(Main.game.editShipWorks(BattleShipGame.editingShip))
								{
									Main.game.addEditingShip();
									Main.game.editNextShip();
								}
							}
						}
					}
					else
					{
						if(!Main.editingMode)
						{
							if(Main.readyToSwap)
								Main.endTurn();
						}
					}
				}
			}
			break;
		case KeyEvent.VK_LEFT:
			if(!Main.gameOver)
			{
				if(Main.placingShips && Main.editingMode)
				{
					Ship testShip = new Ship(BattleShipGame.editingShip);
					testShip.x--;
//					if(Main.game.editShipWorks(testShip))
					if(Main.game.editShipInBounds(testShip))
						BattleShipGame.editingShip = testShip;
				}
			}
			break;
		case KeyEvent.VK_RIGHT:
			if(!Main.gameOver)
			{
				if(Main.placingShips && Main.editingMode)
				{
					Ship testShip = new Ship(BattleShipGame.editingShip);
					testShip.x++;
//					if(Main.game.editShipWorks(testShip))
					if(Main.game.editShipInBounds(testShip))
						BattleShipGame.editingShip = testShip;
				}
			}
			break;
		case KeyEvent.VK_UP:
			if(!Main.gameOver)
			{
				if(Main.placingShips && Main.editingMode)
				{
					Ship testShip = new Ship(BattleShipGame.editingShip);
					testShip.y--;
//					if(Main.game.editShipWorks(testShip))
					if(Main.game.editShipInBounds(testShip))
						BattleShipGame.editingShip = testShip;
				}
			}
			break;
		case KeyEvent.VK_DOWN:
			if(!Main.gameOver)
			{
				if(Main.placingShips && Main.editingMode)
				{
					Ship testShip = new Ship(BattleShipGame.editingShip);
					testShip.y++;
//					if(Main.game.editShipWorks(testShip))
					if(Main.game.editShipInBounds(testShip))
						BattleShipGame.editingShip = testShip;
				}
			}	
			break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();
		switch(key)
		{
		}
	}
	
	public void keyTyped(KeyEvent e)
	{

	}
	
	public void mouseClicked(MouseEvent e)
	{
		setMousePos(e);
		mouseToggle(e, true);
		click(e);
		mouseToggle(e, false);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		setMousePos(e);
//		click(e);
	}
	
	public void mousePressed(MouseEvent e)
	{
		mouseToggle(e, true);
	}

	public void mouseReleased(MouseEvent e)
	{
		mouseToggle(e, false);
	}
	
	public static void click(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			if(!Main.gameOver && !Main.placingShips && !Main.editingMode && !Main.readyToSwap)
			{
				int[] pos1 = Main.game.mySea.getTile(Main.mouseNow.x, Main.mouseNow.y);
				int[] pos2 = Main.game.theirSea.getTile(Main.mouseNow.x, Main.mouseNow.y);
				boolean isTheirs = false;
				
				if(Main.game.theirSea.isTile(pos2[0], pos2[1]))
					isTheirs = true;
				
				int xPos = -1, yPos = -1;
				if(!isTheirs)
				{
					xPos = pos1[0];
					yPos = pos1[1];
					if(!Main.game.myTurn && !Main.game.mySea.isBombed(xPos, yPos))
					{
						Ship junkShip = Main.game.mySea.bombTile(xPos, yPos);
						Main.readyToSwap = true;
					}
				}
				else
				{
					xPos = pos2[0];
					yPos = pos2[1];
					if(Main.game.myTurn && !Main.game.theirSea.isBombed(xPos, yPos))
					{
						Ship junkShip = Main.game.theirSea.bombTile(xPos, yPos);
						Main.readyToSwap = true;
					}
				}
			}
			else
			{
				if(Main.editingMode)
					System.out.println("can't click - in editing mode");
				if(Main.placingShips)
				{
					String who = "they";
					if(Main.game.myTurnEditing)
						who = "you";
					System.out.println("can't click - "+who+" are placing ships");
				}
				if(Main.readyToSwap)
					System.out.println("can't click - not your turn");
				if(Main.gameOver)
					System.out.println("can't click - game is over");
			}
		}
		else	//if not primary click
		{
			if(!Main.gameOver && !Main.placingShips && !Main.editingMode && !Main.readyToSwap)
			{
				int xPos = 0;
				int yPos = 0;
				
				int[][] sea;
				if(Main.game.myTurn)
					sea = Main.game.theirSea.sea;
				else
					sea = Main.game.mySea.sea;
				
				//find first available tile
				boolean breakAll = false;
				for(int y = 0; y < sea[0].length; y++)
				{
					for(int x = 0; x < sea.length; x++)
					{
						if(sea[x][y] == Sea.OCEAN)
						{
							xPos = x;
							yPos = y;
							breakAll = true;
						}
						if(breakAll)
							break;
					}
					if(breakAll)
							break;
				}
				
				//if you can't miss any more shots
				if(!breakAll)
				{
					for(int y = 0; y < sea[0].length; y++)
					{
						for(int x = 0; x < sea.length; x++)
						{
							if(sea[x][y] == Sea.SHIP)
							{
								xPos = x;
								yPos = y;
								breakAll = true;
							}
							if(breakAll)
								break;
						}
						if(breakAll)
								break;
					}
				}
				
				//bomb chosen tile
				if(Main.game.myTurn && !Main.game.theirSea.isBombed(xPos, yPos))
				{
					Ship junkShip = Main.game.theirSea.bombTile(xPos, yPos);
					Main.readyToSwap = true;
				}
				else if(!Main.game.myTurn && !Main.game.mySea.isBombed(xPos, yPos))
				{
					Ship junkShip = Main.game.mySea.bombTile(xPos, yPos);
					Main.readyToSwap = true;
				}
			}
		}
	}
	
	public static void mouseToggle(MouseEvent e, boolean toggle)
	{
		if(e.getButton() == MouseEvent.BUTTON1)			//left click
			Main.isMouseLeft = toggle;
		else if(e.getButton() == MouseEvent.BUTTON2)	//middle click
			Main.isMouseMiddle = toggle;
		else if(e.getButton() == MouseEvent.BUTTON3)	//right click
			Main.isMouseRight = toggle;
	}
	
	public void mouseMoved(MouseEvent e)
	{
		setMousePos(e);
	}
	
	public void setMousePos(MouseEvent e)
	{
		Main.mousePrev.x = Main.mouseNow.x;
		Main.mousePrev.y = Main.mouseNow.y;
		Main.mouseNow.setLocation(e.getX()/Main.pixelSize, e.getY()/Main.pixelSize);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if(e.getWheelRotation() < 0)			//scrolled up
		{
		}
		else if(e.getWheelRotation() > 0)		//scrolled down
		{
		}
	}
	
	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}
}
