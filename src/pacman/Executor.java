package pacman;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import pacman.controllers.BFS_Controller;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.Kenny_DFS_Controller;
import pacman.controllers.Kenny_AStar_Controller;
import pacman.controllers.Kenny_Genetic_Controller;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.Kenny_QLearning_Controller;
import pacman.controllers.Kenny_NeuralNetwork_Controller;
import pacman.controllers.NeuralNetwork;
import pacman.controllers.NeuralNetwork.NeuralNetworkComparator;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.NearestPillPacManVS;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Game;
import pacman.game.GameView;

import static pacman.game.Constants.*;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Executor
{	
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
    static int connectionUid = 0;
    static int nodeUid = 0;
    static int networkUid = 0;
    public static void main(String[] args)
    {
        Executor exec=new Executor();

        boolean visual=true;
        
        String algoToRun = "NeuralNetwork";
        
        
        switch(algoToRun){
            case "DFS":
                exec.runGameTimed(new Kenny_DFS_Controller(), new StarterGhosts(),visual);
    //            exec.runExperiment(new Kenny_DFS_Controller(), new StarterGhosts(), 10);
                break;

            case "AStar":
                exec.runGameTimed(new Kenny_AStar_Controller(), new StarterGhosts(),visual);
    //            exec.runExperiment(new Kenny_AStar_Controller(), new StarterGhosts(), 10);            
                break;

            case "Genetic":
                exec.runGameTimed(new Kenny_Genetic_Controller(), new StarterGhosts(),visual);
    //            exec.runExperiment(new Kenny_Genetic_Controller(), new StarterGhosts(), 10);  

                break;

            case "QLearning":

                //NOTE: for qlearning to work, the path needs to be updated, approx 100 MB of training data will be created
                String pathToData= "C:/Users/Kenny/Desktop/AI/trainingData.txt";
                     Kenny_QLearning_Controller qLearningController = new Kenny_QLearning_Controller(pathToData);

                    for(int i = 0; i < 100; i++){
                        exec.runExperiment(qLearningController, new StarterGhosts(), 100);
                        System.out.println("trial # " + i*100);
                        qLearningController.writeoutdata();
                    }

                    exec.runExperiment(qLearningController, new StarterGhosts(), 10);
                    exec.runGameTimed(qLearningController,new StarterGhosts(),visual);
                    qLearningController.writeoutdata();

                break;

            case "NeuralNetwork":
                Comparator<NeuralNetwork> comparator = new NeuralNetwork.NeuralNetworkComparator();
                TreeSet<NeuralNetwork> population =    new TreeSet<NeuralNetwork>(comparator);
                NeuralNetwork neuralNetwork = new NeuralNetwork();
                for (int i = 0; i < 10; i++){
                    NeuralNetwork nn = new NeuralNetwork(neuralNetwork);
                    nn.randomMutation(0.2, 0, 1);
                    System.out.println("adding a new item " + population.add(nn));
                }
                System.out.println("sorted set size: " + population.size());


                for(int i = 0; i < 500; i ++)
                {
                    System.out.println("best fitness: " + population.last().getFitness());
                    NeuralNetwork worstNet = population.pollFirst();
                    System.out.println("on test: " + i + ".  Testing node number: "+ worstNet.getUid());
                    System.out.println(worstNet);
                    double fitness = exec.runExperiment(new Kenny_NeuralNetwork_Controller(worstNet), new StarterGhosts(), 3);
                    System.out.println("result fitness: " + fitness);
                    worstNet.setFitness(fitness);
                    population.add(worstNet);//might not be worst anymore
                    worstNet = population.pollFirst();
                    System.out.println("removing weak nn number " + worstNet.getUid() + "  with fitness " + worstNet.getFitness());
                    NeuralNetwork newNet = new NeuralNetwork(population.last());
                    newNet.randomMutation(0.1, 0.1, 0.5);
                    population.add(newNet);

                }
                System.out.println("best neuralnetwork score: " + population.last().getFitness());
                System.out.println(population.last());
                break;

            default:
                System.out.println("set algoToRun to one of the following [DFS, AStar, Genetic, QLearning, NeuralNetwork]");
        }

    }
        
    
    //These are used to evolve the neural networks and need to be global
    public static int getNextConnectionUid(){
        connectionUid ++;
        return connectionUid;
    }
    
    public static int getNextNodeUid(){
        nodeUid ++;
        return nodeUid;
    }
    public static int getNextNetworkUid(){
        networkUid ++;
        return networkUid;
    }
   
        
        
        
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public double runExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	double avgScore=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
                        if(i % 100 == 0){
//                            System.out.println("trial # " + i);
                        }
			game=new Game(rnd.nextLong());
			int numMoves = 0;
			while(!game.gameOver())
			{
//                            numMoves++;
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
                    
			}
//                        System.out.println("numMoves: " + numMoves);
			
			avgScore+=game.getScore();
			System.out.println(i+"\t"+game.getScore());
		}
		
		System.out.println(avgScore/trials);
          return avgScore/trials;
    }
	
    public void test(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,Game game)
    {
        if(!game.gameOver()){
            game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));

            test(pacManController, ghostController, game);
        }
//        System.out.println(game.getScore());
        
    }
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public void runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
	}
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
                if(!pacManController.hasComputed()){
                    System.out.println("We timed out.");
                }

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        	gv.repaint();
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
	
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
 			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

 			try
			{
				int waited=DELAY/INTERVAL_WAIT;
				
				for(int j=0;j<DELAY/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((DELAY/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
 	        if(visual)
 	        	gv.repaint();
 		}
 		
 		pacManController.terminate();
 		ghostController.terminate();
 	}
    
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,String fileName)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}