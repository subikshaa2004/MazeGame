import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TextArea;
import javafx.geometry.Pos;
import java.util.*;

public class Mazegame1 extends Application {
    private int[][] maze;
    private int numRows;
    private int numCols;
    private int playerRow;
    private int playerCol;
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;
    private Canvas canvas;
    private GraphicsContext gc;
    private Map<Position, Obstacle> obstacles; 
    private Map<Position, String> obstacleQuestions; 
    private List<Position> shortestPath = new ArrayList<>();
    private boolean exploring = false;
    private boolean isCompleted = false;
    private int shortestPathLength = 0;
    private int totalMoves = 0;
    private long startTime;
    private Label movesLabel;
    private Label timeLabel;
    private Timeline timeline;
    private Alert alert;
    private int[][] visitedNodes;

    public Mazegame1() {
        obstacles = new HashMap<>();
        obstacleQuestions = new HashMap<>();
    }

public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();
    HBox buttonBox = new HBox(10); 
    Button startButton = new Button("Start Game");
    startButton.setOnAction(event -> startGame());
    buttonBox.setStyle("-fx-padding: 10px;");

    Button newMazeButton = new Button("New Maze");
    newMazeButton.setOnAction(e -> generateNewMaze());

    Button shortestPathButton = new Button("Show Shortest Path");
    shortestPathButton.setOnAction(e -> displayShortestPath());

    Button tryAgainButton = new Button("Try Again");
    tryAgainButton.setOnAction(e -> resetGame());

    Button instructionsButton = new Button("Instructions");
    instructionsButton.setOnAction(e -> showInstructions());

    Button dfsButtonI = new Button("DFS Info");
    dfsButtonI.setOnAction(e -> showDFSInfo());

    Button bfsButtonI = new Button("BFS Info");
    bfsButtonI.setOnAction(e -> showBFSInfo());

    Label movesLabel = new Label("Moves: 0");
    Label timeLabel = new Label("Time: 00:00");

    VBox root1 = new VBox(20);
    root1.setAlignment(Pos.CENTER);
    root1.getChildren().addAll(startButton, instructionsButton, dfsButtonI, bfsButtonI);

    HBox dfsBfsButtonBox = new HBox(10);
    Button dfsButton = new Button("DFS Visualization");
    dfsButton.setOnAction(e -> visualizeAlgorithm("DFS"));

    Button bfsButton = new Button("BFS Visualization");
    bfsButton.setOnAction(e -> visualizeAlgorithm("BFS"));

    newMazeButton.setFocusTraversable(false);
    shortestPathButton.setFocusTraversable(false);
    tryAgainButton.setFocusTraversable(false);
    dfsButton.setFocusTraversable(false);
    bfsButton.setFocusTraversable(false);
    instructionsButton.setFocusTraversable(false);
    dfsButtonI.setFocusTraversable(false);
    bfsButtonI.setFocusTraversable(false);

    buttonBox.getChildren().addAll(newMazeButton, shortestPathButton, tryAgainButton, movesLabel, timeLabel,instructionsButton); 
    dfsBfsButtonBox.getChildren().addAll(dfsButton, bfsButton,dfsButtonI,bfsButtonI);

    root.setTop(buttonBox);
    root.setBottom(dfsBfsButtonBox);

    canvas = new Canvas(600, 600);
    gc = canvas.getGraphicsContext2D();
    root.setCenter(canvas);

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Maze Game");
    primaryStage.show();

    generateNewMaze();
    drawMaze();

    canvas.setFocusTraversable(true);

    canvas.setOnKeyPressed(event -> {
        handleKeyPress(event.getCode());
        drawMaze();
        totalMoves++;
        movesLabel.setText("Moves: " + totalMoves);

        if (playerRow == endRow && playerCol == endCol && !isCompleted) {
            isCompleted = true;
            displayCompletionDialog(movesLabel, timeLabel);
        }
    });

    timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
        if (!isCompleted) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - startTime) / 1000;
            timeLabel.setText("Time: " + String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60));
        }
    }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
}

private void startGame() {
    Mazegame1 mazeGame = new Mazegame1(); 
    Stage gameStage = new Stage();
    mazeGame.start(gameStage);
}

