# RiverCracker

RiverCracker is a Java emulation of Minecraft 1.7–1.17 river generation. It replicates the river-generation algorithms found in Minecraft (with some limitations) and is organized into multiple source files for modularity.


## Important Limitation

There are specific types of rivers called ocean rivers, that only generate when near an ocean biome, which this program is not currently able to do. (A fix for this should be coming soon.)

To be safe, don't input coordinates for rivers that don't split off into two rivers before entering an ocean and don't use any in general that connect directly to an ocean within like 200 blocks.

## Running the program

  0. Make sure you have the Java Development Kit (JDK) installed.
    Download the JDK from: https://www.oracle.com/ca-en/java/technologies/downloads/

  1. Place all files (RiverCracker.java, RiverGenerator.java, Bruteforce.java, RiverGUI.java) in the same folder.
  
  2. Open a command prompt (or terminal) in that folder.

  3. Compile the program using

     javac RiverCracker.java RiverGenerator.java Bruteforce.java RiverGUI.java
     
## Running the Program

  • To launch the GUI run
  
         java RiverCracker gui

  • To generate a river image for a specific seed (saved as a PNG file):
         java RiverCracker generate <seed> <output.png>
     Example:
     
         java RiverCracker generate 123456789 output.png

  • To run the program in bruteforce mode (searching for seeds based on bounding boxes or seed files):
         java RiverCracker bruteforce [bounding box numbers and/or seedfile.txt]
     
There are two ways to specify the bounding boxes:

       - **Scale Mode (Default):**
           Provide groups of three numbers: {x-coordinate} {z-coordinate} {scale}.
           The program converts each triple into a box centered at (x, z) with the specified scale.
           Example:
               java RiverCracker bruteforce -672 712 8 -704 649 8 -745 665 8
(For your X and Z Cooridinatees it is recommended to go to what is visually the middle of a river and then setting scale to 8, in the example above each “8” represents the scale value.)

       - **Range Mode:**
           If you prefer to specify exact bounding box coordinates, supply groups of four numbers (xmin, zmin, xmax, zmax) and append the word **range** at the end.
           Example:
               java RiverCracker bruteforce 320 -103 323 -97 -200 400 -193 408 range
(Make sure to put the lower number first. For negative number it's reversed i.e -201 is considered lower than -200)


## Structures

You can put in the name of text file (ending in “.txt”) as at the end of your command to have less false positive.

If you know where a structure with "liftable bits" is you can put the position of the structure in cubiomes (make sure you have the right version) run it on incremental until you get around 250k results.
Example:
  
      java RiverCracker bruteforce -672 712 8 -704 649 8 -745 665 8 -1075 1085 8 Villages.txt


## Structure (Code)

  • RiverCracker.java – Contains the main entry point and mode-selection logic.
  
  • RiverGenerator.java – Contains the core river generation logic and helper methods.
  
  • Bruteforce.java – Implements the bruteforce seed search mode.
  
  • RiverGUI.java – Implements the GUI and image generation mode.
