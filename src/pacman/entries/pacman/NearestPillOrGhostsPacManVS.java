package pacman.entries.pacman;

import java.util.Arrays;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class NearestPillOrGhostsPacManVS extends Controller<MOVE> {

    private static final int MIN_GHOST_DISTANCE = 20; // Distance à partir de laquelle on considère un fantôme
    private static final int DANGEROUS_DISTANCE = 15; // Distance à considérer comme dangereuse

    @Override
    public MOVE getMove(Game game, long timeDue) {
        int currentNodeIndex = game.getPacmanCurrentNodeIndex();
        MOVE lastMove = game.getPacmanLastMoveMade();

        // Dessiner les lignes vers les fantômes
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostLairTime(ghost) == 0) {
                game.getShortestPath(currentNodeIndex, game.getGhostCurrentNodeIndex(ghost));
            }
        }

        // 1. Vérifier les fantômes comestibles
        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(currentNodeIndex, ghostNode);

                // Poursuivre les fantômes comestibles si assez proches
                if (distance < MIN_GHOST_DISTANCE) {
                    return game.getNextMoveTowardsTarget(currentNodeIndex, ghostNode, lastMove, DM.PATH);
                }
            }
        }

        // 2. Gérer les pacgommes
        int[] activePills = game.getActivePillsIndices();
        int[] activePowerPills = game.getActivePowerPillsIndices();

        // Vérifier si un fantôme dangereux est proche
        boolean dangerClose = false;
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(currentNodeIndex, ghostNode);

                if (distance < DANGEROUS_DISTANCE) {
                    dangerClose = true;
                    break;
                }
            }
        }

        // Si un fantôme non comestible est proche, priorité aux super pacgommes
        if (dangerClose && activePowerPills.length > 0) {
            int nearest = game.getClosestNodeIndexFromNodeIndex(currentNodeIndex, activePowerPills, DM.PATH);
            return game.getNextMoveTowardsTarget(currentNodeIndex, nearest, lastMove, DM.PATH);
        }

        // Combiner les tableaux de pacgommes
        int[] targetNodes = new int[activePills.length + activePowerPills.length];
        System.arraycopy(activePills, 0, targetNodes, 0, activePills.length);
        System.arraycopy(activePowerPills, 0, targetNodes, activePills.length, activePowerPills.length);

        if (targetNodes.length == 0) {
            return MOVE.NEUTRAL;
        }

        // Aller à la pacgomme la plus proche
        int nearest = game.getClosestNodeIndexFromNodeIndex(currentNodeIndex, targetNodes, DM.PATH);
        return game.getNextMoveTowardsTarget(currentNodeIndex, nearest, lastMove, DM.PATH);
    }
}