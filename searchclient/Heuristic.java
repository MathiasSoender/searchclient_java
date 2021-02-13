package searchclient;

import java.util.*;

import static java.lang.Math.min;

public abstract class Heuristic
        implements Comparator<State>
{
    HashMap<Integer[][],Character> goalmap;
    Graph<String> graph = new Graph<String>();

    public Heuristic(State initialState)
    {
        goalmap = new HashMap<Integer[][], Character>();
        for (int row = 0; row < initialState.goals.length - 1; row++) {
            for (int col = 0; col < initialState.goals[row].length - 1; col++) {

                char goal = initialState.goals[row][col];
                if (goal != 0){
                    goalmap.put(new Integer[row][col], goal);
                }
            }
        }



    }

    public void createGraph(State initialState) {
        //creating a graph, to find the shortest path efficiently

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

    public int breathFirstTraversal(Graph graph, String root, String goal) {
        Set<String> visited = new LinkedHashSet<String>();
        Deque<String> queue = new ArrayDeque<>();
        Deque<Integer> depth = new ArrayDeque<>();

        queue.push(root);
        depth.push(0);
        Integer depthint = 0;

        while (!queue.isEmpty()) {
            String vertex = queue.pollFirst();
            depthint = depth.pollFirst();
            if (vertex.equals(goal)) {
                return depthint;
            }
            if (!visited.contains(vertex)) {
                visited.add(vertex);
                for (Object v : graph.getAdjacent(vertex)) {
                    queue.addLast((String) v);
                    depth.addLast(depthint+1);

                }
            }
        }

        return depthint;
    }

    public int h_shortestdistance(State s) {
        double distance = 0;

        for (int row = 1; row < s.goals.length - 1; row++)
        {
            for (int col = 1; col < s.goals[row].length - 1; col++)
            {
                char goal = s.goals[row][col];

                if('A' <= goal && goal <= 'Z') {
                    for (int i = 0; i < s.boxes.length; i++) {
                        for (int j = 0; j < s.boxes[i].length; j++) {
                            if (s.boxes[i][j] == goal) {
                                Integer agent_to_box = breathFirstTraversal(graph,s.agentRows[0] +" "+s.agentCols[0],i+" "+j);
                                Integer box_to_goal = breathFirstTraversal(graph,i+" "+j,row+" "+col);
                                distance += box_to_goal+agent_to_box;
                            }
                        }
                    }
                }

                else if ('0' <= goal && goal <= '9' &&
                        !(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col))
                {
                    distance += breathFirstTraversal(graph,s.agentRows[goal - '0'] +" "+s.agentCols[goal - '0'],row+" "+col);
                }
            }
        }

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
