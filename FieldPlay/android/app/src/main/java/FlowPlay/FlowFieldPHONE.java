package FlowPlay;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Stack; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class FlowFieldPHONE extends PApplet {

PGraphics screen;
PGraphics newParticles;
PGraphics tickBuffer;

// array containing all particles
Particle[] particles;
// predefined hard limit on number of particles
int maxParticles = 1000;

// alpha value of background, increacing will make trails dissapear sooner
final float fade = 0.05f;

// height of options menu
float menuHeight;
// options colors
int optionsBackground;
int optionsOutline;
int textColor;
float textSize;
PFont font;

// simulation coordinate center
PVector viewCenter = new PVector(0, 0);
// simulation coordinate width and height
float viewWidth, viewHeight;
// magnification factor
float zoom = 100;

// location of mouse in previous frame
PVector lastClick = convert(new PVector(0, 0));
// mouse was just pressed (and last frame it was not) 
boolean firstClick = false;
// if mouse is no longer pressed (and last frame it was)
boolean mouseWasPressed = false;
boolean optionsChanged = true;
boolean viewCenterChanged = false;

// buttons
Button randomize;
Button hue;
Button center;

// if the simulation is calculating colors by angle or velocity
boolean angleHue = true;

// mapping of functions to their abbreviations
static HashMap<String, String> functionKey;

static String[] arity1 = {"cos", "sin", "exp"};
static String[] arity2 = {"pow", "min", "max", "-", "+", "/", "*"};

final int maxEquationLength = 5;
static Equation vx;
static Equation vy;

public void setup() {
  frameRate(100);
  
  
  background(0);
  noStroke();

  optionsBackground = color(0, 0, 0);
  optionsOutline = color(255, 255, 255);
  textColor = color(255, 255, 255);
  textSize = 50;

  menuHeight = height/4;

  randomize = new Button(width*5/8, height-menuHeight, width*3/8-1, menuHeight/4, color(0), color(75));
  hue = new Button(randomize.x, randomize.y + randomize.h +  (menuHeight*1/8), randomize.w, randomize.h, color(0), color(75));
  center = new Button(randomize.x, height - randomize.h, randomize.w, randomize.h - 1, color(0), color(75));


  font = createFont("Arial", textSize);
  textFont(font);

  particles = new Particle[maxParticles];

  functionKey = new HashMap<String, String>();
  functionKey.put("cos", "c");
  functionKey.put("sin", "s");
  functionKey.put("exp", "e");
  functionKey.put("pow", "p");
  functionKey.put("min", "<");
  functionKey.put("max", ">");

  vx  = new Equation("cos(min(p.x/p.y, length(p)*p.x))", "cx/y)<l*x))");
  vy  = new Equation("cos(p.y)", "cy)");

  tickBuffer = createGraphics(width, round(height-menuHeight), JAVA2D);
}

public void addParticles() {
  for (int i = 0; i < maxParticles; i++) {
    if (random(1) < 0.02f) {
      particles[i] = randomParticle();
    } else if (particles[i] == null) { 
      continue;
    }
  }
}

public void removeParticles() {
  for (int i = 0; i < maxParticles; i++) {
    if (particles[i] == null)
      continue; 
    PVector pos = particles[i].p;
    if (pos.x > viewCenter.x + (viewWidth / 2) || pos.x < viewCenter.x - (viewWidth / 2) || 
      pos.y > viewCenter.y + (viewHeight / 2) + 5 || pos.y < viewCenter.y - (viewHeight/2))
      particles[i] = null;
  }
}

public Particle randomParticle() {
  float x = viewCenter.x + random(viewWidth) - viewWidth/2;
  float y = viewCenter.y + random(viewHeight) - viewHeight/2;
  return new Particle(x, y);
}

public void updateParticles() {
  for (int i = 0; i < maxParticles; i++) {
    if (particles[i] == null)
      continue;
    particles[i].update();
  }
}

public void generateFunction() {
  vx = populate((int) random(1, maxEquationLength));
  vy = populate((int) random(1, maxEquationLength));
}

public Equation populate(int len ) {
  String pretty;
  String code;
  if (len == 1) {
    float rand = random(1);
    if (rand < 0.33f)
      return new Equation("p.x", "x");
    if (rand < 0.66f)
      return new Equation("p.y", "y");
    return new Equation("length(p)", "l");
  }
  if (random(1) < 0.5f) {
    String func = arity1[(int)random(arity1.length)];
    pretty = func + "(" + populate(len-1).pretty + ")";
    code = functionKey.get(func) + populate(len-1).code + ")";
  } else {
    String func = arity2[(int)random(arity2.length)];
    Equation a = populate(len/2);
    Equation b = populate(len-len/2);

    if (func.equals("+") || func.equals("-") || func.equals("/") || func.equals("*")) {
      pretty = a.pretty + func + b.pretty;
      code = a.code + func + b.code;
    } else if (func.equals("pow")) {
      pretty = func + "(abs(" + a.pretty + "), " + b.pretty + ")"; 
      code = a.code + functionKey.get(func) + b.code;
    } else {
      pretty = func + "(" + a.pretty + ", " + b.pretty + ")"; 
      code = a.code + functionKey.get(func) + b.code;
    }
  }
  return new Equation(pretty, code);
}



public void drawParticles() {
  for (int i = 0; i < maxParticles; i++) {
    if (particles[i] == null)
      continue;

    if (angleHue)
      fill(particles[i].angleColor());
    else
      fill(particles[i].velocityColor());

    PVector pos = particles[i].p;

    float relativeX = pos.x - viewCenter.x;
    float relativeY = pos.y - viewCenter.y;

    float imgX = relativeX*zoom + (width/2);
    float imgY = (height-menuHeight)/2 - relativeY*zoom;

    ellipse(imgX, imgY, 5, 5);
  }
}

public void drawOptions() {
  if (!optionsChanged)
    return;
  optionsChanged = false;

  fill(optionsBackground);
  stroke(optionsOutline);
  rect(0, height-menuHeight, width-1, menuHeight-1);

  fill(randomize.current);
  rect(randomize.x, randomize.y, randomize.w, randomize.h);
  fill(hue.current);
  rect(hue.x, hue.y, hue.w, hue.h);
  fill(center.current);
  rect(center.x, center.y, center.w, center.h);

  fill(textColor);
  textSize(50);
  textAlign(LEFT, CENTER);
  text("v.x = " + vx.pretty + ";", 10, height-menuHeight, width/2, menuHeight/2);
  text("v.y = " + vy.pretty + ";", 10, height-(menuHeight/2), width/2, menuHeight/2);
  textAlign(CENTER, CENTER);

  // randomize button text
  text("Randomize", randomize.x, randomize.y, randomize.w, randomize.h);

  // hue button text
  String s = angleHue ? "Color By: Angle" : "Color By: Velocity";
  text(s, hue.x, hue.y, hue.w, hue.h);

  text("Center", center.x, center.y, center.w, center.h);

  noStroke();
}

public void handleMouseOperations() {
  PVector thisClick = convert(new PVector(mouseX, mouseY));
  if (mousePressed && !mouseOnOptions()) {
    if (lastClick != null) {
      PVector delta = PVector.sub(thisClick, lastClick);
      viewCenter.add(PVector.mult(delta, -1/100f));
      viewCenterChanged = true;
    }
    viewCenterChanged = false;
  }

  if (randomize.mouseHovering()) {
    if (mouseWasPressed && !mousePressed) {
      generateFunction();
      randomize.current = randomize.normal;
      optionsChanged = true;
    }
    if (mousePressed && !mouseWasPressed){
      randomize.current = randomize.hover;
      optionsChanged = true;
    }
  } else {
    if(randomize.current == randomize.hover)
      optionsChanged = true;
    randomize.current = randomize.normal;
  }

  if (hue.mouseHovering()) {
    if (mouseWasPressed && !mousePressed) {
      hue.current = hue.normal;
      angleHue ^= true;
      optionsChanged = true;
    }
    if (mousePressed && !mouseWasPressed){
      hue.current = hue.hover;
      optionsChanged = true;
    }
  } else {
    if(hue.current == hue.hover)
      optionsChanged = true;
    hue.current = hue.normal;
  }

  if (center.mouseHovering()) {
    if (mouseWasPressed && !mousePressed) {
        center.current = center.normal;
      viewCenter.set(0, 0);
      viewCenterChanged = false;
      optionsChanged = true;
    }      
    if (mousePressed && !mouseWasPressed){
      center.current = center.hover;
      optionsChanged = true;
    }
  } else {
    if(center.current == center.hover)
      optionsChanged = true;
    center.current = center.normal;
  }

  mouseWasPressed = mousePressed;
  if (!mousePressed)
    lastClick = null;
  else
    lastClick = thisClick;
}

public PVector convert(PVector vec) {
  return new PVector(vec.x + width/2, (height-menuHeight)/2 - vec.y);
}

public boolean mouseOnOptions() {
  return mouseY > height-menuHeight;
}

public void drawTickMarks() {

  if (mousePressed)
    return;
  if (viewCenterChanged)
    return;

  viewCenterChanged = true;
  tickBuffer = createGraphics(width, round(height-menuHeight));
  tickBuffer.beginDraw();
  tickBuffer.textFont(font);
  tickBuffer.fill(textColor);
  tickBuffer.textSize(textSize*3/4);
  tickBuffer.text(viewCenter.x, width/2, height-menuHeight - 15);
  tickBuffer.text(viewCenter.x + viewWidth/4, width*3/4, height-menuHeight - 15);
  tickBuffer.text(viewCenter.x - viewWidth/4, width/4, height-menuHeight - 15);

  tickBuffer.text(viewCenter.y, width - 110, (height-menuHeight)/2);
  tickBuffer.text(viewCenter.y + viewHeight/4, width - 110, (height-menuHeight)/4);
  tickBuffer.text(viewCenter.y - viewHeight/4, width - 110, (height-menuHeight)*3/4);

  tickBuffer.endDraw();
}

//void mouseWheel(MouseEvent event) {
//  float scroll = event.getCount();
//  if (scroll > 6)
//    scroll = 6;
//  if (scroll < -6)
//    scroll = -6;
//  scroll /= 3;
//  zoom -= scroll;

//  zoom = Math.max(0.1, zoom);
//}

public void draw() {
  println(mouseX  + " " + mouseY);
  println(mousePressed && randomize.mouseHovering());

  viewWidth = width/zoom;
  viewHeight = (height - menuHeight)/zoom;

  fill(0, fade*255);
  rect(0, 0, width, height - menuHeight);

  addParticles();
  updateParticles();
  removeParticles();
  drawParticles();

  handleMouseOperations();

  drawOptions();
  drawTickMarks();
  image(tickBuffer, 0, 0);
}
class Button {
  float x, y, w, h;
  int normal;
  int hover;
  int current;
  
  public Button(float x, float y, float w, float h, int normal, int hover) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.normal = normal;
    this.hover = hover;
  }

  public boolean mouseHovering() {
    return mouseX > x && 
      mouseX < x + w &&
      mouseY > y &&
      mouseY < y + h;
  }
  
}
class Equation {
  String pretty;
  String code;

