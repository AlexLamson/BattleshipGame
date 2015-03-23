import java.applet.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.*;

public class Main extends Applet implements Runnable
{
	private static final long serialVersionUID = 8864158495101925325L;		//because stupid warnings
	
	public static int changeDelaySeconds = 4, gridSize = 10;
	
	public static int pixelSize = 1;	//change the scale the pixels are multiplied by when drawn to
	
	public static int computerSpeed = 10;		//higher number for slower computers
	public static int tickTime = 5;
	public static boolean isRunning = false;
	
	public static String windowName = "BattleShip";

	public static boolean debugMode = true;
	
	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static int screenWidth = (int)screenSize.getWidth();
	public static int screenHeight = (int)screenSize.getHeight();
	public static Dimension realSize;															//size of whole window
	public static Dimension size = new Dimension(screenWidth*9/10, screenWidth*9/10/2);			//drawable area
	// public static Dimension size = new Dimension(800, 400);									//drawable area
	public static Dimension pixel = new Dimension(size.width/pixelSize, size.height/pixelSize);	//"pixels" in drawable area

	public static Point mousePrev = new Point(0, 0);
	public static Point mouseNow = new Point(0, 0);
	
	public static boolean isMouseLeft = false;
	public static boolean isMouseMiddle = false;
	public static boolean isMouseRight = false;

	private Image screen;
	public static JFrame frame;
	public static BattleShipGame game;
	public static boolean readyToSwap = false, swappingNow  = false;
	public static long timerLength, timerPos;
	public static boolean placingShips = true, manualAdding = true, editingMode = false;
	public static boolean gameOver = false, rightWins = false;
	public static boolean isTwoPlayers = true;
	
	public Main()
	{
		setPreferredSize(size);
		requestFocus();
	}

	public static void restart()
	{
		Main main = new Main();
		main.start();
	}

	public void start()
	{
		//define objects
		startGame();
		
		addKeyListener(new Listening());
		addMouseListener(new Listening());
		addMouseMotionListener(new Listening());
		addMouseWheelListener(new Listening());
		
		//start the main loop
		isRunning = true;
		new Thread(this).start();
		requestFocus();
	}

	public void stop()
	{
		isRunning = false;
	}
	
	public static void startGame()
	{
		System.out.println("Game started");
		game = new BattleShipGame(0, 0, pixel.width, pixel.height, gridSize);
		
		if(manualAdding)
		{
			editingMode = true;
			game.editNextShip();
		}
		
		if(debugMode)
			changeDelaySeconds = 1;
		
		timerLength = changeDelaySeconds * (1000/(tickTime*(int)computerSpeed));
		timerPos = timerLength;
	}
	
	public static void reset()
	{
		System.out.println("Game reset");
		BattleShipAI.reset();
		
		game.destroy();
		startGame();
		
		game.mySea.renderEditingShip = true;
		game.editingPos = -1;
		game.editNextShip();
		
		readyToSwap = false;
		swappingNow  = false;
		placingShips = true;
		manualAdding = true;
		editingMode = true;
		gameOver = false;
		rightWins = false;
		editingMode = true;
		
		if(!debugMode)
		{
			int twoPlayers = JOptionPane.showConfirmDialog(null, "Play with 2 players?", "2 Players?", JOptionPane.YES_NO_OPTION);
			if(twoPlayers == 1)			//they said no
				isTwoPlayers = false;
			else
				isTwoPlayers = true;
		}
	}
	
	public void checkGameOver()
	{
		// if((isTwoPlayers && !placingShips) && game.gameFinished())
		if(!placingShips && game.gameFinished())
		{
			System.out.println("Game over");
			
			gameOver = true;
			game.makeRightWin(game.isMyWin());
			
//			if(!debugMode)
//			{
				render();
				String winner = "left side wins";
				if(game.isMyWin())
					winner = "right side wins";
				if(!isTwoPlayers && !game.isMyWin())
					winner = "the computer wins";
				else
					winner = "you win";
				String turns = " in "+game.totalTurns+" turns";
				JOptionPane.showMessageDialog(null, "Game over - "+winner+turns+"!");
//			}
		}
	}
	
	//starts countdown
	public static void swap()
	{
		if(isTwoPlayers)
		{
			readyToSwap = false;
			swappingNow = true;
			game.swapStates();
		}
	}
	
