package pacman;

import java.io.*;
import java.util.*;

import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
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
import pacman.entries.pacman.JunctionBasedPacMan;
import pacman.entries.pacman.NearestPillOrGhostsPacManVS;
import pacman.game.Game;
import pacman.game.GameView;
import static pacman.game.Constants.*;
import java.awt.Color;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Executor {
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		int delay = 10;
		boolean visual = true;
		int numTrials = 20;

		Executor exec = new Executor();

		/* run a game in synchronous mode: game waits until controllers respond. */
		System.out.println("STARTER PACMAN vs LEGACY2THERECKONING");
		exec.runGame(new StarterPacMan(), new Legacy2TheReckoning(), visual, delay);

		/* run multiple games in batch mode - good for testing. */

//		System.out.println("STARTER PACMAN vs LEGACY2THERECONING");
//		exec.runExperiment(new StarterPacMan(), new Legacy2TheReckoning(),numTrials);
//		System.out.println("RANDOM PACMAN vs LEGACY2THERECONING");
//		exec.runExperiment(new RandomPacMan(), new Legacy2TheReckoning(),numTrials);
//		System.out.println("NEAREST PILL PACMAN vs LEGACY2THERECONING");
//		exec.runExperiment(new NearestPillPacMan(), new Legacy2TheReckoning(),numTrials);
//
//
//		System.out.println("STARTER PACMAN vs starter GHOSTS");
//		exec.runExperiment(new StarterPacMan(), new StarterGhosts(),numTrials);
//		System.out.println("RANDOM PACMAN vs RANDOM GHOSTS");
//		exec.runExperiment(new RandomPacMan(),  new StarterGhosts(),numTrials);
//		System.out.println("NEAREST PILL PACMAN vs RANDOM GHOSTS");
//		exec.runExperiment(new NearestPillPacMan(), new StarterGhosts(),numTrials);




		/* run the game in asynchronous mode. */

//		exec.runGameTimed(new MyPacMan(),new AggressiveGhosts(),visual);
//		exec.runGameTimed(new RandomPacMan(), new AvengersEvolution(evolutionFile),visual);
//		exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);


		/* run the game in asynchronous mode but advance as soon as both controllers are ready  - this is the mode of the competition.
		time limit of DELAY ms still applies.*/

//		boolean visual=true;
//		boolean fixedTime=false;
//		exec.runGameTimedSpeedOptimised(new MyMCTSPacMan(new AggressiveGhosts()),new AggressiveGhosts(),fixedTime,visual);


		/* run game in asynchronous mode and record it to file for replay at a later stage. */


		//String fileName = "replay.txt";
		//exec.runGameTimedRecorded(new RandomPacMan(), new RandomGhosts(), visual, fileName);
		//exec.replayGame(fileName, visual);
	}

	/**
	 * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
	 * against a chosen opponent: the random nature of the game means that performance can vary from game to game.
	 * Running many games and looking at the average score (and standard deviation/error) helps to get a better
	 * idea of how well the controller is likely to do in the competition.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController  The Ghosts controller
	 * @param trials           The number of trials to be executed
	 */
	public void runExperiment(Controller<MOVE> pacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, int trials) {
		double avgScore = 0;

		Random rnd = new Random(0);
		Game game;

		for (int i = 0; i < trials; i++) {
			game = new Game(rnd.nextLong());

			while (!game.gameOver()) {
				game.advanceGame(pacManController.getMove(game.copy(), System.currentTimeMillis() + DELAY),
						ghostController.getMove(game.copy(), System.currentTimeMillis() + DELAY));
			}

			avgScore += game.getScore();
			System.out.println(i + "\t" + game.getScore());
		}

		System.out.println(avgScore / trials);
	}

	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController  The Ghosts controller
	 * @param visual           Indicates whether or not to use visuals
	 * @param delay            The delay between time-steps
	 */
	public void runGame(Controller<MOVE> pacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, boolean visual, int delay) {
		Game game = new Game(0);

		GameView gv = null;

		if (visual)
			gv = new GameView(game).showGame();


		while (!game.gameOver()) {
			game.advanceGame(pacManController.getMove(game.copy(), -1), ghostController.getMove(game.copy(), -1));

			try {
				Thread.sleep(delay);
			} catch (Exception e) {
			}

			if (visual)
				gv.repaint();
		}
	}

	/**
	 * Run the game with time limit (asynchronous mode). This is how it will be done in the competition.
	 * Can be played with and without visual display of game states.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController  The Ghosts controller
	 * @param visual           Indicates whether or not to use visuals
	 */
	public void runGameTimed(Controller<MOVE> pacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, boolean visual) {
		Game game = new Game(0);

		GameView gv = null;

		if (visual)
			gv = new GameView(game).showGame();

		if (pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController) pacManController).getKeyboardInput());

		new Thread(pacManController).start();
		new Thread(ghostController).start();

		while (!game.gameOver()) {
			pacManController.update(game.copy(), System.currentTimeMillis() + DELAY);
			ghostController.update(game.copy(), System.currentTimeMillis() + DELAY);

			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			game.advanceGame(pacManController.getMove(), ghostController.getMove());

			if (visual)
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
	 * @param ghostController  The Ghosts controller
	 * @param fixedTime        Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual           Indicates whether or not to use visuals
	 */
	public void runGameTimedSpeedOptimised(Controller<MOVE> pacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, boolean fixedTime, boolean visual) {
		Game game = new Game(0);

		GameView gv = null;

		if (visual)
			gv = new GameView(game).showGame();

		if (pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController) pacManController).getKeyboardInput());

		new Thread(pacManController).start();
		new Thread(ghostController).start();

		while (!game.gameOver()) {
			pacManController.update(game.copy(), System.currentTimeMillis() + DELAY);
			ghostController.update(game.copy(), System.currentTimeMillis() + DELAY);

			try {
				int waited = DELAY / INTERVAL_WAIT;

				for (int j = 0; j < DELAY / INTERVAL_WAIT; j++) {
					Thread.sleep(INTERVAL_WAIT);

					if (pacManController.hasComputed() && ghostController.hasComputed()) {
						waited = j;
						break;
					}
				}

				if (fixedTime)
					Thread.sleep(((DELAY / INTERVAL_WAIT) - waited) * INTERVAL_WAIT);

				game.advanceGame(pacManController.getMove(), ghostController.getMove());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (visual)
				gv.repaint();
		}

		pacManController.terminate();
		ghostController.terminate();
	}

	/**
	 * Run a game in asynchronous mode and recorded.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController  The Ghosts controller
	 * @param visual           Whether to run the game with visuals
	 * @param fileName         The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController, Controller<EnumMap<GHOST, MOVE>> ghostController, boolean visual, String fileName) {
		Game game = new Game(0);
		GameView gv = null;

		if (visual) {
			gv = new GameView(game).showGame();
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

			while (!game.gameOver()) {
				MOVE pacManMove = pacManController.getMove(game.copy(), System.currentTimeMillis() + DELAY);
				EnumMap<GHOST, MOVE> ghostMoves = ghostController.getMove(game.copy(), System.currentTimeMillis() + DELAY);

				game.advanceGame(pacManMove, ghostMoves);

				saveGameState(game, bw);

				if (visual) {
					gv.repaint();
					try {
						Thread.sleep(DELAY);
					} catch (Exception e) {
					}
				}
			}

			bw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual   Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName, boolean visual) {
		Game game = new Game(0);
		GameView gv = null;

		if (visual) {
			gv = new GameView(game).showGame();
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();

			while (line != null && !game.gameOver()) {
				loadGameState(game, line);

				if (visual) {
					gv.repaint();
					try {
						Thread.sleep(DELAY);
					} catch (Exception e) {
					}
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	//save file for replays
	public static void saveToFile(String data, String name, boolean append) {
		try {
			FileOutputStream outS = new FileOutputStream(name, append);
			PrintWriter pw = new PrintWriter(outS);

			pw.println(data);
			pw.flush();
			outS.close();

		} catch (IOException e) {
			System.out.println("Could not save data!");
		}
	}

	//load a replay
	private static ArrayList<String> loadReplay(String fileName) {
		ArrayList<String> replay = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String input = br.readLine();

			while (input != null) {
				if (!input.equals(""))
					replay.add(input);

				input = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return replay;
	}


	private void loadGameState(Game game, String line) {
		String[] parts = line.split(", ");
		HashMap<String, String> state = new HashMap<>();

		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2) {
				state.put(keyValue[0], keyValue[1]);
			}
		}

		// Construction de la chaîne dans le format attendu par setGameState
		StringBuilder gameState = new StringBuilder();

		// Paramètres principaux du jeu
		gameState.append(state.get("mazeIndex")).append(",");
		gameState.append(state.get("totalTime")).append(",");
		gameState.append(state.get("score")).append(",");
		gameState.append(state.get("currentLevelTime")).append(",");
		gameState.append(state.get("levelCount")).append(",");
		gameState.append(state.get("pacman.currentNodeIndex")).append(",");
		gameState.append(state.get("pacman.lastMoveMade")).append(",");
		gameState.append(state.get("pacman.numberOfLivesRemaining")).append(",");
		gameState.append(state.get("pacman.hasReceivedExtraLife")).append(",");

		// Ajout des informations sur les fantômes
		for (GHOST ghost : GHOST.values()) {
			gameState.append(state.get("ghost." + ghost + ".currentNodeIndex")).append(",");
			gameState.append(state.get("ghost." + ghost + ".edibleTime")).append(",");
			gameState.append("0,"); // lairTime (approximation)
			gameState.append(state.get("ghost." + ghost + ".lastMoveMade")).append(",");
		}

		// État des pilules (approximation simplifiée)
		// Note: ce format doit correspondre exactement à ce qu'attend Game.setGameState()
		gameState.append("1111111111111111111111111111111111111111111111111,");
		gameState.append("1111,");

		gameState.append(state.get("timeOfLastGlobalReversal")).append(",");
		gameState.append(state.get("pacmanWasEaten")).append(",");

		// États des fantômes mangés
		for (GHOST ghost : GHOST.values()) {
			gameState.append("false,");
		}

		// Finalisation
		gameState.append("false,"); // pillWasEaten
		gameState.append("false"); // powerPillWasEaten

		game.setGameState(gameState.toString());
	}

	private void saveGameState(Game game, BufferedWriter fw) throws IOException {
		HashMap<String, String> gameState = game.getGameStateAsMap();
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for (Map.Entry<String, String> entry : gameState.entrySet()) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}

		fw.write(sb.toString());
		fw.newLine();
	}
}
