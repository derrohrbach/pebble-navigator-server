package de.derrohrbach.pbnavigator.dto;

public class StyleDto {
    private String shape;
    private String backgroundColor;
    private String backgroundColor2;
    private String foregroundColor;
    private String borderColor;

    public StyleDto() {}

    public StyleDto(String shape, String backgroundColor, String backgroundColor2, 
                   String foregroundColor, String borderColor) {
        this.shape = shape;
        this.backgroundColor = backgroundColor;
        this.backgroundColor2 = backgroundColor2;
        this.foregroundColor = foregroundColor;
        this.borderColor = borderColor;
    }

    // Getters and Setters
    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public String getBackgroundColor2() { return backgroundColor2; }
    public void setBackgroundColor2(String backgroundColor2) { this.backgroundColor2 = backgroundColor2; }

    public String getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(String foregroundColor) { this.foregroundColor = foregroundColor; }

    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }
}