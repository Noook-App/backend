package com.personalspace.api.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "grocery_items")
public class GroceryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String quantity;

    @Column(nullable = false)
    private boolean checked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grocery_list_id", nullable = false)
    private GroceryList groceryList;

    @ManyToMany
    @JoinTable(
            name = "grocery_item_label_mappings",
            joinColumns = @JoinColumn(name = "grocery_item_id"),
            inverseJoinColumns = @JoinColumn(name = "grocery_item_label_id")
    )
    private Set<GroceryItemLabel> labels = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public GroceryItem() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    public GroceryList getGroceryList() { return groceryList; }
    public void setGroceryList(GroceryList groceryList) { this.groceryList = groceryList; }

    public Set<GroceryItemLabel> getLabels() { return labels; }
    public void setLabels(Set<GroceryItemLabel> labels) { this.labels = labels; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
