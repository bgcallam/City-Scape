import processing.core.*; import processing.opengl.*; import javax.media.opengl.*; import javax.media.opengl.glu.*; import processing.video.*; import imageadjuster.*; import damkjer.ocd.*; import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; public class CityScape extends PApplet {







/***************************************************
 *  Based on "Explode" by Daniel Shiffman.
 *  Reworked by Benjamin Callam 4/15/08
 * 
 ***************** SETTINGS ************************/
int windowWidth = screen.width;   // dimensions of the window
int windowHeight =screen.height;
int imageWidth = windowWidth/2;                    // dimesions of the camera capture
int imageHeight = windowHeight/2;
int cellsize = 40;      // size of the cells
int framerate = 30;     // frames per second
boolean renderOut = false;
boolean fogToggle = true;  // control with 'f' key
boolean blendToggle = false; // control with 'b' key
PFont font_1 = createFont("SFSquareHeadExtended", 10);
PFont font_2 = createFont("SFSquareHeadExtended", 24);
float[] fogColor = { 
  1.0f, 1.0f, 1.0f, 1.0f};
/***************************************************/


ImageAdjuster adjust = new ImageAdjuster(this);
Capture movieFrame; 
PImage tempImage;
boolean newFrame=false;
MotionTrack tracker;
Camera camera1, camera2;
boolean toggleImage = false;
int COLS, ROWS;  // Number of columns and rows in our system

PGraphicsOpenGL pgl;
GL opengl;
GLU glu;



// ***************************************
// Run in fullscreen NOTE: the name below has to be the same as the app
static public void main(String args[]) {
  PApplet.main(new String[] { 
    "--present", "CityScape"                                                                     }
  );
}

public void setup()
{
  //background(0);
  size(windowWidth, windowHeight, OPENGL);
  //hint(ENABLE_OPENGL_4X_SMOOTH); // don't really know what this does...
  noStroke();
  lights();   
  //spotLight(0, 0, 0, 200, 200, 200, -1,0,0, PI/16,2 );
  //directionalLight(204, 204, 204, -1, 0, 0);
lightSpecular(204, 204, 204);
specular(255, 255, 255);


  camera1 = new Camera(this,windowWidth/2,windowHeight/2,windowWidth,windowWidth/2,windowHeight/2,0);
  //camera2 = new Camera(this,windowWidth/2,windowHeight/2,300);

  pgl = ((PGraphicsOpenGL)g);
  opengl = pgl.gl;
  glu = ((PGraphicsOpenGL)g).glu;
  opengl.glHint( GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST );   //Realy Nice perspective calculations

  smooth();
  perspective( PI*0.25f, windowWidth/windowHeight, 1.0f, 5000 );

frameRate(framerate);
  movieFrame = new Capture(this, imageWidth, imageHeight, 30);    // create the video stream
  tempImage = new PImage(windowWidth, windowHeight);
  tracker = new MotionTrack(new PImage(imageWidth,imageHeight));  // create the motion tracker
  COLS = width/cellsize;                                          // Calculate # of columns
  ROWS = height/cellsize;                                         // Calculate # of rows
  colorMode(RGB,255,255,255,100);                                 // Setting the colormode
  textFont(font_1);
  // loadPixels();
}



// ==================================================
// captureEvent()
// ==================================================
public void captureEvent(Capture cam)
{
  movieFrame.read();
  newFrame = true;
}


public void draw() {

  if (newFrame)
  {
    newFrame=false;

    lights();
    directionalLight(250, 250, 250, -1, -1, -1);


    // OpenGL fog
    if(fogToggle == true){
      //opengl = pgl.beginGL();
      opengl.glEnable( GL.GL_FOG );
      opengl.glFogi( GL.GL_FOG_MODE, GL.GL_LINEAR );
      opengl.glFogfv( GL.GL_FOG_COLOR, fogColor, 0);
      opengl.glFogf( GL.GL_FOG_DENSITY, 0.0007f );
      opengl.glFogf( GL.GL_FOG_START, 400 );
      opengl.glFogf( GL.GL_FOG_END, 1800 );
      opengl.glHint( GL.GL_FOG_HINT, GL.GL_NICEST );
      //pgl.endGL();
    } 
    else {
      opengl.glDisable( GL.GL_FOG);
    }

    // OpenGL blend
    opengl = pgl.beginGL();
    if(blendToggle==true){
      opengl.glDepthMask( false );
      // opengl.glDisable( GL.GL_DEPTH_TEST );
      //  opengl.glDisable( GL.GL_CULL_FACE );
      opengl.glEnable( GL.GL_BLEND );					
      opengl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
    }
    pgl.endGL();

    // camera view toggle
    if(toggleImage == true){  // display the camera image
      PImage bgTemp = new PImage(windowWidth, windowHeight);
      bgTemp.copy(movieFrame, 0, 0, imageWidth, imageHeight, 0, 0, windowWidth, windowHeight);
      background(bgTemp);
    }
    else{ // Clear the screen
      background(255);
    }


    // Analyze for motion and resize to fit the screen (bigger)
    tempImage.copy(tracker.processImage(movieFrame), 0, 0, imageWidth, imageHeight, 0, 0, windowWidth, windowHeight);
    //color maxPeak;
tempImage.pixels = reverse(tempImage.pixels);
    // Drawstuff
    rectMode(CENTER);
    Cube frameTower;
      textFont(font_1);
    for ( int i = 0; i < COLS;i++) {
      // Begin loop for rows
      for ( int j = 0; j < ROWS;j++) {

        int x = i*cellsize + cellsize/2; // x position
        int y = j*cellsize + cellsize/2; // y position

        //int loc = (windowWidth - x - 1) + y*windowWidth; // Reversing x to mirror the image           // Pixel array location
        int loc = ( x) + y*windowWidth; // Reversing x to mirror the image           // Pixel array location
        // Translate to the location, set fill and stroke, and draw the rect
        // NOTE: swaps y,z for parallel text orientation
        pushMatrix();
        translate(x,-tempImage.pixels[loc]/2,y); // Half height because Cube draws from middle
        
        fill(color(tempImage.pixels[loc]/3));
        noStroke();
        frameTower = new Cube(cellsize, (tempImage.pixels[loc]), cellsize);
        frameTower.create();

        popMatrix();
        
        if(tempImage.pixels[loc]>350) {
          fill(0xffFF003D);
        } 
        else if(tempImage.pixels[loc]>250){
           fill(0xffFC930A);
        } 
        else if(tempImage.pixels[loc]>150){
           fill(0xffF7C41F);
        } 
        else if(tempImage.pixels[loc]>100){
           fill(0xffE0E05A);
        } 
        else if(tempImage.pixels[loc]>50){
           fill(0xffCCF390);
        } 
        else {
           fill(color((tempImage.pixels[loc]/3)+100));
        }

        text((tempImage.pixels[loc]), x-cellsize/2, -(tempImage.pixels[loc]),y);

      }
    }   

    //camera2.feed();
    //PImage temp = get();
    //background(0);
    camera1.feed();  // grab the camera
    //image(temp,0,0);

  } // END IF

  // TOTAL movement per frame
  textMode(SCREEN);
  textFont(font_2);
  text(tracker.getTotal(),50,50);
  textFont(font_1);
  text("fps: " + frameRate,50,70);
  textMode(MODEL);

  //image(get(),0,0);
  //text(tracker.getTotal(),0,0);
  // Fork off threads to save the images every second
  if(PApplet.parseInt(frameCount%frameRate)==0 && renderOut==true) {
    new WriteImage(movieFrame, "_camera");
    new WriteImage(get(), "_screen");
  }

}



//  +/- and arrow keys control the camera
public void keyPressed(){
  if(key == CODED) { 
    if (keyCode == RIGHT) { 
      camera1.arc(radians(2));
    } 
    else if (keyCode == LEFT) { 
      camera1.arc(radians(-2));
    } 
    if (keyCode == UP) { 
      camera1.boom(5);
    } 
    else if (keyCode == DOWN) { 
      camera1.boom(-5);
    }  
  }
  if (key =='='){
    camera1.zoom(radians(-2));
  }
  else
    if (key =='-'){
      camera1.zoom(radians(2));
    }
  if (key ==' '){
    if(toggleImage==true){
      toggleImage = false; 
    } 
    else {
      toggleImage = true;
    }
  }
  if (key =='f'){
    if(fogToggle==true){
      fogToggle = false; 
    } 
    else {
      fogToggle = true;
    }
  }
  if (key =='b'){
    if(blendToggle==true){
      blendToggle = false; 
    } 
    else {
      blendToggle = true;
    }
  }
    if (key =='r'){
    if(renderOut==true){
      renderOut = false; 
    } 
    else {
      renderOut = true;
    }
  }
}

// Saves the images using a different thread to prevent jitter.
class WriteImage extends Thread{
  private PImage theImage;
  private String theTitle;

  public WriteImage(PImage daImage, String daTitle) {
    //Thread t = new Thread(this);
    theImage = daImage;
    theTitle = daTitle;
    this.start();
  }

  public void run(){
    //println("Saved " + theTitle);
    theImage.save(savePath(theTitle+"/" + frameCount + ".jpg")); //WTF?!?
  } 
}

class Point3D{
  float x, y, z;

  // constructors
  Point3D(){
  }

  Point3D(float x, float y, float z){
    this.x = x;
    this.y = y;
    this.z = z;
  }
}

class Cube{
  Point3D[] vertices = new Point3D[24];
  float w, h, d;

  // constructors
  // default constructor
  Cube(){
  }

  Cube(float w, float h, float d){
    this.w = w;
    this.h = h;
    this.d = d;

    // cube composed of 6 quads
    //front
    vertices[0] = new Point3D(-w/2,-h/2,d/2);
    vertices[1] = new Point3D(w/2,-h/2,d/2);
    vertices[2] = new Point3D(w/2,h/2,d/2);
    vertices[3] = new Point3D(-w/2,h/2,d/2);
    //left
    vertices[4] = new Point3D(-w/2,-h/2,d/2);
    vertices[5] = new Point3D(-w/2,-h/2,-d/2);
    vertices[6] = new Point3D(-w/2,h/2,-d/2);
    vertices[7] = new Point3D(-w/2,h/2,d/2);
    //right
    vertices[8] = new Point3D(w/2,-h/2,d/2);
    vertices[9] = new Point3D(w/2,-h/2,-d/2);
    vertices[10] = new Point3D(w/2,h/2,-d/2);
    vertices[11] = new Point3D(w/2,h/2,d/2);
    //back
    vertices[12] = new Point3D(-w/2,-h/2,-d/2);  
    vertices[13] = new Point3D(w/2,-h/2,-d/2);
    vertices[14] = new Point3D(w/2,h/2,-d/2);
    vertices[15] = new Point3D(-w/2,h/2,-d/2);
    //bottom
    vertices[16] = new Point3D(-w/2,-h/2,d/2);
    vertices[17] = new Point3D(-w/2,-h/2,-d/2);
    vertices[18] = new Point3D(w/2,-h/2,-d/2);
    vertices[19] = new Point3D(w/2,-h/2,d/2);
    //top
    vertices[20] = new Point3D(-w/2,h/2,d/2);
    vertices[21] = new Point3D(-w/2,h/2,-d/2);
    vertices[22] = new Point3D(w/2,h/2,-d/2);
    vertices[23] = new Point3D(w/2,h/2,d/2);
  }

  public void create(){
    // draw cube
    for (int i=0; i<6; i++){
      beginShape(QUADS);
      for (int j=0; j<4; j++){
        vertex(vertices[j+4*i].x, vertices[j+4*i].y, vertices[j+4*i].z);
      }
      endShape();
    }
  }
}

// --------------------------------------------
// Super Fast Blur v1.1
// by Mario Klingemann <http://incubator.quasimondo.com>
// --------------------------------------------
public void fastblur(PImage img,int radius){

  if (radius<1){
    return;
  }
  int w=img.width;
  int h=img.height;
  int wm=w-1;
  int hm=h-1;
  int wh=w*h;
  int div=radius+radius+1;
  int r[]=new int[wh];
  int g[]=new int[wh];
  int b[]=new int[wh];
  int rsum,gsum,bsum,x,y,i,p,p1,p2,yp,yi,yw;
  int vmin[] = new int[max(w,h)];
  int vmax[] = new int[max(w,h)];
  int[] pix=img.pixels;
  int dv[]=new int[256*div];
  for (i=0;i<256*div;i++){
    dv[i]=(i/div);
  }

  yw=yi=0;

  for (y=0;y<h;y++){
    rsum=gsum=bsum=0;
    for(i=-radius;i<=radius;i++){
      p=pix[yi+min(wm,max(i,0))];
      rsum+=(p & 0xff0000)>>16;
      gsum+=(p & 0x00ff00)>>8;
      bsum+= p & 0x0000ff;
    }
    for (x=0;x<w;x++){

      r[yi]=dv[rsum];
      g[yi]=dv[gsum];
      b[yi]=dv[bsum];

      if(y==0){
        vmin[x]=min(x+radius+1,wm);
        vmax[x]=max(x-radius,0);
      }
      p1=pix[yw+vmin[x]];
      p2=pix[yw+vmax[x]];

      rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
      gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
      bsum+= (p1 & 0x0000ff)-(p2 & 0x0000ff);
      yi++;
    }
    yw+=w;
  }

  for (x=0;x<w;x++){
    rsum=gsum=bsum=0;
    yp=-radius*w;
    for(i=-radius;i<=radius;i++){
      yi=max(0,yp)+x;
      rsum+=r[yi];
      gsum+=g[yi];
      bsum+=b[yi];
      yp+=w;
    }
    yi=x;
    for (y=0;y<h;y++){
      pix[yi]=0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
      if(x==0){
        vmin[y]=min(y+radius+1,hm)*w;
        vmax[y]=max(y-radius,0)*w;
      }
      p1=x+vmin[y];
      p2=x+vmax[y];

      rsum+=r[p1]-r[p2];
      gsum+=g[p1]-g[p2];
      bsum+=b[p1]-b[p2];

      yi+=w;
    }
  }

}

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
  public PImage processImage(PImage thisFrame) {

    // Make the pixels[] array available for direct manipulation
    thisFrame.loadPixels();

    int[] diffMask;
    diffMask = new int[numPixels];
    movementSum = 0;

    for (int i = 0; i < numPixels; i++) { // For each pixel in the video frame...
      // Fetch the current color in that location, and also the color
      // of the background in that spot
      int currColor = thisFrame.pixels[i];
      int prevColor = previousFrame[i];
      // Extract the red, green, and blue components of the current pixel\u2019s color
      int currR = (currColor >> 16) & 0xFF;
      int currG = (currColor >> 8) & 0xFF;
      int currB = currColor & 0xFF;
      // Extract the red, green, and blue components of the background pixel\u2019s color
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
        movements[i] = PApplet.parseInt((movements[i]+change)/2);
      }
      else{ // subtract movement marker
        if(movements[i]>0) {
          movements[i] = PApplet.parseInt(movements[i] - (movements[i]/frameRate/2));
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
 public int getTotal(){
   return movementSum; 
 }

} // END CLASS MotionTrack

}