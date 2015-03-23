import java.util.ArrayList;

public class Ship
{
	public int x = 0, y = 0, xSize = 1, ySize = 1;
	public static Ship placeholderShip = new Ship();
	
	//placeholder ship
	public Ship() { }
	
	//copy constructor
	public Ship(Ship ship)
	{
		this.x = ship.x;
		this.y = ship.y;
		this.xSize = ship.xSize;
		this.ySize = ship.ySize;
	}
	
	// x,y is upper left corner of ship, xSize and ySize determine lengths in either direction
	public Ship(int size, int seaXSize, int seaYSize, ArrayList<Ship> fleet)
	{
		boolean collides = true;
		while(collides)
		{
			if(Math.random() >= 0.5)
			{
				this.xSize = size;
				this.ySize = 1;
			}
			else
			{
				this.xSize = 1;
				this.ySize = size;
			}
			
			x = random(0, seaXSize-this.xSize);
			y = random(0, seaYSize-this.ySize);
			
			Ship testShip = new Ship(this.x, this.y, this.xSize, this.ySize, true);
			boolean testCollides = false;
			for(Ship fleetShip : fleet)
			{
				if(testShip.contains(fleetShip))
				{
					testCollides = true;
					break;
				}
			}
			if(!testCollides)
				collides = false;
		}
//		System.out.println(this);
	}
	
	// x,y is upper left corner of ship, xSize and ySize determine lengths in either direction
	public Ship(int size, int seaXSize, int seaYSize)
	{
		if(Math.random() >= 0.5)
		{
			this.xSize = size;
			this.ySize = 1;
		}
		else
		{
			this.xSize = 1;
			this.ySize = size;
		}
		
		x = random(0, seaXSize-this.xSize);
		y = random(0, seaYSize-this.ySize);
		
//		System.out.println(this);
	}
	
	// x,y is upper left corner of ship, xSize and ySize determine lengths in either direction
	public Ship(int xSize, int ySize, int seaXSize, int seaYSize)
	{
		x = random(0, seaXSize-xSize);
		y = random(0, seaYSize-ySize);
		this.xSize = xSize;
		this.ySize = ySize;
	}
	
	// boolean to distinguish from the other constructor
	public Ship(int x, int y, int xSize, int ySize, boolean whatever)
	{
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;
		
//		System.out.println(this);
	}
	
	// generate random int between floor and ceiling, inclusive
	public int random(int floor, int ceiling)
	{
		if(ceiling < 0)
			return 0;
		return (int)((Math.random()*(1.0+ceiling-floor))+floor);
	}
	
	public void tick()
	{
		
	}
	
	//return the length of the ship
	public int getSize()
	{
		if(xSize == 1)
			return ySize;
		return xSize;
	}
	
	//return true if this ship intersects that ship
	public boolean contains(Ship ship)
	{
		for(int x = this.x; x < this.x+xSize; x++)
			for(int y = this.y; y < this.y+ySize; y++)
				if(ship.contains(x,y))
					return true;
		return false;
	}
	
	//return true if point is within the bounds of the ship
	public boolean contains(int x, int y)
	{
		if(x >= this.x && x <= this.x+xSize-1 && y >= this.y && y <= this.y+ySize-1)
			return true;
		return false;
	}
	
	public void rotate()
	{
		int xSave = xSize;
		xSize = ySize;
		ySize = xSave;
	}
	
	public String toString()
	{
		return "ship("+x+" - "+(x+xSize-1)+" , "+y+" - "+(y+ySize-1)+")";
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Ship)
		{
			Ship objShip = (Ship)obj;
			if(objShip.x == this.x && objShip.y == this.y && objShip.xSize == this.xSize && objShip.ySize == this.ySize)
				return true;
		}
		return false;
	}
}
