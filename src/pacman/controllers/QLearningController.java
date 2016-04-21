package pacman.controllers;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pacman.controllers.BFS_Controller.ghosts;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;


//This is a Qlearning controller that learns the benefit of transitions between
//states as it goes. 
//One issue I found with the pacman problem was that the shear number of states
//is fairly prohibitive.
public class QLearningController extends Controller<MOVE> {
    
    //Store our state changes and corresponding Qvalues in a map
    Map<String,Double> values;
    
    //learning parameters
    private double epsilon = 0.05; // exploration rate
    private double gamma = 0.8; //discount rate
    private double alpha = 0.2; //learning rate
    
    String datapath; //path to the text file where we store training data 
    private boolean training; //are we training right now
    
    Random rnd; //random used for exploration
    
    public QLearningController(String pathToTrainingData){
        this.training = training;
        this.rnd=new Random(System.currentTimeMillis());
        this.values = new HashMap<String, Double>();
        
        this.datapath = pathToTrainingData;
        //Read in the training data
        try 
        {
            FileInputStream fis = new FileInputStream(datapath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.values = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } 
        catch (IOException e)
        {
            System.out.println("Could not read data!");	
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(QLearningController.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    //store the hashmap to a text file
    public void writeoutdata(){
        try 
        {
            FileOutputStream fos=new FileOutputStream(this.datapath,false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.values);
            oos.close();
            fos.close();
        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }
    
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
        
        MOVE move = getAction(game);
        Game gameCopy = game.copy();
        gameCopy.advanceGame(move, ghosts.getMove(gameCopy, 0));
        update(game, move, computeReward(game, move));
        return move;
        
           
    }
    
    private double getQValue(Game game, MOVE move){
        String state = game.getQStateGameState();
        if (this.values.get(state + "," + move) == null){
            return 0;
        }
        return values.get(state + "," + move);
    }
    
    //return the optimal qvalue from a state
    private double computeValueFromQValues(Game game){
        return getQValue(game, computeActionFromQValues(game));
    }
    
    //return the movement that provides the best q score
    private MOVE computeActionFromQValues(Game game){
        double bestQ = Integer.MIN_VALUE;
        int pacmanCurrentNodeIndex = game.getPacmanCurrentNodeIndex();
        MOVE[] possibleMoves = game.getPossibleMoves(pacmanCurrentNodeIndex);
        MOVE bestMove = possibleMoves[0];
        for(MOVE m:possibleMoves){
            double qValue = getQValue(game,m);
//            System.out.print(m + ":" + qValue + ", ");
            if (qValue > bestQ){
                bestQ = qValue;
                bestMove = m;
            }
        }
//        System.out.println(" Going to go: " + bestMove);
        return bestMove;
    }
    
    
    // decide where to go next (note, this is while training, so we will 
    // occasionally throw in a random "exploration" movement.
    private MOVE getAction(Game game){
        MOVE move;
        if(rnd.nextDouble()<this.epsilon){
            move = MOVE.values()[rnd.nextInt(MOVE.values().length)];
        }
        else{
            move = computeActionFromQValues(game);
        }
        return move;
    }
    
    //update the values hashmap for the move between game and the next State
    private void update(Game game, MOVE move, double reward){
        String key = game.getQStateGameState()+","+move;
        Game nextState = game.copy();
        nextState.advanceGame(move, ghosts.getMove(nextState, 0));
        
        //qvalue is equal to:
        // ((1- learning rate) * old value) 
        // + learning rate * (Reward + discount*optimal future reward)
        double qvalue = (1 - this.alpha) * getQValue(game, move) + this.alpha * (reward + this.gamma * computeValueFromQValues(nextState));
        this.values.put(key, qvalue);
    }
    
    
    //compute the reward between states.  I just use difference in score, or 
    // - 100 for getting eaten.
    double computeReward(Game game, MOVE move){
        if(game.wasPacManEaten()){
            return -100;
        }
        double initialscore = game.getScore();
        Game gameCopy = game.copy();
        gameCopy.advanceGame(move, ghosts.getMove(gameCopy,0));
        return gameCopy.getScore() - initialscore;
    }
    
    
    
    
//    private int getClosestFoodNode(Game game){
//        int pacmanLocation = game.getPacmanCurrentNodeIndex();
//        int[] allFoodIndices = game.getActivePillsIndices();
//        int closestFood = game.getClosestNodeIndexFromNodeIndex(pacmanLocation, allFoodIndices, DM.PATH);
//        return closestFood;
//    }
//    
//    private int getClosestGhostNode(Game game){
//        int pacmanLocation = game.getPacmanCurrentNodeIndex();
//        int[] allGhostIndices = getGhostsIndices(game);
//        int closestGhost =  game.getClosestNodeIndexFromNodeIndex(pacmanLocation, allGhostIndices, DM.PATH);
//        return closestGhost;
//    }
//    private int getDistanceToClosestFood(Game game){
//        int pacmanLocation = game.getPacmanCurrentNodeIndex();
//        int[] allFoodIndices = game.getActivePillsIndices();
//        int closestFood = game.getClosestNodeIndexFromNodeIndex(pacmanLocation, allFoodIndices, DM.PATH);
//        return game.getShortestPathDistance(pacmanLocation, closestFood);
//    }
//    
//    private int getDistanceToClosestGhost(Game game){
//        int pacmanLocation = game.getPacmanCurrentNodeIndex();
//        int[] allGhostIndices = getGhostsIndices(game);
//        if(allGhostIndices.length == 0){
//            return Integer.MAX_VALUE;
//        }
//        int closestGhost =  game.getClosestNodeIndexFromNodeIndex(pacmanLocation, allGhostIndices, DM.PATH);
//        return game.getShortestPathDistance(pacmanLocation, closestGhost);
//    }
//    
//    private int[] getGhostsIndices(Game game){
//        ArrayList<Integer> ghostArrayList = new ArrayList<Integer>();
//        for( GHOST ghost:GHOST.values()){
//            //add the ghost to the arraylist if it is not edible and not in lair
//            if((!game.isGhostEdible(ghost)) && (game.getGhostLairTime(ghost) == 0)){
//                 ghostArrayList.add(game.getGhostCurrentNodeIndex(ghost));
//            }
//        }
//        int[] ghostIndices = new int[ghostArrayList.size()];
//        for(int i = 0; i < ghostIndices.length; i ++){
//            ghostIndices[i] = ghostArrayList.get(i).intValue();
//        }
//        return ghostIndices;
//    }
//    
//    private boolean decideToFightOrFlee(Game game){
//        if (getDistanceToClosestGhost(game) > 15){
//            return true;
//        }
//        else{
//            return false;
//        }
//    }
//    
//    private MOVE getMoveResponse(Game game, boolean fightOrFlight){
//        int pacmanLocation = game.getPacmanCurrentNodeIndex();
//        if (fightOrFlight == true){
//            return game.getNextMoveTowardsTarget(pacmanLocation, getClosestFoodNode(game), DM.PATH);
//        }
//        else{
//            return game.getNextMoveAwayFromTarget(pacmanLocation, getClosestGhostNode(game), DM.PATH);
//        }
//    }
//    
//    //given an arraylist of moves, what is it's net score
//    private int fitnessScore(Game game, ArrayList<MOVE> moveList){
//        Game gameCopy = game.copy();
//        int initialscore = game.getScore();
//        for(MOVE move : moveList){
//            gameCopy.advanceGame(move, ghosts.getMove(gameCopy, 0));
//        }
//        int finalScore =gameCopy.getScore();
//        return (game.wasPacManEaten()) ? -1 : finalScore;
//    }
}