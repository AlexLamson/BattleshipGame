import java.util.ArrayList;
import java.awt.Point;

public class BattleShipAI
{
	public static boolean destroyingFoundShip = false;		//if false, shoot randomly; if true, destroy ship based on lastNewShip
	public static boolean justDestroyedAShip = false;		//true if AI destroyed a ship last turn
	
	public static Point lastNewShip = new Point(0, 0);		//last hit that hit while destroyingFoundShip was false
	public static Point expansionVector = new Point(1, 0);	//direction that you fire in, in relation to destroyingFoundShip
	public static int timesToExpand = 1;					//times to add the expansionVector to the lastNewShip
	
	public static ArrayList<Point> bombLocs = new ArrayList<Point>();	//locations of the previous shots, in chronological order
	public static ArrayList<Point> hitLocs = new ArrayList<Point>();	//locations of the hit shots, in chronological order
	public static ArrayList<Point> missLocs = new ArrayList<Point>();	//locations of the missed shots, in chronological order
	public static ArrayList<Ship> sunkShips = new ArrayList<Ship>();	//ships that have been sunk, in chronological order
	
	public static ArrayList<ArrayList<Ship>> allCombos = new ArrayList<ArrayList<Ship>>();
	
	public static boolean debug = true;
	public static boolean printScores = false;
	
	/*
	Bugs:
	
	To do:
	
	-maybe add a 0 player mode where the AI fights itself?
		in order to do this, the AI need to be an object, not totally static
	-if the AI hits a ship, fires in 2 directions, hits, but doesn't sink, shift the lastNewShip up, and if that doesn't work, down
	-(how?) games end faster when the human's ships are in the middle of the sea - compensate for this to make it faster when they are more spread out
	-improve scoring: find every possible combination of the remaining ships, and add 1 to the score of each tile that intersects a theoretical ship
		to do this, have a recursive method that takes a ship size, and (PROBLEM: MULTIPLE SHIPS OF SAME SIZE)
	-organzie the methods in the AI class (make it a little easier to find things without searching)
	
	*/
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~methods that give info to the ai~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	//this method is called if the ai sunk a ship
	public static void sunkShip(Ship ship)
	{
		
		System.out.println("AI - sunk a ship");
		justDestroyedAShip = true;
		// destroyingFoundShip = false;
		sunkShips.add(ship);
	}
	
	//this method is called if the ai bombed a ship
	public static void hit(int x, int y)
	{
		System.out.println("AI - hit!");
		if(!destroyingFoundShip)
		{
			System.out.println("AI - found a ship!");
			destroyingFoundShip = true;
			lastNewShip = new Point(x, y);
		}	
		hitLocs.add(new Point(x , y));
	}
	
	//this method is called if the ai bombed the ocean
	public static void miss(int x, int y)
	{
		System.out.println("AI - miss!");
		missLocs.add(new Point(x , y));
	}
	
