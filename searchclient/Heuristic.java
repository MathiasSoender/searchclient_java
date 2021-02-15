package searchclient;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.min;

public abstract class Heuristic
        implements Comparator<State>
{
    HashMap<Character, Point2D> goalmap;
    Graph<String> graph = new Graph<String>();
    HashMap<String, Integer> precomputedDistance;


    public Heuristic(State initialState)
    {
        goalmap = new HashMap<Character, Point2D>();
        for (int row = 0; row < initialState.goals.length - 1; row++) {
            for (int col = 0; col < initialState.goals[row].length - 1; col++) {

                char goal = initialState.goals[row][col];
                if (goal != 0){
                    goalmap.put(goal, new Point2D.Double(row,col));
                }
            }
        }

    }

    public void createGraph(State initialState) {
        //creating a graph, to find the shortest path efficiently
        precomputedDistance = new HashMap<String, Integer>();
        for(int i =1; i<initialState.walls.length; i++) {
            for (int j=1; j<initialState.walls[0].length;j++) {
                if (!initialState.walls[i][j]) {
                    String vertex = i+" "+j;
                    graph.addVertex(vertex);
                    if (j!=1){
                        int new_j = j-1;
                        if (!initialState.walls[i][new_j]) {
                            graph.addEdge(i+" "+new_j,vertex,true);
                        }
                    }
                    if (i!=1) {
                        int new_i = i-1;
                        if (!initialState.walls[new_i][j]){
                            graph.addEdge(new_i+" "+j,vertex,true);
                        }
                    }
                }
            }
        }


    }
    // How many goal cells are not yet covered by an object of the right type.
    public int h(State s)
    {

        int count = goalmap.size();
        for (int row = 0; row < s.goals.length; row++) {
            for (int col = 0; col < s.goals[row].length; col++) {
                char goal = s.goals[row][col];
                char box = Character.toLowerCase(s.boxes[row][col]);
                if (goal > 0 && box == goal){
                    count--;
                }
            }
        }
        return count;

    }


    private int breathFirstTraversal(Graph graph, String root, String goal) {

        if (precomputedDistance.containsKey(root + goal)) return precomputedDistance.get(root+goal);
        HashMap<String, ArrayList<String>> parents = new HashMap<String, ArrayList<String>>();

        Set<String> visited = new LinkedHashSet<String>();
        Deque<String> queue = new ArrayDeque<>();
        Deque<Integer> depth = new ArrayDeque<>();


        queue.push(root);
        depth.push(0);
        Integer depthint = 0;
        parents.put(root, new ArrayList<>());

        while (!queue.isEmpty()) {
            String vertex = queue.pollFirst();
            depthint = depth.pollFirst();

            if (vertex.equals(goal)) {
                precomputedDistance.put(root+goal, depthint);

                for(String path : parents.get(vertex)){
                    String[] info = path.split("\\.");
                    precomputedDistance.put(info[0]+goal, depthint - Integer.parseInt(info[1]));

                }
                return depthint;
            }
            if (!visited.contains(vertex)) {
                visited.add(vertex);
                int minDepth = 0;
                for (Object v : graph.getAdjacent(vertex)) {
                    queue.addLast((String) v);
                    depth.addLast(depthint+1);

                    ArrayList<String> parentList = (ArrayList<String>) parents.get(vertex).clone();
                    parentList.add(vertex+"."+depthint);
                    parents.put((String) v, parentList);

                    if (precomputedDistance.containsKey(v + goal)){
                        minDepth = Math.min(precomputedDistance.get(v+goal) + depthint+1, minDepth);
                    }

                }
                if (minDepth != 0) return minDepth;
            }
        }

        return depthint;
    }

    private boolean checkIfAgentFinished(String agentColor, State s){
        boolean done = false;

        for (int row = 1; row < s.goals.length - 1; row++)
        {
            for (int col = 1; col < s.goals[row].length - 1; col++) {
                char goal = s.goals[row][col];

                if ('A' <= goal && goal <= 'Z') {
                    for (int i = 0; i < s.boxes.length; i++) {
                        for (int j = 0; j < s.boxes[i].length; j++) {
                            if (s.boxes[i][j] == goal) {
                                boolean color = (agentColor == s.boxColors[goal-'A'].toString());
                                if (color) {
                                    int box_to_goal = breathFirstTraversal(graph, i + " " + j, row + " " + col);

                                    if (box_to_goal == 0){
                                        done = true;
                                    }
                                    else{
                                        return false;
                                    }


                                }

                            }
                        }
                    }
                }

            }
        }
        return done;
    }

    public int h_shortestdistance(State s) {
        double distance = 0;
        double agent_to_box_sum = 0;
        double box_to_goal_sum = 0;
        String fromagent = "";

        for (int row = 1; row < s.goals.length - 1; row++)
        {
            for (int col = 1; col < s.goals[row].length - 1; col++) {
                char goal = s.goals[row][col];

                if ('A' <= goal && goal <= 'Z') {
                    boolean color = false;
                    for (int i = 0; i < s.boxes.length; i++) {
                        for (int j = 0; j < s.boxes[i].length; j++) {
                            if (s.boxes[i][j] == goal) {
                                for(int a = 0; a<s.agentRows.length;a++) {
                                    color = (s.agentColors[a].toString() == s.boxColors[goal-'A'].toString());
                                    if (color) {
                                        fromagent = s.agentRows[a] + " " + s.agentCols[a];


                                int agent_to_box = breathFirstTraversal(graph, fromagent, i + " " + j);
                                int box_to_goal = breathFirstTraversal(graph, i + " " + j, row + " " + col);
                                // When finished, ensure agent is close to box
                                //*0.5 : This is the behavior of agent after finished. If high (>1), the agent will stand still
                                //If low, the agent will dance a bit (so he does not block)
                                if (checkIfAgentFinished(s.agentColors[a].toString(),s)) agent_to_box_sum +=
                                                                                        agent_to_box * 0.5;
                                if (box_to_goal != 0) {
                                    box_to_goal_sum += box_to_goal;
                                    agent_to_box_sum += agent_to_box;
                                }


                                break;
                                    }

                                }

                        }
                        if (color) break;
                    }
                        // SOme reasons these breaks increase the complexity signficantly.
                    if (color) break;
                }
            }


                else if ('0' <= goal && goal <= '9' &&
                        !(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col))
                {
                        distance += breathFirstTraversal(graph, s.agentRows[goal - '0'] + " " + s.agentCols[goal - '0'], row + " " + col);
                }
            }
        }

        distance += box_to_goal_sum;
        distance += agent_to_box_sum;
        return (int)distance;
    }


    public int h_manhatten(State s) {
        double manhatten = 0;

        for (int row = 1; row < s.goals.length - 1; row++)
        {
            for (int col = 1; col < s.goals[row].length - 1; col++)
            {
                char goal = s.goals[row][col];
                double shortestManhatten = 0;
                if('A' <= goal && goal <= 'Z') {
                    for (int i = 0; i < s.boxes.length; i++) {
                        for (int j = 0; j < s.boxes[i].length; j++) {
                            if (s.boxes[i][j] == goal) {
                                shortestManhatten = min(Math.sqrt((i - row) ^ 2 + (j - col) ^ 2), shortestManhatten);
                            }
                        }
                    }
                    manhatten += shortestManhatten;
                }

                else if ('0' <= goal && goal <= '9' &&
                        !(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col))
                {
                    manhatten += Math.sqrt((s.agentRows[goal-'0'] - row)^2 + (s.agentCols[goal-'0'] - col)^2);
                }
            }
        }
        return (int)manhatten;
    }



    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}


class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
class HeuristicSuggestionGreedy
        extends Heuristic
{
    //Graph<String> graph = new Graph<String>();
    public HeuristicSuggestionGreedy(State initialState)
    {
        super(initialState);
        createGraph(initialState);

    }



    @Override
    public int f(State s)
    {
        return this.h_shortestdistance(s);

    }

    @Override
    public String toString()
    {
        return "suggested greedy evaluation";
    }

}

class HeuristicSuggestionAStar
        extends Heuristic
{
    public HeuristicSuggestionAStar(State initialState)
    {
        super(initialState);
        createGraph(initialState);

    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h_shortestdistance(s);
    }

    @Override
    public String toString()
    {
        return "suggested AStar evaluation";
    }

}
