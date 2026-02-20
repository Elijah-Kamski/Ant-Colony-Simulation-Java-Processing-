# 🐜 Ant Colony Simulation

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Processing](https://img.shields.io/badge/Library-Processing-blue.svg)](https://processing.org/)

An interactive, multi-agent biological simulation built with **Java** and the **Processing** library. This project simulates the collective behavior of two competing ant colonies (**Native vs. Invasive**) using decentralized logic, pheromone-based communication, and a dynamic environmental system.

---

## 🚀 Key Features

* **🧠 Advanced AI Steering** Ants utilize *Reynolds' steering behaviors* to wander, seek food, and follow pheromone trails with organic, lifelike movement.
  
* **📡 Pheromone Grid System** A high-performance grid architecture managing 4 independent data channels (Home/Food for each colony).

* **🍎 Newtonian Physics** Environmental entities like falling leaves are governed by gravity, air drag, and wind for realistic interaction.

* **🌿 Procedural Generation**
    * **Fractal Trees:** Recursive branching algorithms that ensure unique tree structures every run.
    * **Julia Set Roots:** Underground root systems generated via complex-plane fractal mathematics.

* **🌍 Dynamic World Engine** A full 24-hour day/night cycle with sky color interpolation and seasonal changes that directly affect simulation parameters.



---

## 🏗️ Project Architecture

The project follows a **Modular Design Pattern**, strictly separating simulation logic from the rendering engine to ensure scalability and clean code.

### 📁 Directory Structure

> **`setup/`** > The core engine. Contains the `Main` entry point (Java Entry Point), the `ProcessingSetup` (engine bridge), and the `IProcessingApp` interface.
>
> **`antcolony/`** > The simulation's heart. Contains the main controller and sub-packages for `entities`, `environment`, `data`, and `ui`.



---

## ⚔️ Evolutionary Dynamics & Sandbox Control

The simulation functions as a biological sandbox where the user can manipulate the evolutionary traits of two distinct species. This allows for real-time testing of survival strategies and population dominance.

| Colony | Initial Setup | User-Controlled Parameters |
| :--- | :--- | :--- |
| **🔵 Blue (Native)** | Baseline Colony | Metabolism rate, Spawn cost, and Pheromone memory. |
| **🔴 Red (Invasive)** | Challenger Colony | Metabolism rate, Spawn cost, and Pheromone memory. |

### 🔄 The Survival Feedback Loop
The ecosystem is driven by a resource-management cycle that the user can accelerate or constrain:
* **Energy Management:** Ants consume energy based on their **Metabolism** slider. Higher values increase speed but risk starvation.
* **Reproduction:** Returning food to the Queen increases the colony's food stock. A new agent is only produced if the stock exceeds the **Spawn Cost**.
* **Environmental Memory:** The **Memory** slider dictates how long pheromone trails persist. Shorter memory leads to chaotic wandering, while longer memory creates hyper-efficient "highways."

---

## 🛠️ How to Run

1.  **Java:** Ensure you have **Java 8 or higher** installed.
2.  **Dependencies:** Include the **Processing core library** (`core.jar`) in your project's classpath.
3.  **Entry Point:** Run the **`Main`** class located inside the `setup` package.

### 🎮 Controls

* **🖱️ Mouse:** Interact with real-time UI sliders and the "Restart" button.
* **⌨️ 'R' Key:** Trigger an immediate simulation reset.

---

## 📜 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for the full text.

---
