package RLAgent;

import java.io.IOException;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.*;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;

public class Simulator {

	public static void main(String[] args)
	{
		//final String argsString = "-vis on"; // -vis: show display
	    final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
	    final Agent agent = new RLAgent();
	    final BasicTask basicTask = new BasicTask(marioAIOptions);
	    
	    // Run all 10 difficulty levels.
	    for (int i = 0; i < 10; ++i)
	    {
	        int seed = 0;
	        do
	        {
	            marioAIOptions.setLevelDifficulty(i);
	            marioAIOptions.setLevelRandSeed(seed++);
	            marioAIOptions.setAgent(agent);
	            basicTask.setOptionsAndReset(marioAIOptions);
	            
	            // Run the stage. This is where the agent gets the state
	            // and performs an action.
	            basicTask.runSingleEpisode(1);
	            
	            System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
	            Environment environment = basicTask.getEnvironment();	            
	        } while (basicTask.getEnvironment().getEvaluationInfo().marioStatus != Environment.MARIO_STATUS_WIN);
	    }
	    
	    Runtime rt = Runtime.getRuntime();
	    try
	    {
	    	//Process proc = rt.exec("/usr/local/bin/mate " + marioTraceFileName);
	        Process proc = rt.exec("python hello.py");
	    } catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    System.exit(0);

	}
}