private void showInstructions() {
    String instructions = "Welcome to the Maze Game!\n\n" +
            "Objective: Reach the destination point while avoiding obstacles.\n\n" +
            "Controls:\n" +
            "- Use arrow keys to move the player.\n" +
            "- Click 'New Maze' to generate a new maze.\n" +
            "- Click 'Show Shortest Path' to display the shortest path to the destination.\n" +
            "- Click 'Try Again' to reset the game.\n" +
            "- Click 'DFS Visualization' to visualize the Depth First Search algorithm.\n" +
            "- Click 'BFS Visualization' to visualize the Breadth First Search algorithm.\n\n" +
            "Obstacles: Blue blocks represent obstacles. You must answer a question to pass through them.\n\n" +
            "Have fun playing!";
            
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Instructions");
    alert.setHeaderText(null);
    alert.setContentText(instructions);
    alert.showAndWait();
}

private void showDFSInfo() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("DFS Traversal Info");
    alert.setHeaderText(null);

    TextArea textArea = new TextArea();
    textArea.setEditable(false);
    textArea.setWrapText(true);
    textArea.setPrefWidth(400); 
    textArea.setPrefHeight(300); 
    textArea.setText("DFS (Depth-First Search) Traversal:\n\n" +
            "DFS is a graph traversal algorithm that explores as far as possible along each branch before backtracking.\n" +
            "It starts at the root node and explores each branch completely before moving to the next branch.\n" +
            "DFS can be implemented using a stack data structure to keep track of visited nodes.\n\n" +
            "Advantages:\n" +
            "- Requires less memory compared to BFS\n" +
            "- Suitable for solving puzzles and mazes\n\n" +
            "Disadvantages:\n" +
            "- May get stuck in infinite loops on graphs with cycles\n" +
            "- Not guaranteed to find the shortest path\n");

    alert.getDialogPane().setContent(textArea);

    alert.showAndWait();
}

private void showBFSInfo() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("BFS Traversal Info");
    alert.setHeaderText(null);

    TextArea textArea = new TextArea();
    textArea.setEditable(false);
    textArea.setWrapText(true);
    textArea.setPrefWidth(400); 
    textArea.setPrefHeight(300); 
    textArea.setText("BFS (Breadth-First Search) Traversal:\n\n" +
            "BFS is a graph traversal algorithm that explores all the neighboring nodes at the present depth before moving to the next depth level.\n" +
            "It starts at the root node and explores all nodes at the present depth before moving to the next depth level.\n" +
            "BFS can be implemented using a queue data structure to keep track of visited nodes.\n\n"+
            "Advantages:\n" +
            "- Finds the shortest path in an unweighted graph\n" +
            "- Can be used to find connected components in a graph\n\n" +
            "Disadvantages:\n" +
            "- Requires more memory compared to DFS\n" +
            "- Not suitable for large graphs due to memory consumption\n" +
            "- Slower than DFS for finding paths\n");

    alert.getDialogPane().setContent(textArea);

    alert.showAndWait();
}

    private void handleKeyPress(KeyCode keyCode) {
    if (!exploring && !isCompleted) {
        int newRow = playerRow;
        int newCol = playerCol;

        switch (keyCode) {
            case UP:
                newRow--;
                break;
            case DOWN:
                newRow++;
                break;
            case LEFT:
                newCol--;
                break;
            case RIGHT:
                newCol++;
                break;
            default:
                return;
        }

        if (isValidMove(newRow, newCol)) {
            for (Position position : obstacles.keySet()) {
                if (position.row == newRow && position.col == newCol) {
                    Obstacle obstacle = obstacles.get(position);
                    if (obstacle.isVisible()) {
                        displayQuestionDialog(obstacle);
                        return; 
                    }
                }
            }
            playerRow = newRow;
            playerCol = newCol;
        }
    } else {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exploring Maze");
        alert.setHeaderText(null);
        alert.setContentText("Please wait until the DFS/BFS visualization is completed.");
        alert.showAndWait();
    }
}
    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < numRows && col >= 0 && col < numCols && maze[row][col] == 0;
    }
private void displayQuestionDialog(Obstacle obstacle) {
    String question = obstacle.getQuestion();
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Obstacle");
    dialog.setHeaderText("Answer the question to proceed:");
    dialog.setContentText(question);

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(answer -> {
        if (obstacle.checkAnswer(answer)) {
            obstacle.setVisible(false); 
            drawMaze(); 
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Incorrect Answer");
            alert.setHeaderText(null);
            alert.setContentText("Sorry, that's incorrect. You cannot proceed until you answer correctly.");
            alert.showAndWait();
        }
    });
}

