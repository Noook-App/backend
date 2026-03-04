package com.personalspace.api.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "grocery_labels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class GroceryLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "labels")
    private Set<GroceryList> groceryLists = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public GroceryLabel() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getCreatedAt() { return createdAt; }

    public Set<GroceryList> getGroceryLists() { return groceryLists; }
    public void setGroceryLists(Set<GroceryList> groceryLists) { this.groceryLists = groceryLists; }

}
