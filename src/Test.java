import java.awt.Point;

public class Test
{
	public static int[][] sea = new int[10][10];
	
	public static int num = 0;
	
	public static void main(String[] args)
	{
		fillScanShots();
		for(int y = 0; y < sea[0].length; y++)
		{
			for(int x = 0; x < sea.length; x++)
			{
				String extraSpaces = "";
				if((sea[x][y]+"").length() == 1)
					extraSpaces = " ";
				System.out.print(sea[x][y]+extraSpaces+" ");
			}
			System.out.println();
		}
	}
	
	public static void fillScanShots()
    {
        int x, y;
        int num = 10 * 10;
        for (int j = 0; j < 3; j++)
        {
            for (int i = j; i < num; i += 3)
            {
                x = i % 10;
                y = i / 10;
                enqueue(new Point(x, y));
            }
        }
    }
    
    public static void enqueue(Point p)
    {
    	sea[p.x][p.y] = num;
    	num++;
    	// System.out.println(p.x+", "+p.y);
    }
}
