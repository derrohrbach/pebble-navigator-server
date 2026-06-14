package de.derrohrbach.pbnavigator.dto;

public class LineDto {
    private String id;
    private String name;
    private String label;
    private String product;
    private StyleDto style;

    public LineDto() {}

    public LineDto(String id, String name, String label, String product) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.product = product;
    }

    public LineDto(String id, String name, String label, String product, StyleDto style) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.product = product;
        this.style = style;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public StyleDto getStyle() { return style; }
    public void setStyle(StyleDto style) { this.style = style; }
}