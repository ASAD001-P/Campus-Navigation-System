
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

class CampusNavigationSystem extends JFrame {

    private static final String FILE_DIRECTORY = "C:\\Users\\mdasa\\Documents\\NetBeansProjects\\New Folder\\InteractiveCampusNavigationSystem\\src\\";
    private static final String EDGE_WEIGHTS_FILE_WALK = "edge_weights_walk.txt";
    private static final String EDGE_WEIGHTS_FILE_WHEELCHAIR = "edge_weights_wheelchair.txt";
    private static final String EDGE_WEIGHTS_FILE_VEHICLE = "edge_weights_vehicle.txt";

    private Map<String, Point> buildings;
    private Map<String, Map<String, Integer>> distances;
    private java.util.List<String> shortestPath;
    private JButton backButton;
    private JComboBox<String> rootComboBox;
    private JComboBox<String> destinationComboBox;
    private JComboBox<String> transportationModeComboBox;
    private JButton refreshButton;
    public CampusNavigationSystem() {
        initializeCampus();
        loadEdgeWeights(); // Load edge weights based on the selected mode
        initrefreshButton();
        setTitle("Campus Navigation System");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create a JPanel for drawing the campus map
        JPanel campusPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCampus(g);
            }
        };

        // Create text fields and buttons for user input
        rootComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));
        destinationComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));
        transportationModeComboBox = new JComboBox<>(new String[]{"Walk"});
        JButton calculateButton = new JButton("Calculate Shortest Path");
        JButton modifyEdgeButton = new JButton("Modify Distance");
        JButton removeEdgeButton = new JButton("Remove Road");
        JButton backButton = new JButton("Back");
        // Add action listeners to the buttons

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new HomePage().setVisible(true);
            }
        });

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String rootBuilding = (String) rootComboBox.getSelectedItem();
                String destinationBuilding = (String) destinationComboBox.getSelectedItem();

                if (rootBuilding != null && destinationBuilding != null && !rootBuilding.equals(destinationBuilding)) {
                    findShortestPath(rootBuilding, destinationBuilding);
                    repaint(); // Redraw the campus to show the highlighted path
                } else {

                    JOptionPane.showMessageDialog(null, "Please select different buildings for the root and destination.");
                }
            }
        });

        if (HomePage.dev == 1) {
            modifyEdgeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> building1ComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));
                    JComboBox<String> building2ComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));
                    JTextField weightTextField = new JTextField(5);

                    JPanel modifyEdgePanel = new JPanel();
                    modifyEdgePanel.add(new JLabel("Start:"));
                    modifyEdgePanel.add(building1ComboBox);
                    modifyEdgePanel.add(new JLabel("End:"));
                    modifyEdgePanel.add(building2ComboBox);
                    modifyEdgePanel.add(new JLabel("Distance:"));
                    modifyEdgePanel.add(weightTextField);

                    int result = JOptionPane.showConfirmDialog(null, modifyEdgePanel, "Modify Edge", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        String building1 = (String) building1ComboBox.getSelectedItem();
                        String building2 = (String) building2ComboBox.getSelectedItem();

                        // Check if the selected buildings are different
                        if (building1 != null && building2 != null && !building1.equals(building2)) {
                            String newWeightStr = weightTextField.getText();
                            try {
                                int newWeight = Integer.parseInt(newWeightStr);
                                modifyEdgeWeight(building1, building2, newWeight);
                                saveEdgeWeights(); // Save the modified edge weights to the file
                                repaint(); // Redraw the campus to show the modified edge
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid integer for the Distance.");
                            }
                        } else {
                            // Handle the case where the selected buildings are the same
                            JOptionPane.showMessageDialog(null, "Please select different locations for modifying the Road.");
                        }
                    }
                }
            });
            removeEdgeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> building1ComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));
                    JComboBox<String> building2ComboBox = new JComboBox<>(buildings.keySet().toArray(new String[0]));

                    JPanel removeEdgePanel = new JPanel();
                    removeEdgePanel.add(new JLabel("Start:"));
                    removeEdgePanel.add(building1ComboBox);
                    removeEdgePanel.add(new JLabel("End:"));
                    removeEdgePanel.add(building2ComboBox);

                    int result = JOptionPane.showConfirmDialog(null, removeEdgePanel, "Remove Road", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        String building1 = (String) building1ComboBox.getSelectedItem();
                        String building2 = (String) building2ComboBox.getSelectedItem();

                        if (building1 != null && building2 != null && !building1.equals(building2)) {
                            removeEdge(building1, building2);
                            saveEdgeWeights();
                            repaint();
                        } else {

                            JOptionPane.showMessageDialog(null, "Please select different locations for removing the Road.");
                        }
                    }
                }
            });
        } else {
            // If Developer is 0, disable modify and remove edge buttons
            modifyEdgeButton.setEnabled(false);
            removeEdgeButton.setEnabled(false);
            modifyEdgeButton.setFocusable(false);
            removeEdgeButton.setFocusable(false);
        }
        
        transportationModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                loadEdgeWeights();

                repaint();

                revalidate();
                repaint();
            }
        });

        // Create a JPanel for user input components
        JPanel inputPanel = new JPanel();
        inputPanel.add(backButton);
        inputPanel.add(refreshButton);
        inputPanel.add(new JLabel("Start:"));
        inputPanel.add(rootComboBox);
        inputPanel.add(new JLabel("Destination:"));
        inputPanel.add(destinationComboBox);
        inputPanel.add(new JLabel("Mode:"));
        inputPanel.add(transportationModeComboBox);
        inputPanel.add(calculateButton);
        inputPanel.add(modifyEdgeButton);
        inputPanel.add(removeEdgeButton);
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(campusPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void initializeCampus() {
        buildings = new HashMap<>();
        distances = new HashMap<>();

        addBuilding("Gub_Main_Entrance", new Point(50, 50));
        addBuilding("Ground_D", new Point(950, 100));
        addBuilding("Field_Corner-A", new Point(500, 50));
        addBuilding("Ground_C", new Point(250, 200));
        addBuilding("Under_Construction", new Point(700, 240));
        addBuilding("Secondary_Gate", new Point(50, 350));
        addBuilding("Front", new Point(500, 300));
        addBuilding("L_Building", new Point(500, 350));
        addBuilding("WC", new Point(500, 425));
        addBuilding("Ground_B", new Point(575, 250));
        addBuilding("Ground_A", new Point(650, 100));
        addBuilding("Cafeteria", new Point(500, 500));
        addBuilding("Academic_Building", new Point(700, 200));
        addBuilding("J_Building", new Point(650, 350));
        addBuilding("K_BUilding", new Point(650, 500));
        addBuilding("Field_Corner-B", new Point(800, 50));
        addBuilding("Terminal_Gate-A", new Point(800, 220));
        addBuilding("Annex_Entrance", new Point(800, 350));
        addBuilding("Center_Space", new Point(800, 425));
        addBuilding("Terminal", new Point(950, 200));
        addBuilding("G_Building", new Point(950, 350));
        addBuilding("H_Building", new Point(950, 500));
        addBuilding("Back_Gate", new Point(1100, 50));
        addBuilding("Terminal_Gate-B", new Point(1100, 200));
        addBuilding("E_Building", new Point(1100, 350));
        addBuilding("F_Building", new Point(1100, 500));

        transportationModeComboBox = new JComboBox<>(new String[]{"Walk", "Wheelchair", "Vehicle"});

        loadEdgeWeights();
    }

     private void initrefreshButton() {
    refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Reset any state related to the shortest path
            shortestPath = null;
            
            // Redraw the campus to show the original map
            repaint();
        }
    });
}
    
    private void addBuilding(String name, Point location) {
        buildings.put(name, location);
        distances.put(name, new HashMap<>());
    }

    private void modifyEdgeWeight(String building1, String building2, int newWeight) {
        distances.get(building1).put(building2, newWeight);
        distances.get(building2).put(building1, newWeight);
    }

    private void removeEdge(String building1, String building2) {
        // Remove the edge between building1 and building2
        distances.get(building1).remove(building2);
        distances.get(building2).remove(building1);
    }

    private String getPreviousBuilding(String building, Map<String, Integer> distances) {
        Integer shortestDistance = distances.get(building);
        String previousBuilding = null;

        if (shortestDistance != null) {
            for (Map.Entry<String, Map<String, Integer>> entry : this.distances.entrySet()) {
                String connectedBuilding = entry.getKey();
                Integer distanceToConnectedBuilding = entry.getValue().get(building);

                if (distanceToConnectedBuilding != null
                        && distances.containsKey(connectedBuilding)
                        && distances.get(connectedBuilding) + distanceToConnectedBuilding == shortestDistance) {
                    previousBuilding = connectedBuilding;
                    break;
                }
            }
        }

        return previousBuilding;
    }

    private Map<String, Integer> dijkstra(String rootBuilding) {
        Map<String, Integer> distances = new HashMap<>();
        PriorityQueue<BuildingDistance> pq = new PriorityQueue<>();
        distances.put(rootBuilding, 0);
        pq.add(new BuildingDistance(rootBuilding, 0));

        while (!pq.isEmpty()) {
            BuildingDistance current = pq.poll();
            String currentBuilding = current.building;
            int currentDistance = current.distance;

            for (Map.Entry<String, Integer> entry : this.distances.get(currentBuilding).entrySet()) {
                String connectedBuilding = entry.getKey();
                int edgeWeight = entry.getValue();
                int newDistance = currentDistance + edgeWeight;

                if (!distances.containsKey(connectedBuilding) || newDistance < distances.get(connectedBuilding)) {
                    distances.put(connectedBuilding, newDistance);
                    pq.add(new BuildingDistance(connectedBuilding, newDistance));
                }
            }
        }

        return distances;
    }

    private void findShortestPath(String rootBuilding, String destinationBuilding) {
        Map<String, Integer> shortestDistances = dijkstra(rootBuilding);

        // Construct the shortest path
        shortestPath = new java.util.ArrayList<>();
        String currentBuilding = destinationBuilding;

        while (currentBuilding != null && !currentBuilding.equals(rootBuilding)) {
            shortestPath.add(currentBuilding);
            currentBuilding = getPreviousBuilding(currentBuilding, shortestDistances);
        }

        if (currentBuilding != null) {
            shortestPath.add(rootBuilding);
        }
    }

    private void drawCampus(Graphics g) {
        for (Map.Entry<String, Point> entry : buildings.entrySet()) {
            String buildingName = entry.getKey();
            Point buildingLocation = entry.getValue();

            // Draw buildings
            g.setColor(Color.BLUE);
            g.fillRect(buildingLocation.x, buildingLocation.y, 20, 20);

            // Draw building names centered within the vertices
            g.setColor(Color.BLACK);
            FontMetrics fontMetrics = g.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(buildingName);
            int x = buildingLocation.x + 10 - textWidth / 2;
            int y = buildingLocation.y - 5;
            g.drawString(buildingName, x, y);
        }

        if (shortestPath != null) {
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                String building1 = shortestPath.get(i);
                String building2 = shortestPath.get(i + 1);

                Point location1 = buildings.get(building1);
                Point location2 = buildings.get(building2);

                g.setColor(Color.GREEN);
                g.drawLine(
                        location1.x + 10,
                        location1.y + 10,
                        location2.x + 10,
                        location2.y + 10
                );
            }
        } else {
            for (Map.Entry<String, Point> entry : buildings.entrySet()) {
                String buildingName = entry.getKey();
                Point buildingLocation = entry.getValue();

                for (Map.Entry<String, Integer> distanceEntry : distances.get(buildingName).entrySet()) {
                    String connectedBuilding = distanceEntry.getKey();
                    Point connectedBuildingLocation = buildings.get(connectedBuilding);

                    g.setColor(Color.RED);
                    g.drawLine(
                            buildingLocation.x + 10,
                            buildingLocation.y + 10,
                            connectedBuildingLocation.x + 10,
                            connectedBuildingLocation.y + 10
                    );

                    g.setColor(Color.BLACK);
                    int distance = distanceEntry.getValue();
                    g.drawString(String.valueOf(distance),
                            (buildingLocation.x + connectedBuildingLocation.x) / 2,
                            (buildingLocation.y + connectedBuildingLocation.y) / 2
                    );
                }
            }
        }
    }

    private static class BuildingDistance implements Comparable<BuildingDistance> {

        private final String building;
        private final int distance;

        public BuildingDistance(String building, int distance) {
            this.building = building;
            this.distance = distance;
        }

        @Override
        public int compareTo(BuildingDistance other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    private void saveEdgeWeights() {
    String mode = (String) transportationModeComboBox.getSelectedItem();
    String edgeWeightsFile = FILE_DIRECTORY + File.separator + getEdgeWeightsFile(mode);
    try (PrintWriter writer = new PrintWriter(new FileWriter(edgeWeightsFile))) {
        for (Map.Entry<String, Map<String, Integer>> entry : distances.entrySet()) {
            String building1 = entry.getKey();
            Map<String, Integer> connectedBuildings = entry.getValue();
            for (Map.Entry<String, Integer> connectedBuilding : connectedBuildings.entrySet()) {
                String building2 = connectedBuilding.getKey();
                int weight = connectedBuilding.getValue();
                writer.println(building1 + " " + building2 + " " + weight);
            }
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void loadEdgeWeights() {
        String mode = (String) transportationModeComboBox.getSelectedItem();
        String edgeWeightsFile = FILE_DIRECTORY + getEdgeWeightsFile(mode);
        File file = new File(edgeWeightsFile);

        // Check if the file exists and is not empty
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                try {
                    String building1 = scanner.next();
                    String building2 = scanner.next();
                    if (scanner.hasNextInt()) {
                        int weight = scanner.nextInt();
                        distances.get(building1).put(building2, weight);
                        distances.get(building2).put(building1, weight);
                    } else {
                        System.err.println("Invalid line: " + scanner.nextLine());
                    }
                } catch (InputMismatchException e) {
                    System.err.println("Error reading line: " + scanner.nextLine());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private String getEdgeWeightsFile(String mode) {
        switch (mode) {
            case "Walk":
                return EDGE_WEIGHTS_FILE_WALK;
            case "Wheelchair":
                return EDGE_WEIGHTS_FILE_WHEELCHAIR;
            case "Vehicle":
                return EDGE_WEIGHTS_FILE_VEHICLE;
            default:
                throw new IllegalArgumentException("Invalid transportation mode");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CampusNavigationSystem::new);
    }
}