  public Equation() {
    pretty = "";
    code = "";
  }

  public Equation(String p, String c) {
    this.pretty = p;
    this.code = c;
  }
}


class Particle {
  PVector p, v;

  public Particle(float x, float y) {
    p = new PVector(x, y);
    v = new PVector(0, 0);
  }

  public void update() {
    v = get_velocity();
    p.add(PVector.mult(v, 1/250f));
  }

  public int angleColor() {
    colorMode(HSB, 1.0f);
    int ret = color((atan2(v.y, v.x)+PI)/2/PI, 1, 1);
    colorMode(RGB, 255);
    return ret;
  }

  public int velocityColor() {
    colorMode(HSB, 1.0f);
    int ret = color(v.mag()/15, 1, 1);
    colorMode(RGB, 255);
    return ret;
  }

  public float evaluate(String f) {

    Stack<Character> operators = new Stack<Character>();
    Stack<Float> operands = new Stack<Float>();

    char[] ch = f.toCharArray();

  loop:
    for (int i = 0; i < ch.length; i++) {
      char c = ch[i];

      switch(c) {
        case('x'):
        operands.push(p.x);
        continue loop;
        case('y'):
        operands.push(p.y);
        continue loop;
        case('l'):
        operands.push(length());
        continue loop;
      }

      if (c == ')') {
        //println(operators);
        //println(operands);
        char op = operators.pop();
        float push = 0;
        float a = operands.pop();
        switch(op) {
          case('c'):
          push = cos(a);
          break;
          case('s'):
          push = sin(a);
          break;
          case('e'):
          push = exp(a);
          break;
          case('p'):
          float b = operands.pop();
          push = pow(abs(b), a);
          break;
          case('<'):
          b = operands.pop();
          push = min(b, a);
          break;
          case('>'):        
          b = operands.pop();
          push = max(b, a);
          break;
          case('-'):
          b = operands.pop();
          push = b-a;
          break;
          case('+'):
          b = operands.pop();
          push = b+a;
          break;
          case('/'):
          b = operands.pop();
          push = b/a;
          break;
          case('*'):
          b = operands.pop();
          push = b*a;
          break;
        }
        operands.push(push);
      } else if (c=='(') {
      } else {
        operators.push(c);
      }
    }
    while (!operators.isEmpty()) {
      //println(operators);
      //println(operands);
      char op = operators.pop();
      float push = 0;
      float a = operands.pop();
      switch(op) {
        case('c'):
        push = cos(a);
        break;
        case('s'):
        push = sin(a);
        break;
        case('e'):
        push = exp(a);
        break;
        case('p'):
        float b = operands.pop();
        push = pow(abs(b), a);
        break;
        case('<'):
        b = operands.pop();
        push = min(b, a);
        break;
        case('>'):        
        b = operands.pop();
        push = max(b, a);
        break;
        case('-'):
        b = operands.pop();
        push = b-a;
        break;
        case('+'):
        b = operands.pop();
        push = b+a;
        break;
        case('/'):
        b = operands.pop();
        push = b/a;
        break;
        case('*'):
        b = operands.pop();
        push = b*a;
        break;
      }
      operands.push(push);
    }
    //println(operands.peek());
    return operands.pop();
  }

  public PVector get_velocity() {    
    PVector v = new PVector(0, 0);

    String fx = FlowFieldPHONE.vx.code;
    String fy = FlowFieldPHONE.vy.code;

    v.x = evaluate(fx);
    v.y = evaluate(fy);
    
    return v;
  }

  public float length() {
    return p.mag();
  }
}
  public void settings() {  fullScreen();  smooth(); }
}
