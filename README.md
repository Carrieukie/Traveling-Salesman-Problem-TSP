# Traveling Salesman Problem Solver (Held-Karp Algorithm)

[Watch the video on Vimeo](https://vimeo.com/933548614)


## Introduction
This Android project is a solution to the Traveling Salesman Problem (TSP) using the Held-Karp algorithm. The Traveling Salesman Problem is a classic problem in the field of computer science and optimization, where the goal is to find the shortest possible route that visits each city exactly once and returns to the original city.

The Held-Karp algorithm is a dynamic programming approach used to solve the Traveling Salesman Problem (TSP), which is a classic problem in the field of computer science and optimization. In TSP, the objective is to find the shortest possible route that visits each city exactly once and returns to the original city.

The main idea behind the Held-Karp algorithm is to break down the TSP into smaller subproblems and solve them recursively while storing intermediate results to avoid redundant calculations. Here's a brief explanation of how the algorithm works:

 1. Initialization: The algorithm starts by initializing a table (often referred to as the memoization table) to store the optimal cost of visiting a subset of cities and ending at a specific city. Initially, this table is filled with placeholder values.

 2. Base Case: The base case of the recursion is when there's only one city left to visit. In this case, the cost of traveling from the starting city to this city and back to the starting city is calculated and stored in the memoization table.

 3. Recursive Step: For larger subsets of cities, the algorithm iterates through all possible choices of the next city to visit and recursively computes the cost of the remaining subproblem. The optimal cost for each subset of cities is stored in the memoization table.

 4. Optimal Solution Reconstruction: Once all subproblems have been solved, the optimal route can be reconstructed by tracing back the memoization table, starting from the final city and ending at the starting city.

 5. Complexity Analysis: The time complexity of the Held-Karp algorithm is O(n^2 * 2^n), where 'n' is the number of cities. This complexity arises due to the number of subsets of cities to be considered and the time taken to compute the optimal cost for each subset.

You can checkout its implementation in this project [here](https://github.com/Carrieukie/TravellingSalesMan/blob/main/app/src/main/java/com/karis/travellingsalesman/utils/OptimizationUtils.kt)


## Features
- Solve TSP instances with a given set of cities and their distances.
- Display the optimal route along with the total distance traveled.
- Efficiently handle large instances of the TSP.
- Intuitive user interface for easy interaction.

## Installation
1. Clone the repository to your local machine.
   ```bash
   git clone git@github.com:Carrieukie/TravellingSalesMan.git
   ```
2. Open the project in Android Studio.

3. Obtain an API key for the Google Maps API by following the instructions [here](https://developers.google.com/maps/documentation/android-sdk/get-api-key).

4. Add the API key to the `local.properties` file in the root directory of your project:
```properties
MAPS_API_KEY=your_api_key_here
```

5. Enable the Places API,Distance Matrix API and Routes API in your Google Cloud Platform (GCP) project by following the instructions [here](https://mapsplatform.google.com/#get-started).

## Usage
1. Launch the application.
2. Search for the cities into the provided fields.
3. Click on the "Optimize" button to calculate the optimal route using the Held-Karp algorithm.
4. The optimal route and total distance traveled will be displayed.
5. Explore different TSP instances and observe the efficiency of the Held-Karp algorithm.


## Contributing
Contributions are welcome! If you find any issues or have suggestions for improvements, please feel free to open an issue or create a pull request.

## License
This project is licensed under the MIT License.

## Acknowledgments
- The project utilizes the Held-Karp algorithm for solving the Traveling Salesman Problem.
- Special thanks to the developers and contributors of the libraries and resources used in this project.
