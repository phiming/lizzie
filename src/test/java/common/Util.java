package common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.UnsupportedLookAndFeelException;

import org.json.JSONException;
import org.junit.Test;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.analysis.MoveData;
import featurecat.lizzie.gui.LizzieFrame;
import featurecat.lizzie.rules.Board;
import featurecat.lizzie.rules.BoardData;
import featurecat.lizzie.rules.BoardHistoryList;
import featurecat.lizzie.rules.BoardHistoryNode;
import featurecat.lizzie.rules.SGFParser;
import featurecat.lizzie.rules.Stone;

public class Util {
    
    private static ArrayList<Integer> laneUsageList = new ArrayList<Integer>();
    
    /**
     * Get Variation Tree as String List
     * The logic is same as the function VariationTree.drawTree
     * 
     * @param startLane
     * @param startNode
     * @param variationNumber
     * @param isMain
     */
    public static void getVariationTree(List<String> moveList, int startLane, BoardHistoryNode startNode, int variationNumber, boolean isMain) {
        // Finds depth on leftmost variation of this tree
        int depth = BoardHistoryList.getDepth(startNode) + 1;
        int lane = startLane;
        // Figures out how far out too the right (which lane) we have to go not to collide with other variations
        while (lane < laneUsageList.size() && laneUsageList.get(lane) <= startNode.getData().moveNumber + depth) {
            // laneUsageList keeps a list of how far down it is to a variation in the different "lanes"
            laneUsageList.set(lane, startNode.getData().moveNumber - 1);
            lane++;
        }
        if (lane >= laneUsageList.size())
        {
                laneUsageList.add(0);
        }
        if (variationNumber > 1)
            laneUsageList.set(lane - 1, startNode.getData().moveNumber - 1);
        laneUsageList.set(lane, startNode.getData().moveNumber);

        // At this point, lane contains the lane we should use (the main branch is in lane 0)
        BoardHistoryNode cur  = startNode;

        // Draw main line
        StringBuilder sb = new StringBuilder();
        sb.append(formatMove(cur.getData()));
        while (cur.next() != null) {
            cur = cur.next();
            sb.append(formatMove(cur.getData()));
        }
        moveList.add(sb.toString());
        // Now we have drawn all the nodes in this variation, and has reached the bottom of this variation
        // Move back up, and for each, draw any variations we find
        while (cur.previous() != null && cur != startNode) {
            cur = cur.previous();
            int curwidth = lane;
            // Draw each variation, uses recursion
            for (int i = 1; i < cur.numberOfChildren(); i++) {
                curwidth++;
                // Recursion, depth of recursion will normally not be very deep (one recursion level for every variation that has a variation (sort of))
                getVariationTree(moveList, curwidth, cur.getVariation(i), i, false);
            }
        }
    }
    
    private static String formatMove(BoardData data) {
        String stone = "";
        if (Stone.BLACK.equals(data.lastMoveColor)) stone = "B";
        else if (Stone.WHITE.equals(data.lastMoveColor)) stone = "W";
        else return stone;

        char x = data.lastMove == null ? 't' : (char) (data.lastMove[0] + 'a');
        char y = data.lastMove == null ? 't' : (char) (data.lastMove[1] + 'a');

        return String.format(";%s[%c%c]", stone, x, y);
    }

}
