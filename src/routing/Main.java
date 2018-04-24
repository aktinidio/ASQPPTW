package routing;

public class Main {

	public static void main(String[] args) {
		
		//long stratTime = System.currentTimeMillis();
		(new Controller(1)).exectue();
		for (int i = 1; i <= 7; i++)
			(new Controller(i)).exectue();
		//System.out.println("Execution time: " + (System.currentTimeMillis() - stratTime));
	}
} 