package searchclient;

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

public abstract class Heuristic
        implements Comparator<State>
{
    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
    }
    // How many goal cells are not yet covered by an object of the right type.
    public int h(State s)
    {

        int goalcells = 0;
        for (int row = 1; row < s.goals.length - 1; row++)
        {
            for (int col = 1; col < s.goals[row].length - 1; col++)
            {
                char goal = s.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && s.boxes[row][col] != goal)
                {
                    goalcells+=1;
                }
                else if ('0' <= goal && goal <= '9' &&
                        !(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col))
                {
                    goalcells+=1;
                }
            }
        }
        return goalcells;

    }

    public int h_own(State s) {
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
    public HeuristicSuggestionGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h_own(s);
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
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h_own(s);
    }

    @Override
    public String toString()
    {
        return "suggested AStar evaluation";
    }

}
