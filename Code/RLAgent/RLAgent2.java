package RLAgent;

import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.AgentsPool;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;


public class RLAgent2 extends BasicMarioAIAgent implements Agent{
	
	private static final Object NN = null;
	int round = 0;
	float reward;
	Random rand = new Random();
	boolean[][] all_actions = new boolean[13][6];
	double epsilon = 0.1;// 0.00001;//
	double epsilon_min =  0.001; // 0.000001;//
	double alpha = 0.0003;//0.0003; // learning rate
	double alpha_min = 0.00001;//0.00001;
	double alpha_decay = alpha-alpha_min;
	double dec = 0.99;//0.99; // Reward decay
	double lambda = 0.3;//0.8; // eligibility traces //0.3 good
	double regL2 = 0.0001; // regularization parameter
	double epsilon_decay = epsilon - epsilon_min;	
	double linear_ep = 1500*7000; // linear decay of epsilon over # of moves -> around 1500 per game
	double linear_ep1 = 1500*15000; // alpha decay
	public double num_moves = 0;
	double current_epsilon;
	double current_alpha;
	int number_of_features;
	boolean reset_trace = true;
	//LinearReg LR;
	NN LR;
	boolean isLevelFinished;
	double q_A;
	double the_error;
	int previous_mario_mode = 2;
	int chosenAction;
	State currentState;
	public boolean testRun = false;	
	
	// *** should try normalizing reward
	
	public RLAgent2()
	{
	    super("RLAgent2");
	    if (testRun){
	    	epsilon = 0.001;
	    	epsilon_min = 0.001;
	    	alpha = 0;
	    	alpha_min = 0;
	    }
	    reset();
	    //LR = new LinearReg(number_of_features, 13, "V5.txt");
	    //LR = new LinearReg(number_of_features, 13, "out24.txt", "out23.txt");
	    LR = new NN(number_of_features, 13, 100, "newtest1.txt");
	}
	
	public void reset()
	{		
		
		all_actions[0][1] = true; // 0  Mario.KEY_RIGHT & Mario.KEY_JUMP
		all_actions[0][3] = true;
		all_actions[1][1] = true; // 1  Mario.KEY_RIGHT
		all_actions[2][0] = true; // 2  Mario.KEY_LEFT
		all_actions[3][2] = true; // 3  Mario.KEY_DOWN
		all_actions[4][0] = true; // 4  Mario.KEY_LEFT & Mario.KEY_JUMP
		all_actions[4][3] = true;
		all_actions[5][1] = true; // 5  Mario.KEY_RIGHT & Mario.KEY_SPEED
		all_actions[5][4] = true;
		all_actions[6][0] = true; // 6  Mario.KEY_LEFT & Mario.KEY_SPEED
		all_actions[6][4] = true;
		all_actions[7][1] = true; // 7  Mario.KEY_RIGHT & Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[7][3] = true;
		all_actions[7][4] = true;
		all_actions[8][0] = true; // 8  Mario.KEY_LEFT & Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[8][3] = true;
		all_actions[8][4] = true;
		all_actions[5][3] = true;// 10 Mario.KEY_JUMP
		all_actions[11][4] = true;// 11 Mario.KEY_SPEED
		all_actions[12][3] = true;// 12 Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[12][4] = true;
	    action = new boolean[Environment.numberOfKeys];	 
	    currentState = new State();
	    number_of_features = currentState.integrate().length;
	}	
	
	// Pick the action with the maximum expected value.
	public int getMoveFromOutputs(double[] outputs){
		int maxIndex = 0;	
		
		for (int i = 1; i < outputs.length; i++){
			if ((outputs[i] > outputs[maxIndex])){
				maxIndex = i;
			}
		  }
		return maxIndex;	  
	}
	
	public void giveIntermediateReward(float intermediateReward){
		
		float distance_reward = ((float) 1)*(this.currentState.getDeltaDistance());
		reward = distance_reward;
		
		// Give a negative reward when Mario is stuck.
		if(this.currentState.isStuck() == 1)
		{
			reward -= 10;//100;
		}
		
		float mode_reward = 100*(this.currentState.getMarioMode() - previous_mario_mode);/*800*/ 
		reward += mode_reward;
		previous_mario_mode = this.currentState.getMarioMode();		
			
		float kill_reward = (this.currentState.getKillsByFire() + this.currentState.getKillsByStomp())*20;		
		reward += kill_reward;
		
		if(this.currentState.getMarioStatus() == Mario.STATUS_DEAD){
			reward = -100;//-1000;
		}
	}
	
	
	public double[] get_inputs(){
		int[] stateInfo = currentState.integrate();
		double[] castedState = new double[stateInfo.length];
		for (int i=0;i<stateInfo.length;i++) castedState[i] = (double) stateInfo[i];
		return castedState;
	}
	
	public void integrateObservation(Environment environment){
		super.integrateObservation(environment);
		currentState.update(environment);
		this.isLevelFinished = environment.isLevelFinished();
	}
	
	public void update_eps_and_alpha(){
		if(!testRun){
			current_epsilon = epsilon_min + (((linear_ep - num_moves)/linear_ep) * epsilon_decay); // calculate epsilon
			current_epsilon = Math.max(epsilon_min, current_epsilon);
			current_alpha = alpha_min +(((linear_ep1 - num_moves)/linear_ep1) * alpha_decay);
			current_alpha = Math.max(alpha_min, current_alpha);
			num_moves++;
		}
		else{
			current_epsilon = 0.001;
			current_alpha = 0.0;
		}
	}
	
	public boolean[] getAction()
	{		
		double[] inputs = get_inputs();
		update_eps_and_alpha();
		
		double[] q_values = LR.get_output(inputs, true);
		
		int action_num = 0;
		if (rand.nextDouble() < current_epsilon){
	    	action_num = rand.nextInt(all_actions.length);
	    	reset_trace = true;
	    } else {
	    	action_num = getMoveFromOutputs(q_values);
	    	reset_trace = false;
	    }
		
		q_A = q_values[action_num];
		LR.add_elig(inputs, action_num);
		the_error = (-1) * q_A; // -Q(S,a)
	
		action = all_actions[action_num].clone();
		chosenAction = action_num;
		//run_counter(action_num);
	    return action;
	}
		
	public void train(){
		if(!testRun){
			double[] inputs = get_inputs(); //S'
			double[] q_values_prime = LR.get_output(inputs, false);
			int action_num = getMoveFromOutputs(q_values_prime);
			double q_A_prime = q_values_prime[action_num]; // max_a(Q(S',a))
			if (isLevelFinished){
				the_error += reward;
				previous_mario_mode = 2;
				/*System.out.print("r: "+ reward);
				System.out.printf("\t Q: %.2f",the_error);				
				System.out.printf("  \tE: %.2f",the_error);
				System.out.println();
				System.out.println("alpha: "+current_alpha);
				System.out.println("epsilon: "+current_epsilon);
				System.out.print("Biases: ");*/
				//LR.print_biases();
				
				//PRESSED_BUTTON = 0;
				//run_button_count = 0;
			} else {
				the_error += reward + (dec*q_A_prime);
			}
			LR.train(chosenAction, current_alpha, the_error, lambda, dec, regL2, reset_trace);
			if(isLevelFinished){
				LR.reset_traces();
			}
		}
	}	
	
	public boolean[] getAction(int action_num){
		return all_actions[action_num].clone();
	}	
}

