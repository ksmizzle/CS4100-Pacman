/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import java.util.ArrayList;
import java.util.List;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 *
 * @author amy
 */
public class PacManNode 
{
    Game gameState;
    int depth;
    
    public PacManNode(Game game, int depth)
    {
        this.gameState = game;
        this.depth = depth;
    }
}