	public static void reset()
	{
		destroyingFoundShip = false;
		justDestroyedAShip = false;
		
		lastNewShip = new Point(0, 0);
		expansionVector = new Point(1, 0);
		timesToExpand = 1;
		
		bombLocs = new ArrayList<Point>();
		hitLocs = new ArrayList<Point>();
		missLocs = new ArrayList<Point>();
		sunkShips = new ArrayList<Ship>();
	
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~methods that give info to the game~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	//return the point that the algorithm wants to bomb
	public static int[] takeShot(int[][] enemySea)
	{
		int xPos = 0, yPos = 0;			//the location to bomb
		
		if(justDestroyedAShip)
		{
			if(timesToExpand >= sunkShips.get(sunkShips.size()-1).getSize())
			{
				if(debug)
					System.out.println("AI - There was another ship back there!");
				timesToExpand = 1;			//reset times to expand
				expansionVector.x *= -1;	//flip expansion vector
				expansionVector.y *= -1;
			}
			else
			{
				destroyingFoundShip = false;
				justDestroyedAShip = false;
				resetExpansionVector();
				timesToExpand = 1;
			}
		}
		else
		{
			timesToExpand = 1;
		}
		
		// System.out.println("expansion: ("+expansionVector.x+", "+expansionVector.y+
		// 	") * "+timesToExpand+" from ("+(lastNewShip.x+1)+", "+((char)(lastNewShip.y+'A'))+
		// 	"), destroyingFoundShip: "+destroyingFoundShip+", justDestroyedAShip: "+justDestroyedAShip);
		
		if(destroyingFoundShip)
		{
			//shoot randomly (placeholder)
			// Point randomTile = randomOceanTile(enemySea);
			// xPos = randomTile.x;
			// yPos = randomTile.y;
			
			int timesLooped = 0;
			while(true && timesLooped < 100)
			{
				timesLooped++;
				
				//expand out from the discovered ship
				xPos = lastNewShip.x;
				yPos = lastNewShip.y;
				for(int i = 0; i < timesToExpand; i++)
				{
					xPos += expansionVector.getX();
					yPos += expansionVector.getY();
				}
				
				// if(isTile(xPos, yPos, enemySea))
				// 	System.out.println("checking ("+xPos+" , "+yPos+") with value "+enemySea[xPos][yPos]);
				
				//decide what to do based on the tile
				if(!isTile(xPos, yPos, enemySea))
				{
					if(debug)
						System.out.println("AI - resetting expansion and rotating 1 time");
					
					timesToExpand = 1;
					rotateExpansionVector();	//rotate once and repeat
				}
				else if(enemySea[xPos][yPos] == Sea.OCEAN)
				{
					if(debug)
						System.out.println("AI - firing at ("+xPos+" , "+yPos+")");
					break;	//shoot here
				}
				else if(enemySea[xPos][yPos] == Sea.HIT)
				{
					timesToExpand++;			//expand again
					if(debug)
						System.out.println("AI - increasing expansion to "+timesToExpand);
				}
				else if(enemySea[xPos][yPos] == Sea.MISS && timesToExpand == 1)
				{
					if(debug)
						System.out.println("AI - rotating 1 time");
					rotateExpansionVector();
				}
				else if(enemySea[xPos][yPos] == Sea.MISS && timesToExpand > 1)
				{
					if(debug)
						System.out.println("AI - resetting expansion and flipping expansion vector");
					timesToExpand = 1;			//reset times to expand
					expansionVector.x *= -1;	//flip expansion vector
					expansionVector.y *= -1;
				}
			}
			
			if(timesLooped == 100)
			{
				if(debug)
					System.err.println("AI - looped through too many times");
				destroyingFoundShip = false;
				//shoot randomly (placeholder)
				 Point randomTile = randomOceanTile(enemySea);
				 xPos = randomTile.x;
				 yPos = randomTile.y;
			}
			
		}
		else
		{
			//create the scored sea and print it
			int[][] scoredSea = getScoredSea(enemySea);
			if(printScores)
				System.out.println();
			for(int y = 0; y < scoredSea[0].length; y++)
			{
				for(int x = 0; x < scoredSea.length; x++)
				{
					String extraSpace = "";
					if((scoredSea[x][y]+"").length() == 1)
						extraSpace = "  ";
					if((scoredSea[x][y]+"").length() == 2)
						extraSpace = " ";
					if(printScores)
						System.out.print(scoredSea[x][y]+extraSpace+" ");
				}
				if(printScores)
					System.out.println();
			}
			if(printScores)
				System.out.println();
			
			
			//get the highest score of all tiles
			int bestTile = 0;
			for(int y = 0; y < scoredSea[0].length; y++)
				for(int x = 0; x < scoredSea.length; x++)
					if(scoredSea[x][y] > bestTile)
						bestTile = scoredSea[x][y];
			
			
			//create arraylist of points for each tile with highest score
			ArrayList<Point> bestTiles = new ArrayList<Point>();
			for(int y = 0; y < scoredSea[0].length; y++)
			{
				for(int x = 0; x < scoredSea.length; x++)
				{
					if(scoredSea[x][y] == bestTile)
						bestTiles.add(new Point(x, y));
				}
			}
			
			//pick a random tile out of the best-scoring tiles
			int randomPos = (int)(Math.random()*bestTiles.size());
			Point randomTile = bestTiles.get(randomPos);
			
			if(debug)
				System.out.println("AI - randomly shooting");
			// Point randomTile = randomOceanTile(enemySea);
			xPos = randomTile.x;
			yPos = randomTile.y;
		}
		
		if(!isTile(xPos, yPos, enemySea))
		{
			System.err.println("AI - suggested invalid position ("+xPos+" , "+yPos+")");
		}
		
		// timesToExpand = 1;
		// resetExpansionVector();
		// justDestroyedAShip = false;
		
		bombLocs.add(new Point(xPos, yPos));
		return new int[]{xPos , yPos};
	}
	
	//place ships randomly
	public static ArrayList<Ship> getShips(int[][] friendlySea)
	{
		Sea testSea = new Sea(friendlySea.length, friendlySea[0].length);
		testSea.reset();
		return testSea.fleet;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~methods within this class~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	//return the length of the longest ship (this method is so I can scale more easily)
	public static int getLongestShip()
	{
		int largestSize = 0;
		for(int i = 0; i < BattleShipGame.shipSizes.length; i++)
		{
			if(BattleShipGame.shipSizes[i] > largestSize)
				largestSize = BattleShipGame.shipSizes[i];
		}
		return largestSize;
	}
	
	public static int[][] getScoredSea(int[][] sea)
	{
		int[][] scoredSea = new int[sea.length][sea[0].length];
		for(int y = 0; y < sea[0].length; y++)
			for(int x = 0; x < sea.length; x++)
				// scoredSea[x][y] = getScore(x, y, sea, 2);
				scoredSea[x][y] = getScore(x, y, sea, getSmallestShipNotDestroyed());
		
		for(int y = 0; y < scoredSea[0].length; y++)
		{
			for(int x = 0; x < scoredSea.length; x++)
			{
				int score = scoredSea[x][y];
				if(isMiss(x-1, y, scoredSea) && isMiss(x+1, y, scoredSea) && isMiss(x, y-1, scoredSea) && isMiss(x, y+1, scoredSea))
					score = 0;
				else
				{
					if(isMiss(x-1, y, scoredSea))
						scoredSea[x][y]--;
					if(isMiss(x+1, y, scoredSea))
						score--;
					if(isMiss(x, y-1, scoredSea))
						score--;
					if(isMiss(x, y+1, scoredSea))
						score--;
				}
				scoredSea[x][y] = score;
			}
		}
		
		return scoredSea;
	}
	
	public static int getScore(int xPos, int yPos, int[][] sea, int range)
	{
		if(!isTile(xPos, yPos, sea))
			return 0;
		if(isBombed(xPos, yPos, sea))
			return 0;
		
		int score = 0;
		for(int y = yPos-range; y < sea[0].length && y <= yPos+range; y++)
		{
			for(int x = xPos-range; x < sea.length && x <= xPos+range; x++)
			{
				if(isTile(x, y, sea) && sea[x][y] == Sea.OCEAN)
				{
					score++;
				}
				
				// if(isOcean(x-1, y, sea) && isOcean(x+1, y, sea) && isOcean(x, y-1, sea) && isOcean(x, y+1, sea))
				// {
				// 	score++;
				// 	if(isOcean(x-2, y, sea) && isOcean(x+2, y, sea) && isOcean(x, y-2, sea) && isOcean(x, y+2, sea))
				// 	{
				// 		score++;
				// 	}
				// }
			}
		}
		
		return score;
	}
	
	public static boolean isZero(int x, int y, int[][] sea)
	{
		return (isTile(x, y, sea) && sea[x][y] == 0);
	}
	
	public static boolean isBombed(int x, int y, int[][] sea)
	{
		return (isHit(x, y, sea) || isMiss(x, y, sea));
	}
	
	public static boolean isMiss(int x, int y, int[][] sea)
	{
		return (isTile(x, y, sea) && sea[x][y] == Sea.MISS);
	}
	
	public static boolean isHit(int x, int y, int[][] sea)
	{
		return (isTile(x, y, sea) && sea[x][y] == Sea.HIT);
	}
	
	public static boolean isOcean(int x, int y, int[][] sea)
	{
		return (isTile(x, y, sea) && sea[x][y] == Sea.OCEAN);
	}
	
	//return true if the tile (x, y) is within the bounds of the sea
	public static boolean isTile(int x, int y, int[][] sea)
	{
		return (x >= 0 && x < sea.length && y >= 0 && y < sea[0].length);
	}
	
	public static int getSmallestShipNotDestroyed()
	{
		ArrayList<Integer> shipSizesLeft = new ArrayList<Integer>();
		
		int initialSize = 0;
		for(int size : BattleShipGame.shipSizes)
			shipSizesLeft.add(new Integer(size));
		
		for(Ship ship : sunkShips)
			shipSizesLeft.remove(new Integer(ship.getSize()));
		
		int smallest = 10000;
		for(Integer in : shipSizesLeft)
			if(in.intValue() < smallest)
				smallest = in.intValue();
		
		return smallest;
	}
	
	public static Point randomOceanTile(int[][] sea)
	{
		ArrayList<Point> possibleTiles = new ArrayList<Point>();
		
		//add the sea
		for(int y = 0; y < sea[0].length; y++)
			for(int x = 0; x < sea.length; x++)
				possibleTiles.add(new Point(x, y));
		
		//remove places that have already been shot
		for(Point p : bombLocs)
			possibleTiles.remove(p);
		
		return possibleTiles.get( (int)(Math.random()*possibleTiles.size()) );	//return a random one
	}
	
	//rotate expansionVector clockwise
	public static void rotateExpansionVector()
	{
		int eX = expansionVector.x, eY = expansionVector.y;
		
		if(eX == 1 && eY == 0)
		{
			eX = 0;
			eY = 1;
		}
		else if(eX == 0 && eY == 1)
		{
			eX = -1;
			eY = 0;
		}
		else if(eX == -1 && eY == 0)
		{
			eX = 0;
			eY = -1;
		}
		else if(eX == 0 && eY == -1)
		{
			eX = 1;
			eY = 0;
		}
		
		expansionVector = new Point(eX, eY);
	}
	
	public static void resetExpansionVector()
	{
		expansionVector = new Point(1, 0);
	}
	
	
}

/*
http://stackoverflow.com/questions/1631414/what-is-the-best-battleship-ai

Strategies to attempt:

- keep track of all possible positions for ships that have >0 hits. The list never gets bigger than ~30K so it can be kept exactly,
unlike the list of all possible positions for all ships (which is very large).

- The GetShot algorithm has two parts, one which generates random shots and the other which tries to finish sinking an already hit ship.
We do random shots if there is a possible position (from the list above) in which all hit ships are sunk.
Otherwise, we try to finish sinking a ship by picking a location to shoot at which eliminates the most possible positions (weighted).

- For random shots, compute best location to shoot based on the likelihood of one of the unsunk ships overlapping the location.

- adaptive algorithm which places ships in locations where the opponent is statistically less likely to shoot.

- adaptive algorithm which prefers to shoot at locations where the opponent is statistically more likely to place his ships.

- place ships mostly not touching each other.
 */