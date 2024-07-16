// The MIT License
// Copyright © 2024 [Gabriel Guerra Rosales, Co: ChatGPT]
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
// to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//Author: Gabriel Guerra Rosales
// CoAuthor: ChatGPT
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.*;


/*
* This Java program receives a 3D object file, interprets the mesh and counts the number of connected components in the mesh
* A connected component is a set of triangles in the mesh where each of the triangles is reachable from any triangle of the set
* Author: Gabriel Guerra Rosales
* Co authors: ChatGPT
* */

//This class represents the mesh
class Mesh {
    private final List<List<Integer>> triangles; //List of triangles finded in my obj represented as a list of vertex
    private final List<List<Float>> vertices; //List of vertices to build the separte connected components
    private final Map<Integer, List<Integer>> adjList; //List to save adjacent triangles or connected triangles

    public Mesh(List<List<Integer>> triangles, List<List<Float>> vertices) {
        this.triangles = triangles;
        this.vertices = vertices;
        this.adjList = buildAdjacencyList();
    }

    //With this method, we save in our adjList all the connected triangles in the mesh
    private Map<Integer, List<Integer>> buildAdjacencyList() {
        Map<Integer, List<Integer>> adjList = new HashMap<>();
        for (int i = 0; i < triangles.size(); i++) {
            adjList.put(i, new ArrayList<>());
            for (int j = 0; j < triangles.size(); j++) {
                if (i != j && isConnected(triangles.get(i), triangles.get(j))) { //If the component is connected the key of the vertex is added to the Map
                    adjList.get(i).add(j);
                }
            }
        }
        return adjList;
    }

    //This method checks if our triangles share at least 2 vertex then it means they are connected
    private boolean isConnected(List<Integer> triangle1, List<Integer> triangle2) {
        Set<Integer> commonVertex = new HashSet<>(triangle1);
        commonVertex.retainAll(triangle2);
        return commonVertex.size() >= 2; //Check the common vertex
    }

    //Using DFS we count the connected components of the mesh
    public List<List<Integer>> getConnectedComponentsIndex() {
        Set<Integer> visited = new HashSet<>();
        List<List<Integer>> components = new ArrayList<>();

        while (visited.size() < triangles.size()) {
            int startTriangle = getRandomUnvisitedTriangle(visited); //Indicate the starting traingle
            List<Integer> component = dfs(startTriangle, visited);
            components.add(component);
        }
        return components;
    }


//Method to select a random triangle of the mesh
    private int getRandomUnvisitedTriangle(Set<Integer> visited) {
        List<Integer> unvisited = new ArrayList<>(); //Not visited triangles
        for (int i = 0; i < triangles.size(); i++) {
            if (!visited.contains(i)) {
                unvisited.add(i);
            }
        }
        return unvisited.get(new Random().nextInt(unvisited.size())); //Return a random not visited triangle
    }


//Algorithm to search in a data structure
private List<Integer> dfs(int start, Set<Integer> visited) {
    List<Integer> element = new ArrayList<>();
    Stack<Integer> stack = new Stack<>(); //Track the visited triangles in a stack
    stack.push(start);
    while (!stack.isEmpty()) {
        int triangle = stack.pop();
        if (!visited.contains(triangle)) {
            visited.add(triangle);
            element.add(triangle); //Mark the actual triangle and push the rest od connected triangles
            stack.addAll(adjList.get(triangle));
        }
    }
    return element;
}

//Method to build the separate components of my connected component
    public List<List<List<Integer>>> getComponentsTriangles(List<List<Integer>> componentIndices) {
        List<List<List<Integer>>> componentsTriangles = new ArrayList<>();
        for (List<Integer> component : componentIndices) {
            List<List<Integer>> componentTriangles = new ArrayList<>();
            for (int index : component) {
                componentTriangles.add(triangles.get(index));
            }
            componentsTriangles.add(componentTriangles);
        }
        return componentsTriangles;
    }
}

public class Main {
    public static void main(String[] args) {
        String objFilePath = "C:/Users/gabri/OneDrive/Documentos/UNIVERSIDAD/6-Semestre/Geometría_Computacional/Periodo_Final/Test_Meshes/ConnectedComponents_01.obj";
        String outputFolder = "output_components";

        MeshData meshData = readObj(objFilePath);
        Mesh mesh = new Mesh(meshData.triangles, meshData.vertices); //Creates a new mesh with the triangles and vertex readed

        List<List<Integer>> componentIndices = mesh.getConnectedComponentsIndex();
        List<List<List<Integer>>> componentsTriangles = mesh.getComponentsTriangles(componentIndices); //Builds the mseh

        new File(outputFolder).mkdirs();
        for (int i = 0; i < componentsTriangles.size(); i++) {
            writeObj(outputFolder + "/component_" + (i + 1) + ".obj", componentsTriangles.get(i), meshData.vertices); //Save the connected component mesh in a folder
        }

        System.out.println("Number of connected components: " + componentsTriangles.size());
    }


    //Method to read an obj file
    private static MeshData readObj(String filePath) {
        List<List<Integer>> triangles = new ArrayList<>();
        List<List<Float>> vertices = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) { //Identifies the vertex of the obj and stores it in a List
                    String[] parts = line.trim().split("\\s+");
                    List<Float> vertex = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        vertex.add(Float.parseFloat(parts[i]));
                    }
                    vertices.add(vertex);
                } else if (line.startsWith("f ")) { //identifies the faces of the obj and stores it in a list
                    String[] parts = line.trim().split("\\s+");
                    List<Integer> triangle = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        int vertexIndex = Integer.parseInt(parts[i].split("/")[0]) - 1;
                        triangle.add(vertexIndex);
                    }
                    triangles.add(triangle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MeshData(triangles, vertices);
    }

    //method to write the obj file with the components information
    private static void writeObj(String filePath, List<List<Integer>> componentTriangles, List<List<Float>> vertices) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (List<Float> vertex : vertices) {
                writer.write("v ");
                for (float coord : vertex) {
                    writer.write(coord + " ");
                }
                writer.write("\n");
            }
            for (List<Integer> triangle : componentTriangles) { //using the vertex and face data, the information is written in a file
                writer.write("f ");
                for (int vertexIndex : triangle) {
                    writer.write((vertexIndex + 1) + " ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//Initialize my new mesh data
    class MeshData {
        List<List<Integer>> triangles;
        List<List<Float>> vertices;

        MeshData(List<List<Integer>> triangles, List<List<Float>> vertices) {
            this.triangles = triangles;
            this.vertices = vertices;
        }
    }

