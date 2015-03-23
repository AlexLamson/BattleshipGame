import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Sea
{
	public int x, y, width, height;
	
	public static Color COORDCOL = Color.lightGray, TESTCOL = Color.green, OCEANCOL = Color.blue, 
			MISSEDCOL = new Color(0, 150, 255), SHIPCOL = Color.gray, 
			HITCOL = Color.red, LINECOL = new Color(255, 150, 0, 255);
	
	public static int TEST = -1, OCEAN = 0, MISS = 1, SHIP = 2, HIT = 3;
	
	// -1 = testing, 0 = open ocean, 1 = missed shot, 2 = undamaged ship, 3 = damaged ship
	public int[][] sea;
	
	public ArrayList<Ship> fleet = new ArrayList<Ship>();
	
	public boolean isEnemy = false;
	
	public static boolean showCoords = true;
	
	public boolean renderEditingShip = false;
	
	public boolean gameWon = false, winner = false;
	
	public Sea(int x, int y, int width, int height, int xSize, int ySize)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		sea = new int[xSize][ySize];
		
		if(!Main.manualAdding)
			addAllShips();
	}
	
	public Sea(int x, int y, int width, int height)
	{
		this(x, y, width, height, 10, 10);
	}
	
	public Sea(int xSize, int ySize)
	{
		this(0, 0, 1, 1, xSize, ySize);
	}
	
	public void destroy()
	{
		int size = fleet.size();
		for(int i = 0; i < size; i++)
			fleet.remove(0);
	}
	
	public boolean isValidFleet(ArrayList<Ship> proposedFleet)
	{
		for(int i = 0; i < proposedFleet.size(); i++)
		{
			Ship testingShip = proposedFleet.get(i);
			boolean works = true;
			for(int j = 0; j < proposedFleet.size(); j++)
			{
				if(i != j)
				{
					if(testingShip.contains(proposedFleet.get(j)))
						works = false;
				}
			}
			if(!works)
			{
				System.err.println("Warning! AI proposed bad fleet arrangement!");
				return false;
			}
		}
		return true;
	}
	
	public void destroyFleet()
	{
		if(fleet.size() == 0)
			return;
		for(int y = 0; y < sea[0].length; y++)
			for(int x = 0; x < sea.length; x++)
				if(sea[x][y] == SHIP)
					bombTile(x, y);
	}
	
	public boolean fleetDestroyed()
	{
		if(fleet.size() == 0)
			return false;
		for(int y = 0; y < sea[0].length; y++)
		{
			for(int x = 0; x < sea.length; x++)
			{
				if(sea[x][y] == SHIP)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public void setEnemy(boolean isEnemy)
	{
		this.isEnemy = isEnemy;
	}
	
	public void reset()
	{
//		System.out.println();
		clearShips();
		addAllShips();
	}
	
	public void addShip(Ship ship)
	{
		fleet.add(ship);
		updateNums();
	}
	
	public void addShip(int size)
	{
		if(size > sea.length && size > sea[0].length)		//don't add if the size is too large
			return;
		
		boolean works = false;
		int tries = 0;
		
		Ship testShip = new Ship();
		while(!works && tries < 100)		//limit tries to prevent infinite loop
		{
			testShip = new Ship(size, sea.length, sea[0].length);
			boolean testWorks = shipWorks(testShip);
			tries++;
			if(testWorks)
				works = true;
		}
		if(works) 
		{
			fleet.add(testShip);
			updateNums();
		}
		else
		{
			System.err.println("addShip("+size+") - Couldn't make a suitable ship");
		}
	}
	
	//return true if the ship doesn't collide with other ships and is in bounds
	public boolean shipWorks(Ship ship)
	{
		if(shipInBounds(ship) && !collides(ship))
			return true;
		return false;
	}
	
	public boolean shipInBounds(Ship ship)
	{
		for(int x = ship.x; x < ship.x+ship.xSize; x++)
			for(int y = ship.y; y < ship.y+ship.ySize; y++)
				if(!isTile(x,y))
					return false;
		return true;
	}
	
	public void addAllShips()
	{
		for(int i = 0; i < BattleShipGame.shipSizes.length; i++)
			addShip(BattleShipGame.shipSizes[i]);
		
		updateNums();
	}
	
	//make all spaces with ships 2
	public void updateNums()
	{
		for(int i = 0; i < fleet.size(); i++)
		{
			Ship ship = fleet.get(i);
			for(int x = ship.x; x < ship.x+ship.xSize; x++)
				for(int y = ship.y; y < ship.y+ship.ySize; y++)
					sea[x][y] = SHIP;
		}
	}
	
	public void clearShips()
	{
		int size = fleet.size();
		for(int i = 0; i < size; i++)
			fleet.remove(0);
		
		for(int x = 0; x < sea.length; x++)
			for(int y = 0; y < sea[0].length; y++)
				sea[x][y] = OCEAN;
	}
	
	public void tick()
	{
		
	}
	
	public boolean collides(Ship someShip)
	{
		for(int i = 0; i < fleet.size(); i++)
		{
			Ship ship = fleet.get(i);
			if(someShip.contains(ship))
				return true;
		}
		return false;
	}
	
	public boolean isBombed(int x, int y)
	{
		return (sea[x][y] == HIT || sea[x][y] == MISS);
	}
	
	public boolean isTile(int x, int y)
	{
		return (x >= 0 && x < sea.length && y >= 0 && y < sea[0].length);
	}
	
	public int[] getTile(int x, int y)
	{
		int shift = 0;
		if(showCoords)
			shift = 1;
		
		int tileXSize = width/(sea.length+shift), tileYSize = height/(sea[0].length+shift);
		
		int x1 = (int)(1.0*(x-this.x)/tileXSize);
		int y1 = (int)(1.0*(y-this.y)/tileYSize);
		
		if(showCoords)
		{
			x1 -= 1;
			y1 -= 1;
		}
		
//		if(x1 < 0)
//			x1 = 0;
//		if(y1 < 0)
//			y1 = 0;
		
//		System.out.println(x1+" , "+y1);
		
		return new int[]{x1 , y1};		//placeholder
	}
	
	public void setTile(int x, int y, int num)
	{
		if(isTile(x,y))
			sea[x][y] = num;
	}
	
	//returns a ship if a ship was sunk, otherwise returns placeholder ship
	public Ship bombTile(int x, int y)
	{
		if(isTile(x,y))
		{
			switch(sea[x][y])
			{
			case 0:		//open ocean
				sea[x][y] = MISS;			//change to missed shot
				break;
			case 2:		//undamaged ship
				sea[x][y] = HIT;			//change to damaged ship
				break;
			}
		}
		
		Ship possibleDestroyedShip = getShip(x,y);
		if(possibleDestroyedShip.getSize() != 1 && Main.debugMode)
		{
			boolean val = shipDestroyed(possibleDestroyedShip);
			if(val)
				System.out.println("Ship sunk (size: "+possibleDestroyedShip.getSize()+")!");
		}
		return possibleDestroyedShip;
	}
	
	public boolean shipDestroyed(Ship ship)
	{
		
		for(int x = ship.x; x < ship.x+ship.xSize; x++)
			for(int y = ship.y; y < ship.y+ship.ySize; y++)
				if(sea[x][y] != HIT)
					return false;
		return true;
	}
	
	//return ship that contains (x , y). if there is no ship, return new Ship()
	public Ship getShip(int x, int y)
	{
		for(int i = 0; i < fleet.size(); i++)
		{
			Ship ship = fleet.get(i);
			if(ship.contains(x, y))
				return ship;
		}
		return new Ship();
	}
	
	public Color idToColor(int id)
	{
		switch(id)
		{
		case -1:	//testing
			return TESTCOL;
		case 0:		//open ocean
			return OCEANCOL;
		case 1:		//missed shot
			return MISSEDCOL;
		case 2:		//undamaged ship
			if(gameWon || !isEnemy)
				return SHIPCOL;
			else
				return OCEANCOL;
		case 3:		//damaged ship
			return HITCOL;
		}
		return new Color(0, 150, 0);	//dark green
	}
	
	public void render(Graphics g)
	{
		boolean drawTextMap = false;
		
		int shift = 0;
		if(showCoords)
			shift = 1;
		int tileXSize = width/(sea.length+shift), tileYSize = height/(sea[0].length+shift);
		
		if(showCoords)
		{
			for(int x = -1; x < sea.length; x++)
			{
				if(x == -1)
					renderTile(g, x, -1, COORDCOL);
				else
					renderTile(g, x, -1, ""+(x+1));
			}
			
			for(int y = 0; y < sea[0].length; y++)
				renderTile(g, -1, y, ""+((char)(y+'A')));
		}
		
		for(int y = 0; y < sea[0].length; y++)
		{
			for(int x = 0; x < sea.length; x++)
			{
				renderTile(g, x, y, idToColor(sea[x][y]));
				if(drawTextMap)
					System.out.print(sea[x][y]+" ");
			}
			if(drawTextMap)
				System.out.println();
		}
		if(drawTextMap)
			System.out.println();
		
		// draw lines so you can tell ships apart
		for(int i = 0; i < fleet.size(); i++)
		{
			Ship ship = fleet.get(i);
			
			if(!isEnemy || shipDestroyed(ship))
			{
				g.setColor(LINECOL);
				
				int x1 = (ship.x+shift)*tileXSize + this.x + tileXSize/2;
				int y1 = (ship.y+shift)*tileYSize + this.y + tileYSize/2;
				
				int x2 = (ship.x+ship.xSize-1+shift)*tileXSize + this.x + tileXSize/2;
				int y2 = (ship.y+ship.ySize-1+shift)*tileYSize + this.y + tileYSize/2;
				
				g.drawLine(x1, y1, x2, y2);
			}
		}
		
		if(renderEditingShip)
		{
			Ship editingShip = new Ship(BattleShipGame.editingShip);
			for(int x = editingShip.x; x < editingShip.x+editingShip.xSize; x++)
				for(int y = editingShip.y; y < editingShip.y+editingShip.ySize; y++)
					renderTile(g, x, y, SHIPCOL);
		}
		
		if(gameWon)
		{
			int alpha = 100;
			if(winner)
			{
				g.setColor(new Color(0, 255, 0, alpha));
				g.fillRect(x, y, width, height);
			}
			else	//loser
			{
				g.setColor(new Color(255, 0, 0, alpha));
				g.fillRect(x, y, width, height);
			}
		}
	}
	
	public void renderTile(Graphics g, int xPos, int yPos, Color color)
	{
		int shift = 0;
		if(showCoords)
			shift = 1;
		int tileXSize = width/(sea.length+shift), tileYSize = height/(sea[0].length+shift);
		
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
		g.fillRect((xPos+shift)*tileXSize + this.x, (yPos+shift)*tileYSize + this.y, tileXSize, tileYSize);
		
		g.setColor(Color.black);
		g.drawRect((xPos+shift)*tileXSize + this.x, (yPos+shift)*tileYSize + this.y, tileXSize, tileYSize);
	}
	
	public void renderTile(Graphics g, int xPos, int yPos, String str)
	{
		renderTile(g, xPos, yPos, COORDCOL);
		
		int shift = 0;
		if(showCoords)
			shift = 1;
		int tileXSize = width/(sea.length+shift), tileYSize = height/(sea[0].length+shift);
		
		int xText = (xPos+shift)*tileXSize + this.x + tileXSize/2;
		int yText = (yPos+shift)*tileYSize + this.y + tileYSize/2;
		
		
		Font fontSave = g.getFont();
		Font font = new Font("Verdana", Font.BOLD, 18);
		
		
		double fontPercentSize = 0.75;
		
		for(int fSize = 9; fSize < 96; fSize++)
		{
			font = new Font("Verdana", Font.BOLD, fSize);
			FontMetrics fm = g.getFontMetrics(font);
			Rectangle rec = fm.getStringBounds(str, g).getBounds();
			
			if(rec.width > fontPercentSize*tileXSize || rec.height > fontPercentSize*tileYSize)
			{
				font = new Font("Verdana", Font.BOLD, fSize-1);
				break;
			}
		}
		
		g.setFont(font);
		
		FontMetrics fm = g.getFontMetrics(font);
        xText += -(fm.stringWidth(str)/2) + 2;
        yText += fm.getAscent()/2;
        
		g.drawString(str, xText, yText);
		g.setFont(fontSave);
	}
	
	public int[][] getSea()
	{
		return sea;
	}
	
	public int[][] getObfuscatedSea()
	{
		int[][] obfuscatedSea = new int[sea.length][sea[0].length];
		for(int x = 0; x < sea.length; x++)
		{
			for(int y = 0; y < sea[0].length; y++)
			{
				obfuscatedSea[x][y] = sea[x][y];
				if(sea[x][y] == SHIP)
					obfuscatedSea[x][y] = OCEAN;
			}
		}
		return obfuscatedSea;
	}
}
