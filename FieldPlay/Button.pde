class Button {
  float x, y, w, h;
  color normal;
  color hover;
  color current;
  
  public Button(float x, float y, float w, float h, color normal, color hover) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.normal = normal;
    this.hover = hover;
  }

  boolean mouseHovering() {
    return mouseX > x && 
      mouseX < x + w &&
      mouseY > y &&
      mouseY < y + h;
  }
  
}
