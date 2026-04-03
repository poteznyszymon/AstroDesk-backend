package io.astrodesk.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_notes")
public class InventoryNotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String content;

    @NotBlank
    @Column(nullable = false)
    private String author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @JsonIgnore
    private Inventory inventory;

    public InventoryNotes(String content, String author, Inventory inventory) {
        this.content = content;
        this.author = author;
        this.inventory = inventory;
    }

    protected InventoryNotes() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setContent(String content) {
        this.content = content;
    }
}