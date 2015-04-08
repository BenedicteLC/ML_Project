package RLAgent;

import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite; // Monsters, etc.
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene; // Obstacles.

public class State {
	
	// Variables contained within a state.
	private int isMarioBig, isMarioFire;
	private int isGrounded, canJump;
	private int marioStatus;
	private int stepKillsByFire, stepKillsByStomp, totalKillsByFire, totalKillsByStomp;
	private int[] monsters; // 19x19 flattened binary grid.
	private int[] obstacles; // 4x5 flattened binary grid.		
	private float currentMarioXPosition, previousMarioXPosition;	
	
	private Environment environment;
	private byte[][] scene; // Contains monsters and obstacles.
	
	private int[] stateArray; // What we will return.

	public State() {
		this.monsters = new int[19*19];
		this.obstacles = new int[4*5];
		this.stateArray = new int[4 + 19*19 + 4*5];
		this.currentMarioXPosition = 0;
		this.totalKillsByFire = 0;
		this.totalKillsByStomp = 0;
	}
		
	// Update the state variables. 
	public void update(Environment environment) {
		
		// Get the simulator's latest environment.
		this.environment = environment;
		this.scene = environment.getMergedObservationZZ(1,1); //TODO Not confident about that 1,1. Need to check.	
		
		///////////////////
		// Update variables.
		///////////////////
		
		// Update Mario's float position.
		this.previousMarioXPosition = this.currentMarioXPosition;
		this.currentMarioXPosition = environment.getMarioFloatPos()[0];
		
		// Get Marios's current status (running, dead, etc).
		this.marioStatus = this.environment.getMarioStatus();
		
		// Get the amount of kills in this tick.
		this.stepKillsByFire = this.environment.getKillsByFire() - this.totalKillsByFire;
		this.stepKillsByStomp = this.environment.getKillsByStomp() - this.stepKillsByStomp;
		this.totalKillsByFire = this.environment.getKillsByFire();
		this.totalKillsByStomp = this.environment.getKillsByStomp();
		
		// Get Mario's mode (small/big, fire).
		// Mario can be both big and fire at the same time.
		int marioMode = environment.getMarioMode();
		this.isMarioBig = marioMode == 0 ? 0 : 1;
		this.isMarioFire = marioMode == 2 ? 1 : 0;		
		
		// 1 if Mario is grounded, 0 otherwise.
		this.isGrounded = environment.isMarioOnGround() ? 1 : 0;
		// 1 if Mario can jump, 0 otherwise.
		this.canJump = environment.isMarioAbleToJump() || !environment.isMarioOnGround() ? 1 : 0;			
		
		// Fill the monster array.
		int currentTile = 0;
		for(int i = 0; i < scene.length; i++){
			for (int j = 0; j < scene[0].length; j++){
				// For now we're not discriminating between monster types
				// to reduce the state space.
				if(this.containsMonster(scene[i][j])){
					this.monsters[currentTile] = 1;
				}
				else{
					this.monsters[currentTile] = 0;
				}
				currentTile++;
			}
		}		
		
		// Fill the obstacle array.
		currentTile = 0;
		int marioX = 9;
		int marioY = 9;
		for(int i = marioX - 2; i <= marioX + 1; i++){
			for (int j = marioY - 2; j <= marioY + 2; j++){
				if(this.containsObstacle(scene[i][j], i)){
					this.obstacles[currentTile] = 1;
				}
				else{
					this.obstacles[currentTile] = 0;
				}
				currentTile++;
			}
		}
	}
	
	// Convert this state to an array of ints.
	public int[] integrate(){
		
		this.stateArray[0] = this.isMarioBig;
		this.stateArray[1] = this.isMarioFire;
		this.stateArray[2] = this.isGrounded;
		this.stateArray[3] = this.canJump;
		
		for(int i = 0; i < this.monsters.length; i++){
			this.stateArray[4+i] = this.monsters[i];
		}
		
		for(int i = 0; i < this.obstacles.length; i++){
			this.stateArray[4+19*19+i] = this.obstacles[i];
		}
		
		return stateArray;
	}
	
	// Takes a tileValue and check if it's a monster.
	private boolean containsMonster(byte tileValue){
		boolean containsMonster = false;
		
		if(tileValue == Sprite.KIND_GOOMBA 
			|| tileValue == Sprite.KIND_BULLET_BILL
			|| tileValue == Sprite.KIND_ENEMY_FLOWER
			|| tileValue == Sprite.KIND_GOOMBA_WINGED
			|| tileValue == Sprite.KIND_GREEN_KOOPA
			|| tileValue == Sprite.KIND_GREEN_KOOPA_WINGED
			|| tileValue == Sprite.KIND_RED_KOOPA
			|| tileValue == Sprite.KIND_RED_KOOPA_WINGED
			|| tileValue == Sprite.KIND_SPIKY
			|| tileValue == Sprite.KIND_SPIKY_WINGED
			|| tileValue == Sprite.KIND_WAVE_GOOMBA){
			containsMonster = true;
		}
		
		return containsMonster;
	}
	
	// Takes a tileValue and check if it's an obstacle.
	private boolean containsObstacle(byte tileValue, int y){
		boolean containsObstacle = false;
		
		if(tileValue == GeneralizerLevelScene.BREAKABLE_BRICK 
			|| tileValue == GeneralizerLevelScene.BRICK
			|| tileValue == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
			|| tileValue == GeneralizerLevelScene.UNBREAKABLE_BRICK){
			containsObstacle = true;
		}
		// BORDER_CANNOT_PASS_THROUGH are only obstacles when they're not above Mario.
		if(tileValue == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
		&& y >= 9){
			containsObstacle = true;
		}
		
		return containsObstacle;
	}
	
	public float getCurrentMarioXPosition(){
		return this.currentMarioXPosition;
	}
	
	public float getPreviousMarioXPosition(){
		return this.previousMarioXPosition;
	}
	
	public int getMarioStatus(){
		return this.marioStatus;
	}
	
	// Number of kills in this tick.
	public int getKillsByFire(){
		return this.stepKillsByFire;
	}
	
	// Number of kills in this tick.
	public int getKillsByStomp(){
		return this.stepKillsByStomp;
	}
}

/*
 * 
		for(int i =0; i < this.scene.length; i++){
			for(int j =0; j < this.scene[0].length; j++){
				System.out.print(this.scene[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();*/



