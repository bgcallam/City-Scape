/**
 *  MotionTrack by Ben Callam
 *  Class to track change in an image over time
 *   - based on "Frame Differencing" example by Golan Levin
 */

class MotionTrack
{
  int numPixels;
  int[] previousFrame;
  float threshold = 0;
  int[] movements;
  PImage returnImg;
  int movementSum;

  /*******
   ** Constructor
   *******/
  MotionTrack(PImage newFrame) {
    // Make the pixels[] array available for direct manipulation
    newFrame.loadPixels();
    numPixels = newFrame.pixels.length;
    
    returnImg = new PImage(newFrame.width,newFrame.height);
    // the array to store the background image
    previousFrame = new int[numPixels];
    // initialize the previousframe with something
    previousFrame = newFrame.pixels;

    // the array to track movement
    movements = new int[numPixels];
  }

  /*******
   ** checks for motion against the previous frame
   *******/
  PImage processImage(PImage thisFrame) {

    // Make the pixels[] array available for direct manipulation
    thisFrame.loadPixels();

    int[] diffMask;
    diffMask = new int[numPixels];
    movementSum = 0;

    for (int i = 0; i < numPixels; i++) { // For each pixel in the video frame...
      // Fetch the current color in that location, and also the color
      // of the background in that spot
      color currColor = thisFrame.pixels[i];
      color prevColor = previousFrame[i];
      // Extract the red, green, and blue components of the current pixelÕs color
      int currR = (currColor >> 16) & 0xFF;
      int currG = (currColor >> 8) & 0xFF;
      int currB = currColor & 0xFF;
      // Extract the red, green, and blue components of the background pixelÕs color
      int prevR = (prevColor >> 16) & 0xFF;
      int prevG = (prevColor >> 8) & 0xFF;
      int prevB = prevColor & 0xFF;
      // Compute the difference of the red, green, and blue values
      int diffR = abs(currR - prevR);
      int diffG = abs(currG - prevG);
      int diffB = abs(currB - prevB);


      // If the pixel changed record movement
      float change = (diffR + diffG + diffB);
      if(change>75){
        // blend the old and the new
        movementSum += change;
        movements[i] = int((movements[i]+change)/2);
      }
      else{ // subtract movement marker
        if(movements[i]>0) {
          movements[i] = int(movements[i] - (movements[i]/frameRate/2));
        }
        else { 
          movements[i] = 0;
        }
      }

      // Store the current frame for next time.
      previousFrame[i] = thisFrame.pixels[i];
    }
    returnImg.pixels = movements;
    return returnImg;
  }
  
  
 // Returns the toal movement so far 
 int getTotal(){
   return movementSum; 
 }

} // END CLASS MotionTrack
