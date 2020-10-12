Program written in Java/Eclipse.

It can be ran through Eclipse with Histogram.java holding the main class for configuration. If it doesn't already, eclipse needs opencv added as a User Library.

Program requires a file path argument for the directory containing .tif images. All other image types are ignored.
If different file type is required: 

line 59: if(f.toString().endsWith(".tif")) {

.tif should be changed to desired file type


Running through command line.

cd to src file where Histogram.java is located

To compile:
javac -cp /C:/... file path to open cv jar:. Histogram.java

example: (personal using Ubuntu)
javac -cp /home/corwin/opencv_build/opencv/build/bin/opencv-440.jar:. Histogram.java

To run:
java -cp /C:/ ... file path to open cv jar:. -Djava.library.path=/C:/ ... path to open cv library Histogram 'path to image directory'

example: (personal using Ubuntu)
java -cp /home/corwin/opencv_build/opencv/build/bin/opencv-440.jar:.  -Djava.library.path=/home/corwin/opencv_build/opencv/build/lib/ Histogram /home/corwin/Desktop/histogram-images/

Running the program displays the Image and metadata, printed to verify image and histogram function completed along with a histogram print out.

To navigate the photos press n-next, p-previous, q-quit the program. This was used to cycle between the images to make observations between different methods used.

Images are set in a display browser to be viewed and compared as required. There are 4 original images, histogram equalization and histogram matching are done for each. Resulting in 12 images and the histogram to match.

Observations.txt: contains noted observations from applying algorithms to images during Histogram project

Testing.txt: contains testing conditions and expected responses


Printout Template Original Image:
image number: image name
histogram printout from index 1 - 256

Printout Template Modified Image:
image number: type of method applied: image name
histogram printout from index 1 - 256

Example Printout:
1 dark-image-for-histogram-equalization
0|0|0|0|0|0|0|0|0|0|0|0|0|17718|96|506| ....
2 Histogram Equalization: dark-image-for-histogram-equalization.tif
0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|18320|0|1750| ....
3 Histogram Matching: dark-image-for-histogram-equalization.tif
0|0|0|0|0|17718|0|0|0|96|0|506|0|0|0|1750| ....


