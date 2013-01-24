import processing.opengl.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import processing.video.*;
import imageadjuster.*;
import damkjer.ocd.*;


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

void setup()
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
  perspective( PI*0.25, windowWidth/windowHeight, 1.0, 5000 );

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
void captureEvent(Capture cam)
{
  movieFrame.read();
  newFrame = true;
}


void draw() {

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
      opengl.glFogf( GL.GL_FOG_DENSITY, 0.0007 );
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
          fill(#FF003D);
        } 
        else if(tempImage.pixels[loc]>250){
           fill(#FC930A);
        } 
        else if(tempImage.pixels[loc]>150){
           fill(#F7C41F);
        } 
        else if(tempImage.pixels[loc]>100){
           fill(#E0E05A);
        } 
        else if(tempImage.pixels[loc]>50){
           fill(#CCF390);
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
  if(int(frameCount%frameRate)==0 && renderOut==true) {
    new WriteImage(movieFrame, "_camera");
    new WriteImage(get(), "_screen");
  }

}



//  +/- and arrow keys control the camera
void keyPressed(){
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

  void run(){
    //println("Saved " + theTitle);
    theImage.save(savePath(theTitle+"/" + frameCount + ".jpg")); //WTF?!?
  } 
}
