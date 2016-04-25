/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import static pacman.controllers.BFS_Controller.ghosts;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 *
 * @author Kenny
 */

//Note, while the algorithm used is sound, it does not work particularly for
//this application since it can not check whether or not the moves stored in a
//solution are possible to make (pacman could wind up running into a wall)
public class Kenny_Genetic_Controller  extends Controller<MOVE>{
        
    //my solution class, it stores a move path and the score it results in.
    private class Solution {
        MOVE[] movepath;
        int score;
        public Solution(MOVE[] movepath) {
            this.movepath = movepath;
            this.score = solutionScore(movepath);
        }
        @Override
        public String toString(){
            String s = "";
            s = s + score + ": ";
            for (MOVE m: movepath){
                switch(m){
                    case LEFT:
                        s = s + "L";
                        break;
                    case RIGHT:
                        s = s + "R";
                        break;
                    case UP:
                        s = s + "U";
                        break;
                    case DOWN:
                        s = s + "D";
                        break;
                }
            }
            return s;
        }
    }

    //we need a comparator for the solutions
    private class SolutionScoreComparator implements Comparator<Solution>
    {
        @Override
        public int compare(Solution o1, Solution o2) {
            if(o1.score > o2.score){
                return -1;
            }
            if(o1.score < o2.score){
                return 1;
            }
            return 0;
        }
    }
    
    private Random rnd = new Random(System.currentTimeMillis());
    
    //how many solutions to store per generation
    private int numberOfSolutions = 20;
    
    //how many solutions to keep between generations and breed
    private int bestFitPerGen = 5;
    
    //the solution depth (how many moves to store in the array)
    private int solutionDepth = 50;
    
    //game instance data for this move
    private Game gameinstance;
    private long timeDue;
    private MOVE[] possibleMoves;
    
    //a priority queue to store solutions in
    Comparator<Solution> comparator = new SolutionScoreComparator();
    PriorityQueue<Solution> solutionPopulation = new PriorityQueue<Solution>(numberOfSolutions, comparator);
    
    @Override
    public MOVE getMove(Game game, long timeDue) {
        this.gameinstance = game.copy();
        this.timeDue = timeDue;
        this.possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        
        //create the initial generation
        initializeSolutions();
        int generationNumber = 0;
        
        //while we still have time left
        while(timeDue - System.currentTimeMillis() > 5){
            generationNumber++;
            
            //save a temp copy of the best solutions of the generation
            ArrayList<Solution> bestfit = new ArrayList<Solution>();
            for(int i = 0; i < bestFitPerGen; i ++)
            {
                bestfit.add(solutionPopulation.poll());
            }
            
            
            //create a new population for the next generation
            solutionPopulation = new PriorityQueue<Solution>(numberOfSolutions, comparator);
            
            //put the best fit items from the previous gen into the current gen
            for (int i = 0; i < bestFitPerGen; i ++){
                solutionPopulation.add(bestfit.get(i));
            }
            
            //while the current generation isn't full, breed the bestfit items
            //and add the children to the current generation
            int i = 0;
            int j = 1;
            while(solutionPopulation.size() < numberOfSolutions){
                if(i > bestfit.size()-1)
                    i = 0;
                if (j > bestfit.size()-1)
                    j = 0;
                solutionPopulation.add(mateSolutions(bestfit.get(i), bestfit.get(j)));
                i++;
                j++;
            }
        }
        
        //now that we've run out of time, return the best solution from the last gen
//        System.out.println("solution after " + generationNumber + " generations: ");
//        System.out.println(solutionPopulation.peek());
        paintSolution(solutionPopulation.peek(), Color.RED);
        return solutionPopulation.poll().movepath[0];
    }
    
    //a method to generate a new solution based on 2 solutions
    private Solution mateSolutions(Solution sol1, Solution sol2){
        MOVE[] newpath = new MOVE[solutionDepth];
        for(int i = 0; i < solutionDepth; i ++){
            if(rnd.nextInt(2) == 0){
                newpath[i] = sol1.movepath[i];
            }
            else{
                newpath[i] = sol2.movepath[i];
            }
        }
        Solution childsol = new Solution(newpath);
        return childsol;
    }
    
    //debug method to paint a solution on the maze
    private void paintSolution(Solution sol, Color color){
        Game gameCopy = this.gameinstance.copy();
        for (int i = 0; i < solutionDepth; i ++){
            gameCopy.advanceGame(sol.movepath[i], ghosts.getMove(gameCopy, timeDue));
            GameView.addPoints(gameinstance, color, gameCopy.getPacmanCurrentNodeIndex());
        }
    }
    
    //initialize the population of solutions with randomly generated moves
    private void initializeSolutions(){
        solutionPopulation.clear();
        for (int i = 0; i < numberOfSolutions; i ++){
            MOVE[] movepath = new MOVE[solutionDepth];
            
            //the first move is selected from an array of valid moves given the game state
            movepath[0] = this.possibleMoves[rnd.nextInt(this.possibleMoves.length)];
            
            //we cannot select subsequent moves the list of possible moves since
            //when we breed the solutions it is possible for them to become invalid
            for (int j = 1; j < solutionDepth; j ++){
                movepath[j] = MOVE.values()[(rnd.nextInt(MOVE.values().length))];
            }
            Solution newsol = new Solution(movepath);
            solutionPopulation.add(newsol);
        }
    }
    
    //A method that returns the resulting score of following an array of moves (0 if eaten)
    private int solutionScore(MOVE[] solution){
        Game gameCopy = this.gameinstance.copy();
        for (int i = 0; i < solutionDepth; i ++){
            gameCopy.advanceGame(solution[i], ghosts.getMove(gameCopy, this.timeDue));
            if(gameCopy.wasPacManEaten()){
                return 0;
            }
        }
        return gameCopy.getScore();
    }
}
