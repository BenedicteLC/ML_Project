package RLAgent;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class RLAgent extends BasicMarioAIAgent implements Agent{
	
	int trueJumpCounter = 0;
	int trueSpeedCounter = 0;
	
	public RLAgent()
	{
	    super("RLAgent");
	    reset();
	}
	
	public void reset()
	{
	    action = new boolean[Environment.numberOfKeys];	   
	    trueJumpCounter = 0;
	    trueSpeedCounter = 0;
	}
	
	// We won't need this once we have RL.
	private boolean DangerOfAny()
	{

        if ((getReceptiveFieldCellValue(marioEgoRow + 2, marioEgoCol + 1) == 0 &&
            getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) == 0) ||
            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 0 ||
            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 2) != 0 ||
            getEnemiesCellValue(marioEgoRow, marioEgoCol + 1) != 0 ||
            getEnemiesCellValue(marioEgoRow, marioEgoCol + 2) != 0)
            return true;
        else
            return false;
	}
	
	public boolean[] getAction()
	{
	    // this Agent requires observation integrated in advance.
		action[Mario.KEY_SPEED] = true;
		action[Mario.KEY_RIGHT] = true;

	    if (DangerOfAny() && getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 1)  // a coin
	    {
	        if (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
	        {
	            action[Mario.KEY_JUMP] = true;
	        }
	        ++trueJumpCounter;
	    }
	    else
	    {
	        action[Mario.KEY_JUMP] = false;
	        trueJumpCounter = 0;
	    }

	    if (trueJumpCounter > 16)
	    {
	        trueJumpCounter = 0;
	        action[Mario.KEY_JUMP] = false;
	    }
	    
	    return action;
	}
}

