/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import static pacman.controllers.BFS_Controller.ghosts;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 *
 * @author Kenny
 */

//There is currently a known issue with this implementation where pacman
//will get stuck going left-right-left-right.  This is because his default
//move on ties between score potential is right, meaning if there is a 
//pellet to the left, he will move until it is within his depth search,
//then decide he could move right until it is outside the depth.

public class Kenny_DFS_Controller extends Controller<Constants.MOVE>{

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        
        //keep track of what move has the potential to get the highest score
        MOVE highmove = game.getPacmanLastMoveMade();
        
        //variable to keep tack of the highest potential score
        int highscore = -1;
        
        //for each possible move
        for (MOVE m: game.getPossibleMoves(game.getPacmanCurrentNodeIndex())){
            //create a game gopy
            Game gameCopy = game.copy();
            //advance that copy
            gameCopy.advanceGame(m, ghosts.getMove(gameCopy, timeDue));
            //find out what the highest potential score after an additional 10 moves is
            int dfsScore = dfs_highscore(gameCopy, 10);
            System.out.println("Trying Move: " + m + ", potential score is " + dfsScore);
            //store the highest move in highmove
            if(highscore < dfsScore){
                highscore = dfsScore;
                highmove = m;
            }
        }
        //return the move that will give pacman the highest potential score
        return highmove;
    }
    
    
    //The following class is copied from the Instructor's implementation of BFS
    private class PacManNode 
    {
        Game gameState;
        int depth;
        public PacManNode(Game game, int depth)
        {
            this.gameState = game;
            this.depth = depth;
        }
    }
    
    
    
    //depth first search from a location for a highest score in depth moves
    private int dfs_highscore(Game game, int maxdepth){
        Stack<PacManNode> stack = new Stack<PacManNode>();
        Game gameCopy = game.copy();
        
        //add initial game to the stack
        stack.push(new PacManNode(gameCopy, 0));
        int highscore = -1;
        while(!stack.isEmpty()){
            
            //get top of stack
            PacManNode currentNode = stack.pop();
            
            //check all possible moves
            for (MOVE m: currentNode.gameState.getPossibleMoves(currentNode.gameState.getPacmanCurrentNodeIndex())){
                
                //we only need to update highscore if we are at the max depth
                if (currentNode.depth >= maxdepth){
                    if (currentNode.gameState.getScore()>=highscore)
                        highscore = currentNode.gameState.getScore();
                    
                    //This helps to avoid ghosts
                    if (currentNode.gameState.wasPacManEaten())
                    {
                        highscore = -1;
                        return -1;
                    }
                }
                else{
                    gameCopy = currentNode.gameState.copy();
                    gameCopy.advanceGame(m, ghosts.getMove(gameCopy, 0));
                    
                    //This is used for debugging, it draws green squares where the DFS searches.
                    GameView.addPoints(game, Color.GREEN, gameCopy.getPacmanCurrentNodeIndex());
                    
                    //add the next location node to the stack with an incremented depth
                    PacManNode node = new PacManNode(gameCopy, currentNode.depth+1);
                    stack.push(node);
                }
            }
        }
        return highscore;
              
    }
    
}
