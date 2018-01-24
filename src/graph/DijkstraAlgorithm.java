package graph;

import field.Bug;
import field.Field;
import field.TickingBomb;
import move.MoveType;


import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class DijkstraAlgorithm {
    public static void compute(Point position, Graph graph){
        Vertex startingVertex = graph.getVertexAtPosition(position);

        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(graph.getVertices().size(), new DijkstraVertexComparator());

        //initialization
        graph.getVertices().forEach(((point, vertex) -> {
            vertex.setDistanceToVertex(50000);
            vertex.setParent(null);
            priorityQueue.add(vertex);
        }));

        startingVertex.setDistanceToVertex(0);
        if (priorityQueue.remove(startingVertex))
            priorityQueue.add(startingVertex);

        while (!priorityQueue.isEmpty()){
            Vertex current = priorityQueue.poll();
            for (Vertex neighbour : current.getNeighbours()){
                if (!priorityQueue.contains(neighbour)) continue;
                int newDistance = current.getDistanceToVertex() + getNeighbourWeight(current, neighbour, graph.getField());
                if (neighbour.getDistanceToVertex() > newDistance){
                    neighbour.setDistanceToVertex(newDistance);
                    neighbour.setParent(current);
                    if (priorityQueue.remove(neighbour))
                        priorityQueue.add(neighbour);
                }
            }
        }
    }

    private static int getNeighbourWeight(Vertex current, Vertex neighbour, Field field){
        int weight = 1;
        //we check direction of bug
        if (neighbour.getVertexContent().contains(ObjectType.Enemy)){
            MoveType moveType = MoveType.convertPointsToMoveType(current.getPosition(), neighbour.getPosition());
            for (Bug bug : field.getBugs()){
                if (bug.getPosition().equals(neighbour.getPosition())) {
                    if (bug.getDirection() == moveType) {
                        continue;
                    }
                    weight += 80;
                }
            }
        }

        //for each explosion in time we add another weight
        for (int i = 0; i < getNumberOfExplosionsAtVertexAffectedByTickingBomb(neighbour, field); i++) weight =+ 80;

        return weight;
    }

    private static int getNumberOfExplosionsAtVertexAffectedByTickingBomb(Vertex vertex, Field field){
        int numberOfExplosions = 0;
        for (TickingBomb bomb : field.getTickingBombs()){
            if (vertex.getDistanceToVertex() != bomb.getTicks()) continue;      //we will not be on the place in eruption time
            if (getPositionsAffectedByBomb(bomb.getPosition(), field).contains(vertex.getPosition())) numberOfExplosions++;
        }
        return numberOfExplosions;
    }

    private static List<Point> getPositionsAffectedByBomb(Point position, Field field){
        List<Point> positions = new ArrayList<>();
        positions.add(position);
        for (MoveType moveType : field.getPositionValidMoveTypes(position)){
            Point current = MoveType.getPointAfterMove(position, moveType);
            while(field.isPointValid(current)){
                positions.add(current);
                current = MoveType.getPointAfterMove(current, moveType);
            }
        }
        return positions;
    }
}