	public static void endTurn()
	{
		game.totalTurns++;
		if(isTwoPlayers)
		{
			swap();
		}
		else
		{
			game.myTurn = true;
			readyToSwap = false;
			int[] bombPoint = BattleShipAI.takeShot(game.mySea.getObfuscatedSea());
			Ship shipSunk = game.mySea.bombTile(bombPoint[0], bombPoint[1]);
			
			int bombedTile = game.mySea.sea[bombPoint[0]][bombPoint[1]];
			if(bombedTile == Sea.HIT)
			{
				BattleShipAI.hit(bombPoint[0], bombPoint[1]);
			}
			else if(bombedTile == Sea.MISS)
			{
				BattleShipAI.miss(bombPoint[0], bombPoint[1]);
			}
			
			if(game.mySea.shipDestroyed(shipSunk) && !shipSunk.equals(Ship.placeholderShip))	//if it sunk a ship
				BattleShipAI.sunkShip(shipSunk);
		}
	}
	
	public void tick()
	{
//		if(frame.getWidth() != realSize.width || frame.getHeight() != realSize.height)
//			frame.pack();
		
		if(!gameOver)
		{
			if(readyToSwap)
			{
				endTurn();
			}
			
			if(swappingNow)
			{
				readyToSwap = false;
				timerPos--;
				if(timerPos == 0)
				{
					swappingNow = false;
					timerPos = timerLength;
				}
			}
			
			checkGameOver();
			
			game.tick();			//tick the game
		}
	}

	public void render()
	{
		Graphics g = screen.getGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, pixel.width, pixel.height);
		
		//show the black screen with countdown
		if(swappingNow)
		{
			int secondsLeft = (int) ((timerPos / (1000/(tickTime*computerSpeed)) + 1));
			String secString = " seconds";
			if(secondsLeft == 1)
				secString = " second";
			String str = secondsLeft + secString;
			
			g.setColor(Color.black);
			g.fillRect(0, 0, pixel.width, pixel.height);
			
			Font fontSave = g.getFont();
			Font font = new Font("Verdana", Font.BOLD, 96);
			FontMetrics fm = g.getFontMetrics(font);
			
			int xText = pixel.width/2;
			int yText = pixel.height/2;
			
			xText += -(fm.stringWidth(str)/2) + 2;
	        yText += fm.getAscent()/2;
	        
	        g.setColor(Color.white);
			g.setFont(font);
			g.drawString(str, xText, yText);
			g.setFont(fontSave);
		}
		else		//render normally
		{
			game.render(g);			//render the game
		}
		
		g = getGraphics();

		g.drawImage(screen, 0, 0, size.width, size.height, 0, 0, pixel.width, pixel.height, null);
		g.dispose();		//throw it away to avoid lag from too many graphics objects
	}

	public void run()
	{
		screen = createVolatileImage(pixel.width, pixel.height);	//actually use the graphics card (less lag)
		
		render();
		
		if(!debugMode)
		{
			int twoPlayers = JOptionPane.showConfirmDialog(null, "Play with 2 players?", "2 Players?", JOptionPane.YES_NO_OPTION);
			if(twoPlayers == 1)			//they said no
				isTwoPlayers = false;
			else
				isTwoPlayers = true;
		}
		else
		{
			isTwoPlayers = false;
		}
		
		if(!debugMode)
		{
			render();
			JOptionPane.showMessageDialog(null, "Look away after ending your turn\n\n" +
					"Normal Controls:\nClick - Bomb square\nSpace - End turn\n\n" +
					"Editing Controls:\nE - Toggle editing mode\n" +
					"R - Random ship arrangement\nArrows - Move ship\n" +
					"Ctrl - Rotate ship\nSpace - Place ship / Finish placing ships");
		}
		
		while(isRunning)
		{
			tick();			//do math and any calculations
			render();		//draw the objects
			
			try
			{
				Thread.sleep(tickTime*(int)computerSpeed);
			}catch(Exception e){ }
		}
	}

	public static void main(String[] args) {
		Main main = new Main();

		frame = new JFrame();
		frame.add(main);
		frame.pack();

		realSize = new Dimension(frame.getWidth(), frame.getHeight());

		frame.setTitle(windowName);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);		//null makes it go to the center
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		main.start();
	}
}