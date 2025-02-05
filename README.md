# RiverCracker

**RiverCracker** is a java Emulation of minecraft 1.7-1.17 River generation.

## Important Limitations

There are specific types of rivers, called ocean rivers, that only generate when near an ocean biome, which this program is not currently able to emulate. To avoid issues, generally refrain from inputting coordinates for rivers that are closer than 150 blocks to an ocean. A fix for this should be coming shortly.


## Running the program

Make sure you have JDK installed https://www.oracle.com/ca-en/java/technologies/downloads/

Place RiverCracker.java in a new folder

Create a .bat file with the text: javac RiverCracker.java

Run the file and you should see multiple .class files form.

Optionally, create a new .bat file with the text: java RiverCracker when you run it it will activate a gui which displays possible rivers of a seed.

There are other options but the most practical way is to run the program in scale mode.

To run the program in scale mode create a new bat file for scale mode the syntax is java RiverCracker {xpos} {zpos} {scale} scale

For choosing your coordinates, select what appears to be the middle of a river stream for xpos and zpos. Then adjust the scale (8 works best in my opinion) to account for zoom. (If you are unsure what that means, just set the scale to 8.)

Repeat the pattern {xpos} {zpos} 8 as many times as you need.

If you create a .bat file and enter the following text:

java RiverCracker -672 712 8 -704 649 8 -745 665 8 -749 687 8 -763 695 8 -772 680 8 -971 706 8 -1127 758 8 -1061 1152 8 -1077 1186 8 -1113 1210 8 -1141 1187 8 -1154 1163 8 -1171 1141 8 -1147 1111 8 -1124 1090 8 -1097 1071 8 -1075 1085 8 scale

You can test it to make sure the program is working you can enter that prompt and run it and it will open a command prompt and run for a bit and it will tell you the percentage and then say Match found on Seed: 4060776 and then keep telling you the percentage but won't have any other results.

All the 8s in the command represent the scale.

The number immediately preceding the scale is the z-coordinate, and the number before that is the x-coordinate.

You can include as many points as you want. just ensure you specify an x-coordinate, a z-coordinate, and a scale for each point in that exact order, or the program will refuse to run. And don't forget the word scale after you've added all your points.

## Bonus feature

If you know where a structure with "liftable bits" is you can put the position of the structure in cubiomes run it on incremental until you get around 250k results.

You can then take those results and put it into a text file in the same folder as the river cracker and then put the textfile name at the end and make it so it only searches seeds where the structure can spawn which gives you less possible results if you don't have that many rivers.

Example:

java RiverCracker -672 712 8 -704 649 8 -745 665 8 -749 687 8 -763 695 8 -772 680 8 -971 706 8 -1127 758 8 -1061 1152 8 -1077 1186 8 -1113 1210 8 -1141 1187 8 -1154 1163 8 -1171 1141 8 -1147 1111 8 -1124 1090 8 -1097 1071 8 -1075 1085 8 scale Villages.txt
