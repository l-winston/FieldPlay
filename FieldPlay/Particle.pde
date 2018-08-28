import java.util.Stack;

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

  public color angleColor() {
    colorMode(HSB, 1.0);
    color ret = color((atan2(v.y, v.x)+PI)/2/PI, 1, 1);
    colorMode(RGB, 255);
    return ret;
  }

  public color velocityColor() {
    colorMode(HSB, 1.0);
    color ret = color(v.mag()/15, 1, 1);
    colorMode(RGB, 255);
    return ret;
  }

  float evaluate(String f) {

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

  PVector get_velocity() {    
    PVector v = new PVector(0, 0);

    String fx = FieldPlay.vx.code;
    String fy = FieldPlay.vy.code;

    v.x = evaluate(fx);
    v.y = evaluate(fy);
    
    return v;
  }

  float length() {
    return p.mag();
  }
}