private boolean checkAnswer(String answer) {
    return answer.trim().equalsIgnoreCase("2");
}


    private double cellWidth;
    private double cellHeight;

    private void generateNewMaze() {
        totalMoves = 0;
        startTime = System.currentTimeMillis();
        exploring = false;
        isCompleted = false;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Maze Dimensions");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter number of rows and columns separated by space:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] dimensions = result.get().split(" ");
            numRows = Integer.parseInt(dimensions[0]);
            numCols = Integer.parseInt(dimensions[1]);
            maze = generateMaze(numRows, numCols);
            obstacles.clear(); 
            obstacleQuestions.clear(); 
            visitedNodes = new int[numRows][numCols]; 
            playerRow = 1;
            playerCol = 1;
            endRow = numRows - 3;
            endCol = numCols - 3;

            findShortestPath();
            generateObstacles(); 
        } else {
            System.exit(0);
        }

        drawMaze();
    }

    private void findShortestPath() {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        int[][] distances = new int[numRows][numCols];
        Node[][] parents = new Node[numRows][numCols];
        boolean[][] visited = new boolean[numRows][numCols];

        queue.offer(new Node(playerRow, playerCol, 0));
        distances[playerRow][playerCol] = 0;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            int row = current.row;
            int col = current.col;
            if (row == endRow && col == endCol) {
                buildShortestPath(parents, current);
                return;
            }
            visited[row][col] = true;
            for (int[] dir : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (isValidMove(newRow, newCol) && !visited[newRow][newCol]) {
                    int newDistance = distances[row][col] + 1; 
                    if (newDistance < distances[newRow][newCol] || distances[newRow][newCol] == 0) {
                        distances[newRow][newCol] = newDistance;
                        queue.offer(new Node(newRow, newCol, newDistance));
                        parents[newRow][newCol] = current;
                    }
                }
            }
        }
    }

    private void buildShortestPath(Node[][] parents, Node endPosition) {
        shortestPath.clear();
        Node current = endPosition;

        while (current != null) {
            shortestPath.add(new Position(current.row, current.col));
            current = parents[current.row][current.col];
        }

        Collections.reverse(shortestPath);
        shortestPathLength = shortestPath.size() - 1;
    }

    private void displayShortestPath() {
        drawMaze();
        gc.setStroke(Color.VIOLET); 
        gc.setLineWidth(2);
        for (Position position : shortestPath) {
            double centerX = (position.col * cellWidth) + (cellWidth / 2);
            double centerY = (position.row * cellHeight) + (cellHeight / 2);
            gc.strokeRect(centerX - (cellWidth / 4), centerY - (cellHeight / 4), cellWidth / 2, cellHeight / 2);
        }
    }

    private void displayCompletionDialog(Label movesLabel, Label timeLabel) {
        long endTime = System.currentTimeMillis();
        long elapsedTime = (endTime - startTime) / 1000;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText(null);

        String message = String.format("You have reached the destination!\nTotal moves taken: %d\nShortest path length: %d\nTime taken: %02d:%02d", totalMoves, shortestPathLength, elapsedTime / 60, elapsedTime % 60);

        alert.setContentText(message);
        alert.showAndWait();

        movesLabel.setText("Moves: 0");
        timeLabel.setText("Time: 00:00");
    }

    private int[][] generateMaze(int numRows, int numCols) {
        Random random = new Random();
        int[][] maze = new int[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                maze[i][j] = 1;
            }
        }

        int startX = 1;
        int startY = 1;
        maze[startY][startX] = 0;

        Stack<Integer> stackX = new Stack<>();
        Stack<Integer> stackY = new Stack<>();
        stackX.push(startX);
        stackY.push(startY);

        while (!stackX.isEmpty()) {
            int x = stackX.peek();
            int y = stackY.peek();
            boolean foundNeighbor = false;

            int[] dirs = {0, 1, 2, 3};
            for (int i = 0; i < 4; i++) {
                int index = random.nextInt(4);
                int temp = dirs[i];
                dirs[i] = dirs[index];
                dirs[index] = temp;
            }

            for (int dir : dirs) {
                int newX = x;
                int newY = y;
                switch (dir) {
                    case 0:
                        newY -= 2;
                        break;
                    case 1:
                        newY += 2;
                        break;
                    case 2:
                        newX -= 2;
                        break;
                    case 3:
                        newX += 2;
                        break;
                }
                if (newX > 0 && newY > 0 && newX < numCols - 1 && newY < numRows - 1 && maze[newY][newX] == 1) {
                    maze[newY][newX] = 0;
                    maze[y + (newY - y) / 2][x + (newX - x) / 2] = 0;
                    stackX.push(newX);
                    stackY.push(newY);
                    foundNeighbor = true;
                    break;
                }
            }

            if (!foundNeighbor) {
                stackX.pop();
                stackY.pop();
            }
        }

        int endX = numCols - 3;
        int endY = numRows - 3;
        maze[endY][endX] = 0;
        maze[endY - 1][endX] = 0;

        return maze;
    }

    private void resetGame() {
        totalMoves = 0;
        playerRow = 1;
        playerCol = 1;
        isCompleted = false;
        exploring = false;
        startTime = System.currentTimeMillis();
        drawMaze();
    }

    private void visualizeAlgorithm(String algorithm) {
        resetGame();
        exploring = true;

        switch (algorithm) {
            case "DFS":
                visualizeDFS();
                break;
            case "BFS":
                visualizeBFS();
                break;
        }
    }

     private void visualizeDFS() {
        resetGame();
        exploring = true;

        Stack<Position> stack = new Stack<>();
        visitedNodes = new int[numRows][numCols]; 
        stack.push(new Position(playerRow, playerCol));
        final int[] visitedNodeCounter = {1}; 
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (!stack.isEmpty()) {
                Position current = stack.pop();
                playerRow = current.row;
                playerCol = current.col;
                if (visitedNodes[playerRow][playerCol] == 0) {
                    visitedNodes[playerRow][playerCol] = visitedNodeCounter[0]++; 
                }
                drawMaze();
                if (current.row == endRow && current.col == endCol) {
                    exploring = false;
                    System.out.println("DFS Completed!");
                    playerRow = startRow;  
                    playerCol = startCol;
                    timeline.stop(); 
                    return;
                }
                for (int[] dir : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                    int newRow = current.row + dir[0];
                    int newCol = current.col + dir[1];
                    if (isValidMove(newRow, newCol) && visitedNodes[newRow][newCol] == 0) {
                        stack.push(new Position(newRow, newCol));
                    }
                }
            } else {
                exploring = false;
                System.out.println("DFS Completed!");
                playerRow = startRow; 
                playerCol = startCol;
                timeline.stop(); 
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void visualizeBFS() {
        resetGame();
        exploring = true;

        Queue<Position> queue = new LinkedList<>();
        visitedNodes = new int[numRows][numCols]; 
        queue.offer(new Position(playerRow, playerCol));
        final int[] visitedNodeCounter = {1}; 

        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (!queue.isEmpty()) {
                Position current = queue.poll();
                playerRow = current.row;
                playerCol = current.col;
                if (visitedNodes[playerRow][playerCol] == 0) {
                    visitedNodes[playerRow][playerCol] = visitedNodeCounter[0]++; 
                }
                drawMaze();
                if (current.row == endRow && current.col == endCol) {
                    exploring = false;
                    System.out.println("BFS Completed!");
                    playerRow = startRow;  
                    playerCol = startCol;
                    timeline.stop();
                    return;
                }
                for (int[] dir : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                    int newRow = current.row + dir[0];
                    int newCol = current.col + dir[1];
                    if (isValidMove(newRow, newCol) && visitedNodes[newRow][newCol] == 0) {
                        queue.offer(new Position(newRow, newCol));
                    }
                }
            } else {
                exploring = false;
                System.out.println("BFS Completed!");
                playerRow = startRow; 
                playerCol = startCol;
                timeline.stop();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

private void generateObstacles() {
    Random random = new Random();
    int numObstacles = random.nextInt(5) + 5; 
    List<Position> pathCells = new ArrayList<>(shortestPath);
    Set<String> usedQuestions = new HashSet<>(); 
    Collections.shuffle(pathCells);

    for (int i = 0; i < Math.min(numObstacles, pathCells.size()); i++) {
        Position obstaclePosition = pathCells.get(i);
        String question;
        String answer;
        do {
            int questionIndex = random.nextInt(10); 
            switch (questionIndex) {
                case 0:
                    question = "What is the capital of France?";
                    answer = "Paris";
                    break;
                case 1:
                    question = "What is 5 + 3?";
                    answer = "8";
                    break;
                case 2:
                    question = "How many planets are in the solar system?";
                    answer = "8";
                    break;
                case 3:
                    question = "What is the square root of 16?";
                    answer = "4";
                    break;
                case 4:
                    question = "What is the capital of Japan?";
                    answer = "Tokyo";
                    break;
                case 5:
                    question = "what is the basic principle of operation in stack";
                    answer = "last in first out";
                    break;
                case 6:
                    question = "what is the basic principle of operation in queue";
                    answer = "first in first out";
                    break;
                case 7:
                    question = "Is random access of elements possible in array?";
                    answer = "True";
                    break;
                case 8:
                    question = "Is random access of element faster in linked list";
                    answer = "false";
                    break;
                case 9:
                    question = "Who is the prime minister of India";
                    answer = "Dr. Narendra Modi";
                    break;
                case 10:
                    question = "Which is the national animal of India";
                    answer = "Tiger";
                    break;
                default:
                    question = "What is the capital of Germany?";
                    answer = "Berlin";
                    break;
            }
        } while (!usedQuestions.add(question)); 

        obstacles.put(obstaclePosition, new Obstacle(question, answer));
        obstacleQuestions.put(obstaclePosition, question);
    }
}

private boolean isValidObstaclePosition(int row, int col) {
    if ((row == startRow && col == startCol) || (row == endRow && col == endCol)) {
        return false;
    }
    for (Position position : shortestPath) {
        if (position.row == row && position.col == col) {
            return false;
        }
    }
    return true;
}

    private String generateQuestion() {
        Random random = new Random();
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        int result = num1 + num2;
        return String.format("What is the sum of %d and %d?", num1, num2);
    }

    private void drawMaze() {
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    double width = canvas.getWidth();
    double height = canvas.getHeight();
    cellWidth = width / numCols;
    cellHeight = height / numRows;

    for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numCols; j++) {
            double x = j * cellWidth;
            double y = i * cellHeight;
            Color darkViolet=Color.rgb(53,8,107);
            if (maze[i][j] == 1) {
                gc.setFill(darkViolet);
                gc.fillRect(x, y, cellWidth, cellHeight);
            } else {
                gc.setFill(Color.GREY);
                gc.fillRect(x, y, cellWidth, cellHeight);
            }
            if (i == playerRow && j == playerCol) {
                gc.setFill(Color.GREEN);
                gc.fillOval(x + cellWidth * 0.25, y + cellHeight * 0.25, cellWidth * 0.5, cellHeight * 0.5);
            }
            if (i == endRow && j == endCol) {
                gc.setFill(Color.RED);
                gc.fillOval(x + cellWidth * 0.25, y + cellHeight * 0.25, cellWidth * 0.5, cellHeight * 0.5);
            }
             if (visitedNodes != null && visitedNodes[i][j] != 0) {
                gc.setFill(Color.YELLOW);
                gc.fillRect(x, y, cellWidth, cellHeight);
                gc.setFill(Color.BLACK);
                gc.fillText(Integer.toString(visitedNodes[i][j]), x + cellWidth * 0.5, y + cellHeight * 0.5);
            }
            for (Position position : obstacles.keySet()) {
                Obstacle obstacle = obstacles.get(position);
                if (position.row == i && position.col == j && obstacle.isVisible()) {
                    gc.setFill(Color.BLUE);
                    gc.fillRect(x, y, cellWidth, cellHeight);
                }
            }
        }
    }
}

    public static void main(String[] args) {
        launch(args);
    }

    private static class Node implements Comparable<Node> {
        int row;
        int col;
        int distance;

        Node(int row, int col, int distance) {
            this.row = row;
            this.col = col;
            this.distance = distance;
        }

        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    private static class Position 
    {
        int row;
        int col;

        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

   private static class Obstacle 
   {
    private String question;
    private String answer;
    private boolean visible;

    Obstacle(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.visible = true;
    }

    String getQuestion() {
        return question;
    }

    boolean isVisible() {
        return visible;
    }

    void setVisible(boolean visible) {
        this.visible = visible;
    }

    boolean checkAnswer(String answer) {
        return this.answer.equalsIgnoreCase(answer.trim());
    }
}
}