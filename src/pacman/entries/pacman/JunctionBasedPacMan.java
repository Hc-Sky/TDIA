package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class JunctionBasedPacMan extends Controller<MOVE> {

    // Constantes pour les distances
    private static final int GHOST_DANGER_DISTANCE = 15;
    private static final int GHOST_EDIBLE_DISTANCE = 30;

    @Override
    public MOVE getMove(Game game, long timeDue) {

        game.afficherJunctions(game);
        game.afficherClosestJunctions(game);


        int currentNode = game.getPacmanCurrentNodeIndex();
        MOVE lastMoveMade = game.getPacmanLastMoveMade();

        // Si PacMan est sur une jonction, prendre une décision stratégique
        if (game.isJunction(currentNode)) {
            return getJunctionDecision(game, currentNode, lastMoveMade);
        }
        // Sinon, continuer dans la même direction si possible
        else {
            MOVE[] possibleMoves = game.getPossibleMoves(currentNode, lastMoveMade);
            if (possibleMoves.length == 0) {
                return MOVE.NEUTRAL;
            } else if (possibleMoves.length == 1) {
                return possibleMoves[0]; // Si un seul choix, le prendre
            } else {
                // S'il y a plusieurs choix mais que ce n'est pas une jonction,
                // on est probablement dans un virage - prendre la meilleure option
                return getBestNonJunctionMove(game, currentNode, lastMoveMade);
            }
        }
    }

    private MOVE getJunctionDecision(Game game, int currentNode, MOVE lastMoveMade) {
        // 1. Si un fantôme comestible est proche, le poursuivre
        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(currentNode, ghostNode);

                if (distance < GHOST_EDIBLE_DISTANCE) {
                    return game.getNextMoveTowardsTarget(currentNode, ghostNode, lastMoveMade, DM.PATH);
                }
            }
        }

        // 2. Si un fantôme dangereux est proche, l'éviter
        MOVE escapeMove = getEscapeMove(game, currentNode, lastMoveMade);
        if (escapeMove != null) {
            return escapeMove;
        }

        // 3. Chercher les power pills si des fantômes sont à proximité
        boolean ghostsNearby = isAnyGhostClose(game, currentNode, GHOST_DANGER_DISTANCE * 2);
        if (ghostsNearby) {
            int[] powerPills = game.getActivePowerPillsIndices();
            if (powerPills.length > 0) {
                int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(currentNode, powerPills, DM.PATH);
                return game.getNextMoveTowardsTarget(currentNode, nearestPowerPill, lastMoveMade, DM.PATH);
            }
        }

        // 4. Sinon, aller vers la pilule la plus proche
        int[] pills = game.getActivePillsIndices();
        if (pills.length > 0) {
            int nearestPill = game.getClosestNodeIndexFromNodeIndex(currentNode, pills, DM.PATH);
            return game.getNextMoveTowardsTarget(currentNode, nearestPill, lastMoveMade, DM.PATH);
        }

        // 5. Si aucune pilule, explorer en allant vers la jonction la plus éloignée
        int[] junctions = game.getJunctionIndices();
        if (junctions.length > 0) {
            int farthestJunction = game.getFarthestNodeIndexFromNodeIndex(currentNode, junctions, DM.PATH);
            return game.getNextMoveTowardsTarget(currentNode, farthestJunction, lastMoveMade, DM.PATH);
        }

        // Si tout échoue, continuer dans la même direction
        return lastMoveMade;
    }

    private MOVE getEscapeMove(Game game, int currentNode, MOVE lastMoveMade) {
        // Vérifier chaque fantôme non comestible
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(currentNode, ghostNode);

                if (distance < GHOST_DANGER_DISTANCE) {
                    // Trouver la meilleure direction pour fuir
                    return game.getNextMoveAwayFromTarget(currentNode, ghostNode, lastMoveMade, DM.PATH);
                }
            }
        }
        return null; // Aucun fantôme dangereux à proximité
    }

    private boolean isAnyGhostClose(Game game, int node, int threshold) {
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int distance = game.getShortestPathDistance(node, game.getGhostCurrentNodeIndex(ghost));
                if (distance < threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    private MOVE getBestNonJunctionMove(Game game, int currentNode, MOVE lastMoveMade) {
        // Dans un couloir avec plusieurs options mais pas une jonction
        // Par exemple, dans un virage - choisir la direction qui mène vers la pilule la plus proche
        int[] pills = game.getActivePillsIndices();

        if (pills.length > 0) {
            int nearestPill = game.getClosestNodeIndexFromNodeIndex(currentNode, pills, DM.PATH);
            return game.getNextMoveTowardsTarget(currentNode, nearestPill, lastMoveMade, DM.PATH);
        }

        // Si pas de pilule, prendre la première direction possible
        MOVE[] possibleMoves = game.getPossibleMoves(currentNode, lastMoveMade);
        return possibleMoves[0];
    }
}